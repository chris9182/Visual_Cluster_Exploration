package clusterproject.program.Clustering;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import clusterproject.data.NumberVectorClusteringResult;
import de.lmu.ifi.dbs.elki.database.Database;

public interface IClusterer extends Serializable {
	JPanel getOptionsPanel();

	String getName();

	String getSettingsString();

	List<NumberVectorClusteringResult> cluster(Database db, JProgressBar progressBar);

	IClusterer duplicate();

	int getCount();

	void setRandom(Random random);
}
