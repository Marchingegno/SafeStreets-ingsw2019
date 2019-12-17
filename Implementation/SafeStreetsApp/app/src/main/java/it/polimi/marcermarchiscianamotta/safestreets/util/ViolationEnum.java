package it.polimi.marcermarchiscianamotta.safestreets.util;

import androidx.annotation.NonNull;

public enum ViolationEnum {
	PARKING_OUTSIDE_THE_LINES("Parking outside the lines");

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
