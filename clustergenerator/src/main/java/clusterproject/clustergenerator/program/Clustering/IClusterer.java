package clusterproject.clustergenerator.program.Clustering;

import java.io.Serializable;
import java.util.List;

import javax.swing.JPanel;

import clusterproject.clustergenerator.data.NumberVectorClusteringResult;
import de.lmu.ifi.dbs.elki.database.Database;

public interface IClusterer extends Serializable {
	JPanel getOptionsPanel();

	String getName();

	String getSettingsString();

	List<NumberVectorClusteringResult> cluster(Database db);

	IClusterer duplicate();
}
