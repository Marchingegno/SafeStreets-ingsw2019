package it.polimi.marcermarchiscianamotta.safestreets.controller;

import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import it.polimi.marcermarchiscianamotta.safestreets.model.ViolationReport;
import it.polimi.marcermarchiscianamotta.safestreets.util.AuthenticationManager;
import it.polimi.marcermarchiscianamotta.safestreets.util.DatabaseConnection;
import it.polimi.marcermarchiscianamotta.safestreets.util.GeneralUtils;
import it.polimi.marcermarchiscianamotta.safestreets.util.ImageRecognition;
import it.polimi.marcermarchiscianamotta.safestreets.util.ImageRecognitionUser;
import it.polimi.marcermarchiscianamotta.safestreets.util.MapManager;
import it.polimi.marcermarchiscianamotta.safestreets.util.MapUser;
import it.polimi.marcermarchiscianamotta.safestreets.util.StorageConnection;
import it.polimi.marcermarchiscianamotta.safestreets.util.ViolationEnum;
import it.polimi.marcermarchiscianamotta.safestreets.view.ReportViolationActivity;

public class ReportViolationManager implements ImageRecognitionUser, MapUser {

	private static final int MAX_NUM_OF_PHOTOS = 5;
	private static final String TAG = "ReportViolationManager";
	private ReportViolationActivity reportViolationActivity;
	private View rootView;

	private ViolationReport report;

	private List<String> picturesIDOnServer;
	private int numberOfUploadedPhotos = 0;


	public ReportViolationManager(ReportViolationActivity reportViolationActivity, View rootView) {
		this.reportViolationActivity = reportViolationActivity;
		this.rootView = rootView;
		report = new ViolationReport(AuthenticationManager.getUserUid());
	}


	//region Public methods
	//================================================================================
	public void sendViolationReport(String description) {
		report.setDescription(description);
		Toast.makeText(reportViolationActivity, "Uploading photos...", Toast.LENGTH_SHORT).show();
		uploadPhotosToCloudStorage();
	}

	public boolean isReadyToSend() {
		return report.isReadyToSend();
	}

	public void addPhotoToReport(Uri photoPath) {
		report.addPhoto(photoPath);
		ImageRecognition.retrievePlateFromPhoto(reportViolationActivity, photoPath, this);
		MapManager.retrieveLocation(reportViolationActivity, this);
	}

	public void setViolationType(String violationType) {
		//Default violation type is the first one
		ViolationEnum resultType = ViolationEnum.values()[0];
		for (ViolationEnum violationEnum : ViolationEnum.values()) {
			if (violationType.equals(violationEnum.toString()))
				resultType = violationEnum;
		}
		report.setTypeOfViolation(resultType);
	}

	public boolean canTakeAnotherPicture() {
		return report.getPictures().size() < MAX_NUM_OF_PHOTOS;
	}

	public int getMaxNumOfPhotos() {
		return MAX_NUM_OF_PHOTOS;
	}

	public int numberOfPhotos() {
		return report.getPictures().size();
	}

	@Override
	public void onTextRecognized(String result) {
		if (result != null) {
			Log.d(TAG, "Plate found: " + result);
			GeneralUtils.showSnackbar(rootView, "Plate found: " + result);
			if (!report.hasPlate()) {
				report.setlicensePlate(result);
				reportViolationActivity.setPlateText(result);
			}
		} else {
			Log.d(TAG, "No plate found");
			GeneralUtils.showSnackbar(rootView, "No plate found");
		}
	}

	@Override
	public void onLocationFound(double latitude, double longitude) {
		report.setLocation(latitude, longitude);
		String municipality = MapManager.getMunicipalityFromLocation(reportViolationActivity.getApplicationContext(), latitude, longitude);
		reportViolationActivity.setMunicipalityText(municipality);
		report.setMunicipality(municipality);
		Log.d(TAG, "Location[" + latitude + ", " + longitude + "] and Municipality[" + municipality + "] set.");
	}
	//endregion

	//region Private methods
	//================================================================================
	private void uploadPhotosToCloudStorage() {
		picturesIDOnServer = StorageConnection.uploadPicturesToCloudStorage(report.getPictures(), reportViolationActivity,
				taskSnapshot -> {
					checkIfAllUploadsEnded();
					Toast.makeText(reportViolationActivity, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
				},
				e -> {
					Log.e(TAG, "uploadPhotosToCloudStorage:onError", e);
					GeneralUtils.showSnackbar(rootView, "Failed to upload the photos. Please try again.");
					Toast.makeText(reportViolationActivity, "Upload failed", Toast.LENGTH_SHORT).show();
				});
		report.setPicturesIDOnServer(picturesIDOnServer);
	}

	private void checkIfAllUploadsEnded() {
		numberOfUploadedPhotos++;
		if (picturesIDOnServer != null && numberOfUploadedPhotos == picturesIDOnServer.size()) {
			// End upload
			insertViolationReportInDatabase();
		}
	}

	private void insertViolationReportInDatabase() {
		// Upload object to database.
		DatabaseConnection.uploadViolationReport(report.getReportRepresentation(), reportViolationActivity,
				input -> {
					GeneralUtils.showSnackbar(rootView, "Violation report sent successfully!");
					reportViolationActivity.finish();
				},
				e -> {
					GeneralUtils.showSnackbar(rootView, "Failed to send the violation report. Please try again.");
					Log.e(TAG, "Failed to write message", e);
				});
	}


	//endregion
}
