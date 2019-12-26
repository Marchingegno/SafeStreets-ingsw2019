package it.polimi.marcermarchiscianamotta.safestreets.model;

import java.util.ArrayList;
import java.util.List;

public class Cluster {
	private List<String> groups = new ArrayList<>();
	private double latitude;
	private double longitude;
	private String typeOfViolation;

	public Cluster() {
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
