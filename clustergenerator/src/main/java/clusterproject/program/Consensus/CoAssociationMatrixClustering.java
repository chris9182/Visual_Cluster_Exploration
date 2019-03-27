package clusterproject.program.Consensus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import clusterproject.data.PointContainer;

public class CoAssociationMatrixClustering implements ConsensusFunction {

	private final double threshhold = 0.5;

	@Override
	public PointContainer calculateConsensus(List<PointContainer> results) {
		if (results == null || results.isEmpty())
			return null;
		final List<Map<double[], Integer>> assignments = new ArrayList<Map<double[], Integer>>();
		for (final PointContainer result : results)
			assignments.add(result.getLabelMap());

		final int resultCount = results.size();
		final List<double[]> points = results.get(0).getPoints();
		final int pointCount = points.size();

		final double coAssociationMatrix[][] = new double[pointCount][pointCount];

		IntStream.range(0, pointCount).parallel().forEach(i -> {
			final double[] pointi = points.get(i);
			for (int j = 0; j < pointCount; ++j) {
				final double[] pointj = points.get(j);
				coAssociationMatrix[i][j] = 0;
				for (int t = 0; t < resultCount; ++t) {
					if (assignments.get(t).get(pointi) == assignments.get(t).get(pointj))
						++coAssociationMatrix[i][j];
				}
				coAssociationMatrix[i][j] /= resultCount;
			}
		});

		// TODO: generate new clustering via clustering with CoAssociationMatrix (for
		// distances)

		return null;
	}

}
