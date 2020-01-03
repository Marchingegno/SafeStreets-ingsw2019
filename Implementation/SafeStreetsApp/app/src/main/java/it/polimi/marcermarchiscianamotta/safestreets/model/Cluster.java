package it.polimi.marcermarchiscianamotta.safestreets.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a cluster.
 *
 * @author Marcer
 */
public class Cluster implements Serializable {
	private List<String> groups;
	private double latitude;
	private double longitude;
	private ViolationEnum typeOfViolation;
	private Date firstAddedDate;
	private Date lastAddedDate;

	//Constructor
	//================================================================================
	public Cluster(ClusterRepresentation representation) {
		this.groups = new ArrayList<>(representation.getGroups());
		this.latitude = representation.getLatitude();
		this.longitude = representation.getLongitude();
		this.typeOfViolation = (ViolationEnum.valueOf(representation.getTypeOfViolation()));
		this.lastAddedDate = representation.getLastAddedDate();
		this.firstAddedDate = representation.getFirstAddedDate();
	}
	//endregion

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

	public int numberOfGroups() {
		return groups.size();
	}

	@Override
	public String toString() {
		return "Cluster{" +
				"\t\ntypeOfViolation = " + typeOfViolation +
				"\t\nlatitude = " + latitude +
				"\t\nlongitude = " + longitude +
				"\t\nfirstAddedDate = " + firstAddedDate +
				"\t\nlastAddedDate = " + lastAddedDate +
				"\t\ngroups = " + groups +
				'}';
	}

	//endregion
}
