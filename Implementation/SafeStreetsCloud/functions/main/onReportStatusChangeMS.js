'use strict';

// Dependencies
const model = require('./model/model');
const functions = require('firebase-functions');
const admin = require('firebase-admin');
try {
    admin.initializeApp();
} catch (e) { /* App already initialized */ }

// Global variables
const db = admin.firestore();

/**
 * Triggers when the municipality confirms or rejects a violation group.
 */
exports.onReportStatusChangeMS = functions.firestore.document('/municipalities/{municipalityId}/groups/{groupId}').onUpdate(async (change, context) => {
    console.log(`onReportStatusChangeMS started.`);

    await doStatusChangeIfItIsAStatusChange(change);

    console.log(`onReportStatusChangeMS ended.`);
    return null;
});

/**
 * If the group has just been evaluated change the status to all the reports it contains.
 *
 * @param change the updated group.
 */
async function doStatusChangeIfItIsAStatusChange(change) {
    const groupStatusBefore = change.before.get("groupStatus");
    const groupStatusAfter = change.after.get("groupStatus");

    if(groupStatusBefore === model.ReportStatusEnum.APPROVED && (groupStatusAfter === model.ReportStatusEnum.CORRECT || groupStatusAfter === model.ReportStatusEnum.REJECTED)) {
        console.log(`Recognized group status change.`);
        await doStatusChange(change.after);
    } else {
        console.log('Not a group status change.');
    }
}

/**
 * Change the status to all the reports contained in the evaluated group.
 *
 * @param groupSnap the group that has been evaluated by the municipality.
 */
async function doStatusChange(groupSnap) {
    // Get data of the status change.
    const reportIds = groupSnap.get("reports");
    const newStatus = groupSnap.get("groupStatus");
    console.log("Updating status of " + reportIds.length + " reports.");

    // Update status of reports.
    let promisesOfStatusUpdate = [];
    for (let reportId of reportIds) {
        promisesOfStatusUpdate.push(db.collection("violationReports").doc(reportId).update("reportStatus", newStatus));
    }
    await Promise.all(promisesOfStatusUpdate);
}
