package clusterproject.clustergenerator.program.ClusteringResultsViewer;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import clusterproject.clustergenerator.Util;
import clusterproject.clustergenerator.data.ClusteringResult;
import clusterproject.clustergenerator.data.PointContainer;
import clusterproject.clustergenerator.program.ClusterWorkflow;
import clusterproject.clustergenerator.program.IClickHandler;
import clusterproject.clustergenerator.program.MainWindow;
import clusterproject.clustergenerator.program.ClusterViewerElement.ScatterPlot;
import clusterproject.clustergenerator.program.ClusterViewerElement.ScatterPlotMatrix;
import clusterproject.clustergenerator.program.MetaClustering.ClusteringWithDistance;
import clusterproject.clustergenerator.program.MetaClustering.DistanceCalculation;
import clusterproject.clustergenerator.program.MetaClustering.HungarianAlgorithm;
import clusterproject.clustergenerator.program.MetaClustering.IDistanceMeasure;
import clusterproject.clustergenerator.program.MetaClustering.OpticsMetaClustering;
import smile.mds.MDS;

public class ClusteringViewer extends JFrame implements IClickHandler {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static final int OUTER_SPACE = 20;
	public static final int VIEWER_SPACE = 4;

	private final List<ClusteringResult> clusterings;
	private final List<ScatterPlot> viewers;
	private ScatterPlot visibleViewer;

	private final JComboBox<String> clustereringSelector;
	private int currentClustering = -1;
	private final JLayeredPane mainPanel;
	private final SpringLayout layout;
	private final IDistanceMeasure metaDistance;
	private final OpticsPlot oPlot;
	private final double[][] distanceMatrix;
	private final ScatterPlot mdsPlot;
	private int highlighted = -1;

	private int minPTS = 1;
	private double eps = 2;// TODO settings

	private final JButton scatterMatrixButton;

	private final JButton filterButton;

	private JButton saveButton;

	private JButton mainWindowButton;

	private HeatMap heatMap;

	public ClusteringViewer(List<ClusteringResult> sClusterings, IDistanceMeasure metaDistance, int minPTS,
			double eps) {
		getContentPane().setBackground(MainWindow.BACKGROUND_COLOR);
		this.minPTS = minPTS;
		this.eps = eps;
		this.metaDistance = metaDistance;
		this.clusterings = sClusterings;
		mainPanel = new JLayeredPane();
		layout = new SpringLayout();
		mainPanel.setLayout(layout);
		add(mainPanel);
		viewers = new ArrayList<ScatterPlot>();
		for (final ClusteringResult clustering : sClusterings) {
			final PointContainer container = clustering.toPointContainer();
			container.setHeaders(clustering.getHeaders());
			final ScatterPlot plot = new ScatterPlot(null, container, true);
			plot.addAutoAdjust();
			plot.addAutoColor();
			viewers.add(plot);
		}

		final List<String> clusteringIDs = new ArrayList<String>();
		int i = 0;
		for (final ClusteringResult result : sClusterings) {
			clusteringIDs.add(i++ + ": " + result.getParameter().getInfoString());
		}

		String[] idArr = new String[clusteringIDs.size()];
		idArr = clusteringIDs.toArray(idArr);
		clustereringSelector = new JComboBox<>(idArr);

		layout.putConstraint(SpringLayout.NORTH, clustereringSelector, OUTER_SPACE, SpringLayout.NORTH, mainPanel);
		layout.putConstraint(SpringLayout.WEST, clustereringSelector, OUTER_SPACE, SpringLayout.WEST, mainPanel);
		mainPanel.add(clustereringSelector, new Integer(1));

		mainWindowButton = new JButton("Show in Main");
		mainWindowButton.addActionListener(e -> {
			final MainWindow newWindow = new MainWindow(visibleViewer.getPointContainer());
			newWindow.setSize(new Dimension(1000, 800));
			newWindow.setLocationRelativeTo(null);
			newWindow.setVisible(true);
			newWindow.update();
		});
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, mainWindowButton, 0, SpringLayout.VERTICAL_CENTER,
				clustereringSelector);
		layout.putConstraint(SpringLayout.WEST, mainWindowButton, MainWindow.INNER_SPACE, SpringLayout.EAST,
				clustereringSelector);
		mainPanel.add(mainWindowButton, new Integer(1));

		filterButton = new JButton("Filter");
		filterButton.addActionListener(e -> {
			final FilterWindow fw = new FilterWindow(sClusterings, this);// XXX debug
			fw.setSize(new Dimension(800, 600));
			fw.setLocationRelativeTo(null);
			fw.setVisible(true);
		});

		layout.putConstraint(SpringLayout.VERTICAL_CENTER, filterButton, 0, SpringLayout.VERTICAL_CENTER,
				mainWindowButton);
		layout.putConstraint(SpringLayout.WEST, filterButton, MainWindow.INNER_SPACE, SpringLayout.EAST,
				mainWindowButton);
		mainPanel.add(filterButton, new Integer(1));

		scatterMatrixButton = new JButton("Matrix");
		scatterMatrixButton.addActionListener(e -> {
			final ScatterPlotMatrix ms = new ScatterPlotMatrix(visibleViewer.getPointContainer());
			ms.setSize(new Dimension(800, 600));
			ms.setExtendedState(JFrame.MAXIMIZED_BOTH);
			ms.setLocationRelativeTo(null);
			ms.setVisible(true);
		});
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, scatterMatrixButton, 0, SpringLayout.VERTICAL_CENTER,
				filterButton);
		layout.putConstraint(SpringLayout.WEST, scatterMatrixButton, MainWindow.INNER_SPACE, SpringLayout.EAST,
				filterButton);
		mainPanel.add(scatterMatrixButton, new Integer(1));

		saveButton = new JButton("Save");
		saveButton.addActionListener(e -> {
			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.addChoosableFileFilter(ClusterWorkflow.crffilter);
			fileChooser.setApproveButtonText("Save");
			fileChooser.setFileFilter(ClusterWorkflow.crffilter);
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

				if (ClusterWorkflow.crffilter.accept(selectedFile))
					saveCRFFile(selectedFile);
				else if (!selectedFile.getName().contains(".")) {
					saveCRFFile(new File(selectedFile.getPath() + "." + ClusterWorkflow.crffilter.getExtensions()[0]));
				} else {
					return;
				}
				chooserFrame.setVisible(false);
				chooserFrame.dispose();
			});
		});
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, saveButton, 0, SpringLayout.VERTICAL_CENTER,
				scatterMatrixButton);
		layout.putConstraint(SpringLayout.WEST, saveButton, MainWindow.INNER_SPACE, SpringLayout.EAST,
				scatterMatrixButton);
		mainPanel.add(saveButton, new Integer(1));

		final JLabel distLabel = new JLabel("Measure: " + metaDistance.getName());
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, distLabel, 0, SpringLayout.VERTICAL_CENTER,
				scatterMatrixButton);
		layout.putConstraint(SpringLayout.WEST, distLabel, OUTER_SPACE, SpringLayout.EAST, saveButton);
		mainPanel.add(distLabel, new Integer(1));

		distanceMatrix = DistanceCalculation.calculateDistanceMatrix(sClusterings, metaDistance);

		final MDS mds = new MDS(distanceMatrix, 2);
		final double[][] coords = mds.getCoordinates();
		final PointContainer mdsContainer = new PointContainer(coords[0].length);
		mdsContainer.addPoints(coords);
		mdsPlot = new ScatterPlot(null, mdsContainer, true);
		mdsPlot.addAutoAdjust();
		mdsPlot.autoAdjust();
		layout.putConstraint(SpringLayout.NORTH, mdsPlot, VIEWER_SPACE, SpringLayout.SOUTH, clustereringSelector);
		layout.putConstraint(SpringLayout.WEST, mdsPlot, VIEWER_SPACE, SpringLayout.HORIZONTAL_CENTER, mainPanel);
		layout.putConstraint(SpringLayout.SOUTH, mdsPlot, -VIEWER_SPACE, SpringLayout.VERTICAL_CENTER, mainPanel);
		layout.putConstraint(SpringLayout.EAST, mdsPlot, -VIEWER_SPACE, SpringLayout.EAST, mainPanel);
		mainPanel.add(mdsPlot, new Integer(9));

		final JLabel mdsLabel = new JLabel("MDS Plot");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, mdsLabel, 0, SpringLayout.VERTICAL_CENTER,
				clustereringSelector);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, mdsLabel, 0, SpringLayout.HORIZONTAL_CENTER, mdsPlot);
		mainPanel.add(mdsLabel, new Integer(11));

		final OpticsMetaClustering optics = new OpticsMetaClustering(sClusterings, distanceMatrix, minPTS, eps);
		final List<ClusteringWithDistance> list = optics.runOptics();
		oPlot = new OpticsPlot(this, list);
		layout.putConstraint(SpringLayout.NORTH, oPlot, VIEWER_SPACE, SpringLayout.VERTICAL_CENTER, mainPanel);
		layout.putConstraint(SpringLayout.WEST, oPlot, VIEWER_SPACE, SpringLayout.HORIZONTAL_CENTER, mainPanel);
		layout.putConstraint(SpringLayout.SOUTH, oPlot, -VIEWER_SPACE, SpringLayout.SOUTH, mainPanel);
		layout.putConstraint(SpringLayout.EAST, oPlot, -VIEWER_SPACE, SpringLayout.EAST, mainPanel);
		mainPanel.add(oPlot, new Integer(10));

		mdsPlot.setClickHandler(this);

		heatMap = new HeatMap(Util.getSortedDistances(list, distanceMatrix), this, list);
		layout.putConstraint(SpringLayout.NORTH, heatMap, VIEWER_SPACE, SpringLayout.VERTICAL_CENTER, mainPanel);
		layout.putConstraint(SpringLayout.EAST, heatMap, -VIEWER_SPACE, SpringLayout.HORIZONTAL_CENTER, mainPanel);
		layout.putConstraint(SpringLayout.SOUTH, heatMap, -VIEWER_SPACE, SpringLayout.SOUTH, mainPanel);
		layout.putConstraint(SpringLayout.WEST, heatMap, VIEWER_SPACE, SpringLayout.WEST, mainPanel);
		mainPanel.add(heatMap, new Integer(10));

		clustereringSelector.addActionListener(e -> {
			final String selected = (String) clustereringSelector.getSelectedItem();
			final int selection = Integer.parseInt(selected.split(":")[0]);
			highlight(selection);
		});
		showViewer(0);

	}

	private void saveCRFFile(File selectedFile) {
		try {
			final FileOutputStream fileOut = new FileOutputStream(selectedFile);
			final ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(clusterings);
			out.close();
			fileOut.close();
		} catch (final IOException i) {
			i.printStackTrace();
		}

	}

	public void updateMDSPlot(List<ClusteringWithDistance> list) {
		final Integer[] clusterIDs = new Integer[list.size()];
		for (final ClusteringWithDistance clustering : list)
			clusterIDs[clustering.inIndex] = clustering.tag;
		mdsPlot.getPointContainer().setClusterIDs(new ArrayList<Integer>(Arrays.asList(clusterIDs)));
		SwingUtilities.invokeLater(() -> mdsPlot.repaint());
	}

	public void showViewer(int i) {
		clustereringSelector.setSelectedIndex(i);
		final ScatterPlot newViewer = viewers.get(i);
		if (i == highlighted)
			return;
		highlighted = i;
		if (visibleViewer != null) {
			final List<Integer> newClusterIDs = getNewColors(i);
			newViewer.getPointContainer().setClusterIDs(newClusterIDs);
			newViewer.setSelectedDimX(visibleViewer.getSelectedDimX());
			newViewer.setSelectedDimY(visibleViewer.getSelectedDimY());
			newViewer.setIntervalX(visibleViewer.getIntervalX());
			newViewer.setIntervalY(visibleViewer.getIntervalY());
			mainPanel.remove(visibleViewer);
			layout.removeLayoutComponent(visibleViewer);
			visibleViewer = null;
		}
		currentClustering = i;
		visibleViewer = viewers.get(i);
		layout.putConstraint(SpringLayout.NORTH, visibleViewer, VIEWER_SPACE, SpringLayout.SOUTH, clustereringSelector);
		layout.putConstraint(SpringLayout.SOUTH, visibleViewer, -VIEWER_SPACE, SpringLayout.VERTICAL_CENTER, mainPanel);
		layout.putConstraint(SpringLayout.WEST, visibleViewer, VIEWER_SPACE, SpringLayout.WEST, mainPanel);
		layout.putConstraint(SpringLayout.EAST, visibleViewer, -VIEWER_SPACE, SpringLayout.HORIZONTAL_CENTER,
				mainPanel);
		mainPanel.add(visibleViewer, new Integer(2));
		visibleViewer.setVisible(false);

		SwingUtilities.invokeLater(() -> {
			revalidate();
			visibleViewer.setVisible(true);
			visibleViewer.repaint();

			oPlot.repaint();
			mdsPlot.repaint();
		});

	}

	private List<Integer> getNewColors(int i) { // TODO: maybe something with cluster size for color selection?
		final ClusteringResult oldClustering = clusterings.get(currentClustering);
		final ClusteringResult newClustering = clusterings.get(i);
		final Map<Integer, Integer> oldIDMap = visibleViewer.getPointContainer().getIDMap();
		final List<Integer> currentIDs = viewers.get(i).getPointContainer().getOriginalClusterIDs();
		final int matrixSize = oldClustering.getData().length > newClustering.getData().length
				? oldClustering.getData().length
				: newClustering.getData().length;
		final int[][] confusion = new int[matrixSize][matrixSize];
		for (int idx = 0; idx < oldClustering.getData().length; ++idx)
			for (int j = 0; j < newClustering.getData().length; ++j) {
				try {
					confusion[idx][j] = -Util.intersection(oldClustering.getData()[idx],
							newClustering.getData()[j]).length;
				} catch (final ArrayIndexOutOfBoundsException e) {
					confusion[idx][j] = 0;
				}

			}
		final HungarianAlgorithm hungarian = new HungarianAlgorithm(confusion);
		final int[][] assignment = hungarian.findOptimalAssignment();
		final List<Integer> newIDs = new ArrayList<Integer>();

		final Map<Integer, Integer> idMap = new HashMap<Integer, Integer>();

		for (int idx = 0; idx < matrixSize; ++idx) {
			if (oldIDMap != null) {
				if (oldIDMap.get(assignment[idx][1]) == null)
					oldIDMap.put(assignment[idx][1], getNextFree(oldIDMap));
				idMap.put(assignment[idx][0], oldIDMap.get(assignment[idx][1]));
			} else
				idMap.put(assignment[idx][0], assignment[idx][1]);
		}
		viewers.get(i).getPointContainer().setIDMap(idMap);
		for (int idx = 0; idx < currentIDs.size(); ++idx) {
			newIDs.add(idMap.get(currentIDs.get(idx)));
		}
		return newIDs;
	}

	private Integer getNextFree(Map<Integer, Integer> map) {
		int i = 0;
		final HashSet<Integer> vals = new HashSet<Integer>(map.values());
		while (vals.contains(i))
			i++;
		return i;
	}

	public void highlight(int i) {
		mdsPlot.getPointContainer().setHighlighted(i);
		showViewer(i);
	}

	public int getHighlighted() {
		return highlighted;
	}

	public int getClosestPoint(double[] point) {
		double distance = Double.MAX_VALUE;
		int closest = -1;
		final List<double[]> points = mdsPlot.getPointContainer().getPoints();
		final int x = mdsPlot.getSelectedDimX();
		final int y = mdsPlot.getSelectedDimY();
		for (int i = 0; i < points.size(); ++i) {
			final double offsetx = (points.get(i)[x] - point[x]);
			final double offsety = (points.get(i)[y] - point[y]);
			final double curDistance = offsetx * offsetx + offsety * offsety;
			if (curDistance < distance) {
				distance = curDistance;
				closest = i;
			}
		}

		return closest;
	}

	public IDistanceMeasure getDistanceMeasure() {
		return metaDistance;
	}

	@Override
	public void handleClick(double[] point) {
		final int closest = getClosestPoint(point);
		if (closest != -1)
			highlight(closest);

	}

}
