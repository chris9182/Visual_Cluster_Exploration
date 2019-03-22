package clusterproject.program.Clustering;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import clusterproject.data.NumberVectorClusteringResult;
import clusterproject.program.Clustering.Panel.SNNOptions;
import clusterproject.program.Clustering.Parameters.Parameter;
import de.lmu.ifi.dbs.elki.algorithm.clustering.SNNClustering;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.model.Model;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.index.preprocessed.snn.SharedNearestNeighborPreprocessor;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;

public class SNN implements IClusterer {
	private static final long serialVersionUID = -5466140815704959353L;

	private transient SNNOptions optionsPanel = new SNNOptions();
	private int eps;
	private int epsBound;
	private int minPTS;
	private int minPTSBound;
	private int snn;
	private int snnBound;
	private int samples;

	@Override
	public JPanel getOptionsPanel() {
		return optionsPanel;
	}

	@Override
	public String getName() {
		return "Shared Nearest Neighbor";
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
			snn = optionsPanel.getLBsnn();
			snnBound = optionsPanel.getUBsnn();
			samples = optionsPanel.getNSamples();
		}

		for (int i = 0; i < samples; ++i) {
			final Random r = new Random();
			final int calcEps = r.nextInt((epsBound - eps) + 1) + eps;
			final int calcMinPTS = r.nextInt((minPTSBound - minPTS) + 1) + minPTS;
			final int calcSNN = r.nextInt((snnBound - snn) + 1) + snn;

			final ListParameterization params = new ListParameterization();
			params.addParameter(SNNClustering.Parameterizer.EPSILON_ID, calcEps);
			params.addParameter(SNNClustering.Parameterizer.MINPTS_ID, calcMinPTS);
			params.addParameter(SharedNearestNeighborPreprocessor.Factory.NUMBER_OF_NEIGHBORS_ID, calcSNN);
			final SNNClustering<DoubleVector> dbscan = ClassGenericsUtil.parameterizeOrAbort(SNNClustering.class,
					params);
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
			param.addParameter("Num. Neighbors", calcSNN);
			clusterings.add(new NumberVectorClusteringResult(clustersArr, param));

			synchronized (progress) {
				progress.setValue(progress.getValue() + 1);
			}
		}
		return clusterings;
	}

	@Override
	public IClusterer duplicate() {
		return new SNN();
	}

	@Override
	public String getSettingsString() {
		if (optionsPanel != null) {
			eps = optionsPanel.getLBEps();
			epsBound = optionsPanel.getUBEps();
			minPTS = optionsPanel.getLBMinPTS();
			minPTSBound = optionsPanel.getUBMinPTS();
			snn = optionsPanel.getLBsnn();
			snnBound = optionsPanel.getUBsnn();
			samples = optionsPanel.getNSamples();
		}
		return "minPTS{LB:" + minPTS + " UB:" + minPTSBound + "} " + "Epsilon{LB:" + eps + " UB:" + epsBound
				+ "Num. Neighbors{LB:" + snn + " UB:" + snnBound + "} " + " Samples{" + samples + "}";
	}

	@Override
	public int getCount() {
		if (optionsPanel != null) {
			eps = optionsPanel.getLBEps();
			epsBound = optionsPanel.getUBEps();
			minPTS = optionsPanel.getLBMinPTS();
			minPTSBound = optionsPanel.getUBMinPTS();
			snn = optionsPanel.getLBsnn();
			snnBound = optionsPanel.getUBsnn();
			samples = optionsPanel.getNSamples();
		}
		return samples;
	}
}
