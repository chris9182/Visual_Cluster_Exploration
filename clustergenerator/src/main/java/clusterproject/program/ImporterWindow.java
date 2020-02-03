package clusterproject.program;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import clusterproject.data.PointContainer;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class ImporterWindow extends JFrame {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final JFileChooser fileChooser;
	private final JFormattedTextField labelIndexField;
	private File selectedFile;
	private PointContainer pointContainer;
	DataView update;

	public ImporterWindow(PointContainer pointContainer, DataView update) {
		setTitle("Import");
		this.update = update;
		this.pointContainer = pointContainer;
		final JPanel thisPanel = new JPanel();
		add(thisPanel);
		final BoxLayout layout = new BoxLayout(thisPanel, BoxLayout.Y_AXIS);
		thisPanel.setLayout(layout);
		fileChooser = new JFileChooser();
		final FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV-Files", "csv", "txt");
		fileChooser.addChoosableFileFilter(filter);
		final FileNameExtensionFilter filter2 = new FileNameExtensionFilter("Arff-Files", "arff");
		fileChooser.addChoosableFileFilter(filter2);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setBorder(null);
		thisPanel.add(fileChooser);
		fileChooser.addActionListener(e -> {
			if (e.getActionCommand().equals(JFileChooser.CANCEL_SELECTION)) {
				setVisible(false);
				dispose();
				return;
			}
			selectedFile = fileChooser.getSelectedFile();
			if (selectedFile == null)
				return;

			if (filter.accept(selectedFile))
				importCSVFile();
			else if (filter2.accept(selectedFile))
				importARFFFile();
		});
		final JPanel indexPanel = new JPanel();
		indexPanel.setLayout(new BoxLayout(indexPanel, BoxLayout.X_AXIS));
		final NumberFormat integerFieldFormatter = NumberFormat.getIntegerInstance();
		labelIndexField = new JFormattedTextField(integerFieldFormatter);
		labelIndexField.setValue(-1);
		labelIndexField.setHorizontalAlignment(SwingConstants.RIGHT);
		labelIndexField.addActionListener(e -> {
			try {
				labelIndexField.commitEdit();
			} catch (final ParseException e1) {
				// ntd
			}
			selectedFile = fileChooser.getSelectedFile();
			if (selectedFile == null)
				return;
			if (filter.accept(selectedFile))
				importCSVFile();
			else if (filter2.accept(selectedFile))
				importARFFFile();
		});
		final JLabel indexLabel = new JLabel("Index of Cluster Labels");
		indexPanel.add(indexLabel);
		indexPanel.add(labelIndexField);
		thisPanel.add(indexPanel);
		thisPanel.add(Box.createVerticalStrut(0));

	}

	private boolean importARFFFile() {
		if (selectedFile == null)
			return false;
		Reader in = null;
		try {
			in = new FileReader(selectedFile);
			final ArffReader reader = new ArffReader(in);
			final Instances data = reader.getData();
			int labelIndex = -1;
			try {
				labelIndex = Integer.parseInt(labelIndexField.getText());
			} catch (final Exception e) {
			}
			if (labelIndex == -2)
				labelIndex = data.numAttributes() - 1;

			final List<String> headers = new ArrayList<String>();
			for (int i = 0; i < data.numAttributes(); i++) {
				if (i == labelIndex)
					continue;
				headers.add(data.attribute(i).name());
			}
			pointContainer.empty();

			pointContainer.setHeaders(headers);
			final int size = data.numInstances();

			if (labelIndex > -1) {
				data.setClassIndex(labelIndex);
				pointContainer.setUpClusters();
				for (int i = 0; i < size; i++) {
					int classval = (int) data.instance(i).classValue();
					if (classval < 0)
						classval = -1;
					pointContainer.getClusterInformation().addClusterID(classval);
				}
				for (int i = 0; i < size; i++) {
					final double[] point = data.instance(i).toDoubleArray();
					final double[] addPoint = new double[point.length - 1];
					for (int j = 0; j < point.length; ++j) {
						if (j == labelIndex)
							continue;
						final int index = j >= labelIndex ? j - 1 : j;
						addPoint[index] = point[j];
					}
					pointContainer.addPoint(addPoint);

				}
			} else {
				for (int i = 0; i < size; i++) {
					final double[] point = data.instance(i).toDoubleArray();
					pointContainer.addPoint(point);
				}
			}

		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (final IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}

		}
		pointContainer.rebuild();
		update.update();
		SwingUtilities.invokeLater(() -> update.repaint());
		setVisible(false);
		dispose();
		return true;

	}

	private boolean importCSVFile() {
		if (selectedFile == null)
			return false;
		final NumberFormat format = NumberFormat.getInstance();
		int labelIndex = -1;
		try {
			labelIndex = Integer.parseInt(labelIndexField.getText());
		} catch (final Exception e) {
		}
		Reader in = null;
		try {
			in = new FileReader(selectedFile);
			final CSVParser parser = new CSVParser(in,
					CSVFormat.DEFAULT.withAllowMissingColumnNames().withIgnoreSurroundingSpaces());
			pointContainer.empty();
			final List<CSVRecord> records = parser.getRecords();
			final int size = records.get(0).size();
			if (labelIndex == -2)
				labelIndex = size - 1;

			final boolean hasLabels = labelIndex > -1;
			pointContainer.setDim(hasLabels ? size - 1 : size);
			final CSVRecord firstRecord = records.get(0);
			boolean hasHeaders = false;
			for (int i = 0; i < size; ++i) {
				final String entry = firstRecord.get(i);
				try {
					format.parse(entry).doubleValue();
				} catch (final Exception e) {
					hasHeaders = true;
				}
			}
			if (hasHeaders) {
				final List<String> headers = new ArrayList<String>();
				for (int i = 0; i < size; ++i) {
					if (i == labelIndex)
						continue;
					headers.add(firstRecord.get(i));
				}
				pointContainer.setHeaders(headers);
				records.remove(0);
			} else
				pointContainer.generateHeaders();

			for (final CSVRecord record : records) {
				double[] point = null;
				if (labelIndex > -1)
					point = new double[size - 1];
				else
					point = new double[size];

				for (int i = 0; i < size; ++i) {
					if (i == labelIndex) {
						continue;
					}
					final int index = i > labelIndex && hasLabels ? i - 1 : i;
					try {
						point[index] = format.parse(record.get(i)).doubleValue();
					} catch (final Exception e) {
						point[index] = Double.NaN;
					}
				}
				pointContainer.addPoint(point);
			}
			if (hasLabels) {
				final Map<String, Integer> translation = new HashMap<String, Integer>();
				int minFree = 0;
				pointContainer.setUpClusters();
				for (final CSVRecord record : records) {
					final String val = record.get(labelIndex);
					Integer classVal = translation.get(val);
					if (classVal == null) {
						classVal = minFree++;
						translation.put(val, classVal);
					}
					pointContainer.getClusterInformation().addClusterID(classVal);
				}
			}
			parser.close();
		} catch (

		final FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (final IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		pointContainer.rebuild();
		if (pointContainer.getClusterInformation() != null
				&& pointContainer.getClusterInformation().getNumUniqueIDs() < 2)
			pointContainer.removeClusterInfo();

		update.update();
		SwingUtilities.invokeLater(() -> update.repaint());

		setVisible(false);
		dispose();
		return true;

	}

	public File getFile() {
		return selectedFile;
	}

}
