package clusterproject.program.Consensus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import clusterproject.data.PointContainer;

public class CoAssociationMatrixThreshhold implements ConsensusFunction {

	private final static double threshhold = 0.5;

	@Override
	public PointContainer calculateConsensus(List<PointContainer> results, List<Double> weights) {
		if (results == null || results.isEmpty())
			return null;
		final Set<double[]> allpoints = new HashSet<double[]>();
		for (final PointContainer container : results)
			allpoints.addAll(container.getPoints());
		final List<double[]> points = new ArrayList<double[]>(allpoints);
		final int pointCount=points.size();

		final double[][] coAssociationMatrix=CoAssociationMatrix.buildMatrix(results, weights,points,pointCount);
		return link(results, pointCount, points, coAssociationMatrix);
	}

	public static PointContainer link(List<PointContainer> results,int pointCount,List<double[]> points,double[][] coAssociationMatrix) {
		final List<Set<double[]>> consensus = new ArrayList<Set<double[]>>();

		for (int i = 0; i < pointCount; ++i) {
			final Set<double[]> set = new HashSet<double[]>();
			set.add(points.get(i));
			consensus.add(set);
		}

		for (int i = 0; i < pointCount; ++i) {
			for (int j = i + 1; j < pointCount; ++j) {
				if (coAssociationMatrix[i][j] > threshhold) {
					int set1 = -1;
					int set2 = -1;
					final double[] point1 = points.get(i);
					final double[] point2 = points.get(j);
					for (int z = 0; z < consensus.size(); ++z) {
						if (consensus.get(z).contains(point1))
							set1 = z;
					}
					for (int z = 0; z < consensus.size(); ++z) {
						if (consensus.get(z).contains(point2))
							set2 = z;
					}
					if (set1 != set2) {
						consensus.get(set1).addAll(consensus.get(set2));
						consensus.remove(set2);
					}
				}
			}
		}

		final PointContainer newContainer = new PointContainer(results.get(0).getDim());
		newContainer.setPoints(points);
		newContainer.setUpClusters();
		final int size = consensus.size();
		for (int i = 0; i < pointCount; ++i) {
			for (int j = 0; j < size; ++j)
				if (consensus.get(j).contains(points.get(i))) {
					newContainer.getClusterInformation().addClusterID(j);
					break;
				}
			// TODO: use hashtable? break here?
		}
		newContainer.setHeaders(results.get(0).getHeaders());
		return newContainer;
	}

}
