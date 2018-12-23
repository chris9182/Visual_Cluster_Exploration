package clusterproject.clustergenerator.userInterface.Clustering;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import clusterproject.clustergenerator.data.NumberVectorClusteringResult;
import clusterproject.clustergenerator.userInterface.Clustering.Panel.DBScanOptions;
import clusterproject.clustergenerator.userInterface.Clustering.Parameters.Parameter;
import de.lmu.ifi.dbs.elki.algorithm.clustering.DBSCAN;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.model.Model;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;

public class DBScan implements IClusterer {
	private static final long serialVersionUID = -5466140815704959353L;

	private transient DBScanOptions optionsPanel = new DBScanOptions();
	private double eps;
	private double epsStep;
	private double epsBound;
	private int minPTS;
	private int minPTSStep;
	private int minPTSBound;

	@Override
	public JPanel getOptionsPanel() {
		return optionsPanel;
	}

	@Override
	public String getName() {
		return "DBScan";
	}

	@Override
	public List<NumberVectorClusteringResult> cluster(Database db) {
		final List<NumberVectorClusteringResult> clusterings = new ArrayList<NumberVectorClusteringResult>();
		final Relation<NumberVector> rel = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);

		if (optionsPanel != null) {
			eps = optionsPanel.getLBEps();
			epsStep = optionsPanel.getStepEps();
			epsBound = optionsPanel.getUBEps();
			minPTSStep = optionsPanel.getStepMinPTS();
			minPTSBound = optionsPanel.getUBMinPTS();
			minPTS = optionsPanel.getLBMinPTS();
		}
		double calcEps = eps;
		do {
			int calcMinPTS = minPTS;
			do {
				final ListParameterization params = new ListParameterization();
				params.addParameter(DBSCAN.Parameterizer.EPSILON_ID, calcEps);
				params.addParameter(DBSCAN.Parameterizer.MINPTS_ID, calcMinPTS);
				final DBSCAN<DoubleVector> dbscan = ClassGenericsUtil.parameterizeOrAbort(DBSCAN.class, params);
				final Clustering<Model> result = dbscan.run(db);
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
				param.addParameter("minPTS", calcMinPTS);
				param.addParameter("Epsilon", calcEps);
				clusterings.add(new NumberVectorClusteringResult(clustersArr, param));
				calcMinPTS += minPTSStep;
			} while (calcMinPTS < minPTSBound);
			calcEps += epsStep;
		} while (calcEps < epsBound);
		return clusterings;
	}

	@Override
	public IClusterer duplicate() {
		return new DBScan();
	}

	@Override
	public String getSettingsString() {
		if (optionsPanel != null) {
			eps = optionsPanel.getLBEps();
			epsStep = optionsPanel.getStepEps();
			epsBound = optionsPanel.getUBEps();
			minPTS = optionsPanel.getLBMinPTS();
			minPTSStep = optionsPanel.getStepMinPTS();
			minPTSBound = optionsPanel.getUBMinPTS();
		}
		return "minPTS{LB:" + minPTS + " Step:" + minPTSStep + " UB:" + minPTSBound + "} " + "Epsilon{LB:" + eps
				+ " Step:" + epsStep + " UB:" + epsBound + "}";
	}
}
