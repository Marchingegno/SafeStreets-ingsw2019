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
 * Triggers when a new violation report is added.
 */
exports.groupingMS = functions.firestore.document('/users/{userId}/violationReports/{reportId}').onCreate(async (snap, context) => {
    console.log(`groupingMS started.`);

    console.log(`groupingMS ended.`);
    return null;
});
