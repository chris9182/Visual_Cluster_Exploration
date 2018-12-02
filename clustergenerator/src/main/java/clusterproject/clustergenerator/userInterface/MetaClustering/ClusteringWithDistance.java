package clusterproject.clustergenerator.userInterface.MetaClustering;

import clusterproject.clustergenerator.data.ClusteringResult;

public class ClusteringWithDistance {
	public long inIndex;
	public float distance = Float.MAX_VALUE;
	public boolean flag;
	private final ClusteringResult clustering;
	public static IDistanceMeasure measure;

	static {
		measure = new ClusteringError();
	}

	public ClusteringWithDistance(ClusteringResult clustering, int inIndex) {
		this.clustering = clustering;
		this.inIndex = inIndex;
	}

	public float distanceto(ClusteringWithDistance other) {
		return distanceTo(other);
	}

	public float distanceTo(ClusteringWithDistance other) {
		return measure.distanceBetween(this.clustering, other.clustering);
	}
}
