package clusterproject.clustergenerator.program.Clustering;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JPanel;

import clusterproject.clustergenerator.data.NumberVectorClusteringResult;
import clusterproject.clustergenerator.program.Clustering.Panel.CLIQUEOptions;
import clusterproject.clustergenerator.program.Clustering.Parameters.Parameter;
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

	private static final long serialVersionUID = -724435821167392129L;

	private transient CLIQUEOptions optionsPanel = new CLIQUEOptions();
	private double tau;
	private double tauBound;
	private int xsi;
	private int xsiBound;
	private int samples;

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

		if (optionsPanel != null) {
			tau = optionsPanel.getLBtau();
			tauBound = optionsPanel.getUBtau();
			xsi = optionsPanel.getLBxsi();
			xsiBound = optionsPanel.getUBxsi();
			samples = optionsPanel.getNSamples();
		}

		for (int i = 0; i < samples; ++i) {
			final Random r = new Random();
			final double calcTau = tau + (tauBound - tau) * r.nextDouble();
			final int calcXsi = r.nextInt((xsiBound - xsi) + 1) + xsi;
			final boolean calcPrune = r.nextInt(2) == 1;
			final ListParameterization params = new ListParameterization();
			params.addParameter(CLIQUE.TAU_ID, calcTau);
			params.addParameter(CLIQUE.XSI_ID, calcXsi);
			params.addParameter(CLIQUE.PRUNE_ID, calcPrune);// TODO: settings
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
			final Parameter param = new Parameter(getName());
			param.addParameter("xsi", calcXsi);
			param.addParameter("tauilon", calcTau);
			param.addParameter("prune", i % 2 == 0);
			clusterings.add(new NumberVectorClusteringResult(clustersArr, param));// TODO:
			// show
			// pruning

		}
		return clusterings;
	}

	@Override
	public IClusterer duplicate() {
		return new CLIQUEClustering();
	}

	@Override
	public String getSettingsString() {
		if (optionsPanel != null) {
			tau = optionsPanel.getLBtau();
			tauBound = optionsPanel.getUBtau();
			xsi = optionsPanel.getLBxsi();
			xsiBound = optionsPanel.getUBxsi();
			samples = optionsPanel.getNSamples();
		}
		return "xsi{LB:" + xsi + " UB:" + xsiBound + "} " + "tau{LB:" + tau + " UB:" + tauBound + " Samples:" + samples
				+ "}";// TODO: show
		// pruning
	}

}
