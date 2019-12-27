package it.polimi.marcermarchiscianamotta.safestreets.model;

import androidx.annotation.NonNull;

/**
 * The list of possible traffic violations.
 */
public enum ViolationEnum {
	//TODO add more violations from https://en.wikipedia.org/wiki/Parking_violation
	PARKING_IN_A_PROHIBITED_SPACE("Parking in a prohibited space", "#e5ff22"),
	PARKING_ON_A_SIDEWALK("Parking on a sidewalk", "#ff0000"),
	PARKING_TOO_CLOSE_AN_INTERSECTION("Parking too close an intersection", "#0004ff"),
	DOUBLE_PARKING("Double parking", "#00eeff"),
	PARKING_IN_A_HANDICAPPED_ZONE("Parking in a handicapped zone", "#11ff00"),
	PARKING_AT_A_PARKING_METER_WITHOUT_PAYING("Parking at a parking meter without paying", "#ffa200"),
	PARKING_IN_A_ZTL("Parking in a ZTL", "#b300ff"),
	PARKING_FOR_LONGER_THAN_THE_MAXIMUM_TIME("Parking for longer than the maximum time", "#ff00cc"),
	PARKING_OUTSIDE_MARKED_SQUARES("Parking outside marked squares", "#ff2299"),
	OTHER("Other", "#000000");

	private String text;
	private String color;

	ViolationEnum(String string, String color) {
		text = string;
		this.color = color;
	}

	@NonNull
	@Override
	public String toString() {
		return text;
	}

	public String getColor() {
		return color;
	}
}
