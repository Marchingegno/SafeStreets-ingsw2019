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
 * Triggers on time.
 * WARNING: time triggers need the Firebase Blaze payment plan.
 */
exports.municipalityDataRetrieverMS = functions.pubsub.schedule('every 24 hours').onRun((context) => {
    console.log(`municipalityDataRetrieverMS started.`);

    console.log(`municipalityDataRetrieverMS ended.`);
    return null;
});