package it.polimi.marcermarchiscianamotta.safestreets.controller;

import android.app.Activity;
import android.location.Address;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.polimi.marcermarchiscianamotta.safestreets.interfaces.DataRetrieverInterface;
import it.polimi.marcermarchiscianamotta.safestreets.interfaces.MapUser;
import it.polimi.marcermarchiscianamotta.safestreets.model.ViolationEnum;
import it.polimi.marcermarchiscianamotta.safestreets.util.cloud.DatabaseConnection;

/**
 * This class manages the violation's retrieving.
 *
 * @author Marcer
 */
public class RetrieveViolationsManager implements MapUser {

	//Log tag
	private final static String TAG = "RetrieveViolationsManag";

	//Other
	private Activity activity;
	private DataRetrieverInterface caller;
	private List<ViolationEnum> violationTypeToRetrieve;
	private Date intervalStartDate;
	private Date intervalEndDate;

	//Constructor
	//================================================================================
	public RetrieveViolationsManager(Activity activity) {
		if (activity instanceof DataRetrieverInterface) {
			this.activity = activity;
			this.caller = (DataRetrieverInterface) activity;
		} else throw new RuntimeException("Activity does not implement DataRetrieverInterface");
	}
	//endregion

	//region Public methods
	//================================================================================

	/**
	 * Retrieves all the clusters belonging to the specified municipality.
	 *
	 * @param municipality municipality to which the clusters belong to.
	 */
	public void loadClusters(String municipality) {
		DatabaseConnection.getClusters(activity, municipality, violationTypeToRetrieve, intervalStartDate, intervalEndDate,
				// On success.
				retrievedClusters -> caller.onClusterLoaded(retrievedClusters),
				// On failure.
				e -> Log.e(TAG, "Failed to retrieve clusters in " + municipality, e)
		);
	}

	/**
	 * Retrieves the municipality from the specified coordinates and saves all the parameters of the query.
	 * @param location coordinates of the municipality.
	 * @param violationTypes list of violation types to be retrieved.
	 * @param intervalStartDate all clusters created after this date are considered.
	 * @param intervalEndDate all clusters before this date are considered.
	 */
	public void loadClusters(LatLng location, List<ViolationEnum> violationTypes, Date intervalStartDate, Date intervalEndDate) {
		this.violationTypeToRetrieve = new ArrayList<>(violationTypes);
		this.intervalStartDate = intervalStartDate;
		this.intervalEndDate = intervalEndDate;
		MapManager.getAddressFromLocation(activity, this, location);
	}
	//endregion


	//region Overridden methods
	//================================================================================

	/**
	 * Not used.
	 * @param location The location found.
	 */
	@Override
	public void onLocationFound(LatLng location) {
		//Not used
	}

	/**
	 * Once the municipality has been found the clusters are loaded.
	 * @param address the address found.
	 */
	@Override
	public void onAddressFound(Address address) {
		if (address != null)
			loadClusters(address.getLocality());
		else
			Toast.makeText(activity, "Unable to retrieve the address", Toast.LENGTH_SHORT);
	}
	//endregion
}
