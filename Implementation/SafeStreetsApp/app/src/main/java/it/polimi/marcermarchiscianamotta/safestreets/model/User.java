package it.polimi.marcermarchiscianamotta.safestreets.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO add comments
 */
public class User {

	private List<String> violationReportIds;

	public User() {
		// Needed by Firebase
	}

	public User(String initialReference) {
		violationReportIds = new ArrayList<>();
		addReference(initialReference);
	}

	@NonNull
	public List<String> getViolationReportIds() {
		return violationReportIds;
	}

	public void setViolationReportIds(List<String> violationReportIds) {
		this.violationReportIds = violationReportIds;
	}

	public void addReference(String reference) {
		violationReportIds.add(reference);
	}
}
