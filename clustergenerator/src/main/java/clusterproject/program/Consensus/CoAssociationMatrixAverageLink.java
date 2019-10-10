package clusterproject.program.Consensus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

import clusterproject.data.PointContainer;
import clusterproject.util.Container;

public class CoAssociationMatrixAverageLink implements ConsensusFunction {

	private final double threshhold = 0.5;

	@Override
	public PointContainer calculateConsensus(List<PointContainer> results, List<Double> weights) {
		if (results == null || results.isEmpty())
			return null;
		final List<Map<double[], Integer>> assignments = new ArrayList<Map<double[], Integer>>();
		for (final PointContainer result : results)
			assignments.add(result.getLabelMap());

		final int resultCount = results.size();
		final Set<double[]> allpoints = new HashSet<double[]>();
		for (final PointContainer container : results)
			allpoints.addAll(container.getPoints());
		final List<double[]> points = new ArrayList<double[]>(allpoints);
		final int pointCount = points.size();
		final double totalWeights = (weights != null) ? weights.stream().mapToDouble(f -> f.doubleValue()).sum()
				: resultCount;

		final double coAssociationMatrix[][] = new double[pointCount][pointCount];

		if (weights != null) {
			IntStream.range(0, pointCount).parallel().forEach(i -> {
				final double[] pointi = points.get(i);
				for (int j = i + 1; j < pointCount; ++j) {
					final double[] pointj = points.get(j);
					coAssociationMatrix[i][j] = 0;
					double currentWeight = totalWeights;
					for (int t = 0; t < resultCount; ++t) {
						if (assignments.get(t).get(pointi) == null || assignments.get(t).get(pointj) == null)
							currentWeight -= weights.get(t);
						else if (assignments.get(t).get(pointi) == assignments.get(t).get(pointj))
							coAssociationMatrix[i][j] += weights.get(t);

					}
					if (currentWeight <= Double.MIN_NORMAL)
						coAssociationMatrix[i][j] = 0;
					else
						coAssociationMatrix[i][j] /= currentWeight;
				}
			});
		} else {
			IntStream.range(0, pointCount).parallel().forEach(i -> {
				final double[] pointi = points.get(i);
				for (int j = i + 1; j < pointCount; ++j) {
					final double[] pointj = points.get(j);
					coAssociationMatrix[i][j] = 0;
					double currentWeight = totalWeights;
					for (int t = 0; t < resultCount; ++t) {
						if (assignments.get(t).get(pointi) == null || assignments.get(t).get(pointj) == null)
							currentWeight--;
						else if (assignments.get(t).get(pointi) == assignments.get(t).get(pointj))
							++coAssociationMatrix[i][j];
					}
					if (currentWeight <= Double.MIN_NORMAL)
						coAssociationMatrix[i][j] = 0;
					else
						coAssociationMatrix[i][j] /= currentWeight;
				}
			});
		}

		final List<Set<Integer>> consensus = new ArrayList<Set<Integer>>();

		for (int i = 0; i < pointCount; ++i) {
			final Set<Integer> set = new HashSet<Integer>();
			set.add(i);
			consensus.add(set);
		}

		boolean cont = true;
		final ReentrantLock lock = new ReentrantLock();
		while (cont) {
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

			if (maxAvgLink.getValue() < threshhold)
				cont = false;
			else {
				consensus.get(idx1.getValue()).addAll(consensus.get(idx2.getValue()));
				consensus.remove((int) idx2.getValue());
			}
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

	private double calcAvgLink(Set<Integer> s1, Set<Integer> s2, double[][] coAssociationMatrix) {
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
