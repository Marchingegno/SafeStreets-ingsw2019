package it.polimi.marcermarchiscianamotta.safestreets.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.polimi.marcermarchiscianamotta.safestreets.R;
import it.polimi.marcermarchiscianamotta.safestreets.controller.ReportViolationManager;
import it.polimi.marcermarchiscianamotta.safestreets.util.GeneralUtils;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class ReportViolationActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final String TAG = "ReportViolationActivity";
    private static final int RC_CHOOSE_PHOTO = 101;
    private static final int RC_IMAGE_PERMS = 102;
    private static final int RC_LOCATION_PERMS = 124;
    private static final int RC_IMAGE_PATH = 125;
    private static final String READ_EXT_STORAGE_PERMS = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String LOCATION_PERMS = Manifest.permission.ACCESS_FINE_LOCATION;

    private List<String> picturesInUpload;
    private ArrayList<Uri> selectedPhotos = new ArrayList<>();
    private int numberOfUploadedPhotos = 0;
    private boolean failedUplaod = false;
    private final double[] latitude = {0};
    private final double[] longitude = {0};
    FusedLocationProviderClient fusedLocationProviderClient;
    Task<Location> locationTask;

    private ReportViolationManager reportViolationManager;


    @BindView(R.id.report_violation_root) View rootView;

    @BindView(R.id.description) EditText descriptionText;

    @BindView(R.id.report_violation_number_of_photos_added) TextView numberOfPhotosAddedTextView;

    //region Static methods
    //================================================================================
    /**
     * Create intent for launching this activity.
     * @param context context from which to launch the activity.
     * @return intent to launch.
     */
    @NonNull
    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, ReportViolationActivity.class);
    }
    //endregion

    //region Overridden methods
    //================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_report_violation);
        ButterKnife.bind(this); // Needed for @BindView attributes.

        reportViolationManager = new ReportViolationManager(this, rootView);

        //Start the task to get location
        //Ask permission for position
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (!EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            EasyPermissions.requestPermissions(this, "Location permission", RC_LOCATION_PERMS, LOCATION_PERMS);
        } else {
            startLocationTask();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_LOCATION_PERMS) {
        }

        //
        if (requestCode == RC_IMAGE_PATH) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(ReportViolationActivity.this, "Saved: " +  data.getStringExtra("result"), Toast.LENGTH_LONG).show();
                if(selectedPhotos.size() >= 3) {
                    GeneralUtils.showSnackbar(rootView, "Maximum number of photos reached");
                } else {
                    selectedPhotos.add(Uri.parse(data.getStringExtra("result")));
                    numberOfPhotosAddedTextView.setText("Number of photos added: " + selectedPhotos.size() + "/3");
                }
            } else {
                GeneralUtils.showSnackbar(rootView, "No image chosen");
            }
        }

        // If a result of a pick-image action is received then process it.
        if (requestCode == RC_CHOOSE_PHOTO) {
            if (resultCode == RESULT_OK) {
                if(selectedPhotos.size() >= 3) {
                    GeneralUtils.showSnackbar(rootView, "Maximum number of photos reached");
                } else {
                    selectedPhotos.add(data.getData());
                    numberOfPhotosAddedTextView.setText("Number of photos added: " + selectedPhotos.size() + "/3");
                }
            } else {
                GeneralUtils.showSnackbar(rootView, "No image chosen");
            }
        }
        // If a result of a permission-request is received then process it.
        else if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE && EasyPermissions.hasPermissions(this, READ_EXT_STORAGE_PERMS)) {
            pickImageFromStorage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == RC_CHOOSE_PHOTO || requestCode == RC_IMAGE_PERMS) {
            pickImageFromStorage();
        }

        if (requestCode == RC_LOCATION_PERMS) {
            startLocationTask();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, Collections.singletonList(READ_EXT_STORAGE_PERMS))) {
            new AppSettingsDialog.Builder(this).build().show();
            finish();
        } else if (EasyPermissions.somePermissionPermanentlyDenied(this, Collections.singletonList(LOCATION_PERMS))) {
            new AppSettingsDialog.Builder(this).build().show();
            finish();
        }
    }
    //endregion


    //region UI methods
    //================================================================================
    @OnClick(R.id.report_violation_add_photo_temporary)
    public void onClickAddPhoto(View v) {
        startActivityForResult(CameraActivity.createIntent(v.getContext()), RC_IMAGE_PATH);
        //startActivity(CameraActivity.createIntent(v.getContext()));
        // pickImageFromStorage();
    }

    @OnClick(R.id.report_violation_floating_send_button)
    public void onClickSendViolation(View v) {
        //Get description
        String description = descriptionText.getText().toString();

        reportViolationManager.onSendViolationReport(selectedPhotos, description, latitude[0], longitude[0]);
    }
    //endregion


    //region Private methods
    //================================================================================
    private void pickImageFromStorage() {
        if (!EasyPermissions.hasPermissions(this, READ_EXT_STORAGE_PERMS)) {
            EasyPermissions.requestPermissions(this, "Storage permission needed for reading the image from local storage.", RC_IMAGE_PERMS, READ_EXT_STORAGE_PERMS);
            return;
        }

        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RC_CHOOSE_PHOTO);
    }

    private void startLocationTask() {
        locationTask = fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        latitude[0] = location.getLatitude();
                        longitude[0] = location.getLongitude();
                        Toast.makeText(this, "Position successfully obtained", Toast.LENGTH_SHORT).show();
                    } else {
                        latitude[0] = 404;
                        longitude[0] = 404;
                        Toast.makeText(this, "Position failed to obtain.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(location -> Toast.makeText(this, "Failed to obtain position.", Toast.LENGTH_SHORT).show());
    }
    //endregion

}
