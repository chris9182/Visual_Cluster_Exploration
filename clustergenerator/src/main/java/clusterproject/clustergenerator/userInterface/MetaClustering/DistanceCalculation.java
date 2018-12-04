package clusterproject.clustergenerator.userInterface.MetaClustering;

import java.util.List;
import java.util.stream.IntStream;

import clusterproject.clustergenerator.data.ClusteringResult;

public class DistanceCalculation {

	public static double[][] calculateDistanceMatrix(List<ClusteringResult> clusterings, IDistanceMeasure measure) {
		final int size = clusterings.size();
		final double[][] distances = new double[size][size];
		IntStream.range(0, size * size).parallel().forEach(i -> {
			final int x = i % size;
			final int y = i / size;
			distances[x][y] = measure.distanceBetween(clusterings.get(x), clusterings.get(y));
		});
		return distances;
	}
}
