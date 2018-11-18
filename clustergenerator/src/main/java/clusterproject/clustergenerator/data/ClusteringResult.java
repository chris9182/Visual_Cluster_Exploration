package clusterproject.clustergenerator.data;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.utilities.datastructures.arraylike.ArrayLikeUtil;

public class ClusteringResult {
	NumberVector[][] clusterPoints;

	public ClusteringResult(NumberVector[][] clusterPoints) {
		this.clusterPoints = clusterPoints;
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
}
