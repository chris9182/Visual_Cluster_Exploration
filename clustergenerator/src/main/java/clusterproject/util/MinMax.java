package clusterproject.util;

public class MinMax {
	public Double min = Double.MAX_VALUE;
	public Double max = -Double.MAX_VALUE;

	public MinMax() {
	}

	public MinMax(double min, double max) {
		this.min = min;
		this.max = max;
	}

	public void add(Double doubleVal) {
		if (doubleVal.isNaN())
			throw new IllegalArgumentException("Double.NaN cannot be added to MinMax");
		if (doubleVal < min)
			min = doubleVal;
		if (doubleVal > max)
			max = doubleVal;

	}

	public double getRange() {
		return max - min;
	}

	public Double getByBoolean(boolean isMax) {
		if (isMax)
			return max;
		return min;
	}

	public void setByBoolean(boolean isMax, Double newVal) {
		if (newVal.isNaN())
			throw new IllegalArgumentException("Double.NaN cannot be added to MinMax");
		if (isMax)
			max = newVal;
		else
			min = newVal;

	}

}
