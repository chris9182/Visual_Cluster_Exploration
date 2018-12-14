package clusterproject.clustergenerator.userInterface;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import clusterproject.clustergenerator.Util;
import clusterproject.clustergenerator.data.ClusteringResult;
import clusterproject.clustergenerator.data.NumberVectorClusteringResult;
import clusterproject.clustergenerator.data.PointContainer;
import clusterproject.clustergenerator.userInterface.Clustering.CLIQUEClustering;
import clusterproject.clustergenerator.userInterface.Clustering.DBScan;
import clusterproject.clustergenerator.userInterface.Clustering.DiSHClustering;
import clusterproject.clustergenerator.userInterface.Clustering.IClusterer;
import clusterproject.clustergenerator.userInterface.MetaClustering.ClusteringError;
import clusterproject.clustergenerator.userInterface.MetaClustering.IDistanceMeasure;
import clusterproject.clustergenerator.userInterface.MetaClustering.VariationOfInformation;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;

public class ClusterWorkflow extends JFrame {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final int OUTER_SPACE = 20;
	private static final int OPTIONS_WIDTH = 200;
	private static final FileNameExtensionFilter filter = new FileNameExtensionFilter("Clustering Workflow Files (cwf)",
			"cwf");

	private final SpringLayout layout;

	private final PointContainer pointContainer;

	private IClusterer selectedClusterer;

	private final List<IClusterer> clusterers;
	private final JComboBox<String> clustererSelector;
	private List<IClusterer> workflow;

	private final List<IDistanceMeasure> distances;
	private final JComboBox<String> distanceSelector;
	private final JLabel wfLabel;

	private final JButton confirmClustererButton;
	private final JButton executeClusterersButton;
	private final JLayeredPane mainPanel;
	private JScrollPane wfScrollPane;
	private final JButton saveButton;

	public ClusterWorkflow(PointContainer container) {
		pointContainer = container;
		mainPanel = new JLayeredPane();

		add(mainPanel);

		getContentPane().setBackground(MainWindow.BACKGROUND_COLOR);
		clusterers = new ArrayList<IClusterer>();
		distances = new ArrayList<IDistanceMeasure>();
		workflow = new ArrayList<IClusterer>();
		layout = new SpringLayout();
		mainPanel.setLayout(layout);
		final JLabel addLabel = new JLabel("Add");
		mainPanel.add(addLabel, new Integer(1));
		layout.putConstraint(SpringLayout.NORTH, addLabel, OUTER_SPACE, SpringLayout.NORTH, mainPanel);
		layout.putConstraint(SpringLayout.WEST, addLabel, OUTER_SPACE, SpringLayout.WEST, mainPanel);

		initClusterers();
		initDistances();
		final String[] names = new String[clusterers.size()];
		for (int i = 0; i < names.length; ++i)
			names[i] = clusterers.get(i).getName();
		clustererSelector = new JComboBox<>(names);
		mainPanel.add(clustererSelector, new Integer(1));
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, clustererSelector, 0, SpringLayout.VERTICAL_CENTER,
				addLabel);
		layout.putConstraint(SpringLayout.WEST, clustererSelector, 2 * MainWindow.INNER_SPACE, SpringLayout.EAST,
				addLabel);

		clustererSelector.addActionListener(e -> openClustererSettings((String) clustererSelector.getSelectedItem()));

		final String[] names2 = new String[distances.size()];
		for (int i = 0; i < names2.length; ++i)
			names2[i] = distances.get(i).getName();
		distanceSelector = new JComboBox<>(names2);
		// XXX add selector

		wfLabel = new JLabel("Workflow:");

		layout.putConstraint(SpringLayout.NORTH, wfLabel, OUTER_SPACE, SpringLayout.SOUTH, clustererSelector);
		layout.putConstraint(SpringLayout.WEST, wfLabel, OUTER_SPACE, SpringLayout.WEST, mainPanel);
		wfLabel.setVisible(false);
		mainPanel.add(wfLabel, new Integer(1));

