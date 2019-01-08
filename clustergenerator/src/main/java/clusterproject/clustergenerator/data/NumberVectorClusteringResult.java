package clusterproject.clustergenerator.data;

import clusterproject.clustergenerator.program.Clustering.Parameters.Parameter;
import de.lmu.ifi.dbs.elki.data.NumberVector;

public class NumberVectorClusteringResult {
	private final NumberVector[][] clusterPoints;
	private final Parameter parameter;

	public NumberVectorClusteringResult(NumberVector[][] clusterPoints, Parameter param) {
		this.clusterPoints = clusterPoints;
		this.parameter = param;
	}

	public NumberVector[][] getData() {
		return clusterPoints;
	}

	public Parameter getDescription() {
		return parameter;
	}
}
