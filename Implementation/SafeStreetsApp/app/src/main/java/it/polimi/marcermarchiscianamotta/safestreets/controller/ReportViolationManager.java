package it.polimi.marcermarchiscianamotta.safestreets.controller;

import android.app.Activity;
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

public class ReportViolationManager implements ImageRecognitionUser, MapUser {

	private static final int MAX_NUM_OF_PHOTOS = 5;
	private static final String TAG = "ReportViolationManager";
	private Activity activity;
	private View rootView;

	private ViolationReport report;

	private List<String> picturesIDOnServer;
	private int numberOfUploadedPhotos = 0;


	public ReportViolationManager(Activity activity, View rootView) {
		this.activity = activity;
		this.rootView = rootView;
		report = new ViolationReport(AuthenticationManager.getUserUid());
	}


	//region Public methods
	//================================================================================
	public void sendViolationReport(String description) {
		report.setDescription(description);
		Toast.makeText(activity, "Uploading photos...", Toast.LENGTH_SHORT).show();
		uploadPhotosToCloudStorage();
	}

	public boolean isReadyToSend() {
		return report.isReadyToSend();
	}

	public void addPhotoToReport(Uri photoPath) {
		report.addPhoto(photoPath);
		ImageRecognition.retrievePlateFromPhoto(activity, photoPath, this);
		MapManager.retrieveLocation(activity, this);
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
			if (!report.hasPlate())
				report.setLicencePlate(result);
		} else {
			Log.d(TAG, "No plate found");
			GeneralUtils.showSnackbar(rootView, "No plate found");
		}
	}

	@Override
	public void onLocationFound(double latitude, double longitude) {
		report.setLocationAndMunicipality(activity, latitude, longitude);
		Log.d(TAG, "Location and Municipality set.");
	}
	//endregion


	//region Private methods
	//================================================================================
	private void uploadPhotosToCloudStorage() {
		picturesIDOnServer = StorageConnection.uploadPicturesToCloudStorage(report.getPictures(), activity,
				taskSnapshot -> {
					checkIfAllUploadsEnded();
					Toast.makeText(activity, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
				},
				e -> {
					Log.e(TAG, "uploadPhotosToCloudStorage:onError", e);
					GeneralUtils.showSnackbar(rootView, "Failed to upload the photos. Please try again.");
					Toast.makeText(activity, "Upload failed", Toast.LENGTH_SHORT).show();
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
		DatabaseConnection.uploadViolationReport(report.getReportRepresentation(), activity,
				input -> {
					GeneralUtils.showSnackbar(rootView, "Violation report sent successfully!");
					activity.finish();
				},
				e -> {
					GeneralUtils.showSnackbar(rootView, "Failed to send the violation report. Please try again.");
					Log.e(TAG, "Failed to write message", e);
				});
	}


	//endregion
}
