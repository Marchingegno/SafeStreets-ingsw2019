package it.polimi.marcermarchiscianamotta.safestreets.util;

import androidx.annotation.NonNull;

public enum ViolationEnum {
	//TODO add more violations from https://en.wikipedia.org/wiki/Parking_violation
	PARKING_IN_A_PROHIBITED_SPACE("Parking in a prohibited space"),
	PARKING_ON_A_SIDEWALK("Parking on a sidewalk"),
	PARKING_TOO_CLOSE_AN_INTERSECTION("Parking too close an intersection"),
	DOUBLE_PARKING("Double parking"),
	PARKING_IN_A_HANDICAPPED_ZONE("Parking in a handicapped zone"),
	PARKING_AT_A_PARKING_METER_WITHOUT_PAYING("Parking at a parking meter without paying"),
	PARKING_IN_A_ZTL("Parking in a ZTL"),
	PARKING_FOR_LONGER_THAN_THE_MAXIMUM_TIME("Parking for longer than the maximum time"),
	PARKING_OUTSIDE_MARKED_SQUARES("Parking outside marked squares"),
	OTHER("Other");

	private String text;
	ViolationEnum(String string){
		text = string;
	}

	@NonNull
	@Override
	public String toString() {
		return text;
	}
}
