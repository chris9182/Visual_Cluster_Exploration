package clusterproject.program.MetaClustering;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OpticsResult<T> implements Iterable<OpticsContainer<T>> {
	private static final int NOISE_TAG = -2;
	private final List<OpticsContainer<T>> data = new ArrayList<OpticsContainer<T>>();

	public boolean isEmpty() {
		return data.isEmpty();
	}

	public OpticsContainer<T> remove(int i) {
		return data.remove(i);
	}

	public boolean contains(Object o) {
		return data.contains(o);
	}

	public void add(OpticsContainer<T> d) {
		data.add(d);

	}

	public int size() {
		return data.size();
	}

	public OpticsContainer<T> get(int i) {
		return data.get(i);
	}

	public List<?> getList() {
		return data;
	}

	@Override
	public Iterator<OpticsContainer<T>> iterator() {
		return data.iterator();
	}

	public OpticsResult<T> newContainer() {
		return new OpticsResult<T>();
	}

	public void calculateClusters(double threshhold) {
		final OpticsResult<?> clusterOrder = this;
		final int datalength = clusterOrder.size();

		final int[] clusterer = new int[clusterOrder.size()];
		int curindex = 0;
		clusterer[0] = 1;
		for (int i = 1; i < datalength; ++i) {
			if (clusterOrder.get(i).distance > threshhold) {
				clusterer[++curindex] = 0;
			}
			++clusterer[curindex];
		}
		tag(clusterOrder, clusterer);
	}

	private static void tag(OpticsResult<?> clusterOrder, int[] clusterer) {
		int noise = 0;
		int index = 0;
		final int length = clusterer.length;
		for (int i = 0; i < length; ++i) {
			if (clusterer[i] == 1) {
				noise++;
				clusterOrder.get(index++).tag = NOISE_TAG;
			} else {
				final int count = clusterer[i];
				for (int j = 0; j < count; ++j)
					clusterOrder.get(index++).tag = i - noise;
			}
		}
	}

}
