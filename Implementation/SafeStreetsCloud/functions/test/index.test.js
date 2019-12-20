'use strict';

// Dependencies
const chai = require('chai');
const assert = chai.assert;
const sinon = require('sinon');
const admin = require('firebase-admin');
try {
    admin.initializeApp();
} catch (e) { /* App already initialized */ }

// Require and initialize firebase-functions-test in "online mode".
const projectConfig = {
    databaseURL: "https://safestreets-tests.firebaseio.com",
    storageBucket: "safestreets-tests.appspot.com",
    projectId: "safestreets-tests",
};
const test = require('firebase-functions-test')(projectConfig, './test/safestreets-tests-bed8eb22914b.json');

// Global variables
const db = admin.firestore();

describe('Cloud Functions Tests', () => {

    let groupingMS;
    let clusteringMS;
    before(() => {
        groupingMS = require('../groupingMS');
        clusteringMS = require('../clusteringMS');

        // Reset the database.
        return getPromiseOfDatabaseResetting();
    });

    afterEach(() => {
        // Reset the database.
        return getPromiseOfDatabaseResetting();
    });

    after(() => {
        // Do cleanup tasks.
        test.cleanup();
    });

    describe('groupingMS', () => {

        it('Passes change for an approved report, should create a new group.', () => {
            // Create fictional data to pass to the function.
            const violationReportBefore = createExampleViolationReport();
            const violationReportAfter = createExampleViolationReport();
            violationReportAfter.reportStatus = "APPROVED";
            const beforeSnap = test.firestore.makeDocumentSnapshot(violationReportBefore, 'violationReports/test-violation-1');
            const afterSnap = test.firestore.makeDocumentSnapshot(violationReportAfter, 'violationReports/test-violation-1');
            const change = test.makeChange(beforeSnap, afterSnap);

            // Wrap the function.
            const wrapped = test.wrap(groupingMS.groupingMS);

            // Launch function and check its changes.
            return wrapped(change).then(() => {
                return db.collection("municipalities").doc("testMunicip").collection("groups").where("licensePlate", "==", violationReportAfter.licensePlate).get().then(querySnapshot => {
                    assert.equal(querySnapshot.docs.length, 1);
                    for (let groupDocSnap of querySnapshot.docs) {
                        assert.equal(groupDocSnap.data().firstTimestamp.toDate().getSeconds(), violationReportAfter.uploadTimestamp.getSeconds());
                        assert.equal(groupDocSnap.data().groupStatus, "APPROVED");
                        assert.equal(groupDocSnap.data().lastTimestamp.toDate().getSeconds(), violationReportAfter.uploadTimestamp.getSeconds());
                        assert.equal(groupDocSnap.data().latitude, violationReportAfter.latitude);
                        assert.equal(groupDocSnap.data().licensePlate, violationReportAfter.licensePlate);
                        assert.equal(groupDocSnap.data().longitude, violationReportAfter.longitude);
                        assert.deepEqual(groupDocSnap.data().reports, ["test-violation-1"]);
                        assert.equal(groupDocSnap.data().typeOfViolation, violationReportAfter.typeOfViolation);
                    }
                });
            });
        });


        it('Passes a change that is not an approved report, should not create any group.', () => {
            // Create fictional data to pass to the function.
            const violationReportBefore = createExampleViolationReport();
            const violationReportAfter = createExampleViolationReport();
            violationReportAfter.reportStatus = "REJECTED";
            const beforeSnap = test.firestore.makeDocumentSnapshot(violationReportBefore, 'violationReports/test-violation-1');
            const afterSnap = test.firestore.makeDocumentSnapshot(violationReportAfter, 'violationReports/test-violation-1');
            const change = test.makeChange(beforeSnap, afterSnap);

            // Wrap the function.
            const wrapped = test.wrap(groupingMS.groupingMS);

            // Launch function and check its changes.
            return wrapped(change).then(() => {
                return db.collection("municipalities").doc("testMunicip").collection("groups").where("licensePlate", "==", violationReportAfter.licensePlate).get().then(querySnapshot => {
                    return assert.equal(querySnapshot.docs.length, 0);
                });
            });
        });


        it('Passes two consecutive changes of an approved report, should create a new group and then add report to it.', () => {
            // Create fictional data to pass to the function.
            const violationReportBefore1 = createExampleViolationReport();
            const violationReportAfter1 = createExampleViolationReport();
            violationReportAfter1.reportStatus = "APPROVED";
            const beforeSnap1 = test.firestore.makeDocumentSnapshot(violationReportBefore1, 'violationReports/test-violation-1');
            const afterSnap1 = test.firestore.makeDocumentSnapshot(violationReportAfter1, 'violationReports/test-violation-1');
            const change1 = test.makeChange(beforeSnap1, afterSnap1);
            const violationReportBefore2 = createExampleViolationReportThatShouldGroup(new Date('December 19, 2019 06:51:00'));
            const violationReportAfter2 = createExampleViolationReportThatShouldGroup(new Date('December 19, 2019 06:51:00'));
            violationReportAfter2.reportStatus = "APPROVED";
            const beforeSnap2 = test.firestore.makeDocumentSnapshot(violationReportBefore2, 'violationReports/test-violation-2');
            const afterSnap2 = test.firestore.makeDocumentSnapshot(violationReportAfter2, 'violationReports/test-violation-2');
            const change2 = test.makeChange(beforeSnap2, afterSnap2);

            // Wrap the function.
            const wrapped = test.wrap(groupingMS.groupingMS);

            // Launch function and check its changes.
            return wrapped(change1).then(() => {
                return wrapped(change2).then(() => {
                    return db.collection("municipalities").doc("testMunicip").collection("groups").where("licensePlate", "==", violationReportAfter1.licensePlate).get().then(querySnapshot => {
                        assert.equal(querySnapshot.docs.length, 1);
                        for (let groupDocSnap of querySnapshot.docs) {
                            assert.equal(groupDocSnap.data().firstTimestamp.toDate().getSeconds(), violationReportAfter1.uploadTimestamp.getSeconds());
                            assert.equal(groupDocSnap.data().groupStatus, "APPROVED");
                            assert.equal(groupDocSnap.data().lastTimestamp.toDate().getSeconds(), violationReportAfter2.uploadTimestamp.getSeconds());
                            assert.equal(groupDocSnap.data().latitude, violationReportAfter1.latitude);
                            assert.equal(groupDocSnap.data().licensePlate, violationReportAfter1.licensePlate);
                            assert.equal(groupDocSnap.data().longitude, violationReportAfter1.longitude);
                            assert.deepEqual(groupDocSnap.data().reports, ["test-violation-1", "test-violation-2"]);
                            assert.equal(groupDocSnap.data().typeOfViolation, violationReportAfter1.typeOfViolation);
                        }
                    });
                });
            });
        });


        it('Passes two consecutive changes of an approved report (but with earlier date), should create a new group and then add report to it.', () => {
            // Create fictional data to pass to the function.
            const violationReportBefore1 = createExampleViolationReport();
            const violationReportAfter1 = createExampleViolationReport();
            violationReportAfter1.reportStatus = "APPROVED";
            const beforeSnap1 = test.firestore.makeDocumentSnapshot(violationReportBefore1, 'violationReports/test-violation-1');
            const afterSnap1 = test.firestore.makeDocumentSnapshot(violationReportAfter1, 'violationReports/test-violation-1');
            const change1 = test.makeChange(beforeSnap1, afterSnap1);
            const violationReportBefore2 = createExampleViolationReportThatShouldGroup(new Date('December 19, 2019 02:51:00'));
            const violationReportAfter2 = createExampleViolationReportThatShouldGroup(new Date('December 19, 2019 02:51:00'));
            violationReportAfter2.reportStatus = "APPROVED";
            const beforeSnap2 = test.firestore.makeDocumentSnapshot(violationReportBefore2, 'violationReports/test-violation-2');
            const afterSnap2 = test.firestore.makeDocumentSnapshot(violationReportAfter2, 'violationReports/test-violation-2');
            const change2 = test.makeChange(beforeSnap2, afterSnap2);

            // Wrap the function.
            const wrapped = test.wrap(groupingMS.groupingMS);

            // Launch function and check its changes.
            return wrapped(change1).then(() => {
                return wrapped(change2).then(() => {
                    return db.collection("municipalities").doc("testMunicip").collection("groups").where("licensePlate", "==", violationReportAfter1.licensePlate).get().then(querySnapshot => {
                        assert.equal(querySnapshot.docs.length, 1);
                        for (let groupDocSnap of querySnapshot.docs) {
                            assert.equal(groupDocSnap.data().firstTimestamp.toDate().getSeconds(), violationReportAfter2.uploadTimestamp.getSeconds());
                            assert.equal(groupDocSnap.data().groupStatus, "APPROVED");
                            assert.equal(groupDocSnap.data().lastTimestamp.toDate().getSeconds(), violationReportAfter1.uploadTimestamp.getSeconds());
                            assert.equal(groupDocSnap.data().latitude, violationReportAfter1.latitude);
                            assert.equal(groupDocSnap.data().licensePlate, violationReportAfter1.licensePlate);
                            assert.equal(groupDocSnap.data().longitude, violationReportAfter1.longitude);
                            assert.deepEqual(groupDocSnap.data().reports, ["test-violation-1", "test-violation-2"]);
                            assert.equal(groupDocSnap.data().typeOfViolation, violationReportAfter1.typeOfViolation);
                        }
                    });
                });
            });
        });

    });


    describe('clusteringMS', () => {

        it('Passes new group, should create a new cluster.', () => {
            // Create fictional data to pass to the function.
            const groupData = createExampleGroup();
            const snap = test.firestore.makeDocumentSnapshot(groupData, 'municipalities/testMunicip/groups/test-group-1');

            // Wrap the function.
            const wrapped = test.wrap(clusteringMS.clusteringMS);

            // Launch function and check its changes.
            return wrapped(snap, {params: {municipality: "testMunicip"}}).then(() => {
                return db.collection("municipalities").doc("testMunicip").collection("clusters").get().then(querySnapshot => {
                    assert.equal(querySnapshot.docs.length, 1);
                    for (let clusterDocSnap of querySnapshot.docs) {
                        assert.deepEqual(clusterDocSnap.data().groups, ["test-group-1"]);
                        assert.equal(clusterDocSnap.data().latitude, groupData.latitude);
                        assert.equal(clusterDocSnap.data().longitude, groupData.longitude);
                        assert.equal(clusterDocSnap.data().typeOfViolation, groupData.typeOfViolation);
                    }
                });
            });
        });


        it('Passes two consecutive changes of an approved report, should create a new group and then add report to it.', () => {
            // Create fictional data to pass to the function.
            const groupData1 = createExampleGroup();
            const snap1 = test.firestore.makeDocumentSnapshot(groupData1, 'municipalities/testMunicip/groups/test-group-1');
            const groupData2 = createExampleGroupThatShouldCluster();
            const snap2 = test.firestore.makeDocumentSnapshot(groupData2, 'municipalities/testMunicip/groups/test-group-2');

            // Wrap the function.
            const wrapped = test.wrap(clusteringMS.clusteringMS);

            // Launch function and check its changes.
            return wrapped(snap1, {params: {municipality: "testMunicip"}}).then(() => {
                return wrapped(snap2, {params: {municipality: "testMunicip"}}).then(() => {
                    return db.collection("municipalities").doc("testMunicip").collection("clusters").get().then(querySnapshot => {
                        assert.equal(querySnapshot.docs.length, 1);
                        for (let clusterDocSnap of querySnapshot.docs) {
                            assert.deepEqual(clusterDocSnap.data().groups, ["test-group-1", "test-group-2"]);
                            assert.equal(clusterDocSnap.data().latitude, groupData1.latitude);
                            assert.equal(clusterDocSnap.data().longitude, groupData1.longitude);
                            assert.equal(clusterDocSnap.data().typeOfViolation, groupData1.typeOfViolation);
                        }
                    });
                });
            });
        });


    });


    describe('approvingMS', () => {

        let visionStub;
        let approvingMS;
        before(() => {
            const vision = require('@google-cloud/vision');
            visionStub = sinon.stub(vision, 'ImageAnnotatorClient');
            const positiveLabelsFoundStub = [{description: "Other1", score: 1.0}, {description: "Vehicle", score: 0.6}];
            const positiveResultStub = [{ labelAnnotations: positiveLabelsFoundStub}];
            const negativeLabelsFoundStub = [{description: "Other1", score: 1.0}, {description: "Other2", score: 0.6}];
            const negativeResultStub = [{ labelAnnotations: negativeLabelsFoundStub}];
            visionStub.returns({labelDetection: (pictureUri) => {
                if(pictureUri.toString().includes("vehicle"))
                    return positiveResultStub;
                else
                     return negativeResultStub;
            }});
            approvingMS = require('../approvingMS');
        });


        after(() => {
            // Restore stubbed method to their original status.
            visionStub.restore();
        });


        it('Passes new report and stubs image recognition methods with a vehicle in them, should approve the report.', () => {
            // Create fictional data to pass to the function.
            const violationReport = createExampleViolationReport();
            const snap = test.firestore.makeDocumentSnapshot(violationReport, 'violationReports/test-violation-1');

            // Wrap the function.
            const wrapped = test.wrap(approvingMS.approvingMS);

            // Launch function and check its changes.
            return db.collection("violationReports").doc("test-violation-1").create(violationReport).then(() => {
                return wrapped(snap).then(() => {
                    return db.collection("violationReports").where("licensePlate", "==", violationReport.licensePlate).get().then(querySnapshot => {
                        assert.equal(querySnapshot.docs.length, 1);
                        for (let reportDocSnap of querySnapshot.docs) {
                            assert.equal(reportDocSnap.data().reportStatus, "APPROVED");
                        }
                    });
                });
            });
        });


        it('Passes new report and stubs image recognition methods without any vehicle in them, should reject the report.', () => {
            // Create fictional data to pass to the function.
            const violationReport = createExampleViolationReport();
            violationReport.pictures[1] = "other-thing";
            const snap = test.firestore.makeDocumentSnapshot(violationReport, 'violationReports/test-violation-1');

            // Wrap the function.
            const wrapped = test.wrap(approvingMS.approvingMS);

            // Launch function and check its changes.
            return db.collection("violationReports").doc("test-violation-1").create(violationReport).then(() => {
                return wrapped(snap).then(() => {
                    return db.collection("violationReports").where("licensePlate", "==", violationReport.licensePlate).get().then(querySnapshot => {
                        assert.equal(querySnapshot.docs.length, 1);
                        for (let reportDocSnap of querySnapshot.docs) {
                            assert.equal(reportDocSnap.data().reportStatus, "REJECTED");
                        }
                    });
                });
            });
        });

    });

});


