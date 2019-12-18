package it.polimi.marcermarchiscianamotta.safestreets.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;

public class MapManager {


	private static final String TAG = "MapManager";

	/**
	 * Returns the name of the city accordingly to the specified coordinates.
	 *
	 * @param context   the context of the application.
	 * @param latitude  the latitude of the location.
	 * @param longitude the longitude of the location.
	 * @return the name of the city where the location belongs.
	 */
	public static String getMunicipalityFromLocation(Context context, double latitude, double longitude) {
		Address result = null;
		try {
			result = new Geocoder(context).getFromLocation(latitude, longitude, 1).get(0);
		} catch (IOException e) {
			Log.e(TAG, TAG + "failed while retrieving the municipality ", e);
		}
		return result.getLocality();
	}

	static public void retrieveLocation(Context context, MapUser caller) {
		FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
		fusedLocationProviderClient.getLastLocation()
				.addOnSuccessListener(location -> {
					if (location != null) {
						caller.onLocationFound(location.getLatitude(), location.getLongitude());
					}
				})
				.addOnFailureListener(location -> Log.e(TAG, "Failed to retrieve the location"));
	}
}
