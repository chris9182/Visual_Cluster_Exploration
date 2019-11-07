package clusterproject.program.MetaClustering;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OpticsResult<T> implements Iterable<OpticsContainer<T>> {
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

}
