package other.dm.math;

//from https://www.cs.umb.edu/~smimarog/diclens/
//modified
public class Normalize {
	private Normalize() {
	}

	public static void usingMinMaxMethod(final double[] values) {
		double min = values[0];
		double max = values[0];
		for (int i = 1; i < values.length; ++i) {
			final double value = values[i];
			if (value > max) {
				max = value;
			}
			if (value < min) {
				min = value;
			}
		}
		final double divider = max - min;
		for (int i = 0; i < values.length; ++i) {
			values[i] /= divider;
		}
	}

	public static void usingZScoreMethod(final double[] values) {
		double total = 0.0;
		for (final double value : values) {
			total += value;
		}
		final double average = total / values.length;
		double totalError = 0.0;
		for (final double value2 : values) {
			totalError += (average - value2) * (average - value2);
		}
		final double standartDeviation = Math.sqrt(totalError / values.length);
		for (int i = 0; i < values.length; ++i) {
			values[i] -= average / standartDeviation;
		}
	}
}
