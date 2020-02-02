package clusterproject.program.Clustering;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JPanel;

import clusterproject.data.NumberVectorClusteringResult;
import clusterproject.program.Clustering.Panel.KMeansOptions;
import clusterproject.program.Clustering.Parameters.Parameter;
import de.lmu.ifi.dbs.elki.algorithm.clustering.em.EM;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.KMeans;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.model.MeanModel;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;

public class EMClustering extends AbstractClustering implements IELKIClustering {
	private static final long serialVersionUID = -5466140815704959353L;

	private transient KMeansOptions optionsPanel = new KMeansOptions();
	private int minK;
	private int maxK;
	private int samplesEach;

	@Override
	public JPanel getOptionsPanel() {
		return optionsPanel;
	}

	@Override
	public String getName() {
		return "EM";
	}

	@Override
	public List<NumberVectorClusteringResult> cluster(Database db) throws InterruptedException {
		final List<NumberVectorClusteringResult> clusterings = new ArrayList<NumberVectorClusteringResult>(getCount());
		final Relation<DoubleVector> rel = db.getRelation(TypeUtil.DOUBLE_VECTOR_FIELD);

		prepareSettings();
		if (random == null)
			random = new Random();

		for (int s = 0; s < samplesEach; ++s)
			for (int i = minK; i <= maxK; ++i) {
				if (Thread.interrupted())
					throw new InterruptedException();
				final int calcK = i;

				final ListParameterization params = new ListParameterization();
				params.addParameter(EM.Parameterizer.K_ID, calcK);
				params.addParameter(KMeans.SEED_ID, random.nextInt());
				final EM<DoubleVector, MeanModel> em = ClassGenericsUtil.parameterizeOrAbort(EM.class, params);

//				final DBIDs ids = DBIDUtil.randomSample(rel.getDBIDs(), (double) 1,
//						new RandomFactory(random.nextLong()));
				// example for subsample need to update distance measures (meta)
//				final Relation<DoubleVector> rel2 = new ProxyView<DoubleVector>(ids, rel);
				final Clustering<MeanModel> result = em.run(db);// , rel2);
				// int size = 0;
				// for (final Cluster<KMeansModel> cluster : result.getAllClusters()) {
				// size += cluster.size();
				// }
				// System.err.println(size);
				final List<NumberVector[]> clusterList = new ArrayList<NumberVector[]>();
				result.getAllClusters().forEach(cluster -> {
					final List<NumberVector> pointList = new ArrayList<NumberVector>();

					for (final DBIDIter it = cluster.getIDs().iter(); it.valid(); it.advance()) {
						pointList.add(rel.get(it));// rel2
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
				addProgress(1);
			}

		return clusterings;
	}

	@Override
	public IClusterer duplicate() {
		return new EMClustering();
	}

	@Override
	public String getSettingsString() {
		prepareSettings();
		return "k{LB:" + minK + " UB:" + maxK + "} Samples each{" + samplesEach + "}";
	}

	@Override
	public int getCount() {
		prepareSettings();
		return (maxK - minK) * samplesEach;
	}

	private void prepareSettings() {
		if (optionsPanel == null)
			return;

		minK = optionsPanel.getLBK();
		maxK = optionsPanel.getUBK();
		samplesEach = optionsPanel.getSamplesEach();
	}
}
