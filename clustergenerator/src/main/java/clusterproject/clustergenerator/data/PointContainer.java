package clusterproject.clustergenerator.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.spark.mllib.linalg.Vector;

public class PointContainer {
	private List<double[]> points = new ArrayList<double[]>();
	private List<String> headers;
	private Double[] center = null;
	private List<Integer> originalClusterIDs;
	private List<Integer> clusterIDs;
	Map<Integer, Integer> idMap = null;
	private int dim;

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

	// public boolean revalidate() {
	// final boolean old=revalidate;
	// revalidate=true;
	// return !old;
	// }

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

	public void saveIDMap(Map<Integer, Integer> idMap) {
		this.idMap = idMap;

	}

	public Map<Integer, Integer> getIDMap() {
		return idMap;
	}
}
