package clusterproject.clustergenerator.userInterface.Generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JPanel;

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
		BufferedReader br = null;
		String line = "";
		final String cvsSplitBy = ",";
		boolean firstLine = true;
		final NumberFormat format = NumberFormat.getInstance();

		try {
			final File selectedFile = optionsPanel.getFile();
			if (selectedFile == null)
				return false;
			if (optionsPanel.replacePoints())
				container.empty();
			br = new BufferedReader(new FileReader(selectedFile));
			while ((line = br.readLine()) != null) {
				line = line.replace("\"", "");
				// use comma as separator
				final String[] lineArray = line.split(cvsSplitBy);
				final double[] point = new double[lineArray.length];
				// TODO: check headers and set
				try {
					for (int i = 0; i < lineArray.length; ++i)
						point[i] = format.parse(lineArray[i]).doubleValue();
				} catch (final Exception e) {
					if (firstLine) {
						container.setHeaders(new ArrayList<String>(Arrays.asList(lineArray)));
					}
					firstLine = false;
					continue;
				}
				firstLine = false;
				container.addPoint(point);
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
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
