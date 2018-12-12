package clusterproject.clustergenerator.data;

import de.lmu.ifi.dbs.elki.data.NumberVector;

public class NumberVectorClusteringResult {
	private final NumberVector[][] clusterPoints;
	private final String description;

	public NumberVectorClusteringResult(NumberVector[][] clusterPoints, String description) {
		this.clusterPoints = clusterPoints;
		this.description = description;
	}

	public NumberVector[][] getData() {
		return clusterPoints;
	}

	public String getDescription() {
		return description;
	}
}
