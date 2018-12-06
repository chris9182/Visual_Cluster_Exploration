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
			if (x < y)
				return;
			distances[x][y] = measure.distanceBetween(clusterings.get(x), clusterings.get(y));
			if (x != y)
				distances[y][x] = distances[x][y];
		});
		return distances;
	}
}
