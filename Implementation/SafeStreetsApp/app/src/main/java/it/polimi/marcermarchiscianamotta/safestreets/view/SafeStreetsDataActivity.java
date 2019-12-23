package it.polimi.marcermarchiscianamotta.safestreets.view;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.polimi.marcermarchiscianamotta.safestreets.R;
import it.polimi.marcermarchiscianamotta.safestreets.controller.RetrieveViolationsManager;

public class SafeStreetsDataActivity extends AppCompatActivity implements OnMapReadyCallback {

	private static final String TAG = "SafeStreetsDataActivity";

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
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		// Add a marker in Sydney, Australia,
		// and move the map's camera to the same location.
		LatLng sydney = new LatLng(-33.852, 151.211);
		googleMap.addMarker(new MarkerOptions().position(sydney)
				.title("Marker in Sydney"));
		googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
	}
	//endregion

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
