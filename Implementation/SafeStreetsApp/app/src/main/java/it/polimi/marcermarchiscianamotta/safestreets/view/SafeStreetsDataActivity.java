package it.polimi.marcermarchiscianamotta.safestreets.view;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

	//Constants
	private static final float DEFAULT_ZOOM = 18.0f;//Zoom of the map's camera
	private static final float DEFAULT_LATITUDE = 45.478130f;
	private static final float DEFAULT_LONGITUDE = 9.225788f;

	//Request codes
	private static final int RC_LOCATION_PERMS = 301;

	//Permissions
	private static final String LOCATION_PERMS = Manifest.permission.ACCESS_FINE_LOCATION;
	//UI
	DatePickerDialog picker;
	//Map
	@Nullable
	private GoogleMap mMap = null;
	private LatLng defaultLocation = new LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
	//Locations
	private Location lastKnownLocation;
	@BindView(R.id.start_date_view)
	TextView startDateTextView;
	@BindView(R.id.end_date_view)
	TextView endDateTextView;
	//Others
	private long startDate = 0;
	private long endDate = 0;
	private RetrieveViolationsManager retrieveViolationsManager = new RetrieveViolationsManager(this);

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
		return new Intent(context, SafeStreetsDataActivity.class);
	}
	//endregion


	//region Overridden methods
	//================================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_safestreets_data);

		ButterKnife.bind(this); // Needed for @BindView attributes.

		setupDatePickerDialogs();
		setupMapFragment();

		if (!EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
			EasyPermissions.requestPermissions(this, "Location permission", RC_LOCATION_PERMS, LOCATION_PERMS);
		}
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;

		//Sets the style of the map
		try {
			boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));
			if (!success) {
				Log.e(TAG, "Style parsing failed.");
			}
		} catch (Resources.NotFoundException e) {
			Log.e(TAG, "Can't find style. Error: ", e);
		}

		turnOnMyLocationLayer();
		getDeviceLocationAndDisplayIt();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
	}

	@Override
	public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
		turnOnMyLocationLayer();
		getDeviceLocationAndDisplayIt();
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

	//region Private methods
	//================================================================================
	private void setupDatePickerDialogs() {
		final Calendar calendar = Calendar.getInstance();
		int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
		int currentMonth = calendar.get(Calendar.MONTH);
		int currentYear = calendar.get(Calendar.YEAR);

		startDateTextView.setOnClickListener(v -> {
			//On click
			picker = new DatePickerDialog(SafeStreetsDataActivity.this,
					//On date chosen
					(view, selectedYear, selectedMonth, selectedDay) -> {
						String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
						startDateTextView.setText(date);
						//Save chosen starting date
						startDate = convertDateToLong(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear);
						Log.d(TAG, "Start date: " + date + " [" + startDate + "]");
					}, currentYear, currentMonth, currentDay);
			//Update the interval of the dialog
			if (endDate != 0)
				picker.getDatePicker().setMaxDate(endDate);
			else
				picker.getDatePicker().setMaxDate(calendar.getTimeInMillis());
			picker.show();
		});

		endDateTextView.setOnClickListener(v -> {
			//On click
			picker = new DatePickerDialog(SafeStreetsDataActivity.this,
					//On date chosen
					(view, selectedYear, selectedMonth, selectedDay) -> {
						String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
						endDateTextView.setText(date);
						//Save chosen ending date
						endDate = convertDateToLong(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear);
						Log.d(TAG, "End date: " + date + " [" + endDate + "]");
					}, currentYear, currentMonth, currentDay);
			//Update the interval of the dialog
			picker.getDatePicker().setMinDate(startDate);
			picker.getDatePicker().setMaxDate(calendar.getTimeInMillis());
			picker.show();
		});
	}

	private void setupMapFragment() {
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		if (mapFragment != null)
			mapFragment.getMapAsync(this);
		else {
			Log.e(TAG, "mapFragment is null");
			finish();
		}

		Places.initialize(getApplicationContext(), getString(R.string.google_api_key));// Initialize the SDK
		Places.createClient(this); //Creates an instance of PlacesClient
		AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
				getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);// Initialize the AutocompleteSupportFragment

		// Specify the types of place data to return.
		if (autocompleteFragment != null) {
			autocompleteFragment.setPlaceFields(Arrays.asList(
					Place.Field.ID,
					Place.Field.NAME,
					Place.Field.LAT_LNG,
					Place.Field.ADDRESS));
			// Set up a PlaceSelectionListener to handle the response.
			autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
				@Override
				public void onPlaceSelected(@NonNull Place place) {
					LatLng requestedLocation = place.getLatLng();
					Log.d(TAG, "Place: " + place.getName() + "\nID: " + place.getId() + " \nLatLng: " + place.getLatLng() + "\nAddress: " + place.getAddress());
					if (requestedLocation != null && mMap != null) {
						mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(requestedLocation.latitude, requestedLocation.longitude), DEFAULT_ZOOM));
					} else
						Log.e(TAG, "No LatLng associated with the searched place. mMap = " + mMap);
				}

				@Override
				public void onError(@NonNull Status status) {
					Log.e(TAG, "An error occurred in OnPlaceSelectedListener: " + status);
				}
			});
		} else {
			Log.e(TAG, "autocompleteFragment is null in setupMapFragment");
			finish();
		}
	}

	// Turn on the MyLocationLayer and the related control on the map.
	private void turnOnMyLocationLayer() {
		if (mMap == null) {
			Log.e(TAG, "mMap is null in turnOnMyLocationLayer");
			return;
		}
		try {
			mMap.getUiSettings().setMapToolbarEnabled(false);
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
			Log.e(TAG, "Security exception: " + e.getMessage());
		}
	}

	// Get the current location of the device and sets the map's camera position.
	private void getDeviceLocationAndDisplayIt() {
		try {
			if (EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
				MapManager.getLastLocationTask(this)
						.addOnSuccessListener(location -> {
							if (location != null && mMap != null) {
								lastKnownLocation = (Location) location;
								// Set the map's camera position to the current location of the device.
								mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
								loadClusters();//TODO modify
							}
						})
						.addOnFailureListener(location -> {
							Log.d(TAG, "Current location is null. Using defaults.");
							if (mMap != null) {
								mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
								mMap.getUiSettings().setMyLocationButtonEnabled(false);
							} else {
								Log.e(TAG, "mMap is null in getDeviceLocationAndDisplayIt");
								finish();
							}
						});
			}
		} catch (SecurityException e) {
			Log.e(TAG, "Exception: " + e.getMessage());
		}
	}

	private long convertDateToLong(String dateString) {
		Date date = null;
		try {
			date = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALY).parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date == null ? 0 : date.getTime();
	}
	//endregion
}
