package clusterproject.program.MetaClustering;

public class OpticsContainer<T> {
	public int inIndex;
	public double distance = Double.MAX_VALUE;
	public boolean flag;
	public int tag;
	private final T clustering;
	// public static IDistanceMeasure measure;
	//
	// static {
	// measure = new ClusteringError();
	// }

	public OpticsContainer(T clustering, int inIndex) {
		this.clustering = clustering;
		this.inIndex = inIndex;
	}

	public T getObject() {
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
