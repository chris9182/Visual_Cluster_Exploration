package clusterproject.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PointContainer {
	private List<double[]> points = new ArrayList<double[]>();
	private List<String> headers;
	private Double[] center = null;
	private List<Integer> originalClusterIDs;
	private List<Integer> clusterIDs;
	Map<Integer, Integer> idMap = null;
	private int dim;
	private LinkedHashSet<Integer> highlighted = new LinkedHashSet<Integer>();
	private Set<Integer> filteredResults;
	private int groundTruth = -1;

	public PointContainer(int dim) {
		this.dim = dim;
		headers = new ArrayList<String>(dim);
		for (int i = 0; i < dim; i++)
			headers.add(Integer.toString(i));
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

	public Double[] getCalculatedCenter() {
		if (points == null || points.isEmpty())
			return null;
		center = new Double[dim];
		for (int i = 0; i < dim; ++i)
			center[i] = (double) 0;
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

	public double[] getMinMaxFrom(int dimension) {
		final double[] minMax = new double[2];
		minMax[0] = Double.MAX_VALUE;
		minMax[1] = -Double.MAX_VALUE;
		for (final double[] point : points) {
			if (point[dimension] == Double.NaN)
				continue;
			if (minMax[0] > point[dimension])
				minMax[0] = point[dimension];
			if (minMax[1] < point[dimension])
				minMax[1] = point[dimension];
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
		clusterIDs = null;
		originalClusterIDs = null;
		dim = -1;
	}

	public void rebuild() {
		if (points == null || points.isEmpty())
			return;
		dim = points.get(0).length;

		if (headers.isEmpty())
			for (int i = 0; i < dim; i++)
				headers.add(Integer.toString(i));
	}

	public void setUpClusters() {
		clusterIDs = new ArrayList<Integer>();
		originalClusterIDs = new ArrayList<Integer>();
	}

	public void addClusterID(Integer id) {
		clusterIDs.add(id);
		originalClusterIDs.add(id);
	}

	public boolean hasClusters() {
		return clusterIDs != null && clusterIDs.size() == points.size() && originalClusterIDs != null
				&& originalClusterIDs.size() == points.size();
	}

	public List<Integer> getClusterIDs() {
		return clusterIDs;
	}

	public void setClusterIDs(List<Integer> clusterIDs) {
		this.clusterIDs = clusterIDs;

	}

	public void setAllClusterIDs(ArrayList<Integer> clusterIDs) {
		this.clusterIDs = clusterIDs;
		this.originalClusterIDs = clusterIDs;

	}

	public List<Integer> getOriginalClusterIDs() {
		return originalClusterIDs;
	}

	public void setOriginalClusterID(List<Integer> originalClusterIDs) {
		this.originalClusterIDs = originalClusterIDs;

	}

	public void setIDMap(Map<Integer, Integer> idMap) {
		this.idMap = idMap;

	}

	public Map<Integer, Integer> getIDMap() {
		return idMap;
	}

	public LinkedHashSet<Integer> getHighlighted() {
		return highlighted;
	}

	public void setHighlighted(LinkedHashSet<Integer> highlighted2) {
		this.highlighted = highlighted2;
	}

	public void copyClusterInfo(PointContainer container) {
		setOriginalClusterID(container.getOriginalClusterIDs());
		setClusterIDs(container.getClusterIDs());
		setIDMap(container.getIDMap());
		setHighlighted(getHighlighted());
	}

	public void removeClusterInfo() {
		setOriginalClusterID(null);
		setClusterIDs(null);
		setIDMap(null);
		highlighted.clear();
		highlighted.add(-1);

	}

	public void setFilteredResults(Set<Integer> filteredResults) {
		this.filteredResults = filteredResults;

	}

	public Set<Integer> getFilteredIndexes() {
		return filteredResults;
	}

	public void setGroundTruth(int groundTruth) {
		this.groundTruth = groundTruth;

	}

	public int getGroundTruth() {
		return groundTruth;
	}

	public Map<double[], Integer> getLabelMap() {
		if (!hasClusters())
			return null;
		final Map<double[], Integer> assignments = new HashMap<double[], Integer>();
		for (int i = 0; i < points.size(); ++i)
			assignments.put(points.get(i), clusterIDs.get(i));
		return assignments;
	}

	public PointContainer getSample(int maxPoints) {
		final PointContainer sampleContainer = new PointContainer(dim);
		final int size = getPoints().size();
		final int stride = size / maxPoints;
		for (int i = 0; i < maxPoints; ++i) {
			sampleContainer.addPoint(points.get(i * stride));
		}
		if (hasClusters()) {
			sampleContainer.setUpClusters();
			for (int i = 0; i < maxPoints; ++i) {
				sampleContainer.originalClusterIDs.add(originalClusterIDs.get(i * stride));
			}
			if (clusterIDs != null && !clusterIDs.isEmpty())
				for (int i = 0; i < maxPoints; ++i) {
					sampleContainer.clusterIDs.add(clusterIDs.get(i * stride));
				}
		}
		if (highlighted != null && !highlighted.isEmpty()) {
			for (final Integer h : highlighted) {
				if (h % stride == 0 && h / stride < maxPoints)
					sampleContainer.highlighted.add(h / stride);
			}
		}
		if (filteredResults != null && !filteredResults.isEmpty()) {
			for (final Integer h : filteredResults) {
				if (h % stride == 0 && h / stride < maxPoints)
					sampleContainer.filteredResults.add(h / stride);
			}
		}
		if (groundTruth % stride == 0 && groundTruth / stride < maxPoints)
			sampleContainer.groundTruth = groundTruth / stride;
		return sampleContainer;
	}

}
