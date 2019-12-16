package it.polimi.marcermarchiscianamotta.safestreets.model;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

import it.polimi.marcermarchiscianamotta.safestreets.util.MapManager;
import it.polimi.marcermarchiscianamotta.safestreets.util.ViolationEnum;

public class ViolationReport {
	private static final String TAG = "ViolationReport";

	private String userUid;
	private String licencePlate = null;
	private String description;
	private Double latitude;
	private Double longitude;
	private String locality;
	private List<Uri> violationPhotos = new ArrayList<>();
	private Timestamp timestamp;
	private ViolationEnum typeOfViolation = ViolationEnum.PARKING_OUTSIDE_OF_THE_LINES; //TODO update with users choice
	private ReportStatus reportStatus = ReportStatus.SUBMITTED;
	private String statusMotivation = null;

	public ViolationReport(String userUid) {
		this.userUid = userUid;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public void setCoordinates(Context context, double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;

		setLocality(context);
	}

	public String getLocality() {
		return locality;
	}

	private void setLocality(Context context) {
		this.locality = MapManager.getCityFromLocation(context, latitude, longitude);
		Log.d(TAG, "Locality added: " + locality);
	}

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

	public double getLongitude() {
		return longitude;
	}

	public String getTypeOfViolation() {
		return typeOfViolation.toString();
	}

	public void setTypeOfViolation(ViolationEnum typeOfViolation) {
		this.typeOfViolation = typeOfViolation;
	}

	public List<Uri> getViolationPhotos() {
		return violationPhotos;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUserUid() {
		return userUid;
	}

	public void addPhoto(Uri photoPath) {
		violationPhotos.add(photoPath);
		Log.d(TAG, "Photo added: " + photoPath);
	}

	public boolean hasPlate() {
		return licencePlate != null;
	}

	public boolean isReadyToSend() {
		Log.d(TAG, this.toString());
		return userUid != null &&
				licencePlate != null &&
				latitude != null &&
				longitude != null &&
				locality != null &&
				timestamp != null &&
				violationPhotos.size() > 0 &&
				typeOfViolation != null &&
				reportStatus == ReportStatus.SUBMITTED;
	}

	public ViolationReportRepresentation getReportRepresentation() {
		return new ViolationReportRepresentation(
				userUid,
				licencePlate,
				description,
				latitude,
				longitude,
				locality,
				timestamp,
				typeOfViolation);
	}

	@Override
	public String toString() {
		return "ViolationReport[" + '\n' +
				"userUid= " + userUid + '\n' +
				"licencePlate= " + licencePlate + '\n' +
				"description= " + description + '\n' +
				"latitude= " + latitude + '\n' +
				"longitude =" + longitude + '\n' +
				"locality= " + locality + '\n' +
				"violationPhotos= " + violationPhotos + '\n' +
				"timestamp= " + timestamp + '\n' +
				"typeOfViolation= " + typeOfViolation + '\n' +
				"reportStatus= " + reportStatus + '\n' +
				"statusMotivation= " + statusMotivation + ']';
	}
}

