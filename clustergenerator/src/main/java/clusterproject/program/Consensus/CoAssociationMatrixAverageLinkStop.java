package clusterproject.program.Consensus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

import clusterproject.data.PointContainer;
import clusterproject.util.Container;

public class CoAssociationMatrixAverageLinkStop implements IConsensusFunction {

	private static final String name = "CA-Average Link choice";

	@Override
	public PointContainer calculateConsensus(List<PointContainer> results, List<Double> weights) {
		return new CoAssociationMatrixAverageLinkLifetime().calculateConsensus(results, weights);
	}

	private static double calcAvgLink(Set<Integer> s1, Set<Integer> s2, double[][] coAssociationMatrix) {
		double dist = 0;
		for (final Integer i : s1)
			for (final Integer j : s2) {
				dist += coAssociationMatrix[i][j];
			}
		dist /= s1.size() * s2.size();
		return dist;
	}

	@Override
	public boolean supportsClusterNumber() {
		return true;
	}

	@Override
	public PointContainer calculateConsensus(List<PointContainer> results, List<Double> weights, int clusterNumber) {
		if (clusterNumber < 1)
			return new CoAssociationMatrixAverageLinkLifetime().calculateConsensus(results, weights);

		if (results == null || results.isEmpty())
			return null;
		final Set<double[]> allpoints = new HashSet<double[]>();
		for (final PointContainer container : results)
			allpoints.addAll(container.getPoints());
		final List<double[]> points = new ArrayList<double[]>(allpoints);
		final int pointCount = points.size();

		final double[][] coAssociationMatrix = CoAssociationMatrix.buildMatrix(results, weights, points, pointCount);

		return link(results, pointCount, points, coAssociationMatrix, clusterNumber);
	}

	public static PointContainer link(List<PointContainer> results, int pointCount, List<double[]> points,
			double[][] coAssociationMatrix, int clusterNumber) {
		final List<Set<Integer>> consensus = new ArrayList<Set<Integer>>();

		for (int i = 0; i < pointCount; ++i) {
			final Set<Integer> set = new HashSet<Integer>();
			set.add(i);
			consensus.add(set);
		}

		final ReentrantLock lock = new ReentrantLock();
		while (consensus.size() > clusterNumber) {
			final Container<Double> maxAvgLink = new Container<Double>(-Double.MAX_VALUE);
			final Container<Integer> idx1 = new Container<Integer>(-1);
			final Container<Integer> idx2 = new Container<Integer>(-1);
			IntStream.range(0, consensus.size()).parallel().forEach(i -> {
				int myIDx1 = -1;
				int myIDx2 = -1;
				double myMaxAvgLink = -Double.MAX_VALUE;
				final Set<Integer> set1 = consensus.get(i);
				final int size = consensus.size();
				for (int j = i + 1; j < size; ++j) {
					final double avgLink = calcAvgLink(set1, consensus.get(j), coAssociationMatrix);
					if (avgLink > myMaxAvgLink) {
						myIDx1 = i;
						myIDx2 = j;
						myMaxAvgLink = avgLink;
					}
				}
				lock.lock();
				try {
					if (myMaxAvgLink > maxAvgLink.getValue()) {
						idx1.setValue(myIDx1);
						idx2.setValue(myIDx2);
						maxAvgLink.setValue(myMaxAvgLink);
					}
				} finally {
					lock.unlock();
				}

			});
			consensus.get(idx1.getValue()).addAll(consensus.get(idx2.getValue()));
			consensus.remove((int) idx2.getValue());

		}

		final PointContainer newContainer = new PointContainer(results.get(0).getDim());
		newContainer.setPoints(points);
		newContainer.setUpClusters();
		final int size = consensus.size();
		for (int i = 0; i < pointCount; ++i) {
			for (int j = 0; j < size; ++j)
				if (consensus.get(j).contains(i)) {
					newContainer.getClusterInformation().addClusterID(j);
					break;
				}
			// TODO: use hashtable? break here?
		}
		newContainer.setHeaders(results.get(0).getHeaders());
		return newContainer;
	}

	@Override
	public String getName() {
		return name;
	}

}