//region Methods for clusteringMS testing.
//================================================================================
function createExampleGroup() {
    return {
        firstTimestamp: new Date('December 19, 2019 04:51:00'),
        groupStatus: "APPROVED",
        lastTimestamp: new Date('December 19, 2019 04:51:00'),
        latitude: 1.0,
        licensePlate: "TE333ST",
        longitude: 1.0,
        reports: ["test-violation-1", "test_violation-2"],
        typeOfViolation: "DOUBLE_PARKING"
    };
}

function createExampleGroupThatShouldCluster() {
    const exampleGroup = createExampleGroup();
    exampleGroup.latitude += 0.00001;
    exampleGroup.longitude += 0.00001;
    return exampleGroup;
}
//endregion


//region Methods for groupingMS adn approvingMS testing.
//================================================================================
function createExampleViolationReport() {
    return {
        description: "FUNCTION_UNIT_TEST_DATA",
        latitude: 1.0,
        licensePlate: "TE333ST",
        longitude: 1.0,
        municipality: "testMunicip",
        pictures: ["other-thing", "vehicle"],
        reportStatus: "SUBMITTED",
        statusMotivation: null,
        typeOfViolation: "DOUBLE_PARKING",
        uploadTimestamp: new Date('December 19, 2019 04:51:00'),
        userUid: "testUser"
    };
}

function createExampleViolationReportThatShouldGroup(uploadTimestamp) {
    const exampleViolationReport = createExampleViolationReport();
    exampleViolationReport.latitude += 0.00001;
    exampleViolationReport.longitude += 0.00001;
    exampleViolationReport.uploadTimestamp = uploadTimestamp;
    return exampleViolationReport;
}
//endregion


//region General methods.
//================================================================================
function getPromiseOfDatabaseResetting() {
    let groupDeletionPromise = db.collection("municipalities").doc("testMunicip").collection("groups").listDocuments().then(getPromisesOfDeletionOfAllDocuments);
    let clusterDeletionPromise = db.collection("municipalities").doc("testMunicip").collection("clusters").listDocuments().then(getPromisesOfDeletionOfAllDocuments);
    let reportsDeletionPromise = db.collection("violationReports").listDocuments().then(getPromisesOfDeletionOfAllDocuments);
    return Promise.all([groupDeletionPromise, clusterDeletionPromise, reportsDeletionPromise]);
}

function getPromisesOfDeletionOfAllDocuments(documentRefs) {
    let promises = [];
    for (let i = 0; i < documentRefs.length; i++) {
        promises.push(documentRefs[i].delete());
    }
    return Promise.all(promises);
}
//endregion