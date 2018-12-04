package clusterproject.clustergenerator.userInterface.MetaClustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import clusterproject.clustergenerator.data.ClusteringResult;

public class OpticsMetaClustering {
	private final List<ClusteringWithDistance> clusterings;
	private List<ClusteringWithDistance> seedlist;
	private List<ClusteringWithDistance> clusterOrder;
	private final double[][] distanceMatrix;
	private final int minPTS;
	private final double eps;

	public OpticsMetaClustering(List<ClusteringResult> clusterings, double[][] distanceMatrix2, int minPTS,
			double eps) {
		this.minPTS = minPTS;
		this.eps = eps;
		this.distanceMatrix = distanceMatrix2;
		this.clusterings = new ArrayList<ClusteringWithDistance>();
		for (int i = 0; i < clusterings.size(); ++i)
			this.clusterings.add(new ClusteringWithDistance(clusterings.get(i), i));
	}

	public List<ClusteringWithDistance> runOptics() {
		seedlist = new ArrayList<ClusteringWithDistance>();
		clusterOrder = new ArrayList<ClusteringWithDistance>();
		int minvalid = 0;
		final int totalcount = clusterings.size();
		for (int i = 0; i < totalcount; i++) {
			ClusteringWithDistance current = null;
			if (seedlist.isEmpty()) {
				for (int j = minvalid; j < totalcount; j++) {
					minvalid++;
					if (!clusterings.get(j).flag) {
						current = clusterings.get(j);
						break;
					}
				}
			} else {
				current = seedlist.remove(0);
			}

			if (current != null) {
				clusterOrder.add(current);
				current.flag = true;
				final List<Double> distances = new ArrayList<Double>();
				final List<ClusteringWithDistance> neighbours = query(eps, current, distances);
				if (neighbours.size() + 1 >= minPTS) {
					final double coredistance = coredist(distances, minPTS);
					for (int j = 0; j < neighbours.size(); j++) {
						final ClusteringWithDistance neighbour = neighbours.get(j);
						if (!neighbour.flag) {
							final double dist = Math.max(distances.get(j), coredistance);
							if ((neighbour.distance) > dist)
								neighbour.distance = dist;
							if (!seedlist.contains(neighbour))
								seedlist.add(neighbour);
						}
					}
				}
			}
			if (seedlist.size() > 1) {
				double minval = Float.MAX_VALUE;
				int index = -1;
				for (int y = 0; y < seedlist.size(); y++) {
					if (seedlist.get(y).distance < minval) {
						index = y;
						minval = seedlist.get(y).distance;
					}
				}
				Collections.swap(seedlist, 0, index);
			}
		}
		return clusterOrder;
	}

	private List<ClusteringWithDistance> query(double eps2, ClusteringWithDistance start, List<Double> distances) {
		// TODO this can maybe be improved?
		final List<ClusteringWithDistance> result = new ArrayList<ClusteringWithDistance>();
		final int startID = clusterings.indexOf(start);
		for (int i = 0; i < clusterings.size(); ++i) {
			final double distance = distanceMatrix[i][startID];
			if (distance <= eps2 && i != startID) {
				result.add(clusterings.get(i));
				distances.add(distance);
			}
		}
		return result;
	}

	private double coredist(List<Double> distances, int minPTS) {
		double lowerbound = -1;
		double smalest = distances.get(0);
		int count = 1;
		for (int i = 0; i < minPTS - 1; i++) {
			for (int j = 0; j < distances.size(); j++) {
				final double distance = distances.get(j);
				if (distance < smalest && distance > lowerbound) {
					smalest = distance;
					count = 1;
				} else if (distance == lowerbound)
					count++;
			}
			if (i + count >= minPTS - 1)
				return smalest;
			count = 0;
			lowerbound = smalest;
			smalest = Float.MAX_VALUE;
		}
		return lowerbound;
	}

}
