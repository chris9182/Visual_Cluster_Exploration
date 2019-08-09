package clusterproject.program.Clustering;

import java.util.Random;

import javax.swing.JProgressBar;

public abstract class AbstractClustering implements IClusterer {
	private static final long serialVersionUID = -4628728026725356270L;
	protected Random random;
	protected JProgressBar progressBar;

	@Override
	public void setRandom(Random random) {
		this.random = random;
	}

	@Override
	public void setJProgressBar(JProgressBar progressBar) {
		this.progressBar = progressBar;
	}

	public void addProgress(int i) {
		if (progressBar != null)
			synchronized (progressBar) {
				progressBar.setValue(progressBar.getValue() + i);
			}

	}

}
