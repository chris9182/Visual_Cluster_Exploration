package clusterproject.clustergenerator.userInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
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
	private File selectedFile;
	private PointContainer pointContainer;
	MainWindow update;

	public ImporterWindow(PointContainer pointContainer, MainWindow update) {
		setTitle("Import");
		setType(Type.UTILITY);
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
		Reader in = null;
		try {
			if (selectedFile == null)
				return false;

			in = new FileReader(selectedFile);
			final CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT.withFirstRecordAsHeader());
			final int size = parser.getHeaderMap().size();
			final List<String> headers = new ArrayList<String>();
			parser.getHeaderMap().keySet().forEach(t -> headers.add(t));

			final int newDim = headers.size();
			if (newDim != pointContainer.getDim() && !replacePoints()) {
				return false;// TODO set error
			}

			if (replacePoints())
				pointContainer.empty();

			pointContainer.setHeaders(headers);
			final Iterable<CSVRecord> records = parser;
			for (final CSVRecord record : records) {
				final double[] point = new double[size];

				for (int i = 0; i < size; ++i)
					try {
						point[i] = format.parse(record.get(i)).doubleValue();
					} catch (final Exception e) {
						point[i] = Double.NaN;
					}
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

	public File getFile() {
		return selectedFile;
	}

	public boolean replacePoints() {
		return !addBox.isSelected();
	}

}
