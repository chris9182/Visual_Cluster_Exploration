package clusterproject.program.Clustering;

import java.util.List;

import clusterproject.data.ClusteringResult;

public interface ISimpleClusterer extends IClusterer {
	List<ClusteringResult> cluster(double[][] data, List<String> headers);
}
