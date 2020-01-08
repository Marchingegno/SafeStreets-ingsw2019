package it.polimi.marcermarchiscianamotta.safestreets.model;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Contains all the attributes of a report.
 *
 * @author Marcer
 * @author Desno365
 */
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
	private ViolationTypeEnum typeOfViolation;
	private ReportStatusEnum reportStatus = ReportStatusEnum.SUBMITTED;
	private String statusMotivation = null;


	//Constructor
	//================================================================================
	public ViolationReport(String userUid) {
		this.userUid = userUid;
	}
	//endregion

	//region Setter methods
	//================================================================================
	public void setLocation(LatLng location) {
		this.latitude = location.latitude;
		this.longitude = location.longitude;
	}

	public void setMunicipality(String municipality) {
		this.municipality = municipality;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ViolationTypeEnum getTypeOfViolation() {
		return typeOfViolation;
	}

	public void setPicturesIDOnServer(List<String> picturesIDOnServer) {
		this.picturesIDOnServer = picturesIDOnServer;
	}

	public void setTypeOfViolation(ViolationTypeEnum typeOfViolation) {
		this.typeOfViolation = typeOfViolation;
	}
	//endregion

	//region Getter methods
	//================================================================================
	public String getMunicipality() {
		return municipality;
	}

	//endregion

	public String getLicensePlate() {
		return licensePlate;
	}

	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public List<Uri> getPictures() {
		return pictures;
	}

	public Uri getPicture(int index) {
		return pictures.get(index);
	}

	public void setLicensePlate(String licensePlate) {
		this.licensePlate = licensePlate;
		Log.d(TAG, "License plate set to: " + licensePlate);
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

	public String getUserUid() {
		return userUid;
	}

	public LatLng getLatLng() {
		return new LatLng(latitude, longitude);
	}
	//endregion

	//region Public methods
	//================================================================================

	/**
	 * Adds a photo path to the report.
	 *
	 * @param photoPath photo's path to be added.
	 */
	public void addPhoto(Uri photoPath) {
		pictures.add(photoPath);
		Log.d(TAG, "Photo added: " + photoPath);
	}

	/**
	 * Removes the picture at the specified index.
	 *
	 * @param index index of the picture to delete.
	 */
	public void removePhoto(int index) {
		pictures.remove(index);
	}

	/**
	 * Returns true if and only if the license plate is not null.
	 *
	 * @return true if and only if the license plate is not null.
	 */
	public boolean hasPlate() {
		return licensePlate != null;
	}

	/**
	 * Returns true id and only if all the mandatory fields hare specified.
	 *
	 * @return true id and only if all the mandatory fields hare specified.
	 */
	public boolean isReadyToSend() {
		Log.d(TAG, this.toString());
		return userUid != null &&
				licensePlate != null &&
				latitude != null &&
				longitude != null &&
				municipality != null &&
				pictures.size() > 0 &&
				typeOfViolation != null &&
				reportStatus == ReportStatusEnum.SUBMITTED;
	}

	/**
	 * Returns a representation of the report so that it can be sent to the database.
	 *
	 * @return a representation of the report.
	 */
	public ViolationReportRepresentation getReportRepresentation() {
		return new ViolationReportRepresentation(this);
	}

	@Override
	public String toString() {
		return "ViolationReport[" + '\n' +
				"\tuserUid: " + userUid + '\n' +
				"\tlicensePlate: " + licensePlate + '\n' +
				"\tdescription: " + description + '\n' +
				"\tlatitude: " + latitude + '\n' +
				"\tlongitude: " + longitude + '\n' +
				"\tmunicipality: " + municipality + '\n' +
				"\tpictures: " + pictures + '\n' +
				"\tpicturesIDOnServer: " + picturesIDOnServer + '\n' +
				"\tuploadTimestamp: " + uploadTimestamp + '\n' +
				"\ttypeOfViolation: " + typeOfViolation + '\n' +
				"\treportStatus: " + reportStatus + '\n' +
				"\tstatusMotivation: " + statusMotivation + ']';
	}
	//endregion
}

