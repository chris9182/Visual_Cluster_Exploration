package clusterproject.clustergenerator.userInterface.Generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JPanel;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import clusterproject.clustergenerator.data.PointContainer;
import clusterproject.clustergenerator.userInterface.Generator.Panel.CSVOptions;

public class CSVGenerator implements IGenerator {

	private final CSVOptions optionsPanel = new CSVOptions();

	@Override
	public JPanel getOptionsPanel() {
		return optionsPanel;
	}

	@Override
	public String getName() {
		return "CSVGenerator";
	}

	@Override
	public boolean canSimpleGenerate() {
		return true;
	}

	@Override
	public boolean generate(PointContainer container) {

		final NumberFormat format = NumberFormat.getInstance();
		Reader in = null;
		try {
			final File selectedFile = optionsPanel.getFile();
			if (selectedFile == null)
				return false;

			in = new FileReader(selectedFile);
			final CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT.withFirstRecordAsHeader());
			final int size = parser.getHeaderMap().size();
			final List<String> headers = new ArrayList<String>();
			parser.getHeaderMap().keySet().forEach(new Consumer<String>() {
				@Override
				public void accept(String t) {
					headers.add(t);
				}

			});

			final int newDim = headers.size();
			if (newDim != container.getDim() && !optionsPanel.replacePoints()) {
				return false;// TODO set error
			}

			if (optionsPanel.replacePoints())
				container.empty();

			container.setHeaders(headers);
			final Iterable<CSVRecord> records = parser;
			for (final CSVRecord record : records) {
				final double[] point = new double[size];

				for (int i = 0; i < size; ++i)
					try {
						point[i] = format.parse(record.get(i)).doubleValue();
					} catch (final Exception e) {
						point[i] = Double.NaN;
					}
				container.addPoint(point);
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		container.rebuild();
		// TODO: check container.getPoints().size()<1 and do something
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
