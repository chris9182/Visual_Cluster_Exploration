package clusterproject.program.MetaClustering;

import clusterproject.data.ClusteringResult;

public class ClusteringWithDistance {
	public int inIndex;
	public double distance = Double.MAX_VALUE;
	public boolean flag;
	public int tag;
	private final ClusteringResult clustering;
	// public static IDistanceMeasure measure;
	//
	// static {
	// measure = new ClusteringError();
	// }

	public ClusteringWithDistance(ClusteringResult clustering, int inIndex) {
		this.clustering = clustering;
		this.inIndex = inIndex;
	}

	public ClusteringResult getClustering() {
		return clustering;
	}

	// public float distanceto(ClusteringWithDistance other) {
	// return distanceTo(other);
	// }
	//
	// public float distanceTo(ClusteringWithDistance other) {
	// return measure.distanceBetween(this.clustering, other.clustering);
	// }
}
