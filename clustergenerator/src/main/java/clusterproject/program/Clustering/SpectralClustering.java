package clusterproject.program.Clustering;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JPanel;

import clusterproject.data.ClusteringResult;
import clusterproject.program.Clustering.Panel.SpectralOptions;
import clusterproject.program.Clustering.Parameters.Parameter;

public class SpectralClustering extends AbstractClustering implements ISimpleClusterer {
	private static final long serialVersionUID = 8138072550886562661L;
	private transient SpectralOptions optionsPanel = new SpectralOptions();
	private int minK;
	private int maxK;
	private double minSigma;
	private double maxSigma;
	private int samples;

	@Override
	public JPanel getOptionsPanel() {
		return optionsPanel;
	}

	@Override
	public String getName() {
		return "Spectral Clustering";
	}

	@Override
	public String getSettingsString() {
		prepareSettings();
		return "k{LB:" + minK + " UB:" + maxK + "} sigma{LB:" + minSigma + " UB:" + maxSigma + "} Samples{" + samples
				+ "}";
	}

	@Override
	public List<ClusteringResult> cluster(double[][] data, List<String> headers) throws InterruptedException {
		if (random == null)
			random = new Random();
		final List<ClusteringResult> results = new ArrayList<ClusteringResult>();
		final int pointCount = data.length;
		for (int i = 0; i < samples; ++i) {
			if (Thread.interrupted())
				throw new InterruptedException();
			final double calcSigma = minSigma + (maxSigma - minSigma) * random.nextDouble();
			final int calcK = random.nextInt((maxK - minK) + 1) + minK;
			final smile.clustering.SpectralClustering smSpectralClustering = new smile.clustering.SpectralClustering(
					data, calcK, calcSigma);
			final int clusterCount = smSpectralClustering.getNumClusters();
			final int[] labels = smSpectralClustering.getClusterLabel();
			final double[][][] newData = new double[clusterCount][][];
			for (int c = 0; c < clusterCount; ++c) {
				final List<double[]> cluster = new ArrayList<double[]>();
				for (int p = 0; p < pointCount; ++p) {
					if (labels[p] == c)
						cluster.add(data[p]);
				}
				newData[c] = cluster.toArray(new double[cluster.size()][]);
			}
			final Parameter param = new Parameter(getName());
			param.addParameter("k", calcK);
			param.addParameter("sigma", calcSigma);
			results.add(new ClusteringResult(newData, param, headers));
			addProgress(1);
		}
		return results;
	}

	@Override
	public IClusterer duplicate() {
		return new SpectralClustering();
	}

	@Override
	public int getCount() {
		prepareSettings();
		return samples;
	}

	private void prepareSettings() {
		if (optionsPanel == null)
			return;

		minK = optionsPanel.getLBK();
		maxK = optionsPanel.getUBK();
		minSigma = optionsPanel.getLBSigma();
		maxSigma = optionsPanel.getUBSigma();
		samples = optionsPanel.getSamplesEach();
	}
}
