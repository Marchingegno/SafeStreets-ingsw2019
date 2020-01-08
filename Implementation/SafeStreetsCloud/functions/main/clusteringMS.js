'use strict';

// Dependencies
const model = require('./model/model');
const generalUtils = require('./utils/generalUtils');
const functions = require('firebase-functions');
const admin = require('firebase-admin');
try {
    admin.initializeApp();
} catch (e) { /* App already initialized */ }

// Global variables
const db = admin.firestore();

// Constant properties
const DISTANCE_OFFSET_IN_DEGREE = 0.0002; // more or less 20 meters but depends on position

/**
 * Triggers when a new group is created.
 */
exports.clusteringMS = functions.firestore.document('/municipalities/{municipality}/groups/{groupId}').onCreate(async (snap, context) => {
    console.log(`clusteringMS started.`);

    await doClustering(snap, context.params.municipality);

    console.log(`clusteringMS ended.`);
    return null;
});

/**
 * Add the new group to the correct cluster or create a new cluster if necessary.
 *
 * @param groupSnap the group to be clustered.
 * @param municipality the municipality that contains the group.
 */
async function doClustering(groupSnap, municipality) {
    // Get data of the new group.
    const latitude = groupSnap.get("latitude");
    const longitude = groupSnap.get("longitude");
    const typeOfViolation = groupSnap.get("typeOfViolation");
    const firstGroupDate = generalUtils.getNewDateWithDayPrecision(groupSnap.get("firstTimestamp").toDate());
    const lastGroupDate = generalUtils.getNewDateWithDayPrecision(groupSnap.get("lastTimestamp").toDate());

    // Get cluster to which the group must be added, null if no cluster exists.
    const clusterDocSnap = await getClusterOfReport(municipality, typeOfViolation, latitude, longitude);

    if(clusterDocSnap === null) { // If a cluster doesn't exist...
        console.log('No clusters found. Creating a new cluster...');
        await createNewCluster(firstGroupDate, groupSnap.ref.id, lastGroupDate, latitude, longitude, typeOfViolation, municipality);
    } else {
        console.log('A cluster has been found. Adding group to the cluster...');
        await addGroupToExistingCluster(clusterDocSnap, groupSnap.ref.id, firstGroupDate, lastGroupDate, municipality)
    }
}

/**
 * Returns the DocumentSnapshot of the cluster that should contain the group or null if no existing cluster has been found.
 *
 * @param municipality the municipality that should contain both the group and the cluster.
 * @param typeOfViolation type of the violation of the group.
 * @param latitude latitude of the group.
 * @param longitude longitude of the group.
 * @returns {Promise<*>} the DocumentSnapshot of the cluster that should contain the group or null if no existing cluster has been found.
 */
async function getClusterOfReport(municipality, typeOfViolation, latitude, longitude) {
    // Make query on database for getting the cluster (same typeOfViolation, similar location).
    // Note: Queries with range filters on different fields are not supported by Firestore.
    const querySnapshot = await db.collection("municipalities").doc(municipality).collection("clusters")
        .where("typeOfViolation", "==", typeOfViolation)
        .where("latitude", ">=", latitude - DISTANCE_OFFSET_IN_DEGREE)
        .where("latitude", "<=", latitude + DISTANCE_OFFSET_IN_DEGREE)
        .get();

    // Since Firestore is limited on queries we need to check separately the longitude.
    return alsoCheckForLongitudeForQuery(querySnapshot, longitude);
}

function alsoCheckForLongitudeForQuery(querySnapshot, newLongitude) {
    for (let clusterDocSnap of querySnapshot.docs) {
        const clusterLongitude = clusterDocSnap.data().longitude;

        // Range filter also on longitude.
        if(clusterLongitude >= newLongitude - DISTANCE_OFFSET_IN_DEGREE && clusterLongitude <= newLongitude + DISTANCE_OFFSET_IN_DEGREE) {
            return clusterDocSnap; // Found the cluster.
        }
    }
    return null; // No cluster has been found.
}

/**
 * Creates a new cluster in the database with the given group.
 *
 * @param firstGroupDate date with day precision of the firstTimestamp field of the group
 * @param groupId group to be added to the new cluster.
 * @param lastGroupDate date with day precision of the lastTimestamp field of the group
 * @param latitude latitude of the cluster.
 * @param longitude longitude of the cluster,
 * @param typeOfViolation type of the violation of the cluster.
 * @param municipality the municipality that should contain both the group and the cluster.
 */
async function createNewCluster(firstGroupDate, groupId, lastGroupDate, latitude, longitude, typeOfViolation, municipality) {
    // Create cluster data.
    const newCluster = model.newCluster(
        firstGroupDate,
        new Array(groupId),
        lastGroupDate,
        latitude,
        longitude,
        typeOfViolation
    );

    // Add cluster to database in path: /municipalities/{municipality}/clusters/{cluster}
    await db.collection("municipalities").doc(municipality).collection("clusters").add(newCluster);
}

/**
 * Add the group to an existing cluster in the database.
 *
 * @param clusterDocSnap the cluster on which to add the group.
 * @param groupId the group to add.
 * @param firstGroupDate date with day precision of the firstTimestamp field of the group
 * @param lastGroupDate date with day precision of the lastTimestamp field of the group
 * @param municipality the municipality that contains both the group and the cluster.
 */
async function addGroupToExistingCluster(clusterDocSnap, groupId, firstGroupDate, lastGroupDate, municipality) {
    // Get current data.
    const clusterObject = clusterDocSnap.data();
    const firstAddedDate = clusterObject.firstAddedDate;
    const lastAddedDate = clusterObject.lastAddedDate;

    // Modify data.
    clusterObject.groups.push(groupId); // Add groupId to the groups array.
    if(firstGroupDate < firstAddedDate)
        clusterObject.firstAddedDate = firstGroupDate;
    if(lastGroupDate > lastAddedDate)
        clusterObject.lastAddedDate = lastGroupDate;

    // Update data on the database.
    await db.collection("municipalities").doc(municipality).collection("clusters").doc(clusterDocSnap.ref.id).set(clusterObject);
}
