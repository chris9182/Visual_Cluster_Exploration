package clusterproject.clustergenerator.data;

import java.util.ArrayList;
import java.util.List;

public class PointContainer {
	private List<double[]> points = new ArrayList<double[]>();
	private List<String> headers;
	private Double[] center = null;
	private int dim;
	private int selectedDimX = 1;
	private int selectedDimY = 0;

	public PointContainer(int dim) {
		this.dim = dim;
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

	public void addPointList(List<double[]> newPoints) {
		points.addAll(newPoints);
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

	public int getSelectedDimX() {
		return selectedDimX;
	}

	public int getSelectedDimY() {
		return selectedDimY;
	}

	public void setSelectedDimX(int selectedDimX) {
		this.selectedDimX = selectedDimX;
	}

	public void setSelectedDimY(int selectedDimY) {
		this.selectedDimY = selectedDimY;
	}

	public void empty() {
		points.clear();
		headers.clear();
		selectedDimX = -1;
		selectedDimY = -1;
		dim = -1;
	}

	public void rebuild() {
		if (points == null || points.isEmpty())
			return;
		dim = points.get(0).length;
		if (dim > 1) {
			selectedDimX = 1;
			selectedDimY = 0;
		} else if (dim == 1) {
			selectedDimX = 0;
			selectedDimY = 0;
		} else {
			selectedDimX = -1;
			selectedDimY = -1;
		}
		if (headers.isEmpty())
			for (int i = 0; i < dim; i++)
				headers.add(Integer.toString(i));

	}

}
