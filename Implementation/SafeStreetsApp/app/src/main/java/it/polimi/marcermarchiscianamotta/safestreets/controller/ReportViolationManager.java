package it.polimi.marcermarchiscianamotta.safestreets.controller;

import android.location.Address;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import it.polimi.marcermarchiscianamotta.safestreets.R;
import it.polimi.marcermarchiscianamotta.safestreets.model.ViolationEnum;
import it.polimi.marcermarchiscianamotta.safestreets.model.ViolationReport;
import it.polimi.marcermarchiscianamotta.safestreets.util.GeneralUtils;
import it.polimi.marcermarchiscianamotta.safestreets.util.ImageRecognition;
import it.polimi.marcermarchiscianamotta.safestreets.util.MapManager;
import it.polimi.marcermarchiscianamotta.safestreets.util.cloud.AuthenticationManager;
import it.polimi.marcermarchiscianamotta.safestreets.util.cloud.DatabaseConnection;
import it.polimi.marcermarchiscianamotta.safestreets.util.cloud.StorageConnection;
import it.polimi.marcermarchiscianamotta.safestreets.util.interfaces.ImageRecognitionUser;
import it.polimi.marcermarchiscianamotta.safestreets.util.interfaces.MapUser;
import it.polimi.marcermarchiscianamotta.safestreets.view.ReportViolationActivity;

/**
 * This class manages the violation reporting.
 *
 * @author Marcer
 * @author Desno365
 */
public class ReportViolationManager implements ImageRecognitionUser, MapUser {

	//Tag for logging
	private static final String TAG = "ReportViolationManager";

	//Constants
	private static final int MAX_NUM_OF_PHOTOS = 5;

	//Information about the caller
	private ReportViolationActivity reportViolationActivity;
	private View rootView;

	//The violation report that is going to be sent.
	private ViolationReport report;

	//Identifier of the pictures on the server.
	//They are needed in order to link the report with its pictures correctly.
	private List<String> picturesIDOnServer;

	private int numberOfUploadedPhotos = 0;

	//Constructor
	//================================================================================
	public ReportViolationManager(ReportViolationActivity reportViolationActivity, View rootView) {
		this.reportViolationActivity = reportViolationActivity;
		this.rootView = rootView;
		report = new ViolationReport(AuthenticationManager.getUserUid());
	}

	//region Public methods
	//================================================================================

	/**
	 * Sets the report description and starts uploading the photos to the server.
	 *
	 * @param description report's description.
	 */
	public void sendViolationReport(String description) {
		report.setDescription(description);
		uploadPhotosToCloudStorage();
	}

	/**
	 * Returns true if and only if all mandatory fields of the report are specified.
	 *
	 * @return true if and only if all mandatory fields of the report are specified.
	 */
	public boolean isReadyToSend() {
		return report.isReadyToSend();
	}

	/**
	 * Adds the specified photo path to the report and then stats two processes in
	 * order to retrieve the license plate and the current location.
	 *
	 * @param photoPath the path of the photo to be added to the report.
	 */
	public void addPhotoToReport(Uri photoPath) {
		report.addPhoto(photoPath);
		ImageRecognition.retrievePlateFromPhoto(reportViolationActivity, photoPath, this);
		MapManager.retrieveLocation(reportViolationActivity, this);
	}

	/**
	 * Sets the violation type to the report.
	 *
	 * @param violationType the violation type of the report to be set.
	 */
	public void setViolationType(String violationType) {
		//Default violation type is the first one
		ViolationEnum resultType = ViolationEnum.values()[0];

		//Converts the string to the corresponding enum
		for (ViolationEnum violationEnum : ViolationEnum.values()) {
			if (violationType.equals(violationEnum.toString()))
				resultType = violationEnum;
		}

		report.setTypeOfViolation(resultType);
	}

	/**
	 * Returns true if and only if the photos associated with the report are
	 * less than the maximum number of photos permitted.
	 *
	 * @return true if and only if the photos associated with the report are less than the maximum number of photos permitted.
	 */
	public boolean canTakeAnotherPicture() {
		return report.getPictures().size() < MAX_NUM_OF_PHOTOS;
	}

	/**
	 * Returns the maximum number of pictures that can be taken.
	 *
	 * @return the maximum number of pictures that can be taken.
	 */
	public int getMaxNumOfPictures() {
		return MAX_NUM_OF_PHOTOS;
	}

	/**
	 * Returns the current number of pictures taken.
	 *
	 * @return the current number of pictures taken.
	 */
	public int numberOfPictures() {
		return report.getPictures().size();
	}

