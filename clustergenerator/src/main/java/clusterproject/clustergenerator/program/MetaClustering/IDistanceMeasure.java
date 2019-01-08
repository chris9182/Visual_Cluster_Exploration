package clusterproject.clustergenerator.program.MetaClustering;

import clusterproject.clustergenerator.data.ClusteringResult;

public interface IDistanceMeasure {
	public String getName();

	public double distanceBetween(ClusteringResult clustering, ClusteringResult clustering2);
}
