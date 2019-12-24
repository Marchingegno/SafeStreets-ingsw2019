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
 * Triggers when a new municipality email authorization is added.
 */
exports.municipalityAuthorizerMS = functions.firestore.document('/municipalities/{municipalityId}/auth/{email}').onCreate(async (snap, context) => {
    console.log(`municipalityAuthorizerMS started.`);

    await authorizeByEmail(snap, context);

    console.log(`municipalityAuthorizerMS ended.`);
    return null;
});

async function authorizeByEmail(snap, context) {
    const municipalityId = context.params.municipalityId;
    const email = context.params.email;
    try {
        const user = await auth.getUserByEmail(email);
        if(user.customClaims !== null && user.customClaims !== undefined && user.customClaims.municipalityId !== null)
            throw new Error(String(email + ' is already associated with municipality ' + user.customClaims.municipalityId));
        const tokens = model.newCustomToken(true, municipalityId);
        await auth.setCustomUserClaims(user.uid, tokens);
        console.log(`${user.email} successfully marked as authorized for municipality ${municipalityId}.`);
        await snap.ref.update(model.newOkAuthDocument(user, snap.createTime));
        console.log(`Timestamp saved in database for ${user.email}.`);
    } catch (error) {
        console.error(`There was an error marking ${email} as authorized for municipality ${municipalityId}.`, error);
        await snap.ref.update(model.newErrorAuthDocument(error.toString()));
        console.log(`Error message saved in database for ${email}.`);
    }
}
