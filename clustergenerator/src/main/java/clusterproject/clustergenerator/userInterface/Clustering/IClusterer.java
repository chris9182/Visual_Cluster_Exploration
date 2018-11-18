package clusterproject.clustergenerator.userInterface.Clustering;

import java.util.List;

import javax.swing.JPanel;

import clusterproject.clustergenerator.data.ClusteringResult;
import de.lmu.ifi.dbs.elki.database.Database;

public interface IClusterer {

	JPanel getOptionsPanel();

	String getName();

	String getSettingsString();

	List<ClusteringResult> cluster(Database db);

	IClusterer duplicate();

}
