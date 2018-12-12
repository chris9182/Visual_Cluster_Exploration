package clusterproject.clustergenerator.userInterface;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import clusterproject.clustergenerator.Util;
import clusterproject.clustergenerator.data.ClusteringResult;
import clusterproject.clustergenerator.data.PointContainer;
import clusterproject.clustergenerator.userInterface.MetaClustering.ClusteringWithDistance;
import clusterproject.clustergenerator.userInterface.MetaClustering.DistanceCalculation;
import clusterproject.clustergenerator.userInterface.MetaClustering.HungarianAlgorithm;
import clusterproject.clustergenerator.userInterface.MetaClustering.IDistanceMeasure;
import clusterproject.clustergenerator.userInterface.MetaClustering.OpticsMetaClustering;
import smile.mds.MDS;

public class ClusteringViewer extends JFrame {

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

	public ClusteringViewer(List<ClusteringResult> sClusterings, PointContainer pointContainer,
			IDistanceMeasure metaDistance, int minPTS, double eps) {
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
			container.setHeaders(pointContainer.getHeaders());
			final ScatterPlot plot = new ScatterPlot(null, container, true);
			plot.addAutoAdjust();
			plot.addAutoColor();
			viewers.add(plot);
		}

		final List<String> clusteringIDs = new ArrayList<String>();
		int i = 0;
		for (final ClusteringResult result : sClusterings) {
			clusteringIDs.add(i++ + ": " + result.getDescription());
		}

		String[] idArr = new String[clusteringIDs.size()];
		idArr = clusteringIDs.toArray(idArr);
		clustereringSelector = new JComboBox<>(idArr);

		layout.putConstraint(SpringLayout.NORTH, clustereringSelector, OUTER_SPACE, SpringLayout.NORTH, mainPanel);
		layout.putConstraint(SpringLayout.WEST, clustereringSelector, OUTER_SPACE, SpringLayout.WEST, mainPanel);
		mainPanel.add(clustereringSelector, new Integer(1));

		scatterMatrixButton = new JButton("Matrix");
		scatterMatrixButton.addActionListener(e -> {
			final ScatterPlotMatrix ms = new ScatterPlotMatrix(visibleViewer.getPointContainer());
			ms.setSize(new Dimension(800, 600));
			ms.setExtendedState(JFrame.MAXIMIZED_BOTH);
			ms.setLocationRelativeTo(null);
			ms.setVisible(true);
		});
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, scatterMatrixButton, 0, SpringLayout.VERTICAL_CENTER,
				clustereringSelector);
		layout.putConstraint(SpringLayout.WEST, scatterMatrixButton, OUTER_SPACE, SpringLayout.EAST,
				clustereringSelector);
		mainPanel.add(scatterMatrixButton, new Integer(1));

		final JLabel distLabel = new JLabel("Measure: " + metaDistance.getName());
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, distLabel, 0, SpringLayout.VERTICAL_CENTER,
				clustereringSelector);
		layout.putConstraint(SpringLayout.WEST, distLabel, OUTER_SPACE, SpringLayout.EAST, scatterMatrixButton);
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

		mdsPlot.setClickHandler(oPlot);
		clustereringSelector.addActionListener(e -> {
			final String selected = (String) clustereringSelector.getSelectedItem();
			final int selection = Integer.parseInt(selected.split(":")[0]);
			highlight(selection);
		});
		showViewer(0);

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

}
