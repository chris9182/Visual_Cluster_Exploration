package clusterproject.program.Consensus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import clusterproject.data.PointContainer;

public class CoAssociationMatrixThreshhold implements ConsensusFunction {

	private final double threshhold = 0.75;

	@Override
	public PointContainer calculateConsensus(List<PointContainer> results, List<Double> weights) {// TODO: allow
																									// setting weights
		if (results == null || results.isEmpty())
			return null;
		final List<Map<double[], Integer>> assignments = new ArrayList<Map<double[], Integer>>();
		for (final PointContainer result : results)
			assignments.add(result.getLabelMap());

		final int resultCount = results.size();
		final List<double[]> points = results.get(0).getPoints();
		final int pointCount = points.size();
		final double totalWeights = (weights != null) ? weights.stream().mapToDouble(f -> f.doubleValue()).sum()
				: resultCount;

		final double coAssociationMatrix[][] = new double[pointCount][pointCount];

		IntStream.range(0, pointCount).parallel().forEach(i -> {
			final double[] pointi = points.get(i);
			for (int j = i + 1; j < pointCount; ++j) {
				final double[] pointj = points.get(j);
				coAssociationMatrix[i][j] = 0;
				for (int t = 0; t < resultCount; ++t) {
					if (assignments.get(t).get(pointi) == assignments.get(t).get(pointj))
						if (weights != null)
							coAssociationMatrix[i][j] += weights.get(t);
						else
							++coAssociationMatrix[i][j];
				}

				coAssociationMatrix[i][j] /= totalWeights;
			}
		});

		final List<Set<double[]>> consensus = new ArrayList<Set<double[]>>();

		for (int i = 0; i < pointCount; ++i) {
			final Set<double[]> set = new HashSet<double[]>();
			set.add(points.get(i));
			consensus.add(set);
		}

		for (int i = 0; i < pointCount; ++i) {
			for (int j = i + 1; j < pointCount; ++j) {
				if (coAssociationMatrix[i][j] > threshhold) {
					int set1 = -1;
					int set2 = -1;
					for (int z = 0; z < consensus.size(); ++z) {
						if (consensus.get(z).contains(points.get(i)))
							set1 = z;
					}
					for (int z = 0; z < consensus.size(); ++z) {
						if (consensus.get(z).contains(points.get(j)))
							set2 = z;
					}
					if (set1 != set2) {
						consensus.get(set1).addAll(consensus.get(set2));
						consensus.remove(set2);
					}
				}
			}
		}

		final PointContainer newContainer = new PointContainer(results.get(0).getDim());
		newContainer.setPoints(points);
		newContainer.setUpClusters();
		for (final double[] point : points) {
			for (int i = 0; i < consensus.size(); ++i)
				if (consensus.get(i).contains(point))
					newContainer.addClusterID(i);
		}
		newContainer.setHeaders(results.get(0).getHeaders());
		return newContainer;
	}

}
