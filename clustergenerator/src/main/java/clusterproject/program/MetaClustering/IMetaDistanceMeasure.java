package clusterproject.program.MetaClustering;

import clusterproject.data.ClusteringResult;

public interface IMetaDistanceMeasure {
	public String getName();

	public double distanceBetween(ClusteringResult clustering, ClusteringResult clustering2);
}
