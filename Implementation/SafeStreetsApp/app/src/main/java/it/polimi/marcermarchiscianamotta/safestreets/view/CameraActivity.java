package it.polimi.marcermarchiscianamotta.safestreets.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import it.polimi.marcermarchiscianamotta.safestreets.R;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class CameraActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

	//LOGGING
	//================================================================================
	private static final String TAG = "AndroidCameraApi";

	//Costants
	//================================================================================
	private static final int DEFAULT_WIGHT = 640;
	private static final int DEFAULT_HEITHR = 480;

	//PERMISSIONS
	//================================================================================
	private static final int RC_CAMERA_PERMISSION = 200;
	private static final String CAMERA_PERMS = Manifest.permission.CAMERA;
	private static final int RC_WRITE_EXT_STORAGE_PERMS = 201;
	private static final String WRITE_EXT_STORAGE_PERMS = Manifest.permission.WRITE_EXTERNAL_STORAGE;
	private static File file;
	protected CameraCaptureSession cameraCaptureSessions;
	protected CaptureRequest.Builder captureRequestBuilder;

//	static {
//		ORIENTATIONS.append(Surface.ROTATION_0, 0);
//		ORIENTATIONS.append(Surface.ROTATION_90, 90);
//		ORIENTATIONS.append(Surface.ROTATION_180, 180);
//		ORIENTATIONS.append(Surface.ROTATION_270, 270);
//	}
	private Button returnButton;
	private TextureView textureView;

	//CAMERA
	//================================================================================
	protected CameraDevice cameraDevice;

	private Size imageDimension;
	protected TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
			//When the view is ready to display the camera preview it starts the camera
			openCamera();
		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
		}

		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
			return false;
		}

		@Override
		public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		}
	};

	private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
	//VIEWS
	//================================================================================
	private Button takePictureButton;
	private Handler mBackgroundHandler;
	//Setting up the functions to handle camera callbacks.
	private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
		@Override
		public void onOpened(CameraDevice camera) {
			cameraDevice = camera;
			createCameraPreview();
		}

		@Override
		public void onDisconnected(CameraDevice camera) {
			Log.d(TAG, "Camera disconnected");
			closeCamera();
		}

		@Override
		public void onError(CameraDevice camera, int error) {
			Log.e(TAG, "Error in Camera");
			closeCamera();
		}
	};
	private HandlerThread mBackgroundThread;

	/**
	 * Create intent for launching this activity.
	 *
	 * @param context context from which to launch the activity.
	 * @return intent to launch.
	 */
	@NonNull
	public static Intent createIntent(@NonNull Context context) {
		return new Intent(context, CameraActivity.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);

		textureView = findViewById(R.id.texture);
		textureView.setSurfaceTextureListener(textureListener);

		takePictureButton = findViewById(R.id.btn_takepicture);
		takePictureButton.setOnClickListener(v -> takePicture());

		returnButton = findViewById(R.id.button_return);
		returnButton.setOnClickListener(v -> {
			Intent returnIntent = new Intent();
			setResult(Activity.RESULT_OK, returnIntent);
			finish();
		});

		File pictureFolder = new File(Environment.getExternalStorageDirectory() + "/SafeStreets/Pictures");
		if (!pictureFolder.exists())
			pictureFolder.mkdirs();

		//Check for Camera permissions
		if (!EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
			EasyPermissions.requestPermissions(this, "Camera permission", RC_CAMERA_PERMISSION, CAMERA_PERMS);
		}

		//Check for writing to external storage permissions
		if (!EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			EasyPermissions.requestPermissions(this, "Camera permission", RC_WRITE_EXT_STORAGE_PERMS, WRITE_EXT_STORAGE_PERMS);
		}
	}

	protected void startBackgroundThread() {
		mBackgroundThread = new HandlerThread("Camera Background");
		mBackgroundThread.start();
		mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
	}

	protected void stopBackgroundThread() {
		mBackgroundThread.quitSafely();
		try {
			mBackgroundThread.join();
			mBackgroundThread = null;
			mBackgroundHandler = null;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected void takePicture() {
		if (null == cameraDevice) {
			Log.e(TAG, "There is no cameraDevice");
			return;
		}

		CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
		try {
			assert manager != null;
			CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
			Size[] jpegSizes;

			jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
			int width = DEFAULT_WIGHT;
			int height = DEFAULT_HEITHR;
			if (0 < jpegSizes.length) {
				width = jpegSizes[0].getWidth();
				height = jpegSizes[0].getHeight();
			}

			ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
			List<Surface> outputSurfaces = new ArrayList(2);
			outputSurfaces.add(reader.getSurface());
			outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
			final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
			captureBuilder.addTarget(reader.getSurface());
			captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
//			// Orientation
//			int rotation = getWindowManager().getDefaultDisplay().getRotation();
//			captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
			ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
				@Override
				public void onImageAvailable(ImageReader reader) {
					Image image = null;
					try {
						image = reader.acquireLatestImage();
						ByteBuffer buffer = image.getPlanes()[0].getBuffer();
						byte[] bytes = new byte[buffer.capacity()];
						buffer.get(bytes);
						save(bytes);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if (image != null) {
							image.close();
						}
					}
				}

				private void save(byte[] bytes) throws IOException {
					OutputStream output = null;
					file = new File(Environment.getExternalStorageDirectory() + "/SafeStreets/Pictures/" + Calendar.getInstance().getTime() + ".jpg");
					try {
						output = new FileOutputStream(file);
						output.write(bytes);
					} finally {
						if (null != output) {
							Log.w(TAG, "File not written");
							output.close();
						}
					}
				}
			};


			//Setting up the functions to handle a picture capture from the camera.
			reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
			final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
				@Override
				public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
					super.onCaptureCompleted(session, request, result);
					Intent resultIntent = new Intent();
					resultIntent.putExtra("result", file.toURI().toString());
					setResult(Activity.RESULT_OK, resultIntent);
					finish();
				}
			};

			cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
				@Override
				public void onConfigured(CameraCaptureSession session) {
					try {
						session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
					} catch (CameraAccessException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onConfigureFailed(CameraCaptureSession session) {
				}
			}, mBackgroundHandler);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	protected void createCameraPreview() {
		try {
			SurfaceTexture texture = textureView.getSurfaceTexture();
			assert texture != null;
			texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
			Surface surface = new Surface(texture);

			captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
			captureRequestBuilder.addTarget(surface);
			cameraDevice.createCaptureSession(
					Arrays.asList(surface),
					new CameraCaptureSession.StateCallback() {
						@Override
						public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
							//The camera is already closed
							if (null == cameraDevice) {
								return;
							}
							// When the session is ready, the preview is displayed.
							cameraCaptureSessions = cameraCaptureSession;
							updatePreview();
						}

						@Override
						public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
							Log.e(TAG, "Configuration failed in StateCallBack");
						}
					},
					null);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	private void openCamera() {
		String cameraId;// Identifier of the camera

		CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

		Log.i(TAG, "Opening the camera");
		try {
			assert manager != null;
			cameraId = manager.getCameraIdList()[0];

			CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
			StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
			assert map != null;
			imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
			manager.openCamera(cameraId, stateCallback, null);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
		Log.i(TAG, "Camera opened");
	}

	protected void updatePreview() {
		captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
		try {
			cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	private void closeCamera() {
		if (null != cameraDevice) {
			cameraDevice.close();
			cameraDevice = null;
		}
	}


	@Override
	protected void onResume() {
		super.onResume();
		startBackgroundThread();
		if (textureView.isAvailable()) {
			openCamera();
		} else {
			textureView.setSurfaceTextureListener(textureListener);
		}
	}

	@Override
	protected void onPause() {
		closeCamera();
		stopBackgroundThread();
		super.onPause();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
	}

	@Override
	public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
		if (RC_CAMERA_PERMISSION == requestCode) {
			if (!EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
				EasyPermissions.requestPermissions(this, "Location permission", RC_WRITE_EXT_STORAGE_PERMS, WRITE_EXT_STORAGE_PERMS);
			}
		}
		if (RC_WRITE_EXT_STORAGE_PERMS == requestCode) {
			if (!EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
				EasyPermissions.requestPermissions(this, "Location permission", RC_CAMERA_PERMISSION, CAMERA_PERMS);
			}
		}
	}

	@Override
	public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
		if (EasyPermissions.somePermissionPermanentlyDenied(this, Collections.singletonList(WRITE_EXT_STORAGE_PERMS))) {
			new AppSettingsDialog.Builder(this).build().show();
			finish();
		}
		if (EasyPermissions.somePermissionPermanentlyDenied(this, Collections.singletonList(CAMERA_PERMS))) {
			new AppSettingsDialog.Builder(this).build().show();
			finish();
		}
	}
}