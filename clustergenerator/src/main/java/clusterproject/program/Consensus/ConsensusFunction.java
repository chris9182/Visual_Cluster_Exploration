package clusterproject.program.Consensus;

import java.util.List;

import clusterproject.data.PointContainer;

public interface ConsensusFunction {

	public PointContainer calculateConsensus(List<PointContainer> results, List<Double> weights);
}
