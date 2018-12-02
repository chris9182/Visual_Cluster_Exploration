package clusterproject.clustergenerator.userInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import clusterproject.clustergenerator.Util;
import clusterproject.clustergenerator.data.ClusteringResult;
import clusterproject.clustergenerator.data.PointContainer;
import clusterproject.clustergenerator.userInterface.MetaClustering.ClusteringWithDistance;
import clusterproject.clustergenerator.userInterface.MetaClustering.HungarianAlgorithm;
import clusterproject.clustergenerator.userInterface.MetaClustering.OpticsMetaClustering;

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

	private final JComboBox clustereringSelector;
	private int currentClustering = -1;
	private final JLayeredPane mainPanel;
	private final SpringLayout layout;

	public ClusteringViewer(List<ClusteringResult> clusterings, PointContainer pointContainer) {
		getContentPane().setBackground(MainWindow.BACKGROUND_COLOR);
		this.clusterings = clusterings;
		mainPanel = new JLayeredPane();
		layout = new SpringLayout();
		mainPanel.setLayout(layout);
		add(mainPanel);
		viewers = new ArrayList<ScatterPlot>();
		clusterings.forEach(clustering -> {
			final PointContainer container = clustering.toPointContainer();
			container.setHeaders(pointContainer.getHeaders());
			final ScatterPlot plot = new ScatterPlot(null, container, true);
			plot.addAutoAdjust();
			viewers.add(plot);
		});

		final List<String> clusteringIDs = new ArrayList<String>();
		int i = 0;
		for (final ClusteringResult result : clusterings) {
			clusteringIDs.add(i++ + ": " + result.getDescription());
		}

		String[] idArr = new String[clusteringIDs.size()];
		idArr = clusteringIDs.toArray(idArr);
		clustereringSelector = new JComboBox<>(idArr);
		clustereringSelector.addActionListener(e -> {
			final String selected = (String) clustereringSelector.getSelectedItem();
			showViewer(Integer.parseInt(selected.split(":")[0]));
		});

		layout.putConstraint(SpringLayout.NORTH, clustereringSelector, OUTER_SPACE, SpringLayout.NORTH, mainPanel);
		layout.putConstraint(SpringLayout.WEST, clustereringSelector, OUTER_SPACE, SpringLayout.WEST, mainPanel);
		mainPanel.add(clustereringSelector, new Integer(1));
		showViewer(0);

		final OpticsMetaClustering test = new OpticsMetaClustering(clusterings, 1, 2);
		final List<ClusteringWithDistance> list = test.runOptics();
		list.forEach(t -> System.err.println(t.distance));

	}

	private void showViewer(int i) {
		final ScatterPlot newViewer = viewers.get(i);
		if (visibleViewer != null) {
			final List<Integer> newClusterIDs = getNewColors(i);
			newViewer.getPointContainer().setClusterIDs(newClusterIDs);
			newViewer.setSelectedDimX(visibleViewer.getSelectedDimX());
			newViewer.setSelectedDimY(visibleViewer.getSelectedDimY());
			newViewer.setIntervalX(visibleViewer.getIntervalX());
			newViewer.setIntervalY(visibleViewer.getIntervalY());
			mainPanel.remove(visibleViewer);
			visibleViewer = null;
		}
		currentClustering = i;
		visibleViewer = viewers.get(i);
		layout.putConstraint(SpringLayout.NORTH, visibleViewer, VIEWER_SPACE, SpringLayout.SOUTH, clustereringSelector);
		layout.putConstraint(SpringLayout.SOUTH, visibleViewer, -VIEWER_SPACE, SpringLayout.SOUTH, mainPanel);
		layout.putConstraint(SpringLayout.WEST, visibleViewer, VIEWER_SPACE, SpringLayout.WEST, mainPanel);
		layout.putConstraint(SpringLayout.EAST, visibleViewer, -VIEWER_SPACE, SpringLayout.EAST, mainPanel);
		mainPanel.add(visibleViewer, new Integer(2));
		SwingUtilities.invokeLater(() -> {
			revalidate();
			repaint();
		});

	}

	private List<Integer> getNewColors(int i) {
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
					// System.err.println(confusion[i][j]);
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
				idMap.put(idx, oldIDMap.get(assignment[idx][1]));
			} else
				idMap.put(idx, assignment[idx][1]);
		}

		viewers.get(i).getPointContainer().saveIDMap(idMap);

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

}
