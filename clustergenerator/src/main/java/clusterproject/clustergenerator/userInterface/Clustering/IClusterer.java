package clusterproject.clustergenerator.userInterface.Clustering;

import java.util.List;

import javax.swing.JPanel;

import clusterproject.clustergenerator.data.PointContainer;
import de.lmu.ifi.dbs.elki.data.Clustering;

public interface IClusterer {

	JPanel getOptionsPanel();

	String getName();

	List<Clustering> cluster(PointContainer container);

	IClusterer duplicate();

}
