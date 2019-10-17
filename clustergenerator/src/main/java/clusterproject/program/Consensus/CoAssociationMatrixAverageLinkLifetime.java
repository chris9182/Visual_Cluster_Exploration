package clusterproject.program.Consensus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

import clusterproject.data.PointContainer;
import clusterproject.util.Container;

//XXX improve performance?
public class CoAssociationMatrixAverageLinkLifetime implements ConsensusFunction {

	@Override
	public PointContainer calculateConsensus(List<PointContainer> results, List<Double> weights) {
		if (results == null || results.isEmpty())
			return null;
		final Set<double[]> allpoints = new HashSet<double[]>();
		for (final PointContainer container : results)
			allpoints.addAll(container.getPoints());
		final List<double[]> points = new ArrayList<double[]>(allpoints);
		final int pointCount = points.size();

		final double[][] coAssociationMatrix = CoAssociationMatrix.buildMatrix(results, weights, points, pointCount);

		return link(results, pointCount, points, coAssociationMatrix);
	}

	public static PointContainer link(List<PointContainer> results, int pointCount, List<double[]> points,
			double[][] coAssociationMatrix) {
		final List<Double> lifeTimes = new ArrayList<Double>();
		final List<Set<Integer>> consensus = new ArrayList<Set<Integer>>();

		for (int i = 0; i < pointCount; ++i) {
			final Set<Integer> set = new HashSet<Integer>();
			set.add(i);
			consensus.add(set);
		}

		final ReentrantLock lock = new ReentrantLock();
		while (consensus.size() > 1) {
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
			lifeTimes.add(maxAvgLink.getValue());
		}

		for (int i = 0; i < lifeTimes.size() - 1; ++i) {
			lifeTimes.set(i, lifeTimes.get(i) - lifeTimes.get(i + 1));
		}

		int indMax = 0;
		double maxLifetime = lifeTimes.get(0);
		for (int i = 1; i < lifeTimes.size() - 1; ++i) {
			if (lifeTimes.get(i) >= maxLifetime) {
				maxLifetime = lifeTimes.get(i);
				indMax = i;
			}
		}
		final List<Set<Integer>> consensus2 = new ArrayList<Set<Integer>>();
		for (int i = 0; i < pointCount; ++i) {
			final Set<Integer> set = new HashSet<Integer>();
			set.add(i);
			consensus2.add(set);
		}

		int k = 0;
		while (consensus2.size() > 1 && k < indMax + 1) {
			final Container<Double> maxAvgLink = new Container<Double>(-Double.MAX_VALUE);
			final Container<Integer> idx1 = new Container<Integer>(-1);
			final Container<Integer> idx2 = new Container<Integer>(-1);
			IntStream.range(0, consensus2.size()).parallel().forEach(i -> {
				int myIDx1 = -1;
				int myIDx2 = -1;
				double myMaxAvgLink = -Double.MAX_VALUE;
				final Set<Integer> set1 = consensus2.get(i);
				final int size = consensus2.size();
				for (int j = i + 1; j < size; ++j) {
					final double avgLink = calcAvgLink(set1, consensus2.get(j), coAssociationMatrix);
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
			consensus2.get(idx1.getValue()).addAll(consensus2.get(idx2.getValue()));
			consensus2.remove((int) idx2.getValue());
			k++;
		}

		final PointContainer newContainer = new PointContainer(results.get(0).getDim());
		newContainer.setPoints(points);
		newContainer.setUpClusters();
		final int size = consensus2.size();
		for (int i = 0; i < pointCount; ++i) {
			for (int j = 0; j < size; ++j)
				if (consensus2.get(j).contains(i)) {
					newContainer.getClusterInformation().addClusterID(j);
					break;
				}
			// TODO: use hashtable? break here?
		}
		newContainer.setHeaders(results.get(0).getHeaders());
		return newContainer;
	}

	private static double calcAvgLink(Set<Integer> s1, Set<Integer> s2, double[][] coAssociationMatrix) {
		double dist = 0;
		for (final Integer i : s1)
			for (final Integer j : s2) {
				int x;
				int y;
				if (i < j) {
					x = i;
					y = j;
				} else {
					x = j;
					y = i;
				}
				dist += coAssociationMatrix[x][y];
			}
		dist /= s1.size() * s2.size();
		return dist;
	}

}
