package clusterproject.clustergenerator.userInterface.Clustering;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import clusterproject.clustergenerator.data.ClusteringResult;
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

	DBScanOptions optionsPanel = new DBScanOptions();

	@Override
	public JPanel getOptionsPanel() {
		return optionsPanel;
	}

	@Override
	public String getName() {
		return "DBScan";
	}

	@Override
	public List<ClusteringResult> cluster(Database db) {
		final List<ClusteringResult> clusterings = new ArrayList<ClusteringResult>();
		final Relation<NumberVector> rel = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);

		double eps = optionsPanel.getLBEps();
		final double epsStep = optionsPanel.getStepEps();
		final double epsBound = optionsPanel.getUBEps();

		int minPTS;
		final int minPTSStep = optionsPanel.getStepMinPTS();
		final int minPTSBound = optionsPanel.getUBMinPTS();

		while (eps <= epsBound) {
			minPTS = optionsPanel.getLBMinPTS();
			while (minPTS <= minPTSBound) {
				final ListParameterization params = new ListParameterization();
				params.addParameter(DBSCAN.Parameterizer.EPSILON_ID, eps);
				params.addParameter(DBSCAN.Parameterizer.MINPTS_ID, minPTS);
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
				clusterings.add(new ClusteringResult(clustersArr, "minPTS:" + minPTS + " Epsilon:" + eps));
				minPTS += minPTSStep;
			}
			eps += epsStep;
		}

		if (clusterings.size() == 0) {
			final ListParameterization params0 = new ListParameterization();
			params0.addParameter(DBSCAN.Parameterizer.EPSILON_ID, optionsPanel.getLBEps());
			params0.addParameter(DBSCAN.Parameterizer.MINPTS_ID, optionsPanel.getLBMinPTS());
			final DBSCAN<DoubleVector> dbscan = ClassGenericsUtil.parameterizeOrAbort(DBSCAN.class, params0);
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
			clusterings.add(new ClusteringResult(clustersArr,
					"minPTS:" + optionsPanel.getLBMinPTS() + " Epsilon:" + optionsPanel.getLBEps()));
		}
		return clusterings;
	}

	@Override
	public IClusterer duplicate() {
		return new DBScan();
	}

	@Override
	public String getSettingsString() {
		final double eps = optionsPanel.getLBEps();
		final double epsStep = optionsPanel.getStepEps();
		final double epsBound = optionsPanel.getUBEps();
		final int minPTS = optionsPanel.getLBMinPTS();
		final int minPTSStep = optionsPanel.getStepMinPTS();
		final int minPTSBound = optionsPanel.getUBMinPTS();
		return "minPTS{LB:" + minPTS + " Step:" + minPTSStep + " UB:" + minPTSBound + "} " + "Epsilon{LB:" + eps
				+ " Step:" + epsStep + " UB:" + epsBound + "}";
	}

}
