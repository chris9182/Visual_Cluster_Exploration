package clusterproject.clustergenerator.userInterface.MetaClustering;

import clusterproject.clustergenerator.Util;
import clusterproject.clustergenerator.data.ClusteringResult;

public class ClusteringWithDistance {
	public long inIndex;
	public float distance = Float.MAX_VALUE;
	public boolean flag;
	private final ClusteringResult clustering;
	public static IDistanceMeasure measure;

	static {

		measure = (clustering, clustering2) -> {

			final int matrixSize = clustering.getData().length > clustering2.getData().length
					? clustering.getData().length
					: clustering2.getData().length;
			final int[][] confusion = new int[matrixSize][matrixSize];
			for (int i = 0; i < clustering.getData().length; ++i)
				for (int j = 0; j < clustering2.getData().length; ++j) {
					try {
						confusion[i][j] = -Util.intersection(clustering.getData()[i], clustering2.getData()[j]).length;
						// System.err.println(confusion[i][j]);
					} catch (final ArrayIndexOutOfBoundsException e) {
						confusion[i][j] = 0;
					}

				}
			final HungarianAlgorithm hungarian = new HungarianAlgorithm(confusion);
			final int[][] assignment = hungarian.findOptimalAssignment();
			int dMaxSum = 0;
			for (int i = 0; i < assignment.length; ++i) {
				try {
					dMaxSum += Util.intersection(clustering.getData()[assignment[i][1]],
							clustering2.getData()[assignment[i][0]]).length;// XXX indexes?
				} catch (final ArrayIndexOutOfBoundsException e) {

				}
			}
			final int pointCount = clustering.getPointCount();// TODO this should be the union
			return ((float) pointCount - dMaxSum) / pointCount;
		};
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