		executeClusterersButton = new JButton("Execute Workflow");
		executeClusterersButton.addActionListener(e -> executeWorkflow());
		executeClusterersButton.setVisible(false);
		layout.putConstraint(SpringLayout.SOUTH, executeClusterersButton, -OUTER_SPACE, SpringLayout.SOUTH, mainPanel);
		layout.putConstraint(SpringLayout.EAST, executeClusterersButton, -OPTIONS_WIDTH - 3 * OUTER_SPACE,
				SpringLayout.EAST, mainPanel);
		mainPanel.add(executeClusterersButton, new Integer(1));

		layout.putConstraint(SpringLayout.SOUTH, distanceSelector, -MainWindow.INNER_SPACE, SpringLayout.NORTH,
				executeClusterersButton);
		layout.putConstraint(SpringLayout.EAST, distanceSelector, 0, SpringLayout.EAST, executeClusterersButton);
		distanceSelector.setVisible(false);
		mainPanel.add(distanceSelector, new Integer(1));

		saveButton = new JButton("Save");
		saveButton.setEnabled(false);
		saveButton.addActionListener(e -> {
			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.addChoosableFileFilter(filter);
			fileChooser.setApproveButtonText("Save");
			fileChooser.setFileFilter(filter);
			final JFrame chooserFrame = new JFrame();
			chooserFrame.add(fileChooser);
			chooserFrame.setSize(new Dimension(400, 400));
			chooserFrame.setLocationRelativeTo(null);
			chooserFrame.setVisible(true);

			fileChooser.addActionListener(ev -> {
				if (ev.getActionCommand().equals(JFileChooser.CANCEL_SELECTION)) {
					chooserFrame.setVisible(false);
					chooserFrame.dispose();
					return;
				}
				final File selectedFile = fileChooser.getSelectedFile();
				if (selectedFile == null)
					return;

				if (filter.accept(selectedFile))
					saveCWFFile(selectedFile);
				else if (!selectedFile.getName().contains(".")) {
					saveCWFFile(new File(selectedFile.getPath() + "." + filter.getExtensions()[0]));
				} else {
					return;
				}
				chooserFrame.setVisible(false);
				chooserFrame.dispose();
			});
		});
		layout.putConstraint(SpringLayout.SOUTH, saveButton, -OUTER_SPACE, SpringLayout.SOUTH, mainPanel);
		layout.putConstraint(SpringLayout.WEST, saveButton, (-OPTIONS_WIDTH + MainWindow.INNER_SPACE) / 2 - OUTER_SPACE,
				SpringLayout.EAST, mainPanel);
		layout.putConstraint(SpringLayout.EAST, saveButton, -OUTER_SPACE, SpringLayout.EAST, mainPanel);
		mainPanel.add(saveButton, new Integer(1));

		final JButton loadButton = new JButton("Load");
		loadButton.addActionListener(e -> {
			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.addChoosableFileFilter(filter);
			fileChooser.setFileFilter(filter);
			final JFrame chooserFrame = new JFrame();
			chooserFrame.add(fileChooser);
			chooserFrame.setSize(new Dimension(400, 400));
			chooserFrame.setLocationRelativeTo(null);
			chooserFrame.setVisible(true);

			fileChooser.addActionListener(ev -> {
				if (ev.getActionCommand().equals(JFileChooser.CANCEL_SELECTION)) {
					chooserFrame.setVisible(false);
					chooserFrame.dispose();
					return;
				}
				final File selectedFile = fileChooser.getSelectedFile();
				if (selectedFile == null)
					return;

				if (filter.accept(selectedFile))
					loadCWFFile(selectedFile);
			});
		});
		layout.putConstraint(SpringLayout.SOUTH, loadButton, -OUTER_SPACE, SpringLayout.SOUTH, mainPanel);
		layout.putConstraint(SpringLayout.WEST, loadButton, (-OPTIONS_WIDTH - MainWindow.INNER_SPACE) / 2,
				SpringLayout.WEST, saveButton);
		layout.putConstraint(SpringLayout.EAST, loadButton, -MainWindow.INNER_SPACE, SpringLayout.WEST, saveButton);
		mainPanel.add(loadButton, new Integer(1));

