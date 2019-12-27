package it.polimi.marcermarchiscianamotta.safestreets.util.interfaces;

import java.util.List;

import it.polimi.marcermarchiscianamotta.safestreets.model.Cluster;

public interface ViolationRetrieverUser {

	void onClusterLoaded(List<Cluster> clusters);
}
