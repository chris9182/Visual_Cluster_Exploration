package clusterproject.program.Consensus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import clusterproject.data.PointContainer;
import la.matrix.DenseMatrix;
import la.matrix.Matrix;
import la.matrix.SparseMatrix;
import ml.recovery.MatrixCompletion;

//CURRENTLY NOT WORKING
@Deprecated
public class CoAssociationMatrixWithCompletion implements IConsensusFunction {
	public static final double UPPERBOUND = 0.5;
	public static final double LOWERBOUND = 0.5;
	private static final String name = "CA-Average Link with Completion";

	@Override
	public PointContainer calculateConsensus(List<PointContainer> results, List<Double> weights) {
		if (results == null || results.isEmpty())
			return null;
		final Set<double[]> allpoints = new HashSet<double[]>();
		for (final PointContainer container : results)
			allpoints.addAll(container.getPoints());
		final List<double[]> points = new ArrayList<double[]>(allpoints);
		final int pointCount = points.size();

		double[][] coAssociationMatrix = CoAssociationMatrix.buildMatrix(results, weights, points, pointCount);
		for (int i = 0; i < pointCount; ++i)
			coAssociationMatrix[i][i] = 1;

		for (int i = 0; i < pointCount; ++i)
			for (int j = 0; j < i; ++j) {
				coAssociationMatrix[i][j] = coAssociationMatrix[j][i];
			}

		// XXX needs LAML
		final SparseMatrix smat = new SparseMatrix(pointCount, pointCount);
		final MatrixCompletion completion = new MatrixCompletion();
		final Matrix m = new DenseMatrix(coAssociationMatrix);
		// final double[][] indices=new
		// double[coAssociationMatrix.length][coAssociationMatrix[0].length];
		for (int i = 0; i < coAssociationMatrix.length; ++i)
			for (int j = 0; j < coAssociationMatrix[0].length; ++j)
				if (coAssociationMatrix[i][j] > LOWERBOUND && coAssociationMatrix[i][j] < UPPERBOUND)
					smat.setEntry(i, j, 0);
				else
					smat.setEntry(i, j, 1);
		// indices[i][j]=1;
		// else
		// indices[i][j]=0;

		completion.feedData(m);
		// completion.feedIndices(new DenseMatrix(indices));
		completion.feedIndices(smat);
		// XXX: this uses the LAML library but seems to run forever at the moment?
		completion.run();
		final Matrix result = completion.GetLowRankEstimation();
		System.err.println(result);
		coAssociationMatrix = result.getData();
		return CoAssociationMatrixAverageLink.link(results, pointCount, points, coAssociationMatrix,
				CoAssociationMatrixAverageLink.threshhold);
	}

//XXX ??????
	@Override
	public boolean supportsClusterNumber() {
		return false;
	}

	@Override
	public PointContainer calculateConsensus(List<PointContainer> results, List<Double> weights, int clusterNumber) {
		throw new UnsupportedOperationException(
				"calculation with cluster number is not supported by this consensus function");
	}

	@Override
	public String getName() {
		return name;
	}
}