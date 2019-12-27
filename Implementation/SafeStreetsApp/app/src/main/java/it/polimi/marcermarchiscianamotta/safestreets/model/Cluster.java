package it.polimi.marcermarchiscianamotta.safestreets.model;

import java.util.ArrayList;
import java.util.List;

public class Cluster {
	private List<String> groups;
	private double latitude;
	private double longitude;
	private ViolationEnum typeOfViolation;

	public Cluster(ClusterRepresentation representation) {
		this.groups = new ArrayList<>(representation.getGroups());
		this.latitude = representation.getLatitude();
		this.longitude = representation.getLongitude();
		this.typeOfViolation = (ViolationEnum.valueOf(representation.getTypeOfViolation()));
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

	public ViolationEnum getTypeOfViolation() {
		return typeOfViolation;
	}
}
