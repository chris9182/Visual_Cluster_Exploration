package clusterproject.program.ClusteringResultsViewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
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
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import org.jzy3d.colors.Color;
import org.jzy3d.plot3d.primitives.Scatter;

import com.google.common.util.concurrent.AtomicDouble;

import clusterproject.data.ClusteringResult;
import clusterproject.data.PointContainer;
import clusterproject.program.ClusterWorkflow;
import clusterproject.program.MainWindow;
import clusterproject.program.ClusterViewerElement.ScatterPlot;
import clusterproject.program.ClusterViewerElement.ScatterPlotMatrix;
import clusterproject.program.Clustering.Parameters.Parameter;
import clusterproject.program.ClusteringResultsViewer.FilterWindow.HistogramData;
import clusterproject.program.Consensus.CoAssociationMatrixAverageLinkLifetime;
import clusterproject.program.Consensus.ConsensusFunction;
import clusterproject.program.MetaClustering.ClusteringWithDistance;
import clusterproject.program.MetaClustering.DistanceCalculation;
import clusterproject.program.MetaClustering.HungarianAlgorithm;
import clusterproject.program.MetaClustering.IDistanceMeasure;
import clusterproject.program.MetaClustering.OpticsMetaClustering;
import clusterproject.util.NMI;
import clusterproject.util.Util;
import smile.mds.MDS;

public class ClusteringViewer extends JFrame {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static final int OUTER_SPACE = 20;
	public static final int VIEWER_SPACE = 4;
	public static final int RIGHT_PANEL_WIDTH = 300;
	private static final int MAX_HEATMAP_SIZE = 130;

	private static final int MDS_MAX_DIM = 3;

	private final List<ClusteringResult> clusterings;
	private final List<ClusteringWithDistance> clusteredList;
	private final ScatterPlot[] viewers;
	private ScatterPlot visibleViewer;
	private JPanel viewerPanel;

	private final JComboBox<String> clustereringSelector;
	private final JLayeredPane mainPanel;
	private final SpringLayout layout;
	private final IDistanceMeasure metaDistance;
	private final double[][] distanceMatrix;
	private final OpticsPlot oPlot;
	private HeatMap heatMap;
	private ScatterPlot mdsPlot;
	private Scatter scatter;
	private final LinkedHashSet<Integer> highlighted = new LinkedHashSet<>();
	private final AtomicBoolean dohighlight = new AtomicBoolean(true);

	private final JButton scatterMatrixButton;
	private final FilterWindow filterWindow;
	private JButton saveButton;
	private JButton consensusButton;
	private JButton mainWindowButton;
	private JButton diffButton;
	private JButton resetButton;

	private Set<Integer> filteredIndexes;
	private int selectedViewer = 0;
	private int groundTruth = -1;

