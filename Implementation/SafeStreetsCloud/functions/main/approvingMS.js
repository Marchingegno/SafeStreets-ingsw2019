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
const DEBUG_LABELS_FOUND = true;

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

/**
 * Approves the violation report if its pictures contain at least one vehicle.
 *
 * @param reportSnap
 */
async function doApproving(reportSnap) {
    // Get uris of pictures in the violation report.
    const picturesUris = await getPicturesUrisOfReport(reportSnap);

    // Check if a vehicle is present in one of the pictures using the Google Vision API.
    const vehiclePresent = await isAVehiclePresentInOneOfThePictures(picturesUris);

    // Approve or reject the report.
    if(vehiclePresent) {
        console.log(`Report has been approved.`);
        await reportSnap.ref.update("reportStatus", model.ReportStatusEnum.APPROVED);
        await reportSnap.ref.update("statusMotivation", "Ready for municipality's confirmation.");
    } else {
        console.log(`Report has been rejected.`);
        await reportSnap.ref.update("reportStatus", model.ReportStatusEnum.REJECTED);
        await reportSnap.ref.update("statusMotivation", "No vehicles have been found in the pictures.");
    }
}

/**
 * Returns an array with all the uris of the pictures in the violation report.
 *
 * @param reportSnap the DocumentSnapshot of the violation report.
 * @returns {[]} an array with all the uris of the pictures in the violation report.
 */
function getPicturesUrisOfReport(reportSnap) {
    // Get array of pictureIds in the violation report object.
    const pictureIds = reportSnap.get("pictures");
    const userId = reportSnap.get("userUid");

    // Return pictures uris.
    let picturesUris = [];
    for (let pictureId of pictureIds) {
        picturesUris.push('gs://safestreets-project.appspot.com/pictures/' + userId + '/' + pictureId);
    }
    return picturesUris;
}

/**
 * Returns true if a vehicle is present in one of the pictures.
 *
 * @param picturesUris an array containing all the pictures to look for.
 * @returns {Promise<boolean>} true if a vehicle is present in one of the pictures.
 */
async function isAVehiclePresentInOneOfThePictures(picturesUris) {
    for (let pictureUri of picturesUris) {
        // suppressed the warning since we should do sequentially these awaits for avoiding useless calls to Vision API (that cost money)
        // eslint-disable-next-line no-await-in-loop
        const booleanResult = await isAVehiclePresentInPicture(pictureUri);
        if(booleanResult) {
            return true;
        }
    }
    return false;
}

/**
 * Returns true if a vehicle is present in the picture.
 *
 * @param pictureUri the picture to look for.
 * @returns {Promise<boolean>} true if a vehicle is present in the picture.
 */
async function isAVehiclePresentInPicture(pictureUri) {
    // Perform Vision API elaboration and get the labels.
    const [result] = await client.labelDetection(pictureUri);
    const labelsFound = result.labelAnnotations;

    // Print labels to console if requested.
    if(DEBUG_LABELS_FOUND)
        console.log(labelsFound);

    // Checks if there is a "Vehicle" in the labels.
    for (let i = 0; i < labelsFound.length; i++) {
        if(labelsFound[i].description === 'Vehicle' && labelsFound[i].score >= MINIMUM_SCORE) {
            return true;
        }
    }
    return false;
}
