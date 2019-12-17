package it.polimi.marcermarchiscianamotta.safestreets.model;

import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.polimi.marcermarchiscianamotta.safestreets.util.ViolationEnum;

public class ViolationReport {
	private static final String TAG = "ViolationReport";

	private String userUid;
	private String licensePlate = null;
	private String description;
	private Double latitude;
	private Double longitude;
	private String municipality;
	private List<Uri> pictures = new ArrayList<>();
	private List<String> picturesIDOnServer;
	private Date uploadTimestamp;
	private ViolationEnum typeOfViolation = ViolationEnum.PARKING_OUTSIDE_THE_LINES; //TODO update with users choice
	private ReportStatus reportStatus = ReportStatus.SUBMITTED;
	private String statusMotivation = null;


	public ViolationReport(String userUid) {
		this.userUid = userUid;
	}

	public void setLocation(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public void setMunicipality(String municipality) {
		this.municipality = municipality;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setTypeOfViolation(ViolationEnum typeOfViolation) {
		this.typeOfViolation = typeOfViolation;
	}

	public void setlicensePlate(String licensePlate) {
		this.licensePlate = licensePlate;
		Log.d(TAG, "license plate: " + licensePlate);
	}

	public String getMunicipality() {
		return municipality;
	}

	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public String getlicensePlate() {
		return licensePlate;
	}

	public List<Uri> getPictures() {
		return pictures;
	}

	public ViolationEnum getTypeOfViolation() {
		return typeOfViolation;
	}

	public Date getTimestamp() {
		return uploadTimestamp;
	}

	public List<String> getPicturesIDOnServer() {
		return picturesIDOnServer;
	}

	public String getDescription() {
		return description;
	}

	public void setPicturesIDOnServer(List<String> picturesIDOnServer) {
		this.picturesIDOnServer = picturesIDOnServer;
	}

	public String getUserUid() {
		return userUid;
	}

	public void addPhoto(Uri photoPath) {
		pictures.add(photoPath);
		Log.d(TAG, "Photo added: " + photoPath);
	}

	public boolean hasPlate() {
		return licensePlate != null;
	}

	public boolean isReadyToSend() {
		Log.d(TAG, this.toString());
		return userUid != null &&
				licensePlate != null &&
				latitude != null &&
				longitude != null &&
				municipality != null &&
				pictures.size() > 0 &&
				typeOfViolation != null &&
				reportStatus == ReportStatus.SUBMITTED;
	}

	public ViolationReportRepresentation getReportRepresentation() {
		return new ViolationReportRepresentation(this);
	}

	@Override
	public String toString() {
		return "ViolationReport[" + '\n' +
				"userUid: " + userUid + '\n' +
				"licensePlate: " + licensePlate + '\n' +
				"description: " + description + '\n' +
				"latitude: " + latitude + '\n' +
				"longitude: " + longitude + '\n' +
				"municipality: " + municipality + '\n' +
				"pictures: " + pictures + '\n' +
				"picturesIDOnServer: " + picturesIDOnServer + '\n' +
				"uploadTimestamp: " + uploadTimestamp + '\n' +
				"typeOfViolation: " + typeOfViolation + '\n' +
				"reportStatus: " + reportStatus + '\n' +
				"statusMotivation: " + statusMotivation + ']';
	}
}