	public ClusteringViewer(List<ClusteringResult> clusterings, IDistanceMeasure metaDistance, int minPTS, double eps) {
		getContentPane().setBackground(MainWindow.BACKGROUND_COLOR);
		for (int i = 0; i < clusterings.size(); ++i) {
			if (clusterings.get(i).getParameter().getName().equals(Util.GROUND_TRUTH)) {
				groundTruth = 0;
				if (i != 0) {
					final ClusteringResult result = clusterings.remove(i);
					clusterings.add(0, result);
				}
				break;
			}

		}
		viewers = new ScatterPlot[clusterings.size()];
		highlighted.add(-1);

		this.metaDistance = metaDistance;
		this.clusterings = clusterings;
		mainPanel = new JLayeredPane();
		layout = new SpringLayout();
		mainPanel.setLayout(layout);
		add(mainPanel);

		final IntStream viewerPrepareStream = IntStream.range(0, clusterings.size());
		viewerPrepareStream.parallel().forEach(i -> {
			final ClusteringResult clustering = clusterings.get(i);
			final PointContainer container = clustering.toPointContainer();
			container.setHeaders(clustering.getHeaders());
			final ScatterPlot plot = new ScatterPlot(container, true);
			plot.autoAdjust();
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
			ms.setExtendedState(Frame.MAXIMIZED_BOTH);
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

		consensusButton = new JButton("Consensus");
		consensusButton.addActionListener(e -> {
			// XXX improve and let user choose?
			// final ConsensusFunction function = new CoAssociationMatrixAverageLink();
			final ConsensusFunction function = new CoAssociationMatrixAverageLinkLifetime();
			// final ConsensusFunction function = new CoAssociationMatrixThreshhold();
			// final ConsensusFunction function = new CoAssociationMatrixWithCompletion();
			final List<List<PointContainer>> pointContainers = getContainersByTag();
			final ClusteringResult[] resultArray = new ClusteringResult[pointContainers.size()];
			pointContainers.parallelStream().forEach(t -> {
				final int index = pointContainers.indexOf(t);
				final List<Double> weights = null;
				final PointContainer consensus = function.calculateConsensus(t, weights);
				final double[][][] data = consensus.toData();
				final Parameter param = new Parameter("Consensus");
				param.addParameter("Result ID", index);
				param.addAdditionalParameter("Number of Clusters incl.", t.size());
				resultArray[index] = (new ClusteringResult(data, param, this.clusterings.get(0).getHeaders()));
			});
			final List<ClusteringResult> results = new ArrayList<ClusteringResult>(Arrays.asList(resultArray));
			if (groundTruth >= 0)
				results.add(0, clusterings.get(groundTruth));
			final ClusteringViewer newWindow = new ClusteringViewer(results, metaDistance, minPTS, eps);
			newWindow.setSize(new Dimension(1000, 800));
			newWindow.setLocationRelativeTo(null);
			newWindow.setVisible(true);
		});
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, consensusButton, 0, SpringLayout.VERTICAL_CENTER,
				saveButton);
		layout.putConstraint(SpringLayout.WEST, consensusButton, MainWindow.INNER_SPACE, SpringLayout.EAST, saveButton);
		mainPanel.add(consensusButton, new Integer(1));

		final JLabel distLabel = new JLabel("Measure: " + metaDistance.getName());
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, distLabel, 0, SpringLayout.VERTICAL_CENTER, consensusButton);
		layout.putConstraint(SpringLayout.WEST, distLabel, OUTER_SPACE, SpringLayout.EAST, consensusButton);
		mainPanel.add(distLabel, new Integer(1));

		distanceMatrix = DistanceCalculation.calculateDistanceMatrix(clusterings, metaDistance);

		addParameters();

		MDS mds = null;
		try {
			int k = distanceMatrix.length - 1 < MDS_MAX_DIM ? distanceMatrix.length - 1 : MDS_MAX_DIM;
			k = k < 1 ? 1 : k;
			mds = new MDS(distanceMatrix, k);
			// System.err.println(mds.getProportion()[0] + " " + mds.getProportion()[1]);
			final double[][] coords = mds.getCoordinates();
			final PointContainer mdsContainer = new PointContainer(coords[0].length);
			mdsContainer.addPoints(coords);
			// XXX remove this
			// if (coords[0].length > 2) {
			// final JFrame test = new JFrame();
			// final int size = coords.length;
			// final float x;
			// final float y;
			// final float z;
			// float a;
			//
			// final Coord3d[] points = new Coord3d[size];
			// final Color[] colors = new Color[size];
			// a = 0.5f;
			// for (int i = 0; i < size; i++) {
			// points[i] = new Coord3d(coords[i][0], coords[i][1], coords[i][2]);
			// colors[i] = new Color(.5f, .5f, .5f, a);
			// }
			//
			// scatter = new Scatter(points, colors);
			// test.setSize(800, 800);
			// final Chart chart = AWTChartComponentFactory.chart(Quality.Advanced, "awt");
			// scatter.setWidth(5);
			//
			// final AWTCameraMouseController mouseController = new
			// AWTCameraMouseController() {
			// };
			// final AWTCameraKeyController keyController = new AWTCameraKeyController();
			// test.addMouseListener(mouseController);
			// test.addKeyListener(keyController);
			// chart.addController(mouseController);
			// chart.addController(keyController);
			//
			// chart.getScene().add(scatter);
			//
			// test.add((Component) chart.getCanvas());
			// test.setVisible(true);
			// }

			mdsPlot = new ScatterPlot(mdsContainer, true);
			mdsPlot.addAutoAdjust();
			mdsPlot.autoAdjust();
			layout.putConstraint(SpringLayout.NORTH, mdsPlot, VIEWER_SPACE, SpringLayout.SOUTH, clustereringSelector);
			layout.putConstraint(SpringLayout.WEST, mdsPlot, VIEWER_SPACE - RIGHT_PANEL_WIDTH / 2,
					SpringLayout.HORIZONTAL_CENTER, mainPanel);
			layout.putConstraint(SpringLayout.SOUTH, mdsPlot, -VIEWER_SPACE, SpringLayout.VERTICAL_CENTER, mainPanel);
			layout.putConstraint(SpringLayout.EAST, mdsPlot, -VIEWER_SPACE - RIGHT_PANEL_WIDTH, SpringLayout.EAST,
					mainPanel);
			mainPanel.add(mdsPlot, new Integer(13));

			final JLabel mdsLabel = new JLabel("MDS Plot");
			layout.putConstraint(SpringLayout.VERTICAL_CENTER, mdsLabel, 0, SpringLayout.VERTICAL_CENTER,
					clustereringSelector);
			layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, mdsLabel, 0, SpringLayout.HORIZONTAL_CENTER, mdsPlot);
			mainPanel.add(mdsLabel, new Integer(11));
		} catch (final Exception e) {
			e.printStackTrace();
		}

