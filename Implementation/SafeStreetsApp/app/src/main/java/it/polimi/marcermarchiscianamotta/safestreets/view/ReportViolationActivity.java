package it.polimi.marcermarchiscianamotta.safestreets.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.polimi.marcermarchiscianamotta.safestreets.R;
import it.polimi.marcermarchiscianamotta.safestreets.controller.ReportViolationManager;
import it.polimi.marcermarchiscianamotta.safestreets.model.ViolationEnum;
import it.polimi.marcermarchiscianamotta.safestreets.util.GeneralUtils;
import it.polimi.marcermarchiscianamotta.safestreets.util.LoadPictureTask;
import it.polimi.marcermarchiscianamotta.safestreets.util.SavePictureTask;
import it.polimi.marcermarchiscianamotta.safestreets.util.interfaces.LoadUser;
import it.polimi.marcermarchiscianamotta.safestreets.util.interfaces.SaveUser;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class ReportViolationActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, AdapterView.OnItemSelectedListener, SaveUser, LoadUser {

	private static final String TAG = "ReportViolationActivity";

	private static final int PICTURE_DESIRED_SIZE = 680;

	//fullSizePictureDirectory where all files are saved
	private static String mainDirectoryPath;
	private static File fullSizePictureDirectory;
	private static File thumbnailPictureDirectory;

	//Request codes
	private static final int RC_CAMERA_PERMISSION = 201;
	private static final int RC_READ_EXT_STORAGE_PERMS = 202;
	private static final int RC_WRITE_EXT_STORAGE_PERMS = 203;
	private static final int RC_LOCATION_PERMS = 204;
	private static final int RC_IMAGE_TAKEN = 205;
	private static final int RC_IMAGE_DELETION = 206;

	//Permissions
	private static final String READ_EXT_STORAGE_PERMS = Manifest.permission.READ_EXTERNAL_STORAGE;
	private static final String WRITE_EXT_STORAGE_PERMS = Manifest.permission.WRITE_EXTERNAL_STORAGE;
	private static final String LOCATION_PERMS = Manifest.permission.ACCESS_FINE_LOCATION;
	private static final String CAMERA_PERMS = Manifest.permission.CAMERA;

	private ReportViolationManager reportViolationManager;
	Uri currentPicturePath;

	@BindView(R.id.municipality_text_view)
	TextView municipalityTextView;

	@BindView(R.id.plate_text)
	TextView plateTextView;

	@BindView(R.id.report_violation_root)
	View rootView;

	@BindView(R.id.description)
	EditText descriptionText;

	@BindView(R.id.report_violation_number_of_photos_added)
	TextView numberOfPhotosAddedTextView;

	@BindView(R.id.report_violation_spinner)
	Spinner violationTypeSpinner;

	@BindView(R.id.plate_text_view)
	EditText plateEditText;

	@BindView(R.id.uploading_text_view)
	TextView uploadingTextView;

	@BindView(R.id.uploading_progress_bar)
	ProgressBar uploadingProgressBar;

	@BindView(R.id.photo_linear_layout)
	LinearLayout pictureLinearLayout;
	List<ImageView> pictureViewArray = new ArrayList<>();


	//region Static methods
	//================================================================================

	/**
	 * Create intent for launching this activity.
	 *
	 * @param context context from which to launch the activity.
	 * @return intent to launch.
	 */
	@NonNull
	public static Intent createIntent(@NonNull Context context) {
		return new Intent(context, ReportViolationActivity.class);
	}
	//endregion

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_report_violation);

		// Needed for @BindView attributes.
		ButterKnife.bind(this);

		reportViolationManager = new ReportViolationManager(this, rootView);

		violationTypeSpinner.setOnItemSelectedListener(this);
		ArrayAdapter<ViolationEnum> langAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ViolationEnum.values());
		langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		violationTypeSpinner.setAdapter(langAdapter);

		plateEditText.setOnClickListener(v -> {
			boolean plateChanged;
			plateChanged = reportViolationManager.setPlate(((EditText) v).getText().toString());
			if (plateChanged)
				v.clearFocus();
		});

		//Ask permissions
		if (!EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
			EasyPermissions.requestPermissions(this, "Location permission", RC_LOCATION_PERMS, LOCATION_PERMS);
		}
		if (!EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
			EasyPermissions.requestPermissions(this, "Camera permission", RC_CAMERA_PERMISSION, CAMERA_PERMS);
		}
		if (!EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
			EasyPermissions.requestPermissions(this, "Camera permission", RC_READ_EXT_STORAGE_PERMS, READ_EXT_STORAGE_PERMS);
		}
		if (!EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			EasyPermissions.requestPermissions(this, "Camera permission", RC_WRITE_EXT_STORAGE_PERMS, WRITE_EXT_STORAGE_PERMS);
		}

		mainDirectoryPath = Environment.getExternalStorageDirectory() + "/SafeStreets/";
		fullSizePictureDirectory = new File(mainDirectoryPath + "/Pictures/");
		thumbnailPictureDirectory = new File(mainDirectoryPath + "/Thumbnails/");

		if (!fullSizePictureDirectory.exists()) {
			fullSizePictureDirectory.mkdirs();
		}
		if (!thumbnailPictureDirectory.exists()) {
			thumbnailPictureDirectory.mkdirs();
		}
	}


	//region Callback methods
	//================================================================================
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
			case (RC_IMAGE_TAKEN):
				if (resultCode == RESULT_OK) {
					if (new File(URI.create(currentPicturePath.toString())).exists()) {
						createThumbnailAndDisplay();
					} else {
						GeneralUtils.showSnackbar(rootView, "Photo not found.");
						Log.e(TAG, "Photo not found at " + currentPicturePath.toString());
					}
				}
				break;
			case (RC_IMAGE_DELETION):
				if (resultCode == RESULT_OK) {
					if (Boolean.parseBoolean(data.getStringExtra("Want to delete"))) {
						int indexOfThePictureToDelete = Integer.parseInt(data.getStringExtra("View index"));

						URI pathOfThePictureToDelete = URI.create(data.getStringExtra("Picture path"));
						File pictureToDelete = new File(pathOfThePictureToDelete);

						if (pictureToDelete.exists() &&
								indexOfThePictureToDelete < reportViolationManager.numberOfPictures() &&
								Uri.parse(data.getStringExtra("Picture path")).equals(reportViolationManager.getPicture(indexOfThePictureToDelete))) {
							ImageView imageViewToRemove = pictureViewArray.remove(indexOfThePictureToDelete);

							pictureLinearLayout.removeView(imageViewToRemove);
							reportViolationManager.removePicture(indexOfThePictureToDelete);

							String textToDisplay = "Number of photos added:" + reportViolationManager.numberOfPictures() + "/" + reportViolationManager.getMaxNumOfPictures();
							numberOfPhotosAddedTextView.setText(textToDisplay);

							Log.d(TAG, "Num of image views: " + pictureViewArray.size());
						} else
							Log.e(TAG, "File: " + pathOfThePictureToDelete + " not found");
					}
				}
				break;
			default:
		}
	}

	public void onPictureUploaded(int pictureUploaded, int totalNumberOfPicture) {
		uploadingProgressBar.setProgress(pictureUploaded);
		uploadingTextView.setText("Uploaded " + pictureUploaded + " out of" + totalNumberOfPicture);
	}

	@Override
	public void onPictureSaved(Uri thumbnailCreated) {
		ImageView imageView = createImageView(thumbnailCreated);
		pictureViewArray.add(imageView);
		pictureLinearLayout.addView(imageView);
		reportViolationManager.addPhotoToReport(thumbnailCreated);
		String textToDisplay = "Number of photos added:" + reportViolationManager.numberOfPictures() + "/" + reportViolationManager.getMaxNumOfPictures();
		numberOfPhotosAddedTextView.setText(textToDisplay);
	}

	@Override
	public void onPictureLoaded(Bitmap bitmap) {
		SavePictureTask saver = new SavePictureTask(this);
		saver.setQuality(50);
		String thumbnailDirectory = thumbnailPictureDirectory.toURI().toString();
		Uri pathWhereToSave = Uri.parse(thumbnailDirectory + currentPicturePath.getLastPathSegment());
		saver.setPathWhereToSave(pathWhereToSave);
		saver.execute(bitmap);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
	}

	@Override
	public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
		if (requestCode == RC_LOCATION_PERMS) {
			if (!EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
				EasyPermissions.requestPermissions(this, "Location permission", RC_CAMERA_PERMISSION, CAMERA_PERMS);
			}
		}
		if (requestCode == RC_CAMERA_PERMISSION) {
			if (!EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
				EasyPermissions.requestPermissions(this, "Location permission", RC_READ_EXT_STORAGE_PERMS, READ_EXT_STORAGE_PERMS);
			}
		}
		if (requestCode == RC_READ_EXT_STORAGE_PERMS) {
			if (!EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
				EasyPermissions.requestPermissions(this, "Location permission", RC_WRITE_EXT_STORAGE_PERMS, WRITE_EXT_STORAGE_PERMS);
			}
		}
		if (requestCode == RC_WRITE_EXT_STORAGE_PERMS) {
			//Initializes the directories
			if (!fullSizePictureDirectory.exists()) {
				boolean created = fullSizePictureDirectory.mkdirs();
				if (!created)
					Log.e(TAG, "Folders not created");
			}
		}
	}

	@Override
	public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
		if (EasyPermissions.somePermissionPermanentlyDenied(this, Collections.singletonList(READ_EXT_STORAGE_PERMS))) {
			new AppSettingsDialog.Builder(this).build().show();
			finish();
		} else if (EasyPermissions.somePermissionPermanentlyDenied(this, Collections.singletonList(CAMERA_PERMS))) {
			new AppSettingsDialog.Builder(this).build().show();
			finish();
		} else if (EasyPermissions.somePermissionPermanentlyDenied(this, Collections.singletonList(LOCATION_PERMS))) {
			new AppSettingsDialog.Builder(this).build().show();
			finish();
		} else if (EasyPermissions.somePermissionPermanentlyDenied(this, Collections.singletonList(WRITE_EXT_STORAGE_PERMS))) {
			new AppSettingsDialog.Builder(this).build().show();
			finish();
		}
	}
	//endregion

	//region UI methods
	//================================================================================
	@OnClick(R.id.report_violation_add_photo_temporary)
	public void onClickAddPhoto(View v) {
		if (reportViolationManager.canTakeAnotherPicture()) {
			String fileName = String.valueOf(System.currentTimeMillis());
			File destination = new File(fullSizePictureDirectory, fileName + ".jpg");
			currentPicturePath = Uri.parse(destination.toURI().toString());

			StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
			StrictMode.setVmPolicy(builder.build());
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(destination));
			Log.d(TAG, "Calling the camera and saving at: " + destination.toString());
			startActivityForResult(intent, RC_IMAGE_TAKEN);
		} else
			GeneralUtils.showSnackbar(rootView, "Maximum number of pictures reached.");
	}


	@OnClick(R.id.report_violation_floating_send_button)
	public void onClickSendViolation(View v) {
		reportViolationManager.setPlate(plateEditText.getText().toString());
		if (reportViolationManager.isReadyToSend()) {
			findViewById(R.id.scroll_view).setVisibility(View.GONE);
			findViewById(R.id.uploading_panel).setVisibility(View.VISIBLE);
			findViewById(R.id.report_violation_floating_send_button).setVisibility(View.GONE);
			reportViolationManager.sendViolationReport(descriptionText.getText().toString());
		}
		else
			GeneralUtils.showSnackbar(rootView, "Before reporting, please complete all mandatory fields.");
	}

	public void setPlateText(String plate) {
		plateEditText.setTextColor(getResources().getColor(R.color.black));
		plateEditText.setText(plate);
	}

	public void setAddressText(String municipality) {
		municipalityTextView.setText("Municipality: " + municipality);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		String violationType = parent.getItemAtPosition(position).toString();
		reportViolationManager.setViolationType(violationType);
		Log.d(TAG, "Type of violation set to: " + violationType);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
	}
	//endregion

	//region Private methods
	//================================================================================
	private void createThumbnailAndDisplay() {
		LoadPictureTask loader = new LoadPictureTask(this);
		loader.setMaxDimension(PICTURE_DESIRED_SIZE);
		loader.execute(currentPicturePath);
	}

	private ImageView createImageView(Uri uri) {
		ImageView imageView = new ImageView(this);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(300, 300);
		imageView.setLayoutParams(params);

		imageView.setClickable(true);
		imageView.setImageURI(uri);

		imageView.setOnClickListener(v -> {

			int indexOfTheClickedView = pictureViewArray.indexOf(v);
			Uri pathOfThePictureSelected = reportViolationManager.getPicture(indexOfTheClickedView);
			Log.d(TAG, "Opening :" + pathOfThePictureSelected.toString());
			Log.d(TAG, "Index of the view clicked: " + indexOfTheClickedView);

			Intent i = new Intent(ReportViolationActivity.this, PictureActivity.class);
			i.putExtra("Picture to display", pathOfThePictureSelected.toString());
			i.putExtra("Index of the view associated with the picture", String.valueOf(indexOfTheClickedView));
			startActivityForResult(i, RC_IMAGE_DELETION);
		});
		return imageView;
	}
	//endregion
}