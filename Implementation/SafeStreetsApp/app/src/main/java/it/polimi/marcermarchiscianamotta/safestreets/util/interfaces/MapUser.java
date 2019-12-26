package it.polimi.marcermarchiscianamotta.safestreets.util.interfaces;

import com.google.android.gms.maps.model.LatLng;

/**
 * This interface must be implemented by the classes that wish to retrieve the results of the MapManager.
 */
public interface MapUser {

	/**
	 * This method is called once the process of retrieving the current location has terminated.
	 * @param location The location found.
	 */
	void onLocationFound(LatLng location);
}