		final OpticsMetaClustering optics = new OpticsMetaClustering(clusterings, distanceMatrix, minPTS, eps);
		clusteredList = optics.runOptics();
		oPlot = new OpticsPlot(this, clusteredList);
		layout.putConstraint(SpringLayout.NORTH, oPlot, VIEWER_SPACE, SpringLayout.VERTICAL_CENTER, mainPanel);
		layout.putConstraint(SpringLayout.WEST, oPlot, VIEWER_SPACE - RIGHT_PANEL_WIDTH / 2,
				SpringLayout.HORIZONTAL_CENTER, mainPanel);
		layout.putConstraint(SpringLayout.SOUTH, oPlot, -VIEWER_SPACE, SpringLayout.SOUTH, mainPanel);
		layout.putConstraint(SpringLayout.EAST, oPlot, -VIEWER_SPACE - RIGHT_PANEL_WIDTH, SpringLayout.EAST, mainPanel);
		mainPanel.add(oPlot, new Integer(10));
		final MouseAdapter mouseAdapter = new MouseAdapter() {
			private Point down;
			private Point current;

			@Override
			public void mouseClicked(MouseEvent e) {
				down = null;
				current = null;
				final int closest = getClosestPoint(e.getPoint());
				mouseHighlight(closest, !e.isControlDown(), e.getClickCount() == 1);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				down = (Point) e.getPoint().clone();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				current = (Point) e.getPoint().clone();
				mdsPlot.setSelection(down, current);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (current != null)
					rangeSelect(down, current, !e.isControlDown());
				current = null;// TODO: bug
				mdsPlot.setSelection(null, null);
			}
		};

		if (mdsPlot != null) {
			mdsPlot.addMouseMotionListener(mouseAdapter);
			mdsPlot.addMouseListener(mouseAdapter);
			mdsPlot.getPointContainer().getMetaInformation().setGroundTruth(groundTruth);
		}

		if (clusterings.size() <= MAX_HEATMAP_SIZE) {
			heatMap = new HeatMap(Util.getSortedDistances(clusteredList, distanceMatrix), this, clusteredList);
			layout.putConstraint(SpringLayout.NORTH, heatMap, VIEWER_SPACE, SpringLayout.VERTICAL_CENTER, mainPanel);
			layout.putConstraint(SpringLayout.EAST, heatMap, -VIEWER_SPACE - RIGHT_PANEL_WIDTH / 2,
					SpringLayout.HORIZONTAL_CENTER, mainPanel);
			layout.putConstraint(SpringLayout.SOUTH, heatMap, -VIEWER_SPACE, SpringLayout.SOUTH, mainPanel);
			layout.putConstraint(SpringLayout.WEST, heatMap, VIEWER_SPACE, SpringLayout.WEST, mainPanel);
			mainPanel.add(heatMap, new Integer(10));
		} else {
			layout.putConstraint(SpringLayout.WEST, oPlot, VIEWER_SPACE, SpringLayout.WEST, mainPanel);
		}

