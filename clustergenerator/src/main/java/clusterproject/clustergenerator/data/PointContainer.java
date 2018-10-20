package clusterproject.clustergenerator.data;

import java.util.ArrayList;
import java.util.List;

public class PointContainer {
	private List<Double[]> points=new ArrayList<Double[]>();
	private Double[] center=null;

	public List<Double[]> getPoints() {
		return points;
	}

	public void setPoints(List<Double[]> points) {
		this.points = points;
	}
	public void addPoint(Double[] point) {
		points.add(point);
	}

	//	public boolean revalidate() {
	//		final boolean old=revalidate;
	//		revalidate=true;
	//		return !old;
	//	}

	public Double[] getCalculatedCenter() {
		if(points==null||points.isEmpty())return null;
		final int length=points.get(0).length;
		center=new Double[length];
		for(int i=0;i<length;++i)
			center[i]=(double) 0;
		for(final Double[] point:points) {
			for(int i=0;i<length;++i)
				center[i]+=point[i];
		}
		for(int i=0;i<length;++i)
			center[i]/=points.size();
		return center;
	}

	public double getMinFrom(int dimension) {
		double min=Double.MAX_VALUE;
		for(final Double[] point:points) {
			if(min>point[dimension])
				min=point[dimension];
		}
		return min;
	}

	public double getMaxFrom(int dimension) {
		double max=Double.MIN_VALUE;
		for(final Double[] point:points) {
			if(max<point[dimension])
				max=point[dimension];
		}
		return max;
	}

	public double[] getMinMaxFrom(int dimension) {
		final double[] minMax=new double[2];
		minMax[0]=Double.MAX_VALUE;
		minMax[1]=Double.MIN_VALUE;
		for(final Double[] point:points) {
			if(minMax[0]>point[dimension])
				minMax[0]=point[dimension];
			if(minMax[1]<point[dimension])
				minMax[1]=point[dimension];
		}
		return minMax;
	}

	public void addPointList(List<Double[]> newPoints) {
		points.addAll(points);
	}


}
