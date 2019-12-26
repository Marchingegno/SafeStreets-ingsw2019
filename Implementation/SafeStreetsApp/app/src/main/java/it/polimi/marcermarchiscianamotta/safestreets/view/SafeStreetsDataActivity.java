package it.polimi.marcermarchiscianamotta.safestreets.view;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.polimi.marcermarchiscianamotta.safestreets.R;
import it.polimi.marcermarchiscianamotta.safestreets.controller.RetrieveViolationsManager;
import it.polimi.marcermarchiscianamotta.safestreets.model.Cluster;
import it.polimi.marcermarchiscianamotta.safestreets.model.ViolationEnum;
import it.polimi.marcermarchiscianamotta.safestreets.util.MapManager;
import it.polimi.marcermarchiscianamotta.safestreets.util.cloud.DatabaseConnection;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class SafeStreetsDataActivity extends AppCompatActivity implements OnMapReadyCallback, EasyPermissions.PermissionCallbacks {

	private static final String TAG = "SafeStreetsDataActivity";

	private static final float DEFAULT_ZOOM = 20.0f;//TODO check zoom
	private static final float DEFAULT_LATITUDE = 45.478130f;//TODO check zoom
	private static final float DEFAULT_LONGITUDE = 9.225788f;//TODO check zoom

	private static final int RC_LOCATION_PERMS = 301;

	private static final String LOCATION_PERMS = Manifest.permission.ACCESS_FINE_LOCATION;

	private GoogleMap mMap = null;

	private Location lastKnownLocation;
	private LatLng defaultLocation = new LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);

	DatePickerDialog picker;
	long startDate = 0;
	long endDate = 0;

	@BindView(R.id.start_date_view)
	TextView startDateTextView;
	@BindView(R.id.end_date_view)
	TextView endDateTextView;
	private RetrieveViolationsManager retrieveViolationsManager = new RetrieveViolationsManager(this);

	//region Static methods
	//================================================================================
	@NonNull
	public static Intent createIntent(@NonNull Context context) {
		return new Intent(context, SafeStreetsDataActivity.class);
	}
	//endregion


	//region Overridden methods
	//================================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Retrieve the content view that renders the map.
		setContentView(R.layout.activity_safestreets_data);
		ButterKnife.bind(this); // Needed for @BindView attributes.

		setupDatePickerDialogs();

		// Get the SupportMapFragment and request notification
		// when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		if (!EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
			EasyPermissions.requestPermissions(this, "Location permission", RC_LOCATION_PERMS, LOCATION_PERMS);
		}

		// Add back button to action bar.
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;

		// Do other setup activities here too, as described elsewhere in this tutorial.

		// Turn on the My Location layer and the related control on the map.
		updateLocationUI();

		// Get the current location of the device and set the position of the map.
		getDeviceLocation();
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
	}

	@Override
	public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
	}

	@Override
	public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
		if (EasyPermissions.somePermissionPermanentlyDenied(this, Collections.singletonList(LOCATION_PERMS))) {
			new AppSettingsDialog.Builder(this).build().show();
			finish();
		}
	}
	//endregion
	private void loadClusters() {
		String municipality = MapManager.getAddressFromLocation(this, new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude())).getLocality();
		DatabaseConnection.getClusters(this, municipality,
				// On success.
				reportsResult -> {
					for (Cluster rep : reportsResult) {
						mMap.addMarker(new MarkerOptions().position(new LatLng(rep.getLatitude(), rep.getLongitude()))
								.title(ViolationEnum.valueOf(rep.getTypeOfViolation()).toString()));
					}
				},
				// On failure.
				e -> {
					Log.e(TAG, "Failed to retrieve reports", e);
				});
	}

	private void setupDatePickerDialogs() {
		startDateTextView.setOnClickListener(v -> {
			//On click
			final Calendar calendar = Calendar.getInstance();
			int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
			int currentMonth = calendar.get(Calendar.MONTH);
			int currentYear = calendar.get(Calendar.YEAR);
			// date picker dialog
			picker = new DatePickerDialog(SafeStreetsDataActivity.this,
					//On date set
					(view, selectedYear, selectedMonth, selectedDay) -> {
						String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
						startDateTextView.setText(date);
						startDate = convertDateToLong(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear);
						Log.d(TAG, "Start date: " + date + " [" + startDate + "]");
					}, currentYear, currentMonth, currentDay);
			if (endDate != 0)
				picker.getDatePicker().setMaxDate(endDate);
			else
				picker.getDatePicker().setMaxDate(calendar.getTimeInMillis());
			picker.show();
		});

		endDateTextView.setOnClickListener(v -> {
			//On click
			final Calendar calendar = Calendar.getInstance();
			int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
			int currentMonth = calendar.get(Calendar.MONTH);
			int currentYear = calendar.get(Calendar.YEAR);
			// date picker dialog
			picker = new DatePickerDialog(SafeStreetsDataActivity.this,
					//On date set
					(view, selectedYear, selectedMonth, selectedDay) -> {
						String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
						endDateTextView.setText(date);
						endDate = convertDateToLong(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear);
						Log.d(TAG, "End date: " + date + " [" + endDate + "]");
					}, currentYear, currentMonth, currentDay);
			picker.getDatePicker().setMinDate(startDate);
			picker.getDatePicker().setMaxDate(calendar.getTimeInMillis());
			picker.show();
		});
	}

	private void getDeviceLocation() {
		/*
		 * Get the best and most recent location of the device, which may be null in rare
		 * cases when a location is not available.
		 */
		try {
			if (EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
				Task locationResult = MapManager.getLastLocationTask(this);
				locationResult.addOnCompleteListener(this, new OnCompleteListener() {
					@Override
					public void onComplete(@NonNull Task task) {
						if (task.isSuccessful()) {
							// Set the map's camera position to the current location of the device.
							lastKnownLocation = (Location) task.getResult();
							mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
									new LatLng(lastKnownLocation.getLatitude(),
											lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
							loadClusters();
						} else {
							Log.d(TAG, "Current location is null. Using defaults.");
							Log.e(TAG, "Exception: %s", task.getException());
							mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
							mMap.getUiSettings().setMyLocationButtonEnabled(false);
						}
					}
				});
			}
		} catch (SecurityException e) {
			Log.e("Exception: %s", e.getMessage());
		}
	}

	private void updateLocationUI() {
		if (mMap == null) {
			return;
		}
		try {
			if (EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
				mMap.setMyLocationEnabled(true);
				mMap.getUiSettings().setMyLocationButtonEnabled(true);
			} else {
				mMap.setMyLocationEnabled(false);
				mMap.getUiSettings().setMyLocationButtonEnabled(false);
				lastKnownLocation = null;
				EasyPermissions.requestPermissions(this, "Location permission", RC_LOCATION_PERMS, LOCATION_PERMS);
			}
		} catch (SecurityException e) {
			Log.e("Exception: %s", e.getMessage());
		}
	}

	private long convertDateToLong(String dateString) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			Date date = sdf.parse(dateString);

			return date.getTime();

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