		clustereringSelector.addActionListener(e -> {
			final String selected = (String) clustereringSelector.getSelectedItem();
			final List<Integer> highlighted = new ArrayList<Integer>();
			highlighted.add(Integer.parseInt(selected.split(":")[0]));
			highlight(highlighted, true);
		});

		filterWindow = new FilterWindow(clusterings, this);
		final JScrollPane scrollPaneFilter = new JScrollPane(filterWindow);
		scrollPaneFilter.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPaneFilter.setBorder(null);
		scrollPaneFilter.setOpaque(false);
		layout.putConstraint(SpringLayout.NORTH, scrollPaneFilter, VIEWER_SPACE, SpringLayout.SOUTH,
				clustereringSelector);
		layout.putConstraint(SpringLayout.SOUTH, scrollPaneFilter, -VIEWER_SPACE, SpringLayout.SOUTH, mainPanel);
		layout.putConstraint(SpringLayout.EAST, scrollPaneFilter, 0, SpringLayout.EAST, mainPanel);
		layout.putConstraint(SpringLayout.WEST, scrollPaneFilter, -RIGHT_PANEL_WIDTH, SpringLayout.EAST, mainPanel);
		mainPanel.add(scrollPaneFilter, new Integer(11));

		diffButton = new JButton("Difference");
		diffButton.addActionListener(e -> {
			if (highlighted.size() != 2)
				return;
			final Iterator<Integer> hIter = highlighted.iterator();
			final int id1 = hIter.next();
			final PointContainer c1 = viewers[id1].getPointContainer();
			final int id2 = hIter.next();
			final PointContainer c2 = viewers[id2].getPointContainer();
			c2.getClusterInformation().setClusterIDs(getNewColors(id1, id2));
			final DifferenceWindow newWindow = new DifferenceWindow(viewers[id1], viewers[id2]);
			newWindow.setSize(new Dimension(1000, 800));
			newWindow.setExtendedState(Frame.MAXIMIZED_BOTH);
			newWindow.setLocationRelativeTo(null);
			newWindow.setVisible(true);
			// newWindow.update();

		});

