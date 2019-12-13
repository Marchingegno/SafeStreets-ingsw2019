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
exports.checkViolationReportsTrigger = functions.firestore.document('/violationReports/{reportId}').onCreate(async (snap, context) => {
	console.log(`Starting test`);

	const violationReportId = snap.id;
	const pictures = snap.get("pictures");

	// Get picture files and promises of file existence
	let picturesFiles = [];
	let promisesOfFileExistence = [];
	for (let i = 0; i < pictures.length; i++) {
		picturesFiles[i] = bucket.file(pictures[i]);
		promisesOfFileExistence[i] = queryablePromise.makeQueryablePromise(picturesFiles[i].exists());
	}

	// Execute promises of file existence
	await Promise.all(promisesOfFileExistence);

	// Checks if there exist a wrong picture (picture placed in array but not present in cloud storage)
	let missingPicture = false;
	for (let i = 0; i < promisesOfFileExistence.length; i++) {
		console.log(`Checking picture "${pictures[i]}", exists: "${promisesOfFileExistence[i].getResponse()}".`);
		if(promisesOfFileExistence[i].getResponse() === false) {
			missingPicture = true;
			break;
		}
	}

	if(missingPicture) {
		console.log(`The report "${violationReportId}" has one ore more wrong pictures references. Deleting the report and its pictures...`);

		// Delete pictures
		let promisesOfFileDeletion = [];
		for (let i = 0; i < promisesOfFileExistence.length; i++) {
			if(promisesOfFileExistence[i].getResponse() !== false) {
				promisesOfFileDeletion.push(picturesFiles[i].delete());
			}
		}
		console.log(`Number of files to be deleted from storage: ${promisesOfFileDeletion.length}.`);
		await Promise.all(promisesOfFileDeletion);

		// Delete Violation Report
		await snap.ref.delete();
	} else {
		console.log(`All pictures of report "${violationReportId}" are present.`);
	}
	return null;
});
