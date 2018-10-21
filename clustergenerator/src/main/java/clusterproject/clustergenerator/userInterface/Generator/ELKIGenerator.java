package clusterproject.clustergenerator.userInterface.Generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.swing.JPanel;

import clusterproject.clustergenerator.data.PointContainer;
import de.lmu.ifi.dbs.elki.application.GeneratorXMLSpec;
import de.lmu.ifi.dbs.elki.data.ClassLabel;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.FileBasedDatabaseConnection;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;

public class ELKIGenerator implements IGenerator {

	JPanel optionsPanel = new JPanel();

	@Override
	public JPanel getPanel() {
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
		final String tempOutName = "out.txt";
		final File oldFile = new File(tempOutName);
		oldFile.delete();
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(tempInName, "UTF-8");
			writer.print("<!DOCTYPE dataset PUBLIC \"GeneratorByModel.dtd\" \"GeneratorByModel.dtd\">\r\n"
					+ "<dataset random-seed=\"5\">\r\n" + "  <cluster name=\"Cluster1\" size=\"50\">\r\n"
					+ "    <normal mean=\"0.1\" stddev=\"0.02\" />\r\n"
					+ "    <normal mean=\"0.1\" stddev=\"0.02\" />\r\n" + "  </cluster>\r\n"
					+ "  <cluster name=\"Cluster2\" size=\"50\">\r\n"
					+ "    <normal mean=\"0.28\" stddev=\"0.08\" />\r\n"
					+ "    <normal mean=\"0.28\" stddev=\"0.08\" />\r\n" + "  </cluster>\r\n"
					+ "  <cluster name=\"Cluster3\" size=\"50\">\r\n"
					+ "    <normal mean=\"0.65\" stddev=\"0.13\" />\r\n"
					+ "    <normal mean=\"0.65\" stddev=\"0.13\" />\r\n" + "  </cluster>\r\n" + "</dataset>");

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

		GeneratorXMLSpec.runCLIApplication(GeneratorXMLSpec.class,
				new String[] { "-bymodel.spec", tempInName, "-app.out", tempOutName, });

		final File dataFile = new File(tempOutName);
		final ListParameterization params = new ListParameterization();
		params.addParameter(FileBasedDatabaseConnection.Parameterizer.INPUT_ID, dataFile);
		final Database db = ClassGenericsUtil.parameterizeOrAbort(StaticArrayDatabase.class, params);

		db.initialize();
		// Check the relation has the expected size:

		final Relation<NumberVector> rel = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);
		final Relation<ClassLabel> rel2 = db.getRelation(TypeUtil.CLASSLABEL);
		final DBIDIter it2 = rel2.getDBIDs().iter();
		for (final DBIDIter it = rel.getDBIDs().iter(); it.valid(); it.advance()) {
			it2.advance();
			// To get the vector use:
			final NumberVector v = rel.get(it);
			// v.toArray();
			System.err.println(v.toString());
		}

		final File file1 = new File(tempInName);
		file1.delete();

		// dataFile.delete();
		return false;
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
