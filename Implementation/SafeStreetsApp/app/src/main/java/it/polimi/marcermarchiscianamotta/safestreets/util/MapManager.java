package it.polimi.marcermarchiscianamotta.safestreets.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.io.IOException;

public class MapManager {
	private static final String TAG = "MapManager";

	/**
	 * Returns the name of the city where the location belongs.
	 * @param context the context of the application.
	 * @param latitude the latitude of the location.
	 * @param longitude the longitude of the location.
	 * @return the name of the city where the location belongs.
	 */
	public String getCityFromLocation(Context context, double latitude, double longitude){
		Address result = null;
		try{
			result = new Geocoder(context).getFromLocation(latitude, longitude, 1).get(0);
		}
		catch(IOException e)
		{
			Log.e(TAG, "Sign-in error: ", e);}
		return result.getLocality();
	}


}
