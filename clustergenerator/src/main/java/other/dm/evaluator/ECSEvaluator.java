package other.dm.evaluator;

import java.util.BitSet;

import other.dm.ensemble.ExtractClusterings;

//from https://www.cs.umb.edu/~smimarog/diclens/
//modified
public class ECSEvaluator {
	private int numClusterings;
	private int numObjects;
	private int nClusters;
	private BitSet[] clusters;

	public static double evaluateForPartition(final ECSEvaluator ecs, final BitSet[] partition) {
		double ecsAverage = 0.0;
		for (int i = 0; i < partition.length; ++i) {
			for (int j = i + 1; j < partition.length; ++j) {
				ecsAverage += ecs.evaluate(partition[i], partition[j]);
			}
		}
		ecsAverage /= partition.length * (partition.length - 1) / 2.0;
		return ecsAverage;
	}

	public ECSEvaluator(final int numOfClusterings, final int numOfObjects, final int totalNumberOfClusters,
			final BitSet[] clusters) {
		try {
			this.numClusterings = numOfClusterings;
			this.numObjects = numOfObjects;
			this.clusters = clusters;
			this.nClusters = totalNumberOfClusters;
		} catch (final Exception e) {
			System.out.println(e);
		}
	}

	public ECSEvaluator(final ExtractClusterings.ClusterInfo clusterInfo) {
		this(clusterInfo.partitions.size(), clusterInfo.numOfObjects, clusterInfo.allClustersAsArray().length,
				clusterInfo.allClustersAsArray());
	}

	public double evaluate(final BitSet cluster_1, final BitSet cluster_2) {
		final BitSet dummy = new BitSet(this.numObjects);
		final BitSet c1_OR_c2 = new BitSet(this.numObjects);
		final BitSet c1_AND_c2 = new BitSet(this.numObjects);
		final BitSet c1_ANDCMPL_c2 = new BitSet(this.numObjects);
		final BitSet CMPL_c1_AND_c2 = new BitSet(this.numObjects);
		double sumECS = 0.0;
		if (cluster_1.cardinality() > 0 && cluster_2.cardinality() > 0) {
			c1_OR_c2.xor(c1_OR_c2);
			c1_OR_c2.xor(cluster_1);
			c1_OR_c2.or(cluster_2);
			c1_AND_c2.xor(c1_AND_c2);
			c1_AND_c2.xor(cluster_1);
			c1_AND_c2.and(cluster_2);
			c1_ANDCMPL_c2.xor(c1_ANDCMPL_c2);
			c1_ANDCMPL_c2.xor(cluster_2);
			c1_ANDCMPL_c2.flip(0, this.numObjects);
			c1_ANDCMPL_c2.and(cluster_1);
			CMPL_c1_AND_c2.xor(CMPL_c1_AND_c2);
			CMPL_c1_AND_c2.xor(cluster_1);
			CMPL_c1_AND_c2.flip(0, this.numObjects);
			CMPL_c1_AND_c2.and(cluster_2);
			double ECS = 0.0;
			for (int i = 0; i < this.nClusters; ++i) {
				dummy.xor(dummy);
				dummy.or(c1_OR_c2);
				dummy.and(this.clusters[i]);
				final int count = dummy.cardinality();
				dummy.xor(dummy);
				dummy.or(c1_ANDCMPL_c2);
				dummy.and(this.clusters[i]);
				final int count2 = dummy.cardinality();
				dummy.xor(dummy);
				dummy.or(CMPL_c1_AND_c2);
				dummy.and(this.clusters[i]);
				final int count3 = dummy.cardinality();
				dummy.xor(dummy);
				dummy.or(c1_AND_c2);
				dummy.and(this.clusters[i]);
				final int count4 = dummy.cardinality();
				ECS += count * (count - 1) / 2.0 - count2 * (count2 - 1) / 2.0 - count3 * (count3 - 1) / 2.0
						+ count4 * (count4 - 1) / 2.0;
			}
			ECS += c1_AND_c2.cardinality() * this.numClusterings;
			ECS /= cluster_1.cardinality() * cluster_2.cardinality();
			sumECS += ECS;
		}
		return sumECS;
	}
}
