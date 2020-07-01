package clusterproject.program.MetaClustering;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

public class OpticsResult implements Iterable<OpticsContainer> {
	public static final int NOISE_TAG = -2;
	public static final Comparator<OpticsContainer> comparator = (o1, o2) -> Double.compare(o1.distance, o2.distance);

	private final OpticsContainer[] data;

	public OpticsResult(final int size) {
		data = new OpticsContainer[size];
	}

	public void add(final int i, final OpticsContainer d) {
		data[i] = d;
	}

	public int size() {
		return data.length;
	}

	public OpticsContainer get(final int i) {
		return data[i];
	}

	public OpticsContainer[] getData() {
		return data;
	}

	public void calculateClusters(final double threshhold) {
		final int dataLength = size();
		if (dataLength < 1)
			return;
		final int[] clusterer = new int[dataLength];
		int curindex = 0;
		clusterer[0] = 1;
		for (int i = 1; i < dataLength; ++i) {
			if (data[i].distance > threshhold)
				clusterer[++curindex] = 1;
			else
				++clusterer[curindex];
		}

		// more pleasing colors at the cost of colors to the right changing all
		// the time whenever a new threshold is defined
		orderBasedTag(clusterer, curindex + 1);

		// makes it so that regions keep their color more often at the cost of
		// less pleasing colors
		// indexBasedTag( clusterer, curindex + 1);

		// makes it so regions keep their color even more often based on the index
		// of the smallest distance within cluster. this makes the colors even less
		// pleasing
//    minDistanceBasedTag(clusterer, curindex + 1);

	}

	private void orderBasedTag(final int[] clusterer, final int length) {
		int noise = 0;
		int index = 0;
		for (int i = 0; i < length; ++i) {
			final int count = clusterer[i];
			if (count == 1) {
				data[index].tag = NOISE_TAG;
				++noise;
			} else {
				final int newTag = i - noise;
				for (int j = 0; j < count; ++j)
					data[index + j].tag = newTag;
			}
			index += count;
		}
	}

	private void indexBasedTag(final int[] clusterer, final int length) {
		int index = 0;
		for (int i = 0; i < length; ++i) {
			final int count = clusterer[i];
			if (count == 1) {
				data[index].tag = NOISE_TAG;
			} else {
				int minIndex = Integer.MAX_VALUE;
				for (int j = 0; j < count; ++j) {
					final OpticsContainer container = data[index + j];
					if (container.inIndex < minIndex)
						minIndex = container.inIndex;
				}

				for (int j = 0; j < count; ++j)
					data[index + j].tag = minIndex;
			}
			index += count;
		}

	}

	private void minDistanceBasedTag(final int[] clusterer, final int length) {
		int index = 0;
		for (int i = 0; i < length; ++i) {
			final int count = clusterer[i];
			if (count == 1) {
				data[index].tag = NOISE_TAG;
			} else {
				int minIndex = Integer.MAX_VALUE;
				double minDistance = Float.MAX_VALUE;
				for (int j = 0; j < count; ++j) {
					final OpticsContainer container = data[index + j];
					if (container.distance < minDistance) {
						minIndex = container.inIndex;
						minDistance = container.distance;
					}
				}

				for (int j = 0; j < count; ++j)
					data[index + j].tag = minIndex;
			}
			index += count;
		}

	}

	@Override
	public Iterator<OpticsContainer> iterator() {
		return Arrays.asList(data).iterator();
	}

	public boolean isEmpty() {
		return size() == 0;
	}

}
