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
const storage = admin.storage();
const bucket = storage.bucket();

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
        await doThing(change.after);
    } else {
        console.log('Not a report approval.');
    }
    console.log(`groupingMS ended.`);
    return null;
});

async function doThing(violationReportSnap) {
    const licensePlate = violationReportSnap.get("licensePlate");
    const latitude = violationReportSnap.get("latitude");
    const longitude = violationReportSnap.get("longitude");
    const uploadTimestamp = violationReportSnap.get("uploadTimestamp");
    const violationType = violationReportSnap.get("violationType");

    // TODO get municipality name
    const municipality = getMunicipalityName();

    const initialDate = generalUtils.getNewDateWithAddedHours(new Date(uploadTimestamp.toDate()), -TIME_OFFSET_IN_HOURS);
    console.log("Initial date: " + initialDate.toString());
    const finalDate = generalUtils.getNewDateWithAddedHours(new Date(uploadTimestamp.toDate()), TIME_OFFSET_IN_HOURS);
    console.log("Final date: " + finalDate.toString());

    const groupDocSnap = getGroupOfReport(municipality, licensePlate, violationType, latitude, longitude, uploadTimestamp)

    if(groupDocSnap === null) {
        console.log('No groups found. Creating a new group...');
        await createNewGroup(licensePlate, violationType, initialDate, finalDate, latitude, longitude, violationReportSnap.ref.id, municipality);
    } else {
        console.log('A group has been found. Adding report to the group...');
        await addViolationReportToExistingGroup(groupDocSnap, violationReportSnap.ref.id, finalDate, municipality)
    }
}

async function getGroupOfReport(municipality, licensePlate, violationType, latitude, longitude, uploadTimestamp) {
    // Note: Queries with range filters on different fields are not supported by Firestore.
    const querySnapshot = await db.collection("municipalities").doc(municipality).collection("groups")
        .where("licensePlate", "==", licensePlate)
        .where("violationType", "==", violationType)
        .where("latitude", ">=", latitude - DISTANCE_OFFSET)
        .where("latitude", "<=", latitude + DISTANCE_OFFSET)
        .get();

    return getGroupDocSnapFromQuerySnapshot(querySnapshot, longitude, uploadTimestamp.toDate());
}

function getGroupDocSnapFromQuerySnapshot(querySnapshot, newLongitude, timestamp) {
    for (let queryDocSnap of querySnapshot.docs) {
        const groupLongitude = queryDocSnap.data().longitude;
        console.log("Found group with longitude: " + groupLongitude);
        const groupFirstTimestamp = generalUtils.getNewDateWithAddedHours(queryDocSnap.data().firstTimestamp.toDate(), -TIME_OFFSET_IN_HOURS);
        console.log("Found group with first timestamp: " + groupFirstTimestamp.toString());
        const groupLastTimestamp = generalUtils.getNewDateWithAddedHours(queryDocSnap.data().lastTimestamp.toDate(), TIME_OFFSET_IN_HOURS);
        console.log("Found group with last timestamp: " + groupLastTimestamp.toString());
        if(groupLongitude >= newLongitude - DISTANCE_OFFSET && groupLongitude <= newLongitude + DISTANCE_OFFSET && timestamp >= groupFirstTimestamp && timestamp <= groupLastTimestamp) {
            return queryDocSnap;
        }
    }
    return null;
}

async function createNewGroup(licensePlate, violationType, initialDate, finalDate, latitude, longitude, violationReportId, municipality) {
    const newGroup = {
        licensePlate: licensePlate,
        violationType: violationType,
        groupStatus: "APPROVED",
        firstTimestamp: initialDate,
        lastTimestamp: finalDate,
        latitude: latitude,
        longitude: longitude,
        reports: new Array(violationReportId)
    };
    await db.collection("municipalities").doc(municipality).collection("groups").add(newGroup);
}

async function addViolationReportToExistingGroup(groupDocSnap, violationReportId, finalDate, municipality) {
    const groupObject = groupDocSnap.data();
    groupObject.reports.push(violationReportId);
    groupObject.lastTimestamp = finalDate;
    await db.collection("municipalities").doc(municipality).collection("groups").doc(groupDocSnap.ref.id).set(groupObject);
}

// TODO get municipality name
function getMunicipalityName() {
    return "testMunicip";
}