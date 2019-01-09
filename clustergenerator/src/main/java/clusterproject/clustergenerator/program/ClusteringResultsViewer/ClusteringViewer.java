package clusterproject.clustergenerator.program.ClusteringResultsViewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import com.google.common.util.concurrent.AtomicDouble;

import clusterproject.clustergenerator.Util;
import clusterproject.clustergenerator.data.ClusteringResult;
import clusterproject.clustergenerator.data.PointContainer;
import clusterproject.clustergenerator.program.ClusterWorkflow;
import clusterproject.clustergenerator.program.MainWindow;
import clusterproject.clustergenerator.program.ClusterViewerElement.ScatterPlot;
import clusterproject.clustergenerator.program.ClusterViewerElement.ScatterPlotMatrix;
import clusterproject.clustergenerator.program.MetaClustering.ClusteringWithDistance;
import clusterproject.clustergenerator.program.MetaClustering.DistanceCalculation;
import clusterproject.clustergenerator.program.MetaClustering.HungarianAlgorithm;
import clusterproject.clustergenerator.program.MetaClustering.IDistanceMeasure;
import clusterproject.clustergenerator.program.MetaClustering.OpticsMetaClustering;
import smile.mds.MDS;

public class ClusteringViewer extends JFrame {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static final int OUTER_SPACE = 20;
	public static final int VIEWER_SPACE = 4;
	private static final int RIGHT_PANEL_WIDTH = 300;

	private final List<ClusteringResult> clusterings;
	private final ScatterPlot[] viewers;
	private ScatterPlot visibleViewer;
	private JPanel viewerPanel;

	private final JComboBox<String> clustereringSelector;
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

	private final FilterWindow filterWindow;

	private JButton saveButton;

	private JButton mainWindowButton;

	private HeatMap heatMap;

	private Set<Integer> filteredIndexes;

	public ClusteringViewer(List<ClusteringResult> clusterings, IDistanceMeasure metaDistance, int minPTS, double eps) {
		getContentPane().setBackground(MainWindow.BACKGROUND_COLOR);
		this.minPTS = minPTS;
		this.eps = eps;
		this.metaDistance = metaDistance;
		this.clusterings = clusterings;
		mainPanel = new JLayeredPane();
		layout = new SpringLayout();
		mainPanel.setLayout(layout);
		add(mainPanel);
		viewers = new ScatterPlot[clusterings.size()];
		final IntStream viewerPrepareStream = IntStream.range(0, clusterings.size());
		viewerPrepareStream.parallel().forEach(i -> {
			final ClusteringResult clustering = clusterings.get(i);
			final PointContainer container = clustering.toPointContainer();
			container.setHeaders(clustering.getHeaders());
			final ScatterPlot plot = new ScatterPlot(null, container, true);
			plot.addAutoAdjust();
			plot.addAutoColor();
			viewers[i] = plot;
		});

		final String[] idArr = new String[clusterings.size()];
		for (int i = 0; i < clusterings.size(); ++i) {
			final ClusteringResult result = clusterings.get(i);
			idArr[i] = (i + ": " + result.getParameter().getInfoString());
		}

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

		scatterMatrixButton = new JButton("Matrix");
		scatterMatrixButton.addActionListener(e -> {
			final ScatterPlotMatrix ms = new ScatterPlotMatrix(visibleViewer.getPointContainer());
			ms.setSize(new Dimension(800, 600));
			ms.setExtendedState(JFrame.MAXIMIZED_BOTH);
			ms.setLocationRelativeTo(null);
			ms.setVisible(true);
		});
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, scatterMatrixButton, 0, SpringLayout.VERTICAL_CENTER,
				mainWindowButton);
		layout.putConstraint(SpringLayout.WEST, scatterMatrixButton, MainWindow.INNER_SPACE, SpringLayout.EAST,
				mainWindowButton);
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

		distanceMatrix = DistanceCalculation.calculateDistanceMatrix(clusterings, metaDistance);

