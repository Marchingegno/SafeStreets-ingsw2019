package it.polimi.marcermarchiscianamotta.safestreets.model;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

import it.polimi.marcermarchiscianamotta.safestreets.util.MapManager;
import it.polimi.marcermarchiscianamotta.safestreets.util.ViolationEnum;

public class Report {
	private static final String TAG = "Report";

	private String licencePlate = null;
	private double latitude;
	private double longitude;
	private String locality;
	private List<Uri> violationPhotos = new ArrayList<>();
	private Timestamp timestamp;
	private ViolationEnum typeOfViolation;

	public String getLicencePlate() {
		return licencePlate;
	}

	public void setLicencePlate(String licencePlate) {
		this.licencePlate = licencePlate;
		Log.d(TAG, "Licence plate: " + licencePlate);
	}

	public double getLatitude() {
		return latitude;
	}

	public void setCoordinates(Context context, double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;

		setLocality(context);
	}

	private void setLocality(Context context) {
		this.locality = MapManager.getCityFromLocation(context, latitude, longitude);
		Log.d(TAG, "Locality added: " + locality);
	}

	public String getLocality() {
		return locality;
	}

	public double getLongitude() {
		return longitude;
	}

	public List<Uri> getViolationPhotos() {
		return violationPhotos;
	}

	public void addPhoto(Uri photoPath) {
		violationPhotos.add(photoPath);
		Log.d(TAG, "Photo added: " + photoPath);
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

	public boolean hasPlate() {
		return licencePlate != null;
	}
}