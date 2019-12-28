package it.polimi.marcermarchiscianamotta.safestreets.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Cluster {
	private List<String> groups;
	private double latitude;
	private double longitude;
	private ViolationEnum typeOfViolation;
	private Date firstAddedDate;
	private Date lastAddedDate;


	public Cluster(ClusterRepresentation representation) {
		this.groups = new ArrayList<>(representation.getGroups());
		this.latitude = representation.getLatitude();
		this.longitude = representation.getLongitude();
		this.typeOfViolation = (ViolationEnum.valueOf(representation.getTypeOfViolation()));
		this.lastAddedDate = representation.getLastAddedDate();
		this.firstAddedDate = representation.getFirstAddedDate();
	}

	//region Getter methods
	//================================================================================
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

	public Date getFirstAddedDate() {
		return firstAddedDate;
	}

	public Date getLastAddedDate() {
		return lastAddedDate;
	}
	//endregion

	//region Public methods
	//================================================================================

	@Override
	public String toString() {
		return "Cluster{" +
				"\ntypeOfViolation = " + typeOfViolation +
				"\nlatitude = " + latitude +
				"\nlongitude = " + longitude +
				"\nfirstAddedDate = " + firstAddedDate +
				"\nlastAddedDate = " + lastAddedDate +
				"\ngroups = " + groups +
				'}';
	}

	//endregion
}