		layout.putConstraint(SpringLayout.EAST, diffButton, 0, SpringLayout.WEST, scrollPaneFilter);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, diffButton, 0, SpringLayout.VERTICAL_CENTER, distLabel);
		mainPanel.add(diffButton, new Integer(12));

		resetButton = new JButton("Reset");
		resetButton.addActionListener(e -> {
			filterWindow.resetFilters();
			final Set<Integer> newHighlight = new HashSet<Integer>();
			newHighlight.add(selectedViewer);
			highlight(newHighlight, true);
		});
		layout.putConstraint(SpringLayout.EAST, resetButton, 0, SpringLayout.EAST, scrollPaneFilter);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, resetButton, 0, SpringLayout.VERTICAL_CENTER, distLabel);
		mainPanel.add(resetButton, new Integer(12));

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

	public List<ClusteringResult> getClusterings() {
		return clusterings;
	}

	private void addParameters() {
		// XXX add aditional filter params
		if (groundTruth >= 0) {
			for (int i = 0; i < clusterings.size(); ++i) {
				clusterings.get(i).getParameter().addAdditionalParameter(Util.GROUND_TRUTH,
						distanceMatrix[groundTruth][i]);
			}
			for (int i = 0; i < clusterings.size(); ++i) {
				clusterings.get(i).getParameter().addAdditionalParameter("NMI", getNMIToTruth(i));
			}
		}
		for (final ClusteringResult result : clusterings) {
			int length = 0;
			for (final double[][] cluster : result.getData())
				if (cluster.length > 0)
					++length;
			result.getParameter().addAdditionalParameter(Util.CLUSTER_COUNT, length);
		}

		// for (final ClusteringResult result : clusterings) {
		// int length = 0;
		// int size = 0;
		// for (final double[][] cluster : result.getData()) {
		// if (cluster.length > 0) {
		// ++length;
		// size += cluster.length;
		// }
		// }
		// final double mean = size / length;
		// double sd = 0;
		// for (final double[][] cluster : result.getData()) {
		// if (cluster.length > 0)
		// sd += Math.pow(cluster.length - mean, 2);
		// }
		// sd /= length;
		// sd = Math.sqrt(sd);
		// result.getParameter().addAdditionalParameter(Util.CLUSTER_SIZE_VARIATION_COEF,
		// sd / mean);
		// }

	}

	protected void rangeSelect(Point down, Point current, boolean replace) {
		final Point lower = new Point((int) (down.getX() < current.getX() ? down.getX() : current.getX()),
				(int) (down.getY() < current.getY() ? down.getY() : current.getY()));
		final Point upper = new Point((int) (down.getX() > current.getX() ? down.getX() : current.getX()),
				(int) (down.getY() > current.getY() ? down.getY() : current.getY()));

		final ReentrantLock lock = new ReentrantLock();
		final List<Integer> ids = new ArrayList<Integer>();
		final List<double[]> points = mdsPlot.getPointContainer().getPoints();
		final IntStream stream = IntStream.range(0, points.size());
		stream.parallel().forEach(i -> {
			final double posX = mdsPlot.getPixelX(points.get(i));
			final double posY = mdsPlot.getPixelY(points.get(i));

			if (posX >= lower.getX() && posX <= upper.getX() && posY >= lower.getY() && posY <= upper.getY()) {
				lock.lock();
				ids.add(i);
				lock.unlock();
			}
		});
		if (ids.size() > 0) {
			highlight(ids, replace);
		}

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
		if (scatter != null) {
			final Color[] colors = new Color[list.size()];
			// XXX cleanup
			for (int i = 0; i < list.size(); ++i) {
				final java.awt.Color color = Util.getColor(list.get(i).tag + 2);
				final Color drawColor = list.get(i).tag == -2 ? Color.GRAY
						: new Color(color.getRed(), color.getGreen(), color.getBlue(), .5f);
				colors[list.get(i).inIndex] = list.get(i).inIndex == groundTruth ? Color.BLACK : drawColor;
			}
			scatter.setColors(colors);
		}
		if (mdsPlot == null)
			return;
		final Integer[] clusterIDs = new Integer[list.size()];
		for (final ClusteringWithDistance clustering : list)
			clusterIDs[clustering.inIndex] = clustering.tag;
		mdsPlot.getPointContainer().setUpClusters();
		mdsPlot.getPointContainer().getClusterInformation()
				.setAllClusterIDs(new ArrayList<Integer>(Arrays.asList(clusterIDs)));
		SwingUtilities.invokeLater(() -> mdsPlot.repaint());
	}

	public void showViewer(int i) {
		showViewer(i, true);
	}

	public void showViewer(int i, boolean repaint) {
		clustereringSelector.setSelectedIndex(i);
		final ScatterPlot newViewer = viewers[i];
		if (visibleViewer != null) {
			final List<Integer> newClusterIDs = getNewColors(selectedViewer, i);
			newViewer.getPointContainer().getClusterInformation().setClusterIDs(newClusterIDs);
			newViewer.setSelectedDimX(visibleViewer.getSelectedDimX());
			newViewer.setSelectedDimY(visibleViewer.getSelectedDimY());
			newViewer.setIntervalX(visibleViewer.getIntervalX());
			newViewer.setIntervalY(visibleViewer.getIntervalY());
			viewerPanel.remove(visibleViewer);
		}
		selectedViewer = i;
		visibleViewer = newViewer;
		viewerPanel.add(visibleViewer, BorderLayout.CENTER);
		if (repaint)
			callRepaint();

	}

	private void callRepaint() {
		SwingUtilities.invokeLater(() -> {
			viewerPanel.revalidate();
			viewerPanel.repaint();
			oPlot.repaint();
			if (mdsPlot != null)
				mdsPlot.repaint();
			if (heatMap != null)
				heatMap.repaint();
			filterWindow.repaint();
		});

	}

	private List<Integer> getNewColors(int previous, int other) { // TODO: maybe something with cluster size for color
		// selection?
		final ClusteringResult oldClustering = clusterings.get(previous);
		final ClusteringResult newClustering = clusterings.get(other);
		final Map<Integer, Integer> oldIDMap = visibleViewer.getPointContainer().getClusterInformation().getIDMap();
		final List<Integer> currentIDs = viewers[other].getPointContainer().getClusterInformation()
				.getOriginalClusterIDs();
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
		viewers[other].getPointContainer().getClusterInformation().setIDMap(idMap);
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

	public void mouseHighlight(int index, boolean replace, boolean singleClick) {
		if (index >= 0) {
			final List<Integer> newhighlighted = new ArrayList<Integer>();
			newhighlighted.add(index);
			if (singleClick) {
				highlight(newhighlighted, replace);
			} else {
				clusterHighlight(index, replace);
			}
		}
	}

	private void clusterHighlight(int closest, boolean replace) {

		int clusterid = -3;
		for (final ClusteringWithDistance clustering : clusteredList) {
			if (clustering.inIndex == closest) {
				clusterid = clustering.tag;
				break;
			}
		}
		final List<Integer> indices = new ArrayList<Integer>();
		for (final ClusteringWithDistance clustering : clusteredList) {
			if (clustering.tag == clusterid) {
				indices.add(clustering.inIndex);
			}
		}
		if (clusterid < -2)
			return;
		if (highlighted.contains(closest)) {
			forceHighlight(indices, true, replace);
		} else {
			forceHighlight(indices, false, replace);
		}

	}

	private void forceHighlight(List<Integer> indices, boolean select, boolean replace) {
		if (!dohighlight.get())
			return;
		dohighlight.set(false);
		final int backup = highlighted.iterator().next();
		if (replace) {
			highlighted.clear();
		}
		if (select) {
			for (final int ind : indices) {
				highlighted.add(ind);
			}
		} else {
			for (final int ind : indices) {
				highlighted.remove(ind);
			}
		}
		updateHighlight(backup);
		dohighlight.set(true);

	}

	private void highlight(Collection<Integer> i, boolean replace) {

		if (!dohighlight.get())
			return;
		dohighlight.set(false);// TODO: propper lock
		final int backup = highlighted.iterator().next();
		if (replace) {
			highlighted.clear();
			highlighted.addAll(i);
		} else {
			for (final Integer newInt : i) {
				if (highlighted.contains(newInt))
					highlighted.remove(newInt);
				else
					highlighted.add(newInt);
			}
		}

		updateHighlight(backup);
		dohighlight.set(true);
	}

	private void updateHighlight(int backup) {
		if (highlighted.isEmpty())
			highlighted.add(backup);
		if (highlighted.size() > 1) {
			final List<ClusteringResult> results = new ArrayList<>();
			highlighted.forEach(i1 -> results.add(clusterings.get(i1)));
			setHistogramData(results, HistogramData.Highlited);
		} else {
			if (filterWindow.getClusterings() != clusterings) {
				setHistogramData(clusterings, HistogramData.All);
			}
		}

		diffButton.setVisible(highlighted.size() == 2);
		if (mdsPlot != null)
			mdsPlot.getPointContainer().getMetaInformation().setHighlighted(highlighted);
		if (highlighted.size() > 1 || highlighted.iterator().next() == -1)
			if (highlighted.contains(selectedViewer) || highlighted.size() < 1) {
				callRepaint();
			} else
				showViewer(highlighted.iterator().next());
		else
			showViewer(highlighted.iterator().next());

	}

	public LinkedHashSet<Integer> getHighlighted() {
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
			mdsPlot.getPointContainer().getMetaInformation().setFilteredResults(null);
		else {
			final Set<Integer> filteredIndexes = new HashSet<>();
			for (final ClusteringResult result : filteredResults)
				filteredIndexes.add(clusterings.indexOf(result));
			mdsPlot.getPointContainer().getMetaInformation().setFilteredResults(filteredIndexes);
			this.filteredIndexes = filteredIndexes;
		}

		SwingUtilities.invokeLater(() -> {
			if (heatMap != null)
				heatMap.repaint();
			if (mdsPlot != null)
				mdsPlot.repaint();
			oPlot.repaint();
		});

	}

	public Set<Integer> getFilteredIndexes() {
		return filteredIndexes;
	}

	public int getGroundTruth() {
		return groundTruth;
	}

	public Double getDistanceToTruth(int i) {
		if (groundTruth < 0)
			return Double.NaN;
		return distanceMatrix[i][groundTruth];
	}

	public Double getNMIToTruth(int i) {
		if (groundTruth < 0)
			return Double.NaN;
		return NMI.calc(//

				viewers[groundTruth].getPointContainer(), //
				viewers[i].getPointContainer());
	}

	public void setHistogramData(List<ClusteringResult> newData, HistogramData histogramData) {
		filterWindow.setHistogramData(newData, histogramData);
	}

	public HistogramData getHistogramData() {
		if (filterWindow == null)
			return null;
		return filterWindow.getHistogramData();
	}

	public List<PointContainer> getRelevantContainers() {// XXX: will be removed later for propper selection
		final List<PointContainer> containers = new ArrayList<PointContainer>();
		final List<Integer> tags = mdsPlot.getPointContainer().getClusterInformation().getClusterIDs();
		for (int i = 0; i < tags.size(); ++i)
			if (tags.get(i) >= 0 && (filteredIndexes == null || filteredIndexes.contains(i)) && i != groundTruth)
				containers.add(viewers[i].getPointContainer());
		return containers;
	}

	public List<Double> getRelevantWeightsAcrossMethods() {// XXX: will be removed later for propper selection
		final Map<String, Integer> weights = new HashMap<String, Integer>();
		final List<Integer> tags = mdsPlot.getPointContainer().getClusterInformation().getClusterIDs();
		for (int i = 0; i < tags.size(); ++i)
			if (tags.get(i) >= 0 && (filteredIndexes == null || filteredIndexes.contains(i))) {
				final String name = clusterings.get(i).getParameter().getName();

				if (weights.containsKey(name)) {
					weights.put(name, weights.get(name) + 1);
				} else {
					weights.put(name, 1);
				}
			}
		final List<Double> weightsList = new ArrayList<Double>();
		for (int i = 0; i < tags.size(); ++i)
			if (tags.get(i) >= 0 && (filteredIndexes == null || filteredIndexes.contains(i)))
				weightsList.add(1 / (double) weights.get(clusterings.get(i).getParameter().getName()));
		return weightsList;
	}

	public List<Double> getRelevantWeightsAcrossMetaClusters() {// XXX: will be removed later for propper selection
		final Map<Integer, Integer> weights = new HashMap<Integer, Integer>();
		final List<Integer> tags = mdsPlot.getPointContainer().getClusterInformation().getClusterIDs();
		for (int i = 0; i < tags.size(); ++i)
			if (tags.get(i) >= 0 && (filteredIndexes == null || filteredIndexes.contains(i))) {
				final Integer tag = mdsPlot.getPointContainer().getClusterInformation().getClusterIDs().get(i);

				if (weights.containsKey(tag)) {
					weights.put(tag, weights.get(tag) + 1);
				} else {
					weights.put(tag, 1);
				}
			}
		final List<Double> weightsList = new ArrayList<Double>();
		for (int i = 0; i < tags.size(); ++i)
			if (tags.get(i) >= 0 && (filteredIndexes == null || filteredIndexes.contains(i)))
				weightsList.add(1 / (double) weights
						.get(mdsPlot.getPointContainer().getClusterInformation().getClusterIDs().get(i)));
		return weightsList;
	}

	// TODO: what if there is no mds?
	public List<List<PointContainer>> getContainersByTag() {
		final Map<Integer, List<PointContainer>> containerLists = new TreeMap<Integer, List<PointContainer>>();
		final List<Integer> tags = mdsPlot.getPointContainer().getClusterInformation().getClusterIDs();
		for (int i = 0; i < tags.size(); ++i) {
			final int tag = tags.get(i);
			if (tag >= 0 && (filteredIndexes == null || filteredIndexes.contains(i)) && i != groundTruth) {
				List<PointContainer> containerList = containerLists.get(tag);
				if (containerList == null)
					containerList = new ArrayList<PointContainer>();
				containerList.add(viewers[i].getPointContainer());
				containerLists.put(tag, containerList);
			}
		}
		return new ArrayList<List<PointContainer>>(containerLists.values());
	}

	public ScatterPlot getVisibleViewer() {
		return visibleViewer;
	}

}
