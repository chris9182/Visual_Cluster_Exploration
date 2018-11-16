package clusterproject.clustergenerator.userInterface.Clustering;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import clusterproject.clustergenerator.data.PointContainer;
import de.lmu.ifi.dbs.elki.algorithm.clustering.DBSCAN;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.model.Model;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;

public class DBScan implements IClusterer {

	DBScanOptions optionsPanel = new DBScanOptions();

	@Override
	public JPanel getOptionsPanel() {
		return optionsPanel;
	}

	@Override
	public String getName() {
		return "DBScan";
	}

	@Override
	public List<Clustering> cluster(PointContainer container) {
		double[][] data = new double[container.getPoints().size()][];
		data = container.getPoints().toArray(data);
		final DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data);
		final Database db = new StaticArrayDatabase(dbc, null);

		db.initialize();
		final ListParameterization params = new ListParameterization();
		params.addParameter(DBSCAN.Parameterizer.EPSILON_ID, 20);// XXX set
		params.addParameter(DBSCAN.Parameterizer.MINPTS_ID, 20);

		final DBSCAN<DoubleVector> dbscan = ClassGenericsUtil.parameterizeOrAbort(DBSCAN.class, params);
		final Clustering<Model> result = dbscan.run(db);
		System.err.println(result.getAllClusters().size());

		final List<Clustering> clusterings = new ArrayList<Clustering>();
		clusterings.add(result);

		return clusterings;
	}

	@Override
	public IClusterer duplicate() {
		return new DBScan();
	}

}