	/**
	 * Returns the picture's path in the specified position.
	 *
	 * @param index index of the picture.
	 * @return the picture's path in the specified position.
	 */
	public Uri getPicture(int index) {
		return report.getPicture(index);
	}

	/**
	 * Removes the picture at the specified index.
	 *
	 * @param index index of the picture to remove.
	 */
	public void removePicture(int index) {
		report.removePhoto(index);
	}

	/**
	 * Sets the license plate number to the specified one.
	 *
	 * @param plate license plate number to set.
	 */
	public boolean setPlate(String plate) {
		boolean changed = false;
		if (GeneralUtils.isPlate(plate)) {
			changed = true;
			report.setLicensePlate(plate);
		} else
			GeneralUtils.showSnackbar(rootView, "Please insert a valid licence plate format.");
		return changed;
	}

	/**
	 * If the text recognition process has found a license plate, the report is updated with this new information.
	 * Moreover the view is updated to show the plate found.
	 *
	 * @param result the String found by the text recognition process.
	 */
	@Override
	public void onTextRecognized(String[] result) {
		if (result != null && result.length > 0) {
			Log.d(TAG, "Plate found: " + result[0]);
			GeneralUtils.showSnackbar(rootView, "Plate found: " + result[0]);
			if (!report.hasPlate()) {
				report.setLicensePlate(result[0]);
				reportViolationActivity.setPlateText(result[0]);
			}
		} else {
			Log.d(TAG, "No plate found");
			GeneralUtils.showSnackbar(rootView, "No plate found");
		}
	}

	/**
	 * Once the location has been found retrieves the municipality and updates the report.
	 * Moreover the view is updated to show the municipality.
	 *
	 * @param location The current location.
	 */
	@Override
	public void onLocationFound(LatLng location) {
		report.setLocation(location);
		Log.d(TAG, "Location[" + location.latitude + ", " + location.longitude + "] set.");
		MapManager.getAddressFromLocation(reportViolationActivity.getApplicationContext(), this, location);
	}

	@Override
	public void onAddressFound(Address address) {
		report.setMunicipality(address.getLocality());
		reportViolationActivity.setAddressText(address.getThoroughfare() + ", " + address.getLocality());
		Log.d(TAG, "Address: " + address + " set.");

	}
	//endregion


	//region Private methods
	//================================================================================

	/**
	 * Uploads the photos to the Cloud Storage.
	 */
	private void uploadPhotosToCloudStorage() {
		((ProgressBar) rootView.findViewById(R.id.uploading_progress_bar)).setMax(numberOfPictures());

		Log.d(TAG, "Uploading photos...");

		reportViolationActivity.onPictureUploaded(0, numberOfPictures());
		//In picturesIDOnServer are saved the identifiers of the pictures on the cloud storage so that they can bi linked by the report.
		picturesIDOnServer = StorageConnection.uploadPicturesToCloudStorage(report.getPictures(), reportViolationActivity,
				//Called each time a photo has been uploaded correctly
				taskSnapshot -> {
					checkIfAllUploadsEnded();
					Log.d(TAG, "Uploaded " + numberOfUploadedPhotos + " out of " + numberOfPictures());
					reportViolationActivity.onPictureUploaded(numberOfUploadedPhotos, numberOfPictures());//TODO create interface
				},
				//Called if the upload throws an exception
				e -> {
					Log.e(TAG, "uploadPhotosToCloudStorage:onError", e);
					GeneralUtils.showSnackbar(rootView, "Failed to upload the photos. Please try again.");
					Toast.makeText(reportViolationActivity, "Upload failed", Toast.LENGTH_SHORT).show();
				});

		report.setPicturesIDOnServer(picturesIDOnServer);
	}

	/**
	 * Checks if all the photo have been sent. If so uploads the report.
	 */
	private void checkIfAllUploadsEnded() {
		numberOfUploadedPhotos++;

		//If all photos have been uploaded
		if (picturesIDOnServer != null && numberOfUploadedPhotos == picturesIDOnServer.size())
			insertViolationReportInDatabase();
	}

	/**
	 * Uploads the report to the database.
	 */
	private void insertViolationReportInDatabase() {
		// Upload object to database.
		DatabaseConnection.uploadViolationReport(report.getReportRepresentation(), reportViolationActivity,
				// On success.
				voidObj -> {
					GeneralUtils.showSnackbar(rootView, "Violation report sent successfully!");
					//Close the reporting activity
					reportViolationActivity.finish();
				},
				// On failure.
				e -> {
					GeneralUtils.showSnackbar(rootView, "Failed to send the violation report. Please try again.");
					Log.e(TAG, "Failed to write message", e);
				});
	}
	//endregion
}
