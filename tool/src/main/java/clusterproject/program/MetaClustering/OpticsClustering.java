package clusterproject.program.MetaClustering;

import java.util.Arrays;
import java.util.PriorityQueue;

public class OpticsClustering {
	private final OpticsContainer[] opticsData;
	private final int size;
	private final double[][] distanceMatrix;
	private final int minPTS;
	private final double eps;

	public OpticsClustering(final double[][] distanceMatrix, final int minPTS, final double eps) {
		size = distanceMatrix.length;
		this.minPTS = minPTS;
		this.eps = eps;
		this.distanceMatrix = distanceMatrix;
		this.opticsData = new OpticsContainer[size];
		for (int i = 0; i < size; ++i)
			this.opticsData[i] = new OpticsContainer(i);
	}

	public OpticsResult runOptics() {
		final PriorityQueue<OpticsContainer> seeds = new PriorityQueue<OpticsContainer>(OpticsResult.comparator);
		final OpticsResult clusterOrder = new OpticsResult(size);
		final double[] distances = new double[size - 1];
		final OpticsContainer[] neighbors = new OpticsContainer[size - 1];

		int minvalid = 0;
		final int totalcount = size;
		for (int i = 0; i < totalcount; i++) {
			OpticsContainer current = null;
			if (seeds.isEmpty()) {
				for (int j = minvalid; j < totalcount; ++j) {
					++minvalid;
					if (!opticsData[j].flag) {
						current = opticsData[j];
						break;
					}
				}
			} else {
				current = seeds.remove();
			}

//      if (current == null)return; //this can never be the case but can be used for defensive purposes.

			current.flag = true;
			clusterOrder.add(i, current);

			int neighborSize = query(eps, current, distances, neighbors);

			if (neighborSize + 1 >= minPTS) {
				final double coreDistance = coredist(distances, neighborSize, minPTS);
				for (int j = 0; j < neighborSize; j++) {
					final OpticsContainer neighbor = neighbors[j];
					if (!neighbor.flag) {
						final double newDist = Math.max(coreDistance, distances[j]);
						if (newDist < neighbor.distance) {
							neighbor.distance = newDist;
							seeds.remove(neighbor);
							seeds.add(neighbor);
						}
					}
				}
			}

		}
		return clusterOrder;
	}

	private int query(final double eps, final OpticsContainer start, final double[] distances,
			final OpticsContainer[] neighbors) {
		// TODO this can maybe be improved?
		int count = 0;
		final int startID = start.inIndex;
		for (int i = 0; i < startID; ++i) {
			final double distance = distanceMatrix[startID][i];
			if (distance <= eps) {
				neighbors[count] = opticsData[i];
				distances[count] = distance;
				++count;
			}
		}
		for (int i = startID + 1; i < size; ++i) {
			final double distance = distanceMatrix[startID][i];
			if (distance <= eps) {
				neighbors[count] = opticsData[i];
				distances[count] = distance;
				++count;
			}
		}
		return count;
	}

	private double coredist(final double[] distances, final int length, final int minPTS) {
//    final double[] copy = new double[length];
//    for (int i = 0; i < length; ++i)
//      copy[i] = distances[i];
//    Arrays.sort(copy);
//    if (true) return copy[minPTS - 2];

		// here Floyd-Rivest Algorithm could be even better
		// currently: O((n-k)k+klogk) which is good for small k
		// Floyd-Rivest Algorithm can achieve an expected O(n) through partitioning
		if (minPTS <= 2) {
			double min = distances[0];
			for (int i = 1; i < length; ++i)
				if (min > distances[i])
					min = distances[i];
			return min;
		}
		final int kth = minPTS - 1;
		final double[] min = new double[kth];
		for (int k = 0; k < kth; ++k) {
			min[k] = distances[k];
		}
		Arrays.sort(min);
		for (int i = kth; i < length; ++i) {
			final double distance = distances[i];
			if (distance < min[kth - 1]) {
				int j = kth - 1;
				// make space for the newly added distance dropping the highest one and
				// moving all larger ones up
				while (j > 0 && distance < min[j - 1]) {
					min[j] = min[j - 1];
					--j;
				}
				min[j] = distance;
			}

		}
		return min[kth - 1];
	}

}
