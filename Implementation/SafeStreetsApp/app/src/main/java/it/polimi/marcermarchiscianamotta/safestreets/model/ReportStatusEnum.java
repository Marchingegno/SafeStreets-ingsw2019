package it.polimi.marcermarchiscianamotta.safestreets.model;

/**
 * Represents the possible statuses of the report.
 */
public enum ReportStatusEnum {
	SUBMITTED,
	APPROVED,
	REJECTED,
	CONFIRMED, //TODO remove once no longer in the database
	CORRECT
}
