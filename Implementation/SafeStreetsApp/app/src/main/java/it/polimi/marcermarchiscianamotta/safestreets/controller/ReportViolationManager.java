package it.polimi.marcermarchiscianamotta.safestreets.controller;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.Timestamp;

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

	private List<String> picturesInUpload;
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

	public void setTimestamp(Timestamp timestamp) {
		report.setTimestamp(timestamp);
	}

	public void addPhotoToReport(Uri photoPath) {
		report.addPhoto(photoPath);
		ImageRecognition.retrievePlateFromPhoto(activity, photoPath, this);
		MapManager.retireveLocation(activity, this);
	}

	public boolean canTakeAnotherPicture() {
		return report.getViolationPhotos().size() < MAX_NUM_OF_PHOTOS;
	}

	public int getMaxNumOfPhotos() {
		return MAX_NUM_OF_PHOTOS;
	}

	public int numberOfPhotos() {
		return report.getViolationPhotos().size();
	}

	@Override
	public void onTextRecognized(String result) {
		GeneralUtils.showSnackbar(rootView, result);
		if (result != null) {
			Log.d(TAG, "Plate found: " + result);
			if (!report.hasPlate())
				report.setLicencePlate(result);
		}
	}

	@Override
	public void onLocationFound(double latitude, double longitude) {
		report.setCoordinates(activity, latitude, longitude);
	}
	//endregion


	//region Private methods
	//================================================================================
	private void uploadPhotosToCloudStorage() {
		picturesInUpload = StorageConnection.uploadPicturesToCloudStorage(report.getViolationPhotos(), activity,
				taskSnapshot -> {
					checkIfAllUploadsEnded();
					Toast.makeText(activity, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
				},
				e -> {
					Log.e(TAG, "uploadPhotosToCloudStorage:onError", e);
					GeneralUtils.showSnackbar(rootView, "Failed to upload the photos. Please try again.");
					Toast.makeText(activity, "Upload failed", Toast.LENGTH_SHORT).show();
				});
	}

	private void checkIfAllUploadsEnded() {
		numberOfUploadedPhotos++;
		if (picturesInUpload != null && numberOfUploadedPhotos == picturesInUpload.size()) {
			// End upload
			insertViolationReportInDatabase();
		}
	}

	private void insertViolationReportInDatabase() {
		// Upload object to database.
		DatabaseConnection.uploadViolationReport(report.getReportRepresentation(), activity,
				input -> {
					GeneralUtils.showSnackbar(rootView, "Violation ViolationReport sent successfully!");
					activity.finish();
				},
				e -> {
					GeneralUtils.showSnackbar(rootView, "Failed to send the violation report. Please try again.");
					Log.e(TAG, "Failed to write message", e);
				});
	}


	//endregion
}
