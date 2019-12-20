'use strict';


/**
 * Triggers when a new violation report is created.
 * The changes made by this function may trigger the groupingMS.
 */
if (!process.env.FUNCTION_NAME || process.env.FUNCTION_NAME === 'approvingMS') {
  exports.approvingMS = require('./approvingMS').approvingMS;
}

/**
 * Triggers when a new group is created.
 */
if (!process.env.FUNCTION_NAME || process.env.FUNCTION_NAME === 'clusteringMS') {
  exports.clusteringMS = require('./clusteringMS').clusteringMS;
}

/**
 * Triggers when a violation report is updated, and starts only if the report has been approved.
 * The changes made by this function may trigger the clusteringMS.
 */
if (!process.env.FUNCTION_NAME || process.env.FUNCTION_NAME === 'groupingMS') {
  exports.groupingMS = require('./groupingMS').groupingMS;
}

/**
 * Triggers when the municipality confirms or rejects a violation group.
 */
if (!process.env.FUNCTION_NAME || process.env.FUNCTION_NAME === 'onReportStatusChangeMS') {
  exports.onReportStatusChangeMS = require('./onReportStatusChangeMS').onReportStatusChangeMS;
}

/**
 * Triggers on time.
 * WARNING: time triggers need the Firebase Blaze payment plan.
 */
/*if (!process.env.FUNCTION_NAME || process.env.FUNCTION_NAME === 'municipalityDataRetrieverMS') {
  exports.municipalityDataRetrieverMS = require('./municipalityDataRetrieverMS').municipalityDataRetrieverMS;
}*/





// TODO delete

/**
 * Hello world.
 */
/*if (!process.env.FUNCTION_NAME || process.env.FUNCTION_NAME === 'helloWorld') {
  exports.helloWorld = require('./helloWorld').helloWorldFunction;
}*/


/**
 * Triggers when a new violation report is added.
 */
/*if (!process.env.FUNCTION_NAME || process.env.FUNCTION_NAME === 'checkViolationReports') {
  exports.checkViolationReports = require('./checkViolationReports').checkViolationReportsTrigger;
}*/