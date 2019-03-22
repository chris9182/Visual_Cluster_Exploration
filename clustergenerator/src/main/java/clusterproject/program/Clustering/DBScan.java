package clusterproject.program.Clustering;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import clusterproject.data.NumberVectorClusteringResult;
import clusterproject.program.Clustering.Panel.DBScanOptions;
import clusterproject.program.Clustering.Parameters.Parameter;
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
	private double epsBound;
	private int minPTS;
	private int minPTSBound;
	private int samples;

	@Override
	public JPanel getOptionsPanel() {
		return optionsPanel;
	}

	@Override
	public String getName() {
		return "DBScan";
	}

	@Override
	public List<NumberVectorClusteringResult> cluster(Database db, JProgressBar progress) {
		final List<NumberVectorClusteringResult> clusterings = new ArrayList<NumberVectorClusteringResult>();
		final Relation<NumberVector> rel = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);

		if (optionsPanel != null) {
			eps = optionsPanel.getLBEps();
			epsBound = optionsPanel.getUBEps();
			minPTS = optionsPanel.getLBMinPTS();
			minPTSBound = optionsPanel.getUBMinPTS();
			samples = optionsPanel.getNSamples();
		}

		for (int i = 0; i < samples; ++i) {
			final Random r = new Random();
			final double calcEps = eps + (epsBound - eps) * r.nextDouble();
			final int calcMinPTS = r.nextInt((minPTSBound - minPTS) + 1) + minPTS;

			final ListParameterization params = new ListParameterization();
			params.addParameter(DBSCAN.Parameterizer.EPSILON_ID, calcEps);
			params.addParameter(DBSCAN.Parameterizer.MINPTS_ID, calcMinPTS);
			final DBSCAN<DoubleVector> dbscan = ClassGenericsUtil.parameterizeOrAbort(DBSCAN.class, params);
			final Clustering<Model> result = dbscan.run(db);
			final List<NumberVector[]> clusterList = new ArrayList<NumberVector[]>();

			// System.err.println(calcEps + " " + calcMinPTS);
			// System.err.println(result.getAllClusters().get(0).size());
			// System.err.println(result.getAllClusters().get(1).size());
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

			synchronized (progress) {
				progress.setValue(progress.getValue() + 1);
			}
		}
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
			epsBound = optionsPanel.getUBEps();
			minPTS = optionsPanel.getLBMinPTS();
			minPTSBound = optionsPanel.getUBMinPTS();
			samples = optionsPanel.getNSamples();
		}
		return "minPTS{LB:" + minPTS + " UB:" + minPTSBound + "} " + "Epsilon{LB:" + eps + " UB:" + epsBound
				+ " Samples{" + samples + "}";
	}

	@Override
	public int getCount() {
		if (optionsPanel != null) {
			eps = optionsPanel.getLBEps();
			epsBound = optionsPanel.getUBEps();
			minPTS = optionsPanel.getLBMinPTS();
			minPTSBound = optionsPanel.getUBMinPTS();
			samples = optionsPanel.getNSamples();
		}
		return samples;
	}
}