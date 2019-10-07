package clusterproject.program;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import clusterproject.data.ClusteringResult;
import clusterproject.data.NumberVectorClusteringResult;
import clusterproject.data.PointContainer;
import clusterproject.program.Clustering.DBScan;
import clusterproject.program.Clustering.DiSHClustering;
import clusterproject.program.Clustering.IClusterer;
import clusterproject.program.Clustering.LloydKMeadians;
import clusterproject.program.Clustering.LloydKMeans;
import clusterproject.program.Clustering.MacQueenKMeans;
import clusterproject.program.Clustering.SNN;
import clusterproject.program.Clustering.Parameters.Parameter;
import clusterproject.program.ClusteringResultsViewer.ClusteringViewer;
import clusterproject.program.MetaClustering.ClusteringError;
import clusterproject.program.MetaClustering.IDistanceMeasure;
import clusterproject.program.MetaClustering.VariationOfInformation;
import clusterproject.util.HackedObjectInputStream;
import clusterproject.util.Util;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;

public class ClusterWorkflow extends JFrame {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final int OUTER_SPACE = 20;
	private static final int OPTIONS_WIDTH = 200;
	public static final FileNameExtensionFilter cwffilter = new FileNameExtensionFilter(
			"Clustering Workflow Files (cwf)", "cwf");
	public static final FileNameExtensionFilter crffilter = new FileNameExtensionFilter("Clustering Result Files (crf)",
			"crf");

	private final SpringLayout layout;

	private final PointContainer pointContainer;

	private IClusterer selectedClusterer;

	private final List<IClusterer> clusterers;
	private final JComboBox<String> clustererSelector;
	private final List<IClusterer> workflow;

	private final List<IDistanceMeasure> distances;
	private final JComboBox<String> distanceSelector;
	private final JFormattedTextField minPTSField;
	private final JFormattedTextField epsField;

	private final JLabel wfLabel;

	private final JButton confirmClustererButton;
	private final JButton executeClusterersButton;
	private final JLayeredPane mainPanel;
	private JScrollPane wfScrollPane;
	private final JButton saveButton;
	private JButton loadClusterButton;
	private final JProgressBar progressBar = new JProgressBar(0, 100);
	private final Random seededRandom = new Random();
	private Thread worker;
	private final boolean addGroundTruth = true;

