package clusterproject.clustergenerator.userInterface.Clustering;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import clusterproject.clustergenerator.data.NumberVectorClusteringResult;
import clusterproject.clustergenerator.userInterface.Clustering.Panel.CLIQUEOptions;
import de.lmu.ifi.dbs.elki.algorithm.clustering.subspace.CLIQUE;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.model.SubspaceModel;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;

public class CLIQUEClustering implements IClusterer {

	CLIQUEOptions optionsPanel = new CLIQUEOptions();

	@Override
	public JPanel getOptionsPanel() {
		return optionsPanel;
	}

	@Override
	public String getName() {
		return "CLIQUE #BETA#";
	}

	@Override
	public List<NumberVectorClusteringResult> cluster(Database db) {
		final List<NumberVectorClusteringResult> clusterings = new ArrayList<NumberVectorClusteringResult>();
		final Relation<NumberVector> rel = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);

		double tau = optionsPanel.getLBtau();
		final double tauStep = optionsPanel.getSteptau();
		final double tauBound = optionsPanel.getUBtau();

		int xsi;
		final int xsiStep = optionsPanel.getStepxsi();
		final int xsiBound = optionsPanel.getUBxsi();

		for (int i = 0; i < 2; ++i)
			do {
				xsi = optionsPanel.getLBxsi();
				do {
					final ListParameterization params = new ListParameterization();
					params.addParameter(CLIQUE.TAU_ID, tau);
					params.addParameter(CLIQUE.XSI_ID, xsi);
					params.addParameter(CLIQUE.PRUNE_ID, i % 2 == 0);// TODO: settings
					final CLIQUE<NumberVector> clique = ClassGenericsUtil.parameterizeOrAbort(CLIQUE.class, params);
					final Clustering<SubspaceModel> result = clique.run(rel);
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
					clusterings.add(new NumberVectorClusteringResult(clustersArr,
							getName() + ": xsi:" + xsi + " tauilon:" + tau + " prune:" + Boolean.toString(i % 2 == 0)));// TODO:
					// show
					// pruning
					xsi += xsiStep;
				} while (xsi < xsiBound);
				tau += tauStep;
			} while (tau < tauBound);
		return clusterings;
	}

	@Override
	public IClusterer duplicate() {
		return new CLIQUEClustering();
	}

	@Override
	public String getSettingsString() {
		final double tau = optionsPanel.getLBtau();
		final double tauStep = optionsPanel.getSteptau();
		final double tauBound = optionsPanel.getUBtau();
		final int xsi = optionsPanel.getLBxsi();
		final int xsiStep = optionsPanel.getStepxsi();
		final int xsiBound = optionsPanel.getUBxsi();
		return "xsi{LB:" + xsi + " Step:" + xsiStep + " UB:" + xsiBound + "} " + "tau{LB:" + tau + " Step:" + tauStep
				+ " UB:" + tauBound + "}";// TODO: show pruning
	}

}
