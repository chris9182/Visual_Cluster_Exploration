package clusterproject.clustergenerator.userInterface.Generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.swing.JPanel;

import clusterproject.clustergenerator.data.PointContainer;
import clusterproject.clustergenerator.userInterface.Generator.Panel.ELKIOptions;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.GeneratorXMLDatabaseConnection;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;

public class ELKIGenerator implements IGenerator {

	ELKIOptions optionsPanel = new ELKIOptions();

	@Override
	public JPanel getOptionsPanel() {
		// TODO Auto-generated method stub
		return optionsPanel;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "ELKIGenerator";
	}

	@Override
	public boolean canSimpleGenerate() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean generate(PointContainer container) {
		final String tempInName = "test.xml";
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(tempInName, "UTF-8");
			writer.print("<!DOCTYPE dataset PUBLIC \"GeneratorByModel.dtd\" \"GeneratorByModel.dtd\">\r\n"
					+ optionsPanel.getTemplate());

		} catch (final FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (final UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
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
		db.initialize();
		// Check the relation has the expected size:

		final Relation<NumberVector> rel = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);

		if (optionsPanel.replacePoints()) {
			container.empty();
		}

		for (final DBIDIter it = rel.getDBIDs().iter(); it.valid(); it.advance()) {

			// To get the vector use:
			final NumberVector v = rel.get(it);
			container.addPoint(v.toArray());
		}

		if (optionsPanel.replacePoints()) {
			container.rebuild();
		}

		final File file1 = new File(tempInName);
		file1.delete();
		return true;
	}

	@Override
	public boolean canClickGenerate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean generate(double[] point, PointContainer container) {
		// TODO Auto-generated method stub
		return false;
	}

}
