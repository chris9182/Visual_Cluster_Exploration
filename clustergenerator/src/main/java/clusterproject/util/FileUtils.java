package clusterproject.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.filechooser.FileNameExtensionFilter;

public final class FileUtils {
	public static final FileNameExtensionFilter csvfilter = new FileNameExtensionFilter(
			"single Clustering Result (csv)", "csv");
	public static final FileNameExtensionFilter cwffilter = new FileNameExtensionFilter(
			"Clustering Workflow Files (cwf)", "cwf");
	public static final FileNameExtensionFilter crffilter = new FileNameExtensionFilter("Clustering Result Files (crf)",
			"crf");

	public static final void saveCSVFileWithClass(File selectedFile, double[][][] data, List<String> headers) {
		try {
			final FileWriter writer = new FileWriter(selectedFile);
			writer.append("Class");
			for (String header : headers) {
				writer.append(" , " + header);
			}
			writer.append("\n");
			for (int i = 0; i < data.length; i++) {
				final int clusterID = i + 1;
				for (int j = 0; j < data[i].length; ++j) {
					writer.append(Integer.toString(clusterID));
					for (int k = 0; k < data[i][j].length; ++k)
						writer.append(" , " + Double.toString(data[i][j][k]));
					writer.append("\n");
				}
			}
			writer.close();
		} catch (final IOException e) {
			return;
		}

	}

	public static final void saveCSVFileWithoutClass(File selectedFile, double[][][] data, List<String> headers) {
		try {
			final FileWriter writer = new FileWriter(selectedFile);

			writer.append(headers.get(0));
			for (int i = 1; i < headers.size(); ++i) {
				writer.append(" , " + headers.get(i));
			}
			writer.append("\n");
			for (int i = 0; i < data.length; i++) {
				for (int j = 0; j < data[i].length; ++j) {
					writer.append(Double.toString(data[i][j][0]));
					for (int k = 1; k < data[i][j].length; ++k)
						writer.append(" , " + Double.toString(data[i][j][k]));
					writer.append("\n");
				}
			}
			writer.close();
		} catch (final IOException e) {
			return;
		}

	}
}
