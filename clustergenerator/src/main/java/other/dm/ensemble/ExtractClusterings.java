package other.dm.ensemble;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import other.dm.common.EasyFileReader;

//from https://www.cs.umb.edu/~smimarog/diclens/
//modified
public class ExtractClusterings {
	public static ClusterInfo fromFile(final String filename) {
		final EasyFileReader reader = new EasyFileReader(filename);
		int objectCount = 0;
		final List<BitSet> allClusters = new ArrayList<BitSet>();
		final List<BitSet[]> partitions = new LinkedList<BitSet[]>();
		for (final String line : reader) {
			if (line.trim().length() < 2) {
				continue;
			}
			if (line.charAt(0) == '#') {
				continue;
			}
			final BitSet[] partition = fromString(line);
			objectCount = line.split(",").length;
			partitions.add(partition);
			BitSet[] array;
			for (int length = (array = partition).length, i = 0; i < length; ++i) {
				final BitSet cluster = array[i];
				allClusters.add(cluster);
			}
		}
		final ClusterInfo clusterInfo = new ClusterInfo();
		clusterInfo.numOfObjects = objectCount;
		clusterInfo.partitions = partitions;
		clusterInfo.allClusters = allClusters;
		return clusterInfo;
	}

	public static BitSet[] fromString(final String line) {
		final String[] items = line.split(",");
		final Map<String, BitSet> partition = arrayToBitSets(items);
		partition.remove("N");
		final BitSet[] numericPartition = new BitSet[partition.size()];
		for (final Map.Entry<String, BitSet> entry : partition.entrySet()) {
			numericPartition[Integer.parseInt(entry.getKey()) - 1] = entry.getValue();
		}
		return numericPartition;
	}

	public static ClusterInfo fromArray(final int[][] input) {
		return fromArray(Arrays.asList(input));
	}

	public static ClusterInfo fromArray(final List<int[]> clusterings) {
		int objectCount = 0;
		final List<BitSet> allClusters = new ArrayList<BitSet>();
		final List<BitSet[]> partitions = new LinkedList<BitSet[]>();
		for (final int[] clustering : clusterings) {
			final Integer[] clusteringAsInteger = new Integer[clustering.length];
			for (int i = 0; i < clustering.length; ++i) {
				clusteringAsInteger[i] = clustering[i];
			}
			final Map<Integer, BitSet> clusters = arrayToBitSets(clusteringAsInteger);
			clusters.remove(0);
			objectCount = clustering.length;
			final BitSet[] partitionAsArray = new BitSet[clusters.size()];
			for (final Map.Entry<Integer, BitSet> entry : clusters.entrySet()) {
				final Integer key = entry.getKey() - 1;
				try {
					partitionAsArray[key] = entry.getValue();
				} catch (final ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}
			partitions.add(partitionAsArray);
			BitSet[] array;
			for (int length = (array = partitionAsArray).length, j = 0; j < length; ++j) {
				final BitSet cluster = array[j];
				allClusters.add(cluster);
			}
		}
		final ClusterInfo clusterInfo = new ClusterInfo();
		clusterInfo.numOfObjects = objectCount;
		clusterInfo.partitions = partitions;
		clusterInfo.allClusters = allClusters;
		return clusterInfo;
	}

	private static <T> Map<T, BitSet> arrayToBitSets(final T[] items) {
		final Map<T, BitSet> partition = new HashMap<T, BitSet>();
		for (int objectCount = items.length, i = 0; i < objectCount; ++i) {
			BitSet cluster = partition.get(items[i]);
			if (cluster == null) {
				cluster = new BitSet(objectCount);
				partition.put(items[i], cluster);
			}
			cluster.set(i);
		}
		return partition;
	}

	public static class ClusterInfo {
		public int numOfObjects;
		public List<BitSet[]> partitions;
		public List<BitSet> allClusters;
		private int sizeOfLargestClustering;

		public ClusterInfo() {
			this.sizeOfLargestClustering = 0;
		}

		public int sizeOfLargestClustering() {
			if (this.sizeOfLargestClustering == 0) {
				for (final BitSet[] partition : this.partitions) {
					if (partition.length > this.sizeOfLargestClustering) {
						this.sizeOfLargestClustering = partition.length;
					}
				}
			}
			return this.sizeOfLargestClustering;
		}

		public BitSet[] allClustersAsArray() {
			return this.allClusters.toArray(new BitSet[0]);
		}
	}
}
