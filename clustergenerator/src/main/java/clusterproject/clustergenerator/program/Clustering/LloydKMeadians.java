package clusterproject.clustergenerator.program.Clustering;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import clusterproject.clustergenerator.data.NumberVectorClusteringResult;
import clusterproject.clustergenerator.program.Clustering.Panel.KMeansOptions;
import clusterproject.clustergenerator.program.Clustering.Parameters.Parameter;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.KMediansLloyd;
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

public class LloydKMeadians implements IClusterer {
	private static final long serialVersionUID = -5466140815704959353L;

	private transient KMeansOptions optionsPanel = new KMeansOptions();
	private int minK;
	private int maxK;

	@Override
	public JPanel getOptionsPanel() {
		return optionsPanel;
	}

	@Override
	public String getName() {
		return "LloydKMeadians";
	}

	@Override
	public List<NumberVectorClusteringResult> cluster(Database db, JProgressBar progress) {
		final List<NumberVectorClusteringResult> clusterings = new ArrayList<NumberVectorClusteringResult>();
		final Relation<NumberVector> rel = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);

		if (optionsPanel != null) {
			minK = optionsPanel.getLBK();
			maxK = optionsPanel.getUBK();
		}

		for (int i = minK; i <= maxK; ++i) {
			final int calcK = i;

			final ListParameterization params = new ListParameterization();
			params.addParameter(KMediansLloyd.K_ID, calcK);
			final KMediansLloyd<DoubleVector> dbscan = ClassGenericsUtil.parameterizeOrAbort(KMediansLloyd.class,
					params);
			final Clustering<MeanModel> result = dbscan.run(db);
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

			synchronized (progress) {
				progress.setValue(progress.getValue() + 1);
			}
		}
		return clusterings;
	}

	@Override
	public IClusterer duplicate() {
		return new LloydKMeadians();
	}

	@Override
	public String getSettingsString() {
		if (optionsPanel != null) {
			minK = optionsPanel.getLBK();
			maxK = optionsPanel.getUBK();
		}
		return "k:{LB:" + minK + " UB:" + maxK + "} ";
	}

	@Override
	public int getCount() {
		if (optionsPanel != null) {
			minK = optionsPanel.getLBK();
			maxK = optionsPanel.getUBK();
		}
		return maxK - minK;
	}
}
