package clusterproject.clustergenerator.data;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.utilities.datastructures.arraylike.ArrayLikeUtil;

public class ClusteringResult {
	private final NumberVector[][] clusterPoints;
	private final String description;

	public ClusteringResult(NumberVector[][] clusterPoints, String description) {
		this.clusterPoints = clusterPoints;
		this.description = description;
	}

	public NumberVector[][] getData() {
		return clusterPoints;
	}

	public PointContainer toPointContainer() {
		final PointContainer container = new PointContainer(0);
		container.setUpClusters();
		for (int i = 0; i < clusterPoints.length; ++i)
			for (int j = 0; j < clusterPoints[i].length; ++j) {
				container.addPoint(ArrayLikeUtil.toPrimitiveDoubleArray(clusterPoints[i][j]));
				container.addClusterID(i);
			}
		container.rebuild();
		return container;
	}

	public String getDescription() {
		return description;
	}

}
