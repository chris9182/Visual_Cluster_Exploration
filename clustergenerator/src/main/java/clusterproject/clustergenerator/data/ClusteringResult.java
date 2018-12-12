package clusterproject.clustergenerator.data;

import java.io.Serializable;

public class ClusteringResult implements Serializable {

	private static final long serialVersionUID = -8078630305091815092L;

	private final double[][][] clusterPoints;
	private final String description;

	public ClusteringResult(double[][][] clusterPoints, String description) {
		this.clusterPoints = clusterPoints;
		this.description = description;
	}

	public double[][][] getData() {
		return clusterPoints;
	}

	public PointContainer toPointContainer() {
		final PointContainer container = new PointContainer(0);
		container.setUpClusters();
		for (int i = 0; i < clusterPoints.length; ++i)
			for (int j = 0; j < clusterPoints[i].length; ++j) {
				container.addPoint(clusterPoints[i][j]);
				container.addClusterID(i);
			}

		container.rebuild();
		return container;
	}

	public int getPointCount() {
		int count = 0;
		for (int i = 0; i < clusterPoints.length; ++i)
			count += clusterPoints[i].length;
		return count;
	}

	public String getDescription() {
		return description;
	}
}
