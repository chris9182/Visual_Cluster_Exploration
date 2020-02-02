package clusterproject.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import clusterproject.util.MinMax;

public class PointContainer {
	private int dim;
	private List<double[]> points = new ArrayList<double[]>(300);
	private List<String> headers;
	private ClusterInformation clusterInformation;
	private MetaInformation metaInformation = new MetaInformation();

	public PointContainer(int dim) {
		this.dim = dim;
		headers = new ArrayList<String>(dim);
		for (int i = 0; i < dim; i++)
			headers.add(Integer.toString(i));
	}

	public void generateHeaders() {
		headers = new ArrayList<String>(dim);
		for (int i = 0; i < dim; i++)
			headers.add(Integer.toString(i));
	}

	public MetaInformation getMetaInformation() {
		return metaInformation;
	}

	public void setMetaInformation(MetaInformation metaInformation) {
		this.metaInformation = metaInformation;
	}

	public ClusterInformation getClusterInformation() {
		return clusterInformation;
	}

	public void setClusterInformation(ClusterInformation clusterInformation) {
		this.clusterInformation = clusterInformation;
	}

	public List<double[]> getPoints() {
		return points;
	}

	public void setPoints(List<double[]> points) {
		this.points = points;
	}

	public void addPoint(double[] point) {
		points.add(point);
	}

	public double[] getCalculatedCenter() {
		if (points == null || points.isEmpty())
			return null;
		final double[] center = new double[dim];
		for (int i = 0; i < dim; ++i)
			center[i] = 0;
		for (final double[] point : points) {
			for (int i = 0; i < dim; ++i)
				center[i] += point[i];
		}
		for (int i = 0; i < dim; ++i)
			center[i] /= points.size();
		return center;
	}

	public double getMinFrom(int dimension) {
		double min = Double.MAX_VALUE;
		for (final double[] point : points) {
			if (min > point[dimension])
				min = point[dimension];
		}
		return min;
	}

	public double getMaxFrom(int dimension) {
		double max = -Double.MAX_VALUE;
		for (final double[] point : points) {
			if (max < point[dimension])
				max = point[dimension];
		}
		return max;
	}

	public MinMax getMinMaxFrom(int dimension) {
		final MinMax minMax = new MinMax();
		for (final double[] point : points) {
			if (Double.isNaN(point[dimension]))
				continue;
			minMax.add(point[dimension]);
		}
		return minMax;
	}

	public void addPoints(Collection<double[]> newPoints) {
		points.addAll(newPoints);
	}

	public void addPoints(double[][] pointArr) {
		for (int i = 0; i < pointArr.length; ++i) {
			points.add(pointArr[i]);
		}
	}

	public List<String> getHeaders() {
		return headers;
	}

	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}

	public int getDim() {
		return dim;
	}

	public void setDim(int dim) {
		this.dim = dim;
	}

	public void empty() {
		points.clear();
		headers.clear();
		dim = -1;
		clusterInformation = null;
	}

	public void rebuild() {
		if (points == null || points.isEmpty())
			return;
		dim = points.get(0).length;

		if (headers.isEmpty())
			for (int i = 0; i < dim; i++)
				headers.add(Integer.toString(i));
	}

	public void removeClusterInfo() {
		clusterInformation = null;
		metaInformation.getHighlighted().clear();
		metaInformation.getHighlighted().add(-1);

	}

	public Map<double[], Integer> getLabelMap() {
		if (clusterInformation == null || !clusterInformation.hasClusters())
			return null;
		final Map<double[], Integer> assignments = new HashMap<double[], Integer>(points.size());
		for (int i = 0; i < points.size(); ++i)
			assignments.put(points.get(i), clusterInformation.getClusterIDs().get(i));
		return assignments;
	}

	public Map<double[], Integer> getOriginalLabelMap() {
		if (clusterInformation == null || !clusterInformation.hasClusters())
			return null;
		final Map<double[], Integer> assignments = new HashMap<double[], Integer>(points.size());
		for (int i = 0; i < points.size(); ++i)
			assignments.put(points.get(i), clusterInformation.getOriginalClusterIDs().get(i));
		return assignments;
	}

	public PointContainer getSample(int maxPoints) {
		final PointContainer sampleContainer = new PointContainer(dim);
		final int size = getPoints().size();
		final int stride = size / maxPoints;
		for (int i = 0; i < maxPoints; ++i) {
			sampleContainer.addPoint(points.get(i * stride));
		}
		if (clusterInformation.hasClusters()) {
			sampleContainer.setUpClusters();
			for (int i = 0; i < maxPoints; ++i) {
				sampleContainer.clusterInformation.getOriginalClusterIDs()
						.add(clusterInformation.getOriginalClusterIDs().get(i * stride));
			}
			if (clusterInformation.getClusterIDs() != null && !clusterInformation.getClusterIDs().isEmpty())
				for (int i = 0; i < maxPoints; ++i) {
					sampleContainer.clusterInformation.getClusterIDs()
							.add(clusterInformation.getClusterIDs().get(i * stride));
				}
		}
		if (metaInformation.getHighlighted() != null && !metaInformation.getHighlighted().isEmpty()) {
			for (final Integer h : metaInformation.getHighlighted()) {
				if (h % stride == 0 && h / stride < maxPoints)
					sampleContainer.metaInformation.getHighlighted().add(h / stride);
			}
		}
		if (metaInformation.getFilteredIndexes() != null && !metaInformation.getFilteredIndexes().isEmpty()) {
			for (final Integer h : metaInformation.getFilteredIndexes()) {
				if (h % stride == 0 && h / stride < maxPoints)
					sampleContainer.metaInformation.getFilteredIndexes().add(h / stride);
			}
		}
		if (metaInformation.getGroundTruth() % stride == 0 && metaInformation.getGroundTruth() / stride < maxPoints)
			sampleContainer.metaInformation.setGroundTruth(metaInformation.getGroundTruth() / stride);
		return sampleContainer;
	}

	public int getPointCount() {
		return points.size();
	}

	public boolean hasClusters() {
		if (clusterInformation == null)
			return false;
		return clusterInformation.hasClusters();
	}

	public void setUpClusters() {
		clusterInformation = new ClusterInformation(this);
		clusterInformation.setUpClusters();
	}

	public void copyInfoFrom(PointContainer container) {
		this.clusterInformation = container.clusterInformation;
		this.metaInformation = container.metaInformation;

	}

	public double[][][] toData() {
		final Map<Integer, List<double[]>> buckets = new HashMap<Integer, List<double[]>>(32);
		for (int i = 0; i < points.size(); ++i) {
			int cID = -1;
			if (clusterInformation != null)
				cID = clusterInformation.getOriginalClusterIDs().get(i);
			List<double[]> newPoints = buckets.get(cID);
			if (newPoints == null)
				newPoints = new ArrayList<double[]>();
			newPoints.add(points.get(i));
			buckets.put(cID, newPoints);
		}
		final double[][][] data = new double[buckets.size()][][];
		int i = 0;
		for (final List<double[]> cluster : buckets.values()) {
			double[][] pointCluster = new double[cluster.size()][];
			pointCluster = cluster.toArray(pointCluster);
			data[i++] = pointCluster;
		}
		return data;
	}

	public void checkRemoveClusterInfo() {
		if (clusterInformation == null)
			return;
		if (clusterInformation.getClusterIDs().size() != getPointCount())
			removeClusterInfo();

	}

}
