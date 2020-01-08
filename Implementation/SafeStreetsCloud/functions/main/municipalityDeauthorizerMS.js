'use strict';

// Dependencies
const model = require('./model/model');
const functions = require('firebase-functions');
const admin = require('firebase-admin');
try {
    admin.initializeApp();
} catch (e) { /* App already initialized */ }

// Global variables
const auth = admin.auth();

/**
 * Triggers when a municipality email authorization is removed.
 */
exports.municipalityDeauthorizerMS = functions.firestore.document('/municipalities/{municipalityId}/auth/{email}').onDelete(async (snap, context) => {
    console.log(`municipalityDeauthorizerMS started.`);

    await deauthorizeByEmail(context);

    console.log(`municipalityDeauthorizerMS ended.`);
    return null;
});

async function deauthorizeByEmail(context) {
    const municipalityId = context.params.municipalityId;
    const email = context.params.email;
    try {
        const user = await auth.getUserByEmail(email);
        if(user.customClaims !== null && user.customClaims !== undefined && user.customClaims.municipalityId !== null && municipalityId !== user.customClaims.municipalityId) {
            console.log(`${email} was not marked as authorized for municipality ${municipalityId}.`);
            return;
        }
        const tokens = model.newCustomToken(null, null);
        await auth.setCustomUserClaims(user.uid, tokens);
        console.log(`${email} successfully unmarked as authorized for municipality ${municipalityId}.`);
    } catch(error) {
        console.error(`There was an error un-marking ${email} as authorized for municipality ${municipalityId}.`, error);
    }
}
