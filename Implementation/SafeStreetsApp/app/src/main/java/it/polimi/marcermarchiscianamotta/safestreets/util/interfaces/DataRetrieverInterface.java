package it.polimi.marcermarchiscianamotta.safestreets.util.interfaces;

import java.util.List;

import it.polimi.marcermarchiscianamotta.safestreets.model.Cluster;

/**
 * This interface must be implemented by the classes that wish to retrieve from the database any information.
 */
public interface DataRetrieverInterface {

	/**
	 * This method is called when the cluster has been successfully retrieved. The result is passed as parameter.
	 *
	 * @param clusters the retrieved cluster.
	 */
	void onClusterLoaded(List<Cluster> clusters);
}
