package it.polimi.marcermarchiscianamotta.safestreets.model;

import android.content.Context;
import android.net.Uri;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

import it.polimi.marcermarchiscianamotta.safestreets.util.MapManager;
import it.polimi.marcermarchiscianamotta.safestreets.util.ViolationEnum;

public class Report {
	private String licencePlate;
	private float latitude;
	private float longitude;
	private String locality;
	private List<Uri> violationPhotos = new ArrayList<>();
	private Timestamp timestamp;
	private ViolationEnum typeOfViolation;

	public String getLicencePlate() {
		return licencePlate;
	}

	public void setLicencePlate(String licencePlate) {
		this.licencePlate = licencePlate;
	}

	public float getLatitude() {
		return latitude;
	}

	public void setCoordinates(Context context, float latitude, float longitude) {
		this.latitude = latitude;
		this.longitude = longitude;

		setLocality(context);
	}

	private void setLocality(Context context) {
		this.locality = MapManager.getCityFromLocation(context, latitude, longitude);
	}

	public String getLocality() {
		return locality;
	}

	public float getLongitude() {
		return longitude;
	}

	public List<Uri> getViolationPhotos() {
		return violationPhotos;
	}

	public void setViolationPhotos(List<Uri> violationPhotos) {
		this.violationPhotos = violationPhotos;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public ViolationEnum getTypeOfViolation() {
		return typeOfViolation;
	}

	public void setTypeOfViolation(ViolationEnum typeOfViolation) {
		this.typeOfViolation = typeOfViolation;
	}
}
