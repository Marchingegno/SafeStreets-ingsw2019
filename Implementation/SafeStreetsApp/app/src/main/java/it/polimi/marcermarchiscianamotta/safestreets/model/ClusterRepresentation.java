package it.polimi.marcermarchiscianamotta.safestreets.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a cluster. This class contains only the relevant attributes
 * that need to be sent or retrieved from the database.
 */
public class ClusterRepresentation {
	private List<String> groups = new ArrayList<>();
	private double latitude;
	private double longitude;
	private String typeOfViolation;

	public ClusterRepresentation() {
	}

	public List<String> getGroups() {
		return groups;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public String getTypeOfViolation() {
		return typeOfViolation;
	}
}
