package clusterproject.program.Clustering;

import java.util.List;

import clusterproject.data.NumberVectorClusteringResult;
import de.lmu.ifi.dbs.elki.database.Database;

public interface IELKIClustering extends IClusterer {
	List<NumberVectorClusteringResult> cluster(Database db) throws InterruptedException;
}
