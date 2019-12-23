package it.polimi.marcermarchiscianamotta.safestreets.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;

import java.io.IOException;

import it.polimi.marcermarchiscianamotta.safestreets.util.interfaces.MapUser;

public class MapManager {


	private static final String TAG = "MapManager";

	/**
	 * Returns the name of the city accordingly to the specified coordinates.
	 *
	 * @param context   the context of the application.
	 * @param location the current location.
	 * @return the name of the city where the location belongs.
	 */
	public static Address getAddressFromLocation(Context context, LatLng location) {
		Address result = null;
		try {
			result = new Geocoder(context).getFromLocation(location.latitude, location.longitude, 1).get(0);
		} catch (IOException e) {
			Log.e(TAG, TAG + "failed while retrieving the municipality ", e);
		}
		Log.d(TAG, result.toString());
		return result;
	}

	static public void retrieveLocation(Context context, MapUser caller) {
		FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
		fusedLocationProviderClient.getLastLocation()
				.addOnSuccessListener(location -> {
					if (location != null) {
						LatLng coordinates = new LatLng(location.getLatitude(), location.getLongitude());
						caller.onLocationFound(coordinates);
					}
				})
				.addOnFailureListener(location -> Log.e(TAG, "Failed to retrieve the location"));
	}

	static public Task getLastLocationTask(Context context) {
		FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
		return fusedLocationProviderClient.getLastLocation();
	}
}
