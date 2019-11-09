package other.dm.evaluator;

import java.util.BitSet;

//from https://www.cs.umb.edu/~smimarog/diclens/
//modified
public class ICSEvaluator {
	public static double evaluate(final BitSet[] inputClusters, final BitSet toEvaluate) {
		final BitSet accumulator = new BitSet(toEvaluate.size());
		int total = 0;
		for (final BitSet cluster : inputClusters) {
			accumulator.clear();
			accumulator.or(toEvaluate);
			accumulator.and(cluster);
			final int size = accumulator.cardinality();
			total += size * (size - 1) / 2;
		}
		final int size2 = toEvaluate.cardinality();
		final double denominator = size2 * (size2 - 1) / 2.0;
		if (denominator == 0.0) {
			return 0.0;
		}
		return total / denominator;
	}

	public static double evaluateForPartition(final BitSet[] inputClusters, final BitSet[] toEvaluate) {
		double icsAverage = 0.0;
		for (int i = 0; i < toEvaluate.length; ++i) {
			icsAverage += evaluate(inputClusters, toEvaluate[i]);
		}
		icsAverage /= toEvaluate.length;
		return icsAverage;
	}
}
