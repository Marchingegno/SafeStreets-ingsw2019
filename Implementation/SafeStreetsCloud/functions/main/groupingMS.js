'use strict';

// Dependencies
const generalUtils = require('./utils/generalUtils');
const model = require('./model/model');
const functions = require('firebase-functions');
const admin = require('firebase-admin');
try {
    admin.initializeApp();
} catch (e) { /* App already initialized */ }

// Global variables
const db = admin.firestore();

// Constant properties
const DISTANCE_OFFSET_IN_DEGREE = 0.0001; // more or less 10 meters but depends on position
const TIME_OFFSET_IN_HOURS = 6;

/**
 * Triggers when a violation report is updated, and starts only if the report has been approved.
 * The changes made by this function may trigger the clusteringMS.
 */
exports.groupingMS = functions.firestore.document('/violationReports/{reportId}').onUpdate(async (change, context) => {
    console.log(`groupingMS started.`);

    await doGroupingIfReportIsApproved(change);

    console.log(`groupingMS ended.`);
    return null;
});

/**
 * If the report has just been approved add the new report to the correct group or create a new group if necessary.
 *
 * @param change the updated report.
 */
async function doGroupingIfReportIsApproved(change) {
    const reportStatusBefore = change.before.get("reportStatus");
    const reportStatusAfter = change.after.get("reportStatus");

    if(reportStatusBefore === model.ReportStatusEnum.SUBMITTED && reportStatusAfter === model.ReportStatusEnum.APPROVED) {
        console.log(`Recognized report approval.`);
        await doGrouping(change.after);
    } else {
        console.log('Not a report approval.');
    }
}

/**
 * Add the new report to the correct group or create a new group if necessary.
 *
 * @param violationReportSnap the DocumentSnapshot of the report that has just been approved.
 */
async function doGrouping(violationReportSnap) {
    // Get data of the new report.
    const licensePlate = violationReportSnap.get("licensePlate");
    const latitude = violationReportSnap.get("latitude");
    const longitude = violationReportSnap.get("longitude");
    const uploadTimestamp = violationReportSnap.get("uploadTimestamp");
    const typeOfViolation = violationReportSnap.get("typeOfViolation");
    const municipality = violationReportSnap.get("municipality");

    // Get group to which the report must be added, null if no group exists.
    const groupDocSnap = await getGroupOfReport(municipality, licensePlate, typeOfViolation, latitude, longitude, uploadTimestamp);

    if(groupDocSnap === null) { // If a group doesn't exist...
        console.log('No groups found. Creating a new group...');
        await createNewGroup(licensePlate, typeOfViolation, uploadTimestamp, latitude, longitude, violationReportSnap.ref.id, municipality);
    } else {
        console.log('A group has been found. Adding report to the group...');
        await addViolationReportToExistingGroup(groupDocSnap, violationReportSnap.ref.id, uploadTimestamp.toDate(), municipality)
    }
}

/**
 * Returns the DocumentSnapshot of the group that should contain the report or null if no existing group has been found.
 *
 * @param municipality the municipality that should contain both the group and the cluster.
 * @param licensePlate license plate of the report.
 * @param typeOfViolation type of violation of the report.
 * @param latitude latitude of the report.
 * @param longitude longitude of the report.
 * @param uploadTimestamp upload timestamp of the report.
 * @returns {Promise<null>} the DocumentSnapshot of the group that should contain the report or null if no existing group has been found.
 */
async function getGroupOfReport(municipality, licensePlate, typeOfViolation, latitude, longitude, uploadTimestamp) {
    // Make query on database for getting the group (same licensePlate, same typeOfViolation, similar location and similar timestamp).
    // Note: Queries with range filters on different fields are not supported by Firestore.
    const querySnapshot = await db.collection("municipalities").doc(municipality).collection("groups")
        .where("licensePlate", "==", licensePlate)
        .where("typeOfViolation", "==", typeOfViolation)
        .where("latitude", ">=", latitude - DISTANCE_OFFSET_IN_DEGREE)
        .where("latitude", "<=", latitude + DISTANCE_OFFSET_IN_DEGREE)
        .get();

    // Since Firestore is limited on queries we need to check separately longitude and timestamp.
    return alsoCheckForLongitudeAndTimestampForQuery(querySnapshot, longitude, uploadTimestamp.toDate());
}

function alsoCheckForLongitudeAndTimestampForQuery(querySnapshot, newLongitude, newUploadDate) {
    for (let groupDocSnap of querySnapshot.docs) {
        const groupLongitude = groupDocSnap.data().longitude;
        const groupFirstDate = groupDocSnap.data().firstTimestamp.toDate();
        const groupLastDate = groupDocSnap.data().lastTimestamp.toDate();

        // Range filter also on longitude and date.
        if(groupLongitude >= newLongitude - DISTANCE_OFFSET_IN_DEGREE && groupLongitude <= newLongitude + DISTANCE_OFFSET_IN_DEGREE) {
            if(newUploadDate >= generalUtils.getNewDateWithAddedHours(groupFirstDate, -TIME_OFFSET_IN_HOURS) && newUploadDate <= generalUtils.getNewDateWithAddedHours(groupLastDate, TIME_OFFSET_IN_HOURS))
                return groupDocSnap; // Found the group.
        }
    }
    return null; // No group has been found.
}

/**
 * Creates a new group in the database with the given report.
 *
 * @param licensePlate license plate of the report.
 * @param typeOfViolation type of violation of the report.
 * @param latitude latitude of the report.
 * @param longitude longitude of the report.
 * @param uploadTimestamp upload timestamp of the report.
 * @param violationReportId the report to be added to the group.
 * @param municipality the municipality that should contain both the group and the cluster.
 */
async function createNewGroup(licensePlate, typeOfViolation, uploadTimestamp, latitude, longitude, violationReportId, municipality) {
    // Create group data.
    const newGroup = model.newGroup(
        uploadTimestamp,
        model.ReportStatusEnum.APPROVED,
        uploadTimestamp,
        latitude,
        licensePlate,
        longitude,
        new Array(violationReportId),
        typeOfViolation
    );

    // Add group to database in path: /municipalities/{municipality}/groups/{group}
    await db.collection("municipalities").doc(municipality).collection("groups").add(newGroup);
}

/**
 * Add a group to an existing cluster in the database.
 *
 * @param clusterDocSnap the cluster on which to add the group.
 * @param groupId the group to add.
 * @param municipality the municipality that contains both the group and the cluster.
 */
/**
 * Add the report to an existing group in the database.
 *
 * @param groupDocSnap the group on which to add the group.
 * @param violationReportId the report to add.
 * @param uploadDate the upload date of the report.
 * @param municipality the municipality that contains both the group and the cluster.
 */
async function addViolationReportToExistingGroup(groupDocSnap, violationReportId, uploadDate, municipality) {
    // Modify existing group data.
    const groupObject = groupDocSnap.data();
    const groupFirstDate = groupObject.firstTimestamp.toDate();
    const groupLastDate = groupObject.lastTimestamp.toDate();
    groupObject.reports.push(violationReportId); // Add report to the reports array.
    if(uploadDate > groupLastDate) // If the new report is the last, update the lastTimestamp field.
        groupObject.lastTimestamp = uploadDate;
    else if(uploadDate < groupFirstDate) // If the new report is earlier than first report (processed later), update the firstTimestamp field.
        groupObject.firstTimestamp = uploadDate;

    // Update data on the database.
    await db.collection("municipalities").doc(municipality).collection("groups").doc(groupDocSnap.ref.id).set(groupObject);
}
