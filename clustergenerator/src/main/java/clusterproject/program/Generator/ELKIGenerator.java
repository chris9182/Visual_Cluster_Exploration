package clusterproject.program.Generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.SortedSet;
import java.util.regex.Pattern;

import javax.swing.JPanel;

import clusterproject.data.PointContainer;
import clusterproject.program.Generator.Panel.ELKIOptions;
import de.lmu.ifi.dbs.elki.data.ClassLabel;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.ids.ArrayModifiableDBIDs;
import de.lmu.ifi.dbs.elki.database.ids.DBIDArrayMIter;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.GeneratorXMLDatabaseConnection;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.DatabaseUtil;
import de.lmu.ifi.dbs.elki.utilities.datastructures.arraylike.ArrayLikeUtil;
import de.lmu.ifi.dbs.elki.utilities.exceptions.AbortException;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;

public class ELKIGenerator implements IGenerator {

	private final ELKIOptions optionsPanel;

	public ELKIGenerator() {
		optionsPanel = new ELKIOptions();
	}

	@Override
	public JPanel getOptionsPanel() {
		return optionsPanel;
	}

	@Override
	public String getName() {
		return "ELKIGenerator";
	}

	@Override
	public boolean canSimpleGenerate() {
		return true;
	}

	@Override
	public boolean generate(PointContainer container) {
		boolean debug = false;
		final String tempInName = "temp.xml";
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(tempInName, "UTF-8");
			writer.print("<!DOCTYPE dataset PUBLIC \"GeneratorByModel.dtd\" \"GeneratorByModel.dtd\">\r\n"
					+ optionsPanel.getTemplate());

		} catch (final FileNotFoundException e) {
			e.printStackTrace();

		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			if (writer != null)
				writer.close();
		}

		final ListParameterization params = new ListParameterization();
		params.addParameter(GeneratorXMLDatabaseConnection.Parameterizer.CONFIGFILE_ID, tempInName);

		final GeneratorXMLDatabaseConnection con = ClassGenericsUtil
				.parameterizeOrAbort(GeneratorXMLDatabaseConnection.class, params);

		final Database db = new StaticArrayDatabase(con, null);
		try {
			db.initialize();
		} catch (final AbortException e) {
			// TODO: handle exception
			final File file1 = new File(tempInName);
			if (!debug)
				file1.delete();
			return false;
		}

		final Relation<NumberVector> rel = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);

		final int newDim = ArrayLikeUtil.toPrimitiveDoubleArray(rel.get(rel.getDBIDs().iter())).length;
		if (newDim != container.getDim() && !optionsPanel.replacePoints()) {
			final File file1 = new File(tempInName);
			if (!debug)
				file1.delete();
			return false;// TODO set error
		}

		if (optionsPanel.replacePoints()) {
			container.empty();
		}
		SortedSet<ClassLabel> labels = DatabaseUtil.getClassLabels(db);

		int cid = 0;
		if (optionsPanel.replacePoints())
			container.setUpClusters();
		for (ClassLabel lbl : labels) {
			ArrayModifiableDBIDs amdbids = DatabaseUtil.getObjectsByLabelMatch(db, Pattern.compile(lbl.toString()));
			for (DBIDArrayMIter dbamiter = amdbids.iter(); dbamiter.valid(); dbamiter.advance()) {
				final NumberVector v = rel.get(dbamiter);
				container.addPoint(ArrayLikeUtil.toPrimitiveDoubleArray(v));
				if (optionsPanel.replacePoints())
					container.getClusterInformation().addClusterID(cid);
			}
			cid++;
		}
		if (optionsPanel.replacePoints()) {
			container.rebuild();
		}

		final File file1 = new File(tempInName);
		if (!debug)
			file1.delete();
		return true;
	}

	@Override
	public boolean canClickGenerate() {
		return false;
	}

	@Override
	public boolean generate(double[] point, PointContainer container) {
		return false;
	}

}
