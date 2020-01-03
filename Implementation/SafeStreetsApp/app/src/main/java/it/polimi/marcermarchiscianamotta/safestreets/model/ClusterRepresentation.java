package it.polimi.marcermarchiscianamotta.safestreets.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a cluster. This class contains only the relevant attributes
 * that need to be sent or retrieved from the database.
 *
 * @author Marcer
 */
public class ClusterRepresentation {
	private List<String> groups = new ArrayList<>();
	private double latitude;
	private double longitude;
	private String typeOfViolation;
	private Date firstAddedDate;
	private Date lastAddedDate;

	//Constructor
	//================================================================================
	public ClusterRepresentation() {
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

	public String getTypeOfViolation() {
		return typeOfViolation;
	}

	public Date getFirstAddedDate() {
		return firstAddedDate;
	}

	public Date getLastAddedDate() {
		return lastAddedDate;
	}
	//endregion
}
