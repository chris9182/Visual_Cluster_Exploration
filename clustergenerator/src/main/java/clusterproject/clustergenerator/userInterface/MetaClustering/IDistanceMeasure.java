package clusterproject.clustergenerator.userInterface.MetaClustering;

import clusterproject.clustergenerator.data.ClusteringResult;

public interface IDistanceMeasure {
	public double distanceBetween(ClusteringResult clustering, ClusteringResult clustering2);
}
