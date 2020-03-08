package clusterproject.program.MetaClustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class OpticsMetaClustering<T> {
	private final List<OpticsContainer<T>> clusterings;
	private final int size;
	private PriorityQueue<OpticsContainer<T>> seedlist;
	private OpticsResult<T> clusterOrder;
	private final double[][] distanceMatrix;
	private final int minPTS;
	private final double eps;

	public OpticsMetaClustering(List<T> sClusterings, double[][] distanceMatrix2, int minPTS, double eps) {
		this.minPTS = minPTS;
		this.eps = eps;
		this.distanceMatrix = distanceMatrix2;
		this.clusterings = new ArrayList<OpticsContainer<T>>();
		for (int i = 0; i < sClusterings.size(); ++i)
			this.clusterings.add(new OpticsContainer<T>(sClusterings.get(i), i));
		size = clusterings.size();
	}

	public OpticsResult<T> runOptics() {
		seedlist = new PriorityQueue<OpticsContainer<T>>(OpticsResult.comparator);
		clusterOrder = new OpticsResult<T>();
		int minvalid = 0;
		final int totalcount = size;
		for (int i = 0; i < totalcount; i++) {
			OpticsContainer<T> current = null;
			if (seedlist.isEmpty()) {
				for (int j = minvalid; j < totalcount; j++) {
					minvalid++;
					if (!clusterings.get(j).flag) {
						current = clusterings.get(j);
						break;
					}
				}
			} else {
				current = seedlist.remove();
			}

			if (current != null) {
				clusterOrder.add(current);
				current.flag = true;
				final List<Double> distances = new ArrayList<Double>();
				final List<OpticsContainer<T>> neighbors = query(eps, current, distances);
				final int neighborSize = neighbors.size();
				if (neighborSize + 1 >= minPTS) {
					final double coredistance = coredist(distances, minPTS);
					for (int j = 0; j < neighborSize; j++) {
						final OpticsContainer<T> neighbor = neighbors.get(j);
						if (!neighbor.flag) {
							final double dist = Math.max(distances.get(j), coredistance);
							if ((neighbor.distance) > dist) {
								neighbor.distance = dist;
								seedlist.remove(neighbor);
								seedlist.add(neighbor);
							} else if (!seedlist.contains(neighbor))
								seedlist.add(neighbor);
						}
					}
				}
			}

		}
		return clusterOrder;
	}

	private List<OpticsContainer<T>> query(double eps2, OpticsContainer<T> start, List<Double> distances) {
		// TODO this can maybe be improved?
		final List<OpticsContainer<T>> result = new ArrayList<OpticsContainer<T>>();
		final int startID = start.inIndex;
		for (int i = 0; i < size; ++i) {
			final double distance = distanceMatrix[startID][i];
			if (distance <= eps2 && i != startID) {
				result.add(clusterings.get(i));
				distances.add(distance);
			}
		}
		return result;
	}

	private double coredist(List<Double> distances, int minPTS) {
		// TODO: check if this is right now
		final List<Double> distCopy = new ArrayList<Double>(distances);
		Collections.sort(distCopy, Comparator.comparingDouble(e -> e));
		return distCopy.get(minPTS - 2);
//		double lowerbound = -1;
//		double smalest = distances.get(0);
//		int count = 1;
//		for (int i = 0; i < minPTS - 1; i++) {
//			for (int j = 0; j < distances.size(); j++) {
//				final double distance = distances.get(j);
//				if (distance < smalest && distance > lowerbound) {
//					smalest = distance;
//					count = 1;
//				} else if (distance == lowerbound)
//					count++;
//			}
//			if (i + count >= minPTS - 1)
//				return smalest;
//			count = 0;
//			lowerbound = smalest;
//			smalest = Float.MAX_VALUE;
//		}
//		return lowerbound;
	}

}
