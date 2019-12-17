'use strict';

// Dependencies
const functions = require('firebase-functions');
const admin = require('firebase-admin');
try {
    admin.initializeApp();
} catch (e) { /* App already initialized */ }
const generalUtils = require('./utils/generalUtils');

// Global variables
const db = admin.firestore();

// Constant properties
const DISTANCE_OFFSET = 0.00001 * 5; // this is 5 meters more or less
const TIME_OFFSET_IN_HOURS = 6;

/**
 * Triggers when a new violation report is added.
 */
exports.groupingMS = functions.firestore.document('/violationReports/{reportId}').onUpdate(async (change, context) => {
    console.log(`groupingMS started.`);

    const reportStatusBefore = change.before.get("reportStatus");
    const reportStatusAfter = change.after.get("reportStatus");

    if(reportStatusBefore === "SUBMITTED" && reportStatusAfter === "APPROVED") {
        console.log(`Recognized report approval.`);
        await doGrouping(change.after);
    } else {
        console.log('Not a report approval.');
    }
    console.log(`groupingMS ended.`);
    return null;
});

async function doGrouping(violationReportSnap) {
    // Get data of the new report.
    const licensePlate = violationReportSnap.get("licensePlate");
    const latitude = violationReportSnap.get("latitude");
    const longitude = violationReportSnap.get("longitude");
    const uploadTimestamp = violationReportSnap.get("uploadTimestamp");
    const violationType = violationReportSnap.get("violationType");
    const municipality = violationReportSnap.get("municipality");

    // Get group to which the report must be added, null if no group exists.
    const groupDocSnap = await getGroupOfReport(municipality, licensePlate, violationType, latitude, longitude, uploadTimestamp);

    if(groupDocSnap === null) { // If a group doesn't exist...
        console.log('No groups found. Creating a new group...');
        await createNewGroup(licensePlate, violationType, uploadTimestamp, latitude, longitude, violationReportSnap.ref.id, municipality);
    } else {
        console.log('A group has been found. Adding report to the group...');
        await addViolationReportToExistingGroup(groupDocSnap, violationReportSnap.ref.id, uploadTimestamp.toDate(), municipality)
    }
}

async function getGroupOfReport(municipality, licensePlate, violationType, latitude, longitude, uploadTimestamp) {
    // Make query on database for getting the group (similar location and timestamp).
    // Note: Queries with range filters on different fields are not supported by Firestore.
    const querySnapshot = await db.collection("municipalities").doc(municipality).collection("groups")
        .where("licensePlate", "==", licensePlate)
        .where("violationType", "==", violationType)
        .where("latitude", ">=", latitude - DISTANCE_OFFSET)
        .where("latitude", "<=", latitude + DISTANCE_OFFSET)
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
        if(groupLongitude >= newLongitude - DISTANCE_OFFSET && groupLongitude <= newLongitude + DISTANCE_OFFSET) {
            if(newUploadDate >= generalUtils.getNewDateWithAddedHours(groupFirstDate, -TIME_OFFSET_IN_HOURS) && newUploadDate <= generalUtils.getNewDateWithAddedHours(groupLastDate, TIME_OFFSET_IN_HOURS))
                return groupDocSnap; // Found the group.
        }
    }
    return null; // No group has been found.
}

async function createNewGroup(licensePlate, violationType, uploadTimestamp, latitude, longitude, violationReportId, municipality) {
    // Create group data.
    const newGroup = {
        licensePlate: licensePlate,
        violationType: violationType,
        groupStatus: "APPROVED",
        firstTimestamp: uploadTimestamp,
        lastTimestamp: uploadTimestamp,
        latitude: latitude,
        longitude: longitude,
        reports: new Array(violationReportId)
    };

    // Add group to database in path: /municipalities/{municipality}/groups/{group}
    await db.collection("municipalities").doc(municipality).collection("groups").add(newGroup);
}

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