package clusterproject.clustergenerator.userInterface.Clustering;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import clusterproject.clustergenerator.data.ClusteringResult;
import clusterproject.clustergenerator.userInterface.Clustering.Panel.DiSHOptions;
import de.lmu.ifi.dbs.elki.algorithm.clustering.subspace.DiSH;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.model.SubspaceModel;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;

public class DiSHClustering implements IClusterer {

	DiSHOptions optionsPanel = new DiSHOptions();

	@Override
	public JPanel getOptionsPanel() {
		return optionsPanel;
	}

	@Override
	public String getName() {
		return "DiSH";
	}

	@Override
	public List<ClusteringResult> cluster(Database db) {
		final List<ClusteringResult> clusterings = new ArrayList<ClusteringResult>();
		final Relation<NumberVector> rel = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);

		double eps = optionsPanel.getLBEps();
		final double epsStep = optionsPanel.getStepEps();
		final double epsBound = optionsPanel.getUBEps();

		int Mu;
		final int MuStep = optionsPanel.getStepMu();
		final int MuBound = optionsPanel.getUBMu();

		do {
			Mu = optionsPanel.getLBMu();
			do {
				final ListParameterization params = new ListParameterization();
				params.addParameter(DiSH.Parameterizer.EPSILON_ID, eps);
				params.addParameter(DiSH.Parameterizer.MU_ID, Mu);
				final DiSH<DoubleVector> dbscan = ClassGenericsUtil.parameterizeOrAbort(DiSH.class, params);
				final Clustering<SubspaceModel> result = dbscan.run(db);
				final List<NumberVector[]> clusterList = new ArrayList<NumberVector[]>();
				result.getAllClusters().forEach(cluster -> {
					final List<NumberVector> pointList = new ArrayList<NumberVector>();

					for (final DBIDIter it = cluster.getIDs().iter(); it.valid(); it.advance()) {
						pointList.add(rel.get(it));
						// ArrayLikeUtil.toPrimitiveDoubleArray(v)
					}
					NumberVector[] clusterArr = new NumberVector[pointList.size()];
					clusterArr = pointList.toArray(clusterArr);
					clusterList.add(clusterArr);
				});
				NumberVector[][] clustersArr = new NumberVector[clusterList.size()][];
				clustersArr = clusterList.toArray(clustersArr);
				clusterings.add(new ClusteringResult(clustersArr, getName() + ": Mu:" + Mu + " Epsilon:" + eps));
				Mu += MuStep;
			} while (Mu < MuBound);
			eps += epsStep;
		} while (eps < epsBound);
		return clusterings;
	}

	@Override
	public IClusterer duplicate() {
		return new DiSHClustering();
	}

	@Override
	public String getSettingsString() {
		final double eps = optionsPanel.getLBEps();
		final double epsStep = optionsPanel.getStepEps();
		final double epsBound = optionsPanel.getUBEps();
		final int Mu = optionsPanel.getLBMu();
		final int MuStep = optionsPanel.getStepMu();
		final int MuBound = optionsPanel.getUBMu();
		return "Mu{LB:" + Mu + " Step:" + MuStep + " UB:" + MuBound + "} " + "Epsilon{LB:" + eps + " Step:" + epsStep
				+ " UB:" + epsBound + "}";
	}

}
