package it.polimi.marcermarchiscianamotta.safestreets.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.polimi.marcermarchiscianamotta.safestreets.R;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class CameraActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

	private static final int RC_CAMERA_PERMS = 103;

	private static final String WRITE_EXT_STORAGE_PERMS = Manifest.permission.WRITE_EXTERNAL_STORAGE;
	private static final String CAMERA_PERMS = Manifest.permission.CAMERA;

	private ExecutorService executor = Executors.newSingleThreadExecutor();
	private TextureView viewFinder;

	//region Static methods
	//================================================================================
	/**
	 * Create intent for launching this activity.
	 * @param context context from which to launch the activity.
	 * @return intent to launch.
	 */
	@NonNull
	public static Intent createIntent(@NonNull Context context) {
		return new Intent(context, CameraActivity.class);
	}
	//endregion


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);

		viewFinder = findViewById(R.id.view_finder);

		if (!EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
			EasyPermissions.requestPermissions(this, "Location permission", RC_CAMERA_PERMS, CAMERA_PERMS);
		} else {
			viewFinder.post(this::startCamera);
		}

		viewFinder.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> updateTransform());
	}


	private void startCamera() {
		PreviewConfig previewConfig = new PreviewConfig.Builder().setTargetResolution(new Size(640, 480)).build();

		Preview preview =  new Preview(previewConfig);

		preview.setOnPreviewOutputUpdateListener(previewOutput -> {
			ViewGroup parent = (ViewGroup) viewFinder.getParent();
			parent.removeView(viewFinder);
			parent.addView(viewFinder, 0);
			viewFinder.setSurfaceTexture(previewOutput.getSurfaceTexture());
			updateTransform();
		});

		CameraX.bindToLifecycle(this, preview);
	}

	private void updateTransform() {
		Matrix matrix = new Matrix();

		// Compute the center of the view finder
		float centerX = viewFinder.getWidth() / 2f;
		float centerY = viewFinder.getHeight() / 2f;

		// Correct preview output to account for display rotation
		int rotationDegrees;
		switch(viewFinder.getDisplay().getRotation()){
			case Surface.ROTATION_0:
				rotationDegrees = 0;
				break;
			case Surface.ROTATION_90:
					rotationDegrees = 90;
				break;
			case Surface.ROTATION_180:
					rotationDegrees = 180;
				break;
			case Surface.ROTATION_270:
					rotationDegrees = 270;
				break;
			default: return;
		}

		matrix.postRotate((float) -rotationDegrees, centerX, centerY);

		// Finally, apply transformations to our TextureView
		viewFinder.setTransform(matrix);
	}

	@Override
	public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
		if (requestCode == RC_CAMERA_PERMS) {
			viewFinder.post(this::startCamera);
		}
	}

	@Override
	public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
		if (EasyPermissions.somePermissionPermanentlyDenied(this, Collections.singletonList(CAMERA_PERMS))) {
			new AppSettingsDialog.Builder(this).build().show();
			finish();
		}
	}
}
