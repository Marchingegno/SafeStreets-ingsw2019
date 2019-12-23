'use strict';

// Dependencies
const model = require('./model/model');
const functions = require('firebase-functions');
const admin = require('firebase-admin');
try {
    admin.initializeApp();
} catch (e) { /* App already initialized */ }
const vision = require('@google-cloud/vision');

// Global variables
const client = new vision.ImageAnnotatorClient();

// Constant properties
const MINIMUM_SCORE = 0.5;
const DEBUG_LABELS = true;

/**
 * Triggers when a new violation report is created.
 * The changes made by this function may trigger the groupingMS.
 */
exports.approvingMS = functions.firestore.document('/violationReports/{reportId}').onCreate(async (snap, context) => {
    console.log(`approvingMS started.`);

    await doApproving(snap);

    console.log(`approvingMS ended.`);
    return null;
});

async function doApproving(snap) {
    // Get uris of pictures in the violation report.
    const picturesUris = await getPicturesUrisOfReport(snap);

    // Check if a vehicle is present in one of the pictures using the Google Vision API.
    const vehiclePresent = await isAVehiclePresentInOneOfThePictures(picturesUris);

    // Approve or reject the report.
    if(vehiclePresent) {
        console.log(`Report has been approved.`);
        await snap.ref.update("reportStatus", model.ReportStatusEnum.APPROVED);
    } else {
        console.log(`Report has been rejected.`);
        await snap.ref.update("reportStatus", model.ReportStatusEnum.REJECTED);
        await snap.ref.update("statusMotivation", "No vehicles have been found in the pictures.");
    }
}

function getPicturesUrisOfReport(snap) {
    // Get array of pictures in the violation report object.
    const pictures = snap.get("pictures");
    const userId = snap.get("userUid");

    // Return pictures uris.
    let picturesUris = [];
    for (let i = 0; i < pictures.length; i++) {
        picturesUris[i] = 'gs://safestreets-project.appspot.com/pictures/' + userId + '/' + pictures[i];
    }
    return picturesUris;
}

async function isAVehiclePresentInOneOfThePictures(picturesUris) {
    for (let i = 0; i < picturesUris.length; i++) {
        // suppressed the warning since we should do sequentially these awaits for avoiding useless calls to Vision API (that cost money)
        // eslint-disable-next-line no-await-in-loop
        const booleanResult = await isAVehiclePresentInPicture(picturesUris[i]);
        if(booleanResult) {
            return true;
        }
    }
    return false;
}

async function isAVehiclePresentInPicture(pictureUri) {
    // Perform Vision API elaboration and get the labels.
    const [result] = await client.labelDetection(pictureUri);
    const labelsFound = result.labelAnnotations;

    // Print labels to console if requested.
    if(DEBUG_LABELS)
        console.log(labelsFound);

    // Checks if there is a "Vehicle" in the labels.
    for (let i = 0; i < labelsFound.length; i++) {
        if(labelsFound[i].description === 'Vehicle' && labelsFound[i].score >= MINIMUM_SCORE) {
            return true;
        }
    }
    return false;
}
