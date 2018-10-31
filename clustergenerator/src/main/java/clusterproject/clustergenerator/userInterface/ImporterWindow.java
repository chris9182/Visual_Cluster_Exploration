package clusterproject.clustergenerator.userInterface;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import clusterproject.clustergenerator.data.PointContainer;

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
		this.update = update;
		this.pointContainer = pointContainer;
		final JPanel thisPanel = new JPanel();
		add(thisPanel);
		final BoxLayout layout = new BoxLayout(thisPanel, BoxLayout.Y_AXIS);
		thisPanel.setLayout(layout);
		fileChooser = new JFileChooser();
		fileChooser.setBorder(null);
		fileChooser.setFileFilter(new FileFilter() {

			@Override
			public String getDescription() {
				return "CSV File";
			}

			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(".csv");
			}
		});
		thisPanel.add(fileChooser);
		fileChooser.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals(JFileChooser.CANCEL_SELECTION)) {
					setVisible(false);
					dispose();
					return;
				}
				selectedFile = fileChooser.getSelectedFile();
				if (selectedFile == null)
					return;

				importFile();
			}
		});
		addBox = new JCheckBox("Add");
		thisPanel.add(addBox);

	}

	protected boolean importFile() {
		final NumberFormat format = NumberFormat.getInstance();
		Reader in = null;
		try {
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
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				update.repaint();

			}
		});
		setVisible(false);
		dispose();
		// TODO: check container.getPoints().size()<1 and do something
		return true;

	}

	public File getFile() {
		return selectedFile;
	}

	public boolean replacePoints() {
		return !addBox.isSelected();
	}

}
