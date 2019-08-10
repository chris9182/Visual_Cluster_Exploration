package clusterproject.util;

public class MinMax {
	public Double min = Double.MAX_VALUE;
	public Double max = -Double.MAX_VALUE;

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

}