		final MDS mds = new MDS(distanceMatrix, 2);
		final double[][] coords = mds.getCoordinates();
		final PointContainer mdsContainer = new PointContainer(coords[0].length);
		mdsContainer.addPoints(coords);
		mdsPlot = new ScatterPlot(null, mdsContainer, true);
		mdsPlot.addAutoAdjust();
		mdsPlot.autoAdjust();
		layout.putConstraint(SpringLayout.NORTH, mdsPlot, VIEWER_SPACE, SpringLayout.SOUTH, clustereringSelector);
		layout.putConstraint(SpringLayout.WEST, mdsPlot, VIEWER_SPACE - RIGHT_PANEL_WIDTH / 2,
				SpringLayout.HORIZONTAL_CENTER, mainPanel);
		layout.putConstraint(SpringLayout.SOUTH, mdsPlot, -VIEWER_SPACE, SpringLayout.VERTICAL_CENTER, mainPanel);
		layout.putConstraint(SpringLayout.EAST, mdsPlot, -VIEWER_SPACE - RIGHT_PANEL_WIDTH, SpringLayout.EAST,
				mainPanel);
		mainPanel.add(mdsPlot, new Integer(9));

		final JLabel mdsLabel = new JLabel("MDS Plot");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, mdsLabel, 0, SpringLayout.VERTICAL_CENTER,
				clustereringSelector);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, mdsLabel, 0, SpringLayout.HORIZONTAL_CENTER, mdsPlot);
		mainPanel.add(mdsLabel, new Integer(11));

		final OpticsMetaClustering optics = new OpticsMetaClustering(clusterings, distanceMatrix, minPTS, eps);
		final List<ClusteringWithDistance> list = optics.runOptics();
		oPlot = new OpticsPlot(this, list);
		layout.putConstraint(SpringLayout.NORTH, oPlot, VIEWER_SPACE, SpringLayout.VERTICAL_CENTER, mainPanel);
		layout.putConstraint(SpringLayout.WEST, oPlot, VIEWER_SPACE - RIGHT_PANEL_WIDTH / 2,
				SpringLayout.HORIZONTAL_CENTER, mainPanel);
		layout.putConstraint(SpringLayout.SOUTH, oPlot, -VIEWER_SPACE, SpringLayout.SOUTH, mainPanel);
		layout.putConstraint(SpringLayout.EAST, oPlot, -VIEWER_SPACE - RIGHT_PANEL_WIDTH, SpringLayout.EAST, mainPanel);
		mainPanel.add(oPlot, new Integer(10));

		mdsPlot.setCanvasMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				final int closest = getClosestPoint(e.getPoint());
				if (closest != -1)
					highlight(closest);
				super.mouseClicked(e);
			}
		});

		heatMap = new HeatMap(Util.getSortedDistances(list, distanceMatrix), this, list);
		layout.putConstraint(SpringLayout.NORTH, heatMap, VIEWER_SPACE, SpringLayout.VERTICAL_CENTER, mainPanel);
		layout.putConstraint(SpringLayout.EAST, heatMap, -VIEWER_SPACE - RIGHT_PANEL_WIDTH / 2,
				SpringLayout.HORIZONTAL_CENTER, mainPanel);
		layout.putConstraint(SpringLayout.SOUTH, heatMap, -VIEWER_SPACE, SpringLayout.SOUTH, mainPanel);
		layout.putConstraint(SpringLayout.WEST, heatMap, VIEWER_SPACE, SpringLayout.WEST, mainPanel);
		mainPanel.add(heatMap, new Integer(10));

		clustereringSelector.addActionListener(e -> {
			final String selected = (String) clustereringSelector.getSelectedItem();
			final int selection = Integer.parseInt(selected.split(":")[0]);
			highlight(selection);
		});

		filterWindow = new FilterWindow(clusterings, this);
		layout.putConstraint(SpringLayout.NORTH, filterWindow, VIEWER_SPACE, SpringLayout.SOUTH, clustereringSelector);
		layout.putConstraint(SpringLayout.SOUTH, filterWindow, -VIEWER_SPACE, SpringLayout.SOUTH, mainPanel);
		layout.putConstraint(SpringLayout.EAST, filterWindow, 0, SpringLayout.EAST, mainPanel);
		layout.putConstraint(SpringLayout.WEST, filterWindow, -RIGHT_PANEL_WIDTH, SpringLayout.EAST, mainPanel);
		mainPanel.add(filterWindow, new Integer(11));

		viewerPanel = new JPanel();
		viewerPanel.setOpaque(false);
		viewerPanel.setLayout(new BorderLayout());
		layout.putConstraint(SpringLayout.NORTH, viewerPanel, VIEWER_SPACE, SpringLayout.SOUTH, clustereringSelector);
		layout.putConstraint(SpringLayout.SOUTH, viewerPanel, -VIEWER_SPACE, SpringLayout.VERTICAL_CENTER, mainPanel);
		layout.putConstraint(SpringLayout.WEST, viewerPanel, VIEWER_SPACE, SpringLayout.WEST, mainPanel);
		layout.putConstraint(SpringLayout.EAST, viewerPanel, -VIEWER_SPACE - RIGHT_PANEL_WIDTH / 2,
				SpringLayout.HORIZONTAL_CENTER, mainPanel);
		mainPanel.add(viewerPanel, new Integer(10));

		showViewer(0, false);
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
		showViewer(i, true);
	}

	public void showViewer(int i, boolean repaint) {
		clustereringSelector.setSelectedIndex(i);
		final ScatterPlot newViewer = viewers[i];
		if (i == highlighted)
			return;
		if (visibleViewer != null) {
			final List<Integer> newClusterIDs = getNewColors(i);
			newViewer.getPointContainer().setClusterIDs(newClusterIDs);
			newViewer.setSelectedDimX(visibleViewer.getSelectedDimX());
			newViewer.setSelectedDimY(visibleViewer.getSelectedDimY());
			newViewer.setIntervalX(visibleViewer.getIntervalX());
			newViewer.setIntervalY(visibleViewer.getIntervalY());
			viewerPanel.remove(visibleViewer);
		}
		highlighted = i;
		visibleViewer = newViewer;
		viewerPanel.add(visibleViewer, BorderLayout.CENTER);

		SwingUtilities.invokeLater(() -> {
			viewerPanel.revalidate();
			viewerPanel.repaint();
			oPlot.repaint();
			mdsPlot.repaint();
		});

	}

	private List<Integer> getNewColors(int i) { // TODO: maybe something with cluster size for color selection?
		final ClusteringResult oldClustering = clusterings.get(highlighted);
		final ClusteringResult newClustering = clusterings.get(i);
		final Map<Integer, Integer> oldIDMap = visibleViewer.getPointContainer().getIDMap();
		final List<Integer> currentIDs = viewers[i].getPointContainer().getOriginalClusterIDs();
		final int matrixSize = oldClustering.getData().length > newClustering.getData().length
				? oldClustering.getData().length
				: newClustering.getData().length;
		final int[][] confusion = new int[matrixSize][matrixSize];
		final IntStream intersectionStream = IntStream.range(0, oldClustering.getData().length);
		intersectionStream.parallel().forEach(idx -> {
			for (int j = 0; j < newClustering.getData().length; ++j) {
				try {
					confusion[idx][j] = -Util.intersection(oldClustering.getData()[idx],
							newClustering.getData()[j]).length;
				} catch (final ArrayIndexOutOfBoundsException e) {
					confusion[idx][j] = 0;
				}
			}
		});

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
		viewers[i].getPointContainer().setIDMap(idMap);
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

	public int getClosestPoint(Point point) {
		final AtomicDouble distance = new AtomicDouble(Double.MAX_VALUE);
		final AtomicInteger closest = new AtomicInteger(-1);
		final ReentrantLock lock = new ReentrantLock();
		final List<double[]> points = mdsPlot.getPointContainer().getPoints();
		final IntStream stream = IntStream.range(0, points.size());
		stream.parallel().forEach(i -> {
			final double offsetx = (mdsPlot.getPixelX(points.get(i)) - point.getX());
			final double offsety = (mdsPlot.getPixelY(points.get(i)) - point.getY());
			final double curDistance = offsetx * offsetx + offsety * offsety;
			lock.lock();
			if (curDistance < distance.get()) {
				distance.lazySet(curDistance);
				closest.set(i);
			}
			lock.unlock();
		});
		return closest.get();
	}

	public IDistanceMeasure getDistanceMeasure() {
		return metaDistance;
	}

	public void setFilteredData(Set<ClusteringResult> filteredResults) {
		this.filteredIndexes = null;// TODO display not contained ClusteringResults differently
		if (filteredResults == null)
			mdsPlot.getPointContainer().setFilteredResults(null);
		else {
			final Set<Integer> filteredIndexes = new HashSet<>();
			for (final ClusteringResult result : filteredResults)
				filteredIndexes.add(clusterings.indexOf(result));
			mdsPlot.getPointContainer().setFilteredResults(filteredIndexes);
			this.filteredIndexes = filteredIndexes;
		}

		SwingUtilities.invokeLater(() -> {
			mdsPlot.repaint();
			oPlot.repaint();
		});

	}

	public Set<Integer> getFilteredIndexes() {
		return filteredIndexes;
	}

}
