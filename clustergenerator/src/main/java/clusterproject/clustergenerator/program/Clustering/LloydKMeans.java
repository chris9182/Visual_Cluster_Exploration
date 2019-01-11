package clusterproject.clustergenerator.program.Clustering;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JPanel;

import clusterproject.clustergenerator.data.NumberVectorClusteringResult;
import clusterproject.clustergenerator.program.Clustering.Panel.LloydKMeansOptions;
import clusterproject.clustergenerator.program.Clustering.Parameters.Parameter;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.parallel.ParallelLloydKMeans;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.model.KMeansModel;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;

public class LloydKMeans implements IClusterer {
	private static final long serialVersionUID = -5466140815704959353L;

	private transient LloydKMeansOptions optionsPanel = new LloydKMeansOptions();
	private int minK;
	private int minKBound;
	private int samples;

	@Override
	public JPanel getOptionsPanel() {
		return optionsPanel;
	}

	@Override
	public String getName() {
		return "LloydKMeans";
	}

	@Override
	public List<NumberVectorClusteringResult> cluster(Database db) {
		final List<NumberVectorClusteringResult> clusterings = new ArrayList<NumberVectorClusteringResult>();
		final Relation<NumberVector> rel = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);

		if (optionsPanel != null) {
			minK = optionsPanel.getLBK();
			minKBound = optionsPanel.getUBK();
			samples = optionsPanel.getNSamples();
		}

		for (int i = 0; i < samples; ++i) {
			final Random r = new Random();
			final int calcK = r.nextInt((minKBound - minK) + 1) + minK;

			final ListParameterization params = new ListParameterization();
			params.addParameter(ParallelLloydKMeans.K_ID, calcK);
			final ParallelLloydKMeans<DoubleVector> dbscan = ClassGenericsUtil
					.parameterizeOrAbort(ParallelLloydKMeans.class, params);
			final Clustering<KMeansModel> result = dbscan.run(db);
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
			param.addParameter("k", calcK);
			clusterings.add(new NumberVectorClusteringResult(clustersArr, param));
		}
		return clusterings;
	}

	@Override
	public IClusterer duplicate() {
		return new LloydKMeans();
	}

	@Override
	public String getSettingsString() {
		if (optionsPanel != null) {
			minK = optionsPanel.getLBK();
			minKBound = optionsPanel.getUBK();
			samples = optionsPanel.getNSamples();
		}
		return "k:{LB:" + minK + " UB:" + minKBound + "} " + " Samples{" + samples + "}";
	}
}