		confirmClustererButton = new JButton("Confirm");
		confirmClustererButton.addActionListener(e -> addToWorkflow());
		layout.putConstraint(SpringLayout.SOUTH, confirmClustererButton, -MainWindow.INNER_SPACE, SpringLayout.NORTH,
				loadButton);
		layout.putConstraint(SpringLayout.WEST, confirmClustererButton, -OPTIONS_WIDTH - OUTER_SPACE, SpringLayout.EAST,
				mainPanel);
		layout.putConstraint(SpringLayout.EAST, confirmClustererButton, -OUTER_SPACE, SpringLayout.EAST, mainPanel);
		mainPanel.add(confirmClustererButton, new Integer(1));
		openClustererSettings(clusterers.get(0).getName());

	}

	private void saveCWFFile(File selectedFile) {
		for (final IClusterer clusterer : workflow)
			clusterer.getSettingsString();
		try {
			final FileOutputStream fileOut = new FileOutputStream(selectedFile);
			final ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(workflow);
			out.close();
			fileOut.close();
		} catch (final IOException i) {
			i.printStackTrace();
		}
	}

	private void loadCWFFile(File selectedFile) {
		try {
			final FileInputStream fileIn = new FileInputStream(selectedFile);
			final ObjectInputStream in = new ObjectInputStream(fileIn);
			workflow = (List<IClusterer>) in.readObject();
			in.close();
			fileIn.close();
		} catch (final IOException i) {
			i.printStackTrace();
			return;
		} catch (final ClassNotFoundException c) {
			c.printStackTrace();
			return;
		}
		showWorkflow();

	}

	private void addToWorkflow() {
		workflow.add(selectedClusterer);
		showWorkflow();
		openClustererSettings(selectedClusterer.getName());

	}

	private void executeWorkflow() {
		final List<NumberVectorClusteringResult> clusterings = new ArrayList<NumberVectorClusteringResult>();
		double[][] data = new double[pointContainer.getPoints().size()][];
		data = pointContainer.getPoints().toArray(data);
		final DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data);
		final Database db = new StaticArrayDatabase(dbc, null);
		db.initialize();
		for (final IClusterer clusterer : workflow) {
			final List<NumberVectorClusteringResult> results = clusterer.cluster(db);
			clusterings.addAll(results);
		}

		final List<ClusteringResult> sClusterings = Util.convertClusterings(clusterings);

		final ClusteringViewer cv = new ClusteringViewer(sClusterings, pointContainer, getDistanceMeasure(), 1,
				Double.MAX_VALUE);// TODO: editable minPTS and eps
		cv.setSize(new Dimension(800, 600));
		cv.setExtendedState(JFrame.MAXIMIZED_BOTH);
		cv.setLocationRelativeTo(null);
		cv.setVisible(true);

	}

	private IDistanceMeasure getDistanceMeasure() {
		for (final IDistanceMeasure dist : distances)
			if (dist.getName().equals(distanceSelector.getSelectedItem()))
				return dist;
		return null;
	}

	private void openClustererSettings(String name) {
		// confirmClustererButton.setVisible(false);
		if (selectedClusterer != null) {
			mainPanel.remove(selectedClusterer.getOptionsPanel());
			selectedClusterer.getOptionsPanel().setVisible(false);
		}
		selectedClusterer = null;
		for (final IClusterer clusterer : clusterers)
			if (clusterer.getName().equals(name))
				selectedClusterer = clusterer.duplicate();
		if (selectedClusterer == null)
			return;
		confirmClustererButton.setVisible(true);
		final JPanel options = selectedClusterer.getOptionsPanel();

		layout.putConstraint(SpringLayout.NORTH, options, OUTER_SPACE, SpringLayout.NORTH, mainPanel);
		layout.putConstraint(SpringLayout.EAST, options, -OUTER_SPACE, SpringLayout.EAST, mainPanel);
		layout.putConstraint(SpringLayout.WEST, options, -OUTER_SPACE - OPTIONS_WIDTH, SpringLayout.EAST, mainPanel);
		layout.putConstraint(SpringLayout.SOUTH, options, -MainWindow.INNER_SPACE, SpringLayout.NORTH,
				confirmClustererButton);

		options.setVisible(true);

		mainPanel.add(options, new Integer(1));
		SwingUtilities.invokeLater(() -> {
			revalidate();
			repaint();
		});

	}

	private void showWorkflow() {
		saveButton.setEnabled(!workflow.isEmpty());
		if (workflow.isEmpty()) {
			mainPanel.remove(wfScrollPane);
			wfLabel.setVisible(false);
			executeClusterersButton.setVisible(false);
			distanceSelector.setVisible(false);
			return;
		}
		wfLabel.setVisible(true);
		executeClusterersButton.setVisible(true);
		distanceSelector.setVisible(true);
		if (wfScrollPane != null)
			mainPanel.remove(wfScrollPane);
		final SpringLayout wfLayout = new SpringLayout();
		final JPanel wfPanel = new JPanel(wfLayout);
		wfScrollPane = new JScrollPane(wfPanel);
		wfScrollPane.setBorder(null);
		wfScrollPane.setOpaque(false);
		wfScrollPane.getViewport().setOpaque(false);
		wfPanel.setOpaque(false);

		layout.putConstraint(SpringLayout.NORTH, wfScrollPane, MainWindow.INNER_SPACE, SpringLayout.SOUTH, wfLabel);
		layout.putConstraint(SpringLayout.SOUTH, wfScrollPane, -OUTER_SPACE, SpringLayout.SOUTH, mainPanel);
		layout.putConstraint(SpringLayout.WEST, wfScrollPane, OUTER_SPACE, SpringLayout.WEST, mainPanel);
		layout.putConstraint(SpringLayout.EAST, wfScrollPane, -2 * OUTER_SPACE - OPTIONS_WIDTH, SpringLayout.EAST,
				mainPanel);

		// layout.putConstraint(SpringLayout.NORTH, wfPanel, 0, SpringLayout.NORTH,
		// wfScrollPane);
		// layout.putConstraint(SpringLayout.SOUTH, wfPanel, 0, SpringLayout.SOUTH,
		// wfScrollPane);
		// layout.putConstraint(SpringLayout.WEST, wfPanel, 0, SpringLayout.WEST,
		// wfScrollPane);
		// layout.putConstraint(SpringLayout.EAST, wfPanel, 0, SpringLayout.EAST,
		// wfScrollPane);

		Component alignment = Box.createVerticalStrut(0);
		wfLayout.putConstraint(SpringLayout.NORTH, alignment, 0, SpringLayout.NORTH, wfPanel);
		wfPanel.add(alignment);
		for (final IClusterer clusterer : workflow) {
			final JButton remove = new JButton("X");
			remove.addActionListener(e -> removeFromWorkflow(clusterer));
			wfPanel.add(remove);
			wfLayout.putConstraint(SpringLayout.NORTH, remove, MainWindow.INNER_SPACE, SpringLayout.SOUTH, alignment);
			wfLayout.putConstraint(SpringLayout.WEST, remove, MainWindow.INNER_SPACE, SpringLayout.WEST, wfPanel);
			final JLabel label = new JLabel(clusterer.getName() + ": " + clusterer.getSettingsString());
			wfLayout.putConstraint(SpringLayout.VERTICAL_CENTER, label, 0, SpringLayout.VERTICAL_CENTER, remove);
			wfLayout.putConstraint(SpringLayout.WEST, label, MainWindow.INNER_SPACE, SpringLayout.EAST, remove);
			wfPanel.add(label);

			alignment = remove;
		}

		wfPanel.setPreferredSize(new Dimension(0, MainWindow.INNER_SPACE
				+ workflow.size() * (MainWindow.INNER_SPACE + executeClusterersButton.getHeight())));
		mainPanel.add(wfScrollPane, new Integer(1));

	}

	private void removeFromWorkflow(IClusterer clusterer) {
		workflow.remove(clusterer);
		showWorkflow();
		SwingUtilities.invokeLater(() -> {
			revalidate();
			repaint();
		});

	}

	private void initDistances() {
		distances.add(new ClusteringError());
		distances.add(new VariationOfInformation());
	}

	private void initClusterers() {
		clusterers.add(new DBScan());
		clusterers.add(new DiSHClustering());
		clusterers.add(new CLIQUEClustering());
	}

}
