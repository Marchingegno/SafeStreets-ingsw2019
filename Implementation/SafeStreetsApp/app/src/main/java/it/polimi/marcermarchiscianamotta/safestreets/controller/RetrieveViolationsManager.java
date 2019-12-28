package it.polimi.marcermarchiscianamotta.safestreets.controller;

import android.app.Activity;
import android.location.Address;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.polimi.marcermarchiscianamotta.safestreets.model.ViolationEnum;
import it.polimi.marcermarchiscianamotta.safestreets.util.MapManager;
import it.polimi.marcermarchiscianamotta.safestreets.util.cloud.DatabaseConnection;
import it.polimi.marcermarchiscianamotta.safestreets.util.interfaces.MapUser;
import it.polimi.marcermarchiscianamotta.safestreets.util.interfaces.ViolationRetrieverUser;

public class RetrieveViolationsManager implements MapUser {

	//Log tag
	private final static String TAG = "RetrieveViolationsManag";

	private ViolationRetrieverUser caller;
	private Activity activity;
	private List<ViolationEnum> violationTypeToRetrieve;
	private Date intervalStartDate;
	private Date intervalEndDate;

	public RetrieveViolationsManager(Activity activity) {
		if (activity instanceof ViolationRetrieverUser) {
			this.activity = activity;
			this.caller = (ViolationRetrieverUser) activity;
		} else throw new RuntimeException("Activity does not implement ViolationRetrieverUser");
	}

	public void loadClusters(String municipality) {
		DatabaseConnection.getClusters(activity, municipality, violationTypeToRetrieve, intervalStartDate, intervalEndDate,
				// On success.
				retrievedClusters -> caller.onClusterLoaded(retrievedClusters),
				// On failure.
				e -> Log.e(TAG, "Failed to retrieve clusters in " + municipality, e)
		);
	}

	public void loadClusters(LatLng location, List<ViolationEnum> violationTypes, Date intervalStartDate, Date intervalEndDate) {
		this.violationTypeToRetrieve = new ArrayList<>(violationTypes);
		this.intervalStartDate = intervalStartDate;
		this.intervalEndDate = intervalEndDate;
		MapManager.getAddressFromLocation(activity, this, location);
	}

	@Override
	public void onLocationFound(LatLng location) {

	}

	@Override
	public void onAddressFound(Address address) {
		loadClusters(address.getLocality());
	}
}
