package clusterproject.data;

import java.io.Serializable;
import java.util.List;

import clusterproject.program.Clustering.Parameters.Parameter;

public class ClusteringResult implements Serializable {
	private static final long serialVersionUID = -8078630305091815092L;

	private final double[][][] clusterPoints;
	private final Parameter parameter;
	private final List<String> headers;

	public ClusteringResult(double[][][] clusterPoints, Parameter parameter, List<String> headers) {
		this.clusterPoints = clusterPoints;
		this.parameter = parameter;
		this.headers = headers;
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
				container.getClusterInformation().addClusterID(i);
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

	public Parameter getParameter() {
		return parameter;
	}

	public List<String> getHeaders() {
		return headers;
	}
}
