package clusterproject.program.MetaClustering;

import clusterproject.data.ClusteringResult;
import clusterproject.util.Util;

public class ClusteringError implements IMetaDistanceMeasure {

	@Override
	public double distanceBetween(ClusteringResult clustering, ClusteringResult clustering2) {
		final double[][][] data1 = clustering.getData();
		final double[][][] data2 = clustering2.getData();

		final int matrixSize = data1.length > data2.length ? data1.length : data2.length;
		final int[][] confusion = new int[matrixSize][matrixSize];
		for (int i = 0; i < data1.length; ++i)
			for (int j = 0; j < data2.length; ++j) {
				try {
					confusion[i][j] = -Util.intersection(data1[i], data2[j]).length;
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
				dMaxSum += Util.intersection(data1[assignment[i][1]], data2[assignment[i][0]]).length;// XXX indexes?
			} catch (final ArrayIndexOutOfBoundsException e) {

			}
		}

		final int pointCount = Util.intersection(clustering, clustering2).length;
		return ((double) pointCount - dMaxSum) / pointCount;
	}

	@Override
	public String getName() {
		return "Clustering Error";
	}

}
