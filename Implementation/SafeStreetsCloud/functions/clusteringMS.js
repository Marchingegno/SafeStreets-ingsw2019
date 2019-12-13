'use strict';

// Dependencies
const functions = require('firebase-functions');
const admin = require('firebase-admin');
try {
    admin.initializeApp();
} catch (e) { /* App already initialized */ }
const queryablePromise = require('./utils/queryablePromise');

// Global variables
const db = admin.firestore();
const storage = admin.storage();
const bucket = storage.bucket();

/**
 * Triggers when a new violation report is added.
 */
exports.clusteringMS = functions.firestore.document('/violationReports/{reportId}').onCreate(async (snap, context) => {
    console.log(`clusteringMS started.`);

    console.log(`clusteringMS ended.`);
    return null;
});
