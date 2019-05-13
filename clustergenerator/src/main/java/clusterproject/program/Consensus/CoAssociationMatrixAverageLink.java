package clusterproject.program.Consensus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

import com.google.common.util.concurrent.AtomicDouble;

import clusterproject.data.PointContainer;

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
					if (currentWeight <= 0)
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
					if (currentWeight <= 0)
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
			final AtomicDouble maxAvgLink = new AtomicDouble(-Double.MAX_VALUE);
			final AtomicInteger idx1 = new AtomicInteger(-1);
			final AtomicInteger idx2 = new AtomicInteger(-1);
			IntStream.range(0, consensus.size()).parallel().forEach(i -> {
				int myIDx1 = -1;
				int myIDx2 = -1;
				double myMaxAvgLink = -Double.MAX_VALUE;
				for (int j = i + 1; j < consensus.size(); ++j) {
					final double avgLink = calcAvgLink(consensus.get(i), consensus.get(j), coAssociationMatrix);
					if (avgLink > myMaxAvgLink) {
						myIDx1 = i;
						myIDx2 = j;
						myMaxAvgLink = avgLink;
					}
				}
				lock.lock();
				try {
					if (myMaxAvgLink > maxAvgLink.get()) {
						idx1.set(myIDx1);
						idx2.set(myIDx2);
						maxAvgLink.set(myMaxAvgLink);
					}
				} finally {
					lock.unlock();
				}

			});

			if (maxAvgLink.get() < threshhold)
				cont = false;
			else {
				consensus.get(idx1.get()).addAll(consensus.get(idx2.get()));
				consensus.remove(idx2.get());
			}
		}

		final PointContainer newContainer = new PointContainer(results.get(0).getDim());
		newContainer.setPoints(points);
		newContainer.setUpClusters();
		for (int i = 0; i < pointCount; ++i) {
			for (int j = 0; j < consensus.size(); ++j)
				if (consensus.get(j).contains(i))
					newContainer.addClusterID(j);

		}
		newContainer.setHeaders(results.get(0).getHeaders());
		return newContainer;
	}

	private double calcAvgLink(Set<Integer> s1, Set<Integer> s2, double[][] coAssociationMatrix) {
		double dist = 0;
		for (final Integer i : s1)
			for (final Integer j : s2)
				dist += coAssociationMatrix[i < j ? i : j][i < j ? j : i];
		dist /= s1.size() * s2.size();
		return dist;
	}

}
