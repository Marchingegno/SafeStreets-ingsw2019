'use strict';

// Dependencies
const functions = require('firebase-functions');
const admin = require('firebase-admin');
const queryablePromise = require('./utils/queryablePromise');

// Global variables
const db = admin.firestore();
const storage = admin.storage();
const bucket = storage.bucket();

/**
 * Triggers when the municipality confirms or rejects a violation group.
 */
exports.onReportStatusChangeMS = functions.firestore.document('/municipalities/{municipalityId}/violationGroups/{groupId}').onUpdate(async (change, context) => {
    console.log(`onReportStatusChangeMS started.`);

    console.log(`onReportStatusChangeMS ended.`);
    return null;
});