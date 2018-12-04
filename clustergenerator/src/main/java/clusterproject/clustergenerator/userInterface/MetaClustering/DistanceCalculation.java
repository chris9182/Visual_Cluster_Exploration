package clusterproject.clustergenerator.userInterface.MetaClustering;

import java.util.List;
import java.util.stream.IntStream;

import clusterproject.clustergenerator.data.ClusteringResult;

public class DistanceCalculation {

	public static float[][] calculateDistanceMatrix(List<ClusteringResult> clusterings, IDistanceMeasure measure) {
		final int size = clusterings.size();
		final float[][] distances = new float[size][size];
		final int add = size % 2;
		IntStream.range(0, size * size / 2 + add).parallel().forEach(i -> {
			final int x = i % size;
			final int y = i / size;
			distances[x][y] = measure.distanceBetween(clusterings.get(x), clusterings.get(y));
			distances[y][x] = distances[x][y];
		});

		// for (int i = 0; i < size; ++i)
		// for (int j = 0; j < size; ++j) {
		// distances[i][j] = measure.distanceBetween(clusterings.get(i),
		// clusterings.get(j));
		// }
		return distances;
	}
}
