'use strict';

// Dependencies
const functions = require('firebase-functions');
const admin = require('firebase-admin');
try {
    admin.initializeApp();
} catch (e) { /* App already initialized */ }
const vision = require('@google-cloud/vision');

// Global variables
const db = admin.firestore();
const storage = admin.storage();
const bucket = storage.bucket();
const client = new vision.ImageAnnotatorClient();

// Constant properties
const MINIMUM_SCORE = 0.5;
const DEBUG_LABELS = true;

/**
 * Triggers when a new violation report is added.
 */
exports.approvingMS = functions.firestore.document('/violationReports/{reportId}').onCreate(async (snap, context) => {
    console.log(`approvingMS started.`);

    // Get uris of pictures in the violation report.
    const picturesUris = await getPicturesUrisOfReport(snap);

    // Check if a vehicle is present in one of the pictures using the Google Vision API.
    const vehiclePresent = await isAVehiclePresentInOneOfThePictures(picturesUris);

    if(vehiclePresent) {
        console.log(`Report should be approved.`);
    } else {
        console.log(`Report should be rejected.`);
    }

    console.log(`approvingMS ended.`);
    return null;
});

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
        console.log(booleanResult);
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