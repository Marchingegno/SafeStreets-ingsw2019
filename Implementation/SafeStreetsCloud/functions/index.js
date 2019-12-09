'use strict';


/**
 * Hello world.
 */
if (!process.env.FUNCTION_NAME || process.env.FUNCTION_NAME === 'helloWorld') {
  exports.helloWorld = require('./helloWorld').helloWorldFunction;
}


/**
 * Triggers when a new violation report is added.
 */
if (!process.env.FUNCTION_NAME || process.env.FUNCTION_NAME === 'checkViolationReports') {
  exports.checkViolationReports = require('./checkViolationReports').checkViolationReportsTrigger;
}