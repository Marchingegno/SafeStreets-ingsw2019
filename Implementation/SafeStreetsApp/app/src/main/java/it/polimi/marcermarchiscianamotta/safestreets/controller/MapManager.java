package it.polimi.marcermarchiscianamotta.safestreets.controller;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;

import java.io.IOException;

import it.polimi.marcermarchiscianamotta.safestreets.interfaces.MapUser;

/**
 * Handles map requests.
 *
 * @author Marcer
 */
public class MapManager {

	private static final String TAG = "MapManager";

	/**
	 * Launches a task to find the name of the city accordingly to the specified coordinates.
	 *
	 * @param context  the context of the application.
	 * @param location the specified location.
	 */
	public static void getAddressFromLocation(Context context, MapUser caller, LatLng location) {
		new AddressFromLocationTask(context, caller).execute(location);
	}

	/**
	 * Launches a task to find the current location of the device.
	 *
	 * @param context the context of the application.
	 * @param caller  the MapUser caller.
	 */
	public static void retrieveLocation(Context context, MapUser caller) {
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

	/**
	 * Returns a Task that retrieves the current location.
	 *
	 * @param context the context of the caller.
	 * @return a Task that retrieves the current location.
	 */
	public static Task getLastLocationTask(Context context) {
		FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
		return fusedLocationProviderClient.getLastLocation();
	}

	/**
	 * Task that retrieves an address from a location.
	 */
	private static class AddressFromLocationTask extends AsyncTask<LatLng, Void, Address> {

		private MapUser caller;
		private Context context;
		private Address addressFound;

		//Constructor
		//================================================================================
		AddressFromLocationTask(Context context, MapUser caller) {
			this.caller = caller;
			this.context = context;
		}
		//endregion

		//region Task overridden methods
		//================================================================================
		@Override
		protected Address doInBackground(LatLng... locations) {
			if (locations.length == 1) {
				LatLng location = locations[0];
				Address address = null;
				Log.d(TAG, "Retrieving the address from [" + location.latitude + ", " + location.longitude + "]");
				try {
					address = new Geocoder(context).getFromLocation(location.latitude, location.longitude, 1).get(0);
				} catch (IOException e) {
					Log.e(TAG, TAG + "Failed while retrieving the address from the location ", e);
				}
				Log.d(TAG, "Address found: " + address);
				//Notify the caller
				return address;
			} else
				Log.e(TAG, "Too many arguments");

			return null;
		}

		@Override
		protected void onPostExecute(Address address) {
			super.onPostExecute(address);
			caller.onAddressFound(address);
		}
		//endregion
	}
}
