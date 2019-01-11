package clusterproject.clustergenerator.program;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import clusterproject.clustergenerator.data.PointContainer;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class ImporterWindow extends JFrame {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final JFileChooser fileChooser;
	private final JCheckBox addBox;
	private final JFormattedTextField labelIndexField;
	private File selectedFile;
	private PointContainer pointContainer;
	MainWindow update;

	public ImporterWindow(PointContainer pointContainer, MainWindow update) {
		setTitle("Import");
		this.update = update;
		this.pointContainer = pointContainer;
		final JPanel thisPanel = new JPanel();
		add(thisPanel);
		final BoxLayout layout = new BoxLayout(thisPanel, BoxLayout.Y_AXIS);
		thisPanel.setLayout(layout);
		fileChooser = new JFileChooser();
		final FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV-Files", "csv");
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
		addBox = new JCheckBox("Add");
		thisPanel.add(addBox);
		final JPanel indexPanel = new JPanel();
		indexPanel.setLayout(new BoxLayout(indexPanel, BoxLayout.X_AXIS));
		final NumberFormat integerFieldFormatter = NumberFormat.getIntegerInstance();
		labelIndexField = new JFormattedTextField(integerFieldFormatter);
		labelIndexField.setValue(-1);
		labelIndexField.setHorizontalAlignment(JTextField.RIGHT);
		final JLabel indexLabel = new JLabel("Index of Cluster Labels");
		indexPanel.add(indexLabel);
		indexPanel.add(labelIndexField);
		thisPanel.add(indexPanel);
		thisPanel.add(Box.createVerticalStrut(0));

	}

	private boolean importARFFFile() {
		Reader in = null;
		if (selectedFile == null)
			return false;
		try {
			in = new FileReader(selectedFile);
			final ArffReader reader = new ArffReader(in);
			final Instances data = reader.getData();

			final int size = data.numInstances();
			final List<String> headers = new ArrayList<String>();
			for (int i = 0; i < data.numAttributes(); i++) {
				headers.add(data.attribute(i).name());
			}

			final int newDim = headers.size();
			if (newDim != pointContainer.getDim() && !replacePoints()) {
				return false;// TODO set error
			}

			if (replacePoints())
				pointContainer.empty();

			pointContainer.setHeaders(headers);
			for (int i = 0; i < data.numInstances(); i++) {
				final double[] point = data.instance(i).toDoubleArray();
				pointContainer.addPoint(point);
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
		pointContainer.rebuild();
		update.update();
		SwingUtilities.invokeLater(() -> update.repaint());
		setVisible(false);
		dispose();
		return true;

	}

	private boolean importCSVFile() {
		final NumberFormat format = NumberFormat.getInstance();

		int labelIndex = -1;
		try {
			labelIndex = Integer.parseInt(labelIndexField.getText());
		} catch (final Exception e) {
		}
		Reader in = null;
		try {
			if (selectedFile == null)
				return false;

			in = new FileReader(selectedFile);
			final CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT.withFirstRecordAsHeader());

			final int size = parser.getHeaderMap().size();
			final List<String> headers = new ArrayList<String>();
			boolean hasHeaders = false;
			for (final String string : parser.getHeaderMap().keySet())
				try {
					format.parse(string).doubleValue();
				} catch (final Exception e) {
					hasHeaders = true;
				}
			if (hasHeaders)
				parser.getHeaderMap().keySet().forEach(t -> headers.add(t));

			final int newDim = headers.size();
			if (newDim != pointContainer.getDim() && !replacePoints()) {
				parser.close();
				return false;// TODO set error
			}

			if (replacePoints())
				pointContainer.empty();

			pointContainer.setHeaders(headers);
			final List<CSVRecord> records = parser.getRecords();
			if (!hasHeaders) {
				double[] point = null;
				if (labelIndex > -1)
					point = new double[size - 1];
				else
					point = new double[size];
				int i = 0;
				for (final String string : parser.getHeaderMap().keySet()) {
					if (i == labelIndex) {
						i++;
						continue;
					}
					final int index = i > labelIndex && labelIndex > -1 ? i - 1 : i;
					try {
						point[index] = format.parse(string).doubleValue();
					} catch (final Exception e) {
					}
					++i;
				}
				pointContainer.addPoint(point);
			}
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
					final int index = i > labelIndex && labelIndex > -1 ? i - 1 : i;
					try {
						point[index] = format.parse(record.get(i)).doubleValue();
					} catch (final Exception e) {
						point[index] = Double.NaN;
					}
				}
				pointContainer.addPoint(point);
			}
			if (labelIndex > -1) {
				pointContainer.setUpClusters();
				if (!hasHeaders) {
					int i = 0;
					for (final String string : parser.getHeaderMap().keySet()) {
						if (i == labelIndex)
							try {
								pointContainer.addClusterID((int) format.parse(string).doubleValue());
							} catch (final Exception e) {
								pointContainer.addClusterID(-1);
							}
						++i;
					}
				}
				for (final CSVRecord record : records) {
					try {
						pointContainer.addClusterID((int) format.parse(record.get(labelIndex)).doubleValue());
					} catch (final Exception e) {
						pointContainer.addClusterID(-1);
					}
				}
			}
			parser.close();
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
		pointContainer.rebuild();
		update.update();
		SwingUtilities.invokeLater(() -> update.repaint());
		setVisible(false);
		dispose();
		return true;

	}

	public File getFile() {
		return selectedFile;
	}

	public boolean replacePoints() {
		return !addBox.isSelected();
	}

}
