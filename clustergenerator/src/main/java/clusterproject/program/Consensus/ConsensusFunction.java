package clusterproject.program.Consensus;

import java.util.List;

import clusterproject.data.PointContainer;

public interface ConsensusFunction {

	PointContainer calculateConsensus(List<PointContainer> results, List<Double> weights);

	PointContainer calculateConsensus(List<PointContainer> results, List<Double> weights, int clusterNumber);

	boolean supportsClusterNumber();
}
