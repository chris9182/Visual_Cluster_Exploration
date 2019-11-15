package clusterproject.program.Consensus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import clusterproject.data.PointContainer;

public class CoAssociationMatrix {
	public static double[][] buildMatrix(List<PointContainer> results, List<Double> weights,
			final List<double[]> points, int pointCount) {
		final List<Map<double[], Integer>> assignments = new ArrayList<Map<double[], Integer>>();
		for (final PointContainer result : results)
			assignments.add(result.getOriginalLabelMap());
		final int resultCount = results.size();
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
		IntStream.range(0, pointCount).parallel().forEach(i -> {
			coAssociationMatrix[i][i] = 1;
			for (int j = 0; j < i + 1; ++j) {
				coAssociationMatrix[i][j] = coAssociationMatrix[j][i];
			}
		});
		return coAssociationMatrix;
	}
}
