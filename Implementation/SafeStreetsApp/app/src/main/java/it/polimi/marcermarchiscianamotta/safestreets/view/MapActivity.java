package it.polimi.marcermarchiscianamotta.safestreets.view;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.polimi.marcermarchiscianamotta.safestreets.R;
import it.polimi.marcermarchiscianamotta.safestreets.controller.RetrieveViolationsManager;
import it.polimi.marcermarchiscianamotta.safestreets.model.Cluster;
import it.polimi.marcermarchiscianamotta.safestreets.model.ViolationEnum;
import it.polimi.marcermarchiscianamotta.safestreets.util.GeneralUtils;
import it.polimi.marcermarchiscianamotta.safestreets.util.MapManager;
import it.polimi.marcermarchiscianamotta.safestreets.util.interfaces.DataRetrieverInterface;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, EasyPermissions.PermissionCallbacks, DataRetrieverInterface {

	//Log tag
	private static final String TAG = "MapActivity";

	//Constants
	private static final float DEFAULT_ZOOM = 16.0f;//Zoom of the map's camera
	private static final float DEFAULT_LATITUDE = 45.478130f;
	private static final float DEFAULT_LONGITUDE = 9.225788f;

	//Request codes
	private static final int RC_LOCATION_PERMS = 301;

	//Permissions
	private static final String LOCATION_PERMS = Manifest.permission.ACCESS_FINE_LOCATION;

	//Map
	@Nullable
	private GoogleMap mMap = null;
	private LatLng defaultLocation = new LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
	private List<Marker> markers;
	@BindView(R.id.violation_type_selection_button)
	Button violationTypeSelectionButton;
	@Nullable
	private Location lastKnownLocation;

	//UI
	DatePickerDialog picker;
	@BindView(R.id.start_date_view)
	TextView startDateTextView;
	@BindView(R.id.end_date_view)
	TextView endDateTextView;
	@Nullable
	private LatLng lastSearchedCoordinates;
	AlertDialog violationTypeDialog;

	//Query
	private long startDate = 0;
	private long endDate;
	private List<ViolationEnum> violationTypesSelected = Arrays.asList(ViolationEnum.values());//all types of violation are selected at the beginning
	private RetrieveViolationsManager retrieveViolationsManager;

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
		return new Intent(context, MapActivity.class);
	}
	//endregion


	//region Overridden methods
	//================================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_safestreets_data);

		ButterKnife.bind(this); // Needed for @BindView attributes.

		retrieveViolationsManager = new RetrieveViolationsManager(this);
		setupDatePickerDialogs();
		setupTypeOfViolationDialog();
		setupMapFragment();

		if (!EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
			EasyPermissions.requestPermissions(this, "Location permission", RC_LOCATION_PERMS, LOCATION_PERMS);
		}

		// Add back button to action bar.
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
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
		markers = new ArrayList<>();

		assert mMap != null;
		//When a marker is clicked the corresponding cluster is loaded and the ClusterActivity is launched
		mMap.setOnMarkerClickListener(marker -> {
			//onMarkerClick
			Intent intent = ClusterActivity.createIntent(this);
			intent.putExtra("cluster", (Serializable) marker.getTag());
			startActivity(intent);
			return false;
		});

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

	/**
	 * For each loaded cluster a marker is created on the map.
	 *
	 * @param clusters the retrieved cluster.
	 */
	@Override
	public void onClusterLoaded(List<Cluster> clusters) {
		if (mMap != null) {
			removeMarkers();
			for (Cluster cluster : clusters) {
				addMarker(cluster);
			}
		}
	}
	//endregion

	//region UI methods
	//================================================================================
	@OnClick(R.id.violation_type_selection_button)
	public void onClickChooseViolationTypes(View v) {
		violationTypeDialog.show();
	}
	//endregion

	//region Private methods
	//================================================================================
	private void setupDatePickerDialogs() {
		final Calendar calendar = Calendar.getInstance();
		int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
		int currentMonth = calendar.get(Calendar.MONTH);
		int currentYear = calendar.get(Calendar.YEAR);

		endDate = System.currentTimeMillis();

		startDateTextView.setOnClickListener(v -> {
			//On click
			picker = new DatePickerDialog(MapActivity.this,
					//On date chosen
					(view, selectedYear, selectedMonth, selectedDay) -> {
						String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
						startDateTextView.setText(date);
						//Save chosen starting date
						startDate = GeneralUtils.convertDateToLong(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear + " 00:00:00");
						loadClusters(lastSearchedCoordinates);
						Log.d(TAG, "Start date: " + date + " [" + startDate + "]");
					}, currentYear, currentMonth, currentDay);
			//Update the interval of the dialog
			picker.getDatePicker().setMaxDate(endDate);
			picker.show();
		});

		endDateTextView.setOnClickListener(v -> {
			//On click
			picker = new DatePickerDialog(MapActivity.this,
					//On date chosen
					(view, selectedYear, selectedMonth, selectedDay) -> {
						String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
						endDateTextView.setText(date);
						//Save chosen ending date
						endDate = GeneralUtils.convertDateToLong(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear + " 23:59:59");
						loadClusters(lastSearchedCoordinates);
						Log.d(TAG, "End date: " + date + " [" + endDate + "]");
					}, currentYear, currentMonth, currentDay);
			//Update the interval of the dialog
			picker.getDatePicker().setMinDate(startDate);
			picker.getDatePicker().setMaxDate(System.currentTimeMillis());
			picker.show();
		});
	}

	private void setupTypeOfViolationDialog() {
		List<ViolationEnum> selectedItems = new ArrayList<>();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		int namOfViolations = ViolationEnum.values().length;
		CharSequence[] violationTypes = new CharSequence[namOfViolations];

		for (int i = 0; i < namOfViolations; i++)
			violationTypes[i] = ViolationEnum.values()[i].toString();

		builder.setTitle("Choose the type of violation")
				.setMultiChoiceItems(violationTypes, null,
						(dialog, which, isChecked) -> {
							//On click
							if (isChecked) {// If the user checked the item, add it to the selected items
								selectedItems.add(ViolationEnum.values()[which]);
								Log.d(TAG, "Added to the list of violation types: " + ViolationEnum.values()[which]);
							} else if (selectedItems.contains(ViolationEnum.values()[which])) {// Else, if the item is already in the array, remove it
								selectedItems.remove(ViolationEnum.values()[which]);
								Log.d(TAG, "Removed from the list of violation types: " + ViolationEnum.values()[which]);
							}
						})
				.setPositiveButton("OK",
						(dialog, id) -> {
							//On click
							violationTypesSelected = selectedItems;
							Log.d(TAG, "Violation types selected: " + violationTypesSelected);
							if (lastSearchedCoordinates != null) {
								LatLng coordinates = new LatLng(lastSearchedCoordinates.latitude, lastSearchedCoordinates.longitude);
								loadClusters(coordinates);
							} else {
								Log.e(TAG, "lastSearchedCoordinates is null in setPositiveButton");
							}
						})
				.setNegativeButton("Cancel",
						(dialog, id) -> {
							//On click
							Log.d(TAG, "Violation types selected: " + violationTypesSelected);
						});

		violationTypeDialog = builder.create();
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

		// Specify the types of Place data to return.
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
						lastSearchedCoordinates = new LatLng(requestedLocation.latitude, requestedLocation.longitude);
						mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastSearchedCoordinates, DEFAULT_ZOOM));
						loadClusters(lastSearchedCoordinates);
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
								lastSearchedCoordinates = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
								// Set the map's camera position to the current location of the device.
								mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastSearchedCoordinates, DEFAULT_ZOOM));
								loadClusters(lastSearchedCoordinates);
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

	private void addMarker(Cluster cluster) {
		assert mMap != null; //Checked int the caller
		MarkerOptions markerOption = new MarkerOptions()
				.position(new LatLng(cluster.getLatitude(), cluster.getLongitude()))
				.icon(getMarkerIcon(cluster.getTypeOfViolation().getColor()));
		Marker marker = mMap.addMarker(markerOption);
		marker.setTag(cluster);
		markers.add(marker);
	}

	//Removes the markers from the map
	private void removeMarkers() {
		for (int i = markers.size() - 1; i >= 0; i--) {
			markers.get(i).remove();
			markers.remove(i);
		}
	}

	private BitmapDescriptor getMarkerIcon(String color) {
		float[] hsv = new float[3];
		Color.colorToHSV(Color.parseColor(color), hsv);
		return BitmapDescriptorFactory.defaultMarker(hsv[0]);
	}

	private void loadClusters(LatLng coordinates) {
		retrieveViolationsManager.loadClusters(coordinates, violationTypesSelected, GeneralUtils.convertLongToDate(startDate), GeneralUtils.convertLongToDate(endDate));
	}
	//endregion
}
