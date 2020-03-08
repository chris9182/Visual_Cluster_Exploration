package other.dm.ensemble;

import java.util.BitSet;
import java.util.Collection;

//from https://www.cs.umb.edu/~smimarog/diclens/
//modified
public class MajorityVoter {
	public static BitSet[] voteForMajority(final Collection<? extends Collection<BitSet>> metaClusters,
			final int numOfObjects) {
		final int numOfMetaClusters = metaClusters.size();
		final double[][] votes = new double[numOfMetaClusters][numOfObjects];
		int metaClusterIx = 0;
		for (final Collection<BitSet> metaCluster : metaClusters) {
			for (final BitSet cluster : metaCluster) {
				for (int objIx = cluster.nextSetBit(0); objIx > -1; objIx = cluster.nextSetBit(objIx + 1)) {
					final double[] array = votes[metaClusterIx];
					final int n = objIx;
					++array[n];
				}
			}
			++metaClusterIx;
		}
		final BitSet[] finalClusters = new BitSet[numOfMetaClusters];
		for (int i = 0; i < numOfMetaClusters; ++i) {
			finalClusters[i] = new BitSet();
		}
		for (int columnIx = 0; columnIx < numOfObjects; ++columnIx) {
			double maxVote = -1.0;
			int maxVotedCluster = 0;
			for (int rowIx = 0; rowIx < numOfMetaClusters; ++rowIx) {
				if (votes[rowIx][columnIx] > maxVote) {
					maxVote = votes[rowIx][columnIx];
					maxVotedCluster = rowIx;
				}
			}
			finalClusters[maxVotedCluster].set(columnIx);
		}
		final BitSet[] trimmedClusters = removeEmptyClusters(finalClusters);
		return trimmedClusters;
	}

	private static BitSet[] removeEmptyClusters(final BitSet[] finalClusters) {
		final boolean[] clusterIsNotEmpty = new boolean[finalClusters.length];
		int notEmptyClustersCount = 0;
		for (int i = 0; i < finalClusters.length; ++i) {
			if (finalClusters[i].cardinality() != 0) {
				clusterIsNotEmpty[i] = true;
				++notEmptyClustersCount;
			}
		}
		final BitSet[] trimmedClusters = new BitSet[notEmptyClustersCount];
		int count = 0;
		for (int j = 0; j < finalClusters.length; ++j) {
			if (clusterIsNotEmpty[j]) {
				trimmedClusters[count] = finalClusters[j];
				++count;
			}
		}
		return trimmedClusters;
	}
}
