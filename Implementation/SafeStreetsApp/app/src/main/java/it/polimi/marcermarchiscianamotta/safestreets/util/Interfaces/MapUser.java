package it.polimi.marcermarchiscianamotta.safestreets.util.interfaces;

/**
 * This interface must be implemented by the classes that wish to retrieve the results of the MapManager.
 */
public interface MapUser {

	/**
	 * This method is called once the process of retrieving the current location has terminated.
	 *
	 * @param latitude  the current latitude of the device.
	 * @param longitude the current longitude of the device.
	 */
	void onLocationFound(double latitude, double longitude);
}
