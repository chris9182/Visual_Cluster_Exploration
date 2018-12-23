package clusterproject.clustergenerator.userInterface.Clustering;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import clusterproject.clustergenerator.data.NumberVectorClusteringResult;
import clusterproject.clustergenerator.userInterface.Clustering.Panel.DiSHOptions;
import clusterproject.clustergenerator.userInterface.Clustering.Parameters.Parameter;
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
import scala.collection.mutable.BitSet;

public class DiSHClustering implements IClusterer {
	private static final long serialVersionUID = -7172931268458883217L;

	private transient DiSHOptions optionsPanel = new DiSHOptions();
	private double eps;
	private double epsStep;
	private double epsBound;
	private int Mu;
	private int MuStep;
	private int MuBound;

	@Override
	public JPanel getOptionsPanel() {
		return optionsPanel;
	}

	@Override
	public String getName() {
		return "DiSH";
	}

	@Override
	public List<NumberVectorClusteringResult> cluster(Database db) {
		final List<NumberVectorClusteringResult> clusterings = new ArrayList<NumberVectorClusteringResult>();
		final Relation<NumberVector> rel = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);
		if (optionsPanel != null) {
			eps = optionsPanel.getLBEps();
			epsStep = optionsPanel.getStepEps();
			epsBound = optionsPanel.getUBEps();
			Mu = optionsPanel.getLBMu();
			MuStep = optionsPanel.getStepMu();
			MuBound = optionsPanel.getUBMu();
		}
		double calcEps = eps;
		do {
			int calcMu = Mu;
			do {
				final ListParameterization params = new ListParameterization();
				params.addParameter(DiSH.Parameterizer.EPSILON_ID, calcEps);
				params.addParameter(DiSH.Parameterizer.MU_ID, calcMu);
				final DiSH<DoubleVector> dbscan = ClassGenericsUtil.parameterizeOrAbort(DiSH.class, params);
				final Clustering<SubspaceModel> result = dbscan.run(db);
				final List<NumberVector[]> clusterList = new ArrayList<NumberVector[]>();

				result.getAllClusters().forEach(cluster -> {// what about the hierarchy?
					// XXX debug
					// import scala.collection.mutable.BitSet;
					final BitSet bits = new BitSet(cluster.getModel().getDimensions());
					// System.err.println(bits);
					// XXX debug end

					final List<NumberVector> pointList = new ArrayList<NumberVector>();

					for (final DBIDIter it = cluster.getIDs().iter(); it.valid(); it.advance()) {
						pointList.add(rel.get(it));
					}
					NumberVector[] clusterArr = new NumberVector[pointList.size()];
					clusterArr = pointList.toArray(clusterArr);
					clusterList.add(clusterArr);
				});
				NumberVector[][] clustersArr = new NumberVector[clusterList.size()][];
				clustersArr = clusterList.toArray(clustersArr);
				final Parameter param = new Parameter(getName());
				param.addParameter("Mu", calcMu);
				param.addParameter("Epsilon", calcEps);
				clusterings.add(new NumberVectorClusteringResult(clustersArr, param));
				calcMu += MuStep;
			} while (calcMu < MuBound);
			calcEps += epsStep;
		} while (calcEps < epsBound);
		return clusterings;
	}

	@Override
	public IClusterer duplicate() {
		return new DiSHClustering();
	}

	@Override
	public String getSettingsString() {
		if (optionsPanel != null) {
			eps = optionsPanel.getLBEps();
			epsStep = optionsPanel.getStepEps();
			epsBound = optionsPanel.getUBEps();
			Mu = optionsPanel.getLBMu();
			MuStep = optionsPanel.getStepMu();
			MuBound = optionsPanel.getUBMu();
		}
		return "Mu{LB:" + Mu + " Step:" + MuStep + " UB:" + MuBound + "} " + "Epsilon{LB:" + eps + " Step:" + epsStep
				+ " UB:" + epsBound + "}";
	}

}
