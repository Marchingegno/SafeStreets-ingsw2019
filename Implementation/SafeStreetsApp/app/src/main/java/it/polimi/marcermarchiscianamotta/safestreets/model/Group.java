package it.polimi.marcermarchiscianamotta.safestreets.model;


import androidx.annotation.NonNull;

import java.util.Date;

/**
 * Represents a Group.
 *
 * @author Marcer
 */
public class Group implements Comparable<Group> {
	private double latitude;
	private double longitude;
	private Date firstTimestamp;
	private Date lastTimestamp;
	private ReportStatusEnum groupStatus;
	private ViolationEnum typeOfViolation;
	private String licensePlate;

	//region Getter methods
	//================================================================================
	public Group() {
	}
	//endregion

	//region Getter methods
	//================================================================================
	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public Date getFirstTimestamp() {
		return firstTimestamp;
	}

	public Date getLastTimestamp() {
		return lastTimestamp;
	}

	public ReportStatusEnum getGroupStatus() {
		return groupStatus;
	}

	public ViolationEnum getTypeOfViolation() {
		return typeOfViolation;
	}

	public String getLicensePlate() {
		return licensePlate;
	}
	//endregion

	//region Overridden methods
	//================================================================================
	@NonNull
	@Override
	public String toString() {
		return "Group{" +
				"\t\nlatitude = " + latitude +
				"\t\nlongitude = " + longitude +
				"\t\nfirstTimestamp = " + firstTimestamp +
				"\t\nlastTimestamp = " + lastTimestamp +
				"\t\ngroupStatus = " + groupStatus +
				"\t\ntypeOfViolation = " + typeOfViolation +
				"\t\nlicensePlate = " + licensePlate +
				'}';
	}

	@Override
	public int compareTo(@NonNull Group o) {
		if (firstTimestamp == null)
			return 0;
		else
			return firstTimestamp.compareTo(o.getFirstTimestamp());
	}
	//endregion
}