	public ClusterWorkflow(PointContainer container) {
		final NumberFormat integerFieldFormatter = NumberFormat.getIntegerInstance();
		integerFieldFormatter.setGroupingUsed(false);
		minPTSField = new JFormattedTextField(integerFieldFormatter);
		minPTSField.setValue(2);
		minPTSField.setColumns(5);
		minPTSField.setHorizontalAlignment(SwingConstants.RIGHT);
		final NumberFormat doubleFieldFormatter = NumberFormat.getNumberInstance();
		epsField = new JFormattedTextField(doubleFieldFormatter);
		epsField.setValue(new Double(-1.0));
		epsField.setColumns(5);
		epsField.setHorizontalAlignment(SwingConstants.RIGHT);
		progressBar.addChangeListener(e -> progressBar.repaint());
		progressBar.setStringPainted(true);
		progressBar.setString("Waiting");

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

		loadClusterButton = new JButton("Load Result");
		loadClusterButton.addActionListener(e -> {

			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.addChoosableFileFilter(crffilter);
			fileChooser.setFileFilter(crffilter);
			final JFrame chooserFrame = new JFrame();
			chooserFrame.add(fileChooser);
			chooserFrame.setSize(new Dimension(400, 400));
			chooserFrame.setLocationRelativeTo(null);
			chooserFrame.setResizable(false);
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

				if (crffilter.accept(selectedFile)) {
					loadCRFFile(selectedFile);
					chooserFrame.setVisible(false);
					chooserFrame.dispose();
				}

			});
		});
		layout.putConstraint(SpringLayout.SOUTH, loadClusterButton, -OUTER_SPACE, SpringLayout.SOUTH, mainPanel);
		layout.putConstraint(SpringLayout.EAST, loadClusterButton, -OPTIONS_WIDTH - 3 * OUTER_SPACE, SpringLayout.EAST,
				mainPanel);
		mainPanel.add(loadClusterButton, new Integer(2));

		executeClusterersButton = new JButton("Execute Workflow");
		executeClusterersButton.addActionListener(e -> executeWorkflow());
		executeClusterersButton.setVisible(false);
		layout.putConstraint(SpringLayout.SOUTH, executeClusterersButton, -OUTER_SPACE, SpringLayout.SOUTH, mainPanel);
		layout.putConstraint(SpringLayout.EAST, executeClusterersButton, -OPTIONS_WIDTH - 3 * OUTER_SPACE,
				SpringLayout.EAST, mainPanel);
		mainPanel.add(executeClusterersButton, new Integer(1));

		layout.putConstraint(SpringLayout.VERTICAL_CENTER, progressBar, 0, SpringLayout.VERTICAL_CENTER,
				executeClusterersButton);
		layout.putConstraint(SpringLayout.EAST, progressBar, -MainWindow.INNER_SPACE, SpringLayout.WEST,
				executeClusterersButton);
		mainPanel.add(progressBar, new Integer(1));
		progressBar.setVisible(false);

		layout.putConstraint(SpringLayout.SOUTH, distanceSelector, -MainWindow.INNER_SPACE, SpringLayout.NORTH,
				executeClusterersButton);
		layout.putConstraint(SpringLayout.EAST, distanceSelector, 0, SpringLayout.EAST, executeClusterersButton);
		mainPanel.add(distanceSelector, new Integer(1));

		final JLabel minPtsLabel = new JLabel("MinPts:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, minPtsLabel, 0, SpringLayout.VERTICAL_CENTER,
				distanceSelector);
		layout.putConstraint(SpringLayout.WEST, minPtsLabel, OUTER_SPACE, SpringLayout.WEST, mainPanel);
		mainPanel.add(minPtsLabel, new Integer(1));
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, minPTSField, 0, SpringLayout.VERTICAL_CENTER, minPtsLabel);
		layout.putConstraint(SpringLayout.WEST, minPTSField, MainWindow.INNER_SPACE, SpringLayout.EAST, minPtsLabel);
		mainPanel.add(minPTSField, new Integer(1));

		final JLabel epsLabel = new JLabel("Eps:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, epsLabel, 0, SpringLayout.VERTICAL_CENTER,
				executeClusterersButton);
		layout.putConstraint(SpringLayout.WEST, epsLabel, OUTER_SPACE, SpringLayout.WEST, mainPanel);
		mainPanel.add(epsLabel, new Integer(1));
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, epsField, 0, SpringLayout.VERTICAL_CENTER, epsLabel);
		layout.putConstraint(SpringLayout.WEST, epsField, 0, SpringLayout.WEST, minPTSField);
		mainPanel.add(epsField, new Integer(1));

		saveButton = new JButton("Save Wf");
		saveButton.setEnabled(false);
		saveButton.addActionListener(e -> {
			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.addChoosableFileFilter(cwffilter);
			fileChooser.setApproveButtonText("Save");
			fileChooser.setFileFilter(cwffilter);
			final JFrame chooserFrame = new JFrame();
			chooserFrame.add(fileChooser);
			chooserFrame.setSize(new Dimension(400, 400));
			chooserFrame.setLocationRelativeTo(null);
			chooserFrame.setResizable(false);
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

				if (cwffilter.accept(selectedFile))
					saveCWFFile(selectedFile);
				else if (!selectedFile.getName().contains(".")) {
					saveCWFFile(new File(selectedFile.getPath() + "." + cwffilter.getExtensions()[0]));
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

		final JButton loadButton = new JButton("Load Wf");
		loadButton.addActionListener(e -> {
			if (worker != null && worker.isAlive())
				return;
			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.addChoosableFileFilter(cwffilter);
			fileChooser.setFileFilter(cwffilter);
			final JFrame chooserFrame = new JFrame();
			chooserFrame.add(fileChooser);
			chooserFrame.setSize(new Dimension(400, 400));
			chooserFrame.setLocationRelativeTo(null);
			chooserFrame.setResizable(false);
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

				if (cwffilter.accept(selectedFile)) {
					loadCWFFile(selectedFile);
					chooserFrame.setVisible(false);
					chooserFrame.dispose();
				}
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
		if (worker != null && worker.isAlive())
			return;
		try {
			final FileInputStream fileIn = new FileInputStream(selectedFile);
			final ObjectInputStream in = new HackedObjectInputStream(fileIn);
			workflow.addAll((List<IClusterer>) in.readObject());
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

	private void loadCRFFile(File selectedFile) {
		try {
			final FileInputStream fileIn = new FileInputStream(selectedFile);
			final ObjectInputStream in = new HackedObjectInputStream(fileIn);
			final List<ClusteringResult> sClusterings = (List<ClusteringResult>) in.readObject();

			openClusterViewer(sClusterings);
			in.close();
			fileIn.close();
		} catch (final IOException i) {
			i.printStackTrace();
			return;
		} catch (final ClassNotFoundException c) {
			c.printStackTrace();
			return;
		}

	}

	private void addToWorkflow() {
		if (worker != null && worker.isAlive())
			return;
		progressBar.setValue(0);
		progressBar.setString("Waiting");
		workflow.add(selectedClusterer);
		showWorkflow();
		openClustererSettings(selectedClusterer.getName());

	}

	private synchronized void executeWorkflow() {
		if (worker != null && worker.isAlive())
			return;
		if (pointContainer.getPoints().size() < 2) {
			JOptionPane.showMessageDialog(null, "Not enought data points!");
			return;
		}
		progressBar.setValue(0);
		progressBar.setMaximum(1);
		final List<NumberVectorClusteringResult> clusterings = new ArrayList<NumberVectorClusteringResult>();

		final double[][] data = pointContainer.getPoints().toArray(new double[pointContainer.getPoints().size()][]);
		final DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data);
		final Database db = new StaticArrayDatabase(dbc, null);
		db.initialize();
		int maximum = 0;
		for (final IClusterer clusterer : workflow) {
			clusterer.setJProgressBar(progressBar);
			clusterer.setRandom(seededRandom);
			maximum += clusterer.getCount();
		}
		progressBar.setMaximum(maximum);
		// TODO: enable setting seed from outside
		seededRandom.setSeed(System.currentTimeMillis());
//		seededRandom.setSeed(0);
		worker = new Thread(() -> {
			progressBar.setString("Calculating Clusterings");
			for (final IClusterer clusterer : workflow) {
				final List<NumberVectorClusteringResult> results = clusterer.cluster(db);
				clusterings.addAll(results);
			}
			progressBar.setString("Converting Results");
			if (pointContainer.hasClusters()) {
				final Relation<NumberVector> rel = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);

				final List<List<NumberVector>> pointList = new ArrayList<List<NumberVector>>();
				final Set<Integer> clusterIDs = new HashSet<Integer>(
						pointContainer.getClusterInformation().getClusterIDs());
				final int minID = Collections.min(clusterIDs);
				final int size = Collections.max(clusterIDs) + 1 - minID;

				for (int j = 0; j < size; ++j) {
					pointList.add(new ArrayList<NumberVector>());
				}
				int i = 0;
				for (final DBIDIter it = rel.iterDBIDs(); it.valid(); it.advance()) {
					pointList.get(pointContainer.getClusterInformation().getClusterIDs().get(i) - minID)
							.add(rel.get(it));
					i++;
				}
				final List<List<NumberVector>> betterPointList = new ArrayList<List<NumberVector>>();
				for (final List<NumberVector> lNV1 : pointList) {
					if (lNV1.isEmpty())
						continue;
					betterPointList.add(lNV1);
				}
				if (addGroundTruth) {
					final NumberVector[][] clustersArr = new NumberVector[betterPointList.size()][];
					i = 0;
					for (final List<NumberVector> lNV2 : betterPointList) {
						NumberVector[] clusterArr = new NumberVector[lNV2.size()];
						clusterArr = lNV2.toArray(clusterArr);
						clustersArr[i] = clusterArr;
						++i;
					}
					final Parameter param = new Parameter(Util.GROUND_TRUTH);
					clusterings.add(0, new NumberVectorClusteringResult(clustersArr, param));
				}
			}

			final List<ClusteringResult> sClusterings = Util.convertClusterings(clusterings,
					pointContainer.getHeaders());

			double[][] customData = data;
			if (sClusterings.size() > 0) {
				customData = new double[pointContainer.getPoints().size()][];
				// this is (currently) only safe with no bootstaping
				customData = sClusterings.get(0).toPointContainer().getPoints().toArray(customData);
			}
			// customData can now be used as a reference to the points for non elki
			// clustering algorithms

			progressBar.setString("Calculating Meta");
			openClusterViewer(sClusterings);
			progressBar.setString("Done");
		});
		worker.start();

	}

	private void openClusterViewer(List<ClusteringResult> clusterings) {
		final NumberFormat format = NumberFormat.getInstance();
		Number number = null;
		try {
			number = format.parse(epsField.getText());
		} catch (final ParseException e1) {
			e1.printStackTrace();
		}
		double eps = Double.MAX_VALUE;
		int minPTS = Integer.parseInt(minPTSField.getText());
		if (minPTS < 2)
			minPTS = 2;
		if (number != null)
			eps = number.doubleValue() < 0 ? Double.MAX_VALUE : number.doubleValue();
		final ClusteringViewer cv = new ClusteringViewer(clusterings, getDistanceMeasure(), minPTS, eps);
		cv.setMinimumSize(new Dimension(800, 600));
		cv.setExtendedState(Frame.MAXIMIZED_BOTH);
		cv.setLocationRelativeTo(null);
		cv.pack();
		cv.setVisible(true);
	}

	private IDistanceMeasure getDistanceMeasure() {
		for (final IDistanceMeasure dist : distances)
			if (dist.getName().equals(distanceSelector.getSelectedItem()))
				return dist;
		return null;
	}

	private void openClustererSettings(String name) {
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
			loadClusterButton.setVisible(true);
			executeClusterersButton.setVisible(false);
			progressBar.setVisible(false);
			return;
		}
		wfLabel.setVisible(true);
		loadClusterButton.setVisible(false);
		executeClusterersButton.setVisible(true);
		progressBar.setVisible(true);
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
		layout.putConstraint(SpringLayout.SOUTH, wfScrollPane, -OUTER_SPACE, SpringLayout.NORTH, distanceSelector);
		layout.putConstraint(SpringLayout.WEST, wfScrollPane, OUTER_SPACE, SpringLayout.WEST, mainPanel);
		layout.putConstraint(SpringLayout.EAST, wfScrollPane, -2 * OUTER_SPACE - OPTIONS_WIDTH, SpringLayout.EAST,
				mainPanel);

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
		SwingUtilities.invokeLater(() -> {
			revalidate();
			repaint();

		});

	}

	private void removeFromWorkflow(IClusterer clusterer) {
		workflow.remove(clusterer);
		showWorkflow();
	}

	private void initDistances() {
		// TODO make distances and similar into maps
//		final List<IDistanceMeasure> distancesList = new ArrayList<IDistanceMeasure>();
		distances.add(new VariationOfInformation());
		distances.add(new ClusteringError());
	}

	private void initClusterers() {
		clusterers.add(new DBScan());
		clusterers.add(new DiSHClustering());
		clusterers.add(new SNN());

		// clusterers.add(new CLIQUEClustering());//XXX this is bugged
		clusterers.add(new LloydKMeans());
		clusterers.add(new MacQueenKMeans());
		clusterers.add(new LloydKMeadians());
	}

}
