package clusterproject.clustergenerator.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.spark.mllib.linalg.Vector;

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
		highlighted.add(-1);
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
		double max = Double.MIN_VALUE;
		for (final double[] point : points) {
			if (max < point[dimension])
				max = point[dimension];
		}
		return max;
	}

	public double[] getMinMaxFrom(int dimension) {
		final double[] minMax = new double[2];
		minMax[0] = Double.MAX_VALUE;
		minMax[1] = Double.MIN_VALUE;
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

	public void addPoints(Vector[] vectors) {
		for (int i = 0; i < vectors.length; ++i) {
			points.add(vectors[i].toArray());
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
		return clusterIDs != null && clusterIDs.size() == points.size();
	}

	public List<Integer> getClusterIDs() {
		return clusterIDs;
	}

	public void setClusterIDs(List<Integer> clusterIDs) {
		this.clusterIDs = clusterIDs;

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
}
