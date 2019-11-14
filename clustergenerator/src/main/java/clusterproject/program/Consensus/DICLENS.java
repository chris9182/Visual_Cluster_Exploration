package clusterproject.program.Consensus;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import clusterproject.data.PointContainer;
import clusterproject.util.Util;
import other.diclens.DiclensGUIController;

public class DICLENS implements ConsensusFunction {

	@Override
	public PointContainer calculateConsensus(List<PointContainer> results, List<Double> weights) {
		if (results == null || results.isEmpty())
			return null;
		if (results.size() == 1) {
			final PointContainer consensus = new PointContainer(results.get(0).getDim());
			consensus.addPoints(results.get(0).getPoints());
			consensus.setUpClusters();
			consensus.getClusterInformation().copyIn(results.get(0).getClusterInformation());
			return consensus;
		}
		final int[][] assignments = new int[results.size()][];
		assignments[0] = results.get(0).getClusterInformation().getOriginalClusterIDs().stream().mapToInt(e -> e)
				.toArray();
		final List<double[]> points = results.get(0).getPoints();
		final int pointCount = assignments[0].length;
		IntStream.range(1, results.size()).parallel().forEach(i -> {
			assignments[i] = new int[pointCount];
			final Map<double[], Integer> labels = results.get(i).getOriginalLabelMap();
			for (int j = 0; j < pointCount; ++j)
				assignments[i][j] = labels.get(points.get(j));
		});
		// fix indices to start with 1 and be consecutive
		IntStream.range(0, results.size()).parallel()
				.forEach(i -> assignments[i] = Util.makeConsecutiveStartingWith(1, assignments[i]));
		// from:
		// https://www.cs.umb.edu/~smimarog/diclens/
		final int[] assignment = DiclensGUIController.runAlgorithm(assignments);
		final PointContainer consensus = new PointContainer(results.get(0).getDim());
		consensus.addPoints(results.get(0).getPoints());
		consensus.setUpClusters();
		consensus.getClusterInformation()
				.setOriginalClusterID(Arrays.stream(assignment).boxed().collect(Collectors.toList()));
		return consensus;
	}

}
