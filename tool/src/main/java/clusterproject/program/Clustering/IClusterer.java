package clusterproject.program.Clustering;

import java.io.Serializable;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

public interface IClusterer extends Serializable {
	JPanel getOptionsPanel();

	String getName();

	String getSettingsString();

	IClusterer duplicate();

	int getCount();

	void setRandom(Random random);

	void setJProgressBar(JProgressBar progressBar);
}
