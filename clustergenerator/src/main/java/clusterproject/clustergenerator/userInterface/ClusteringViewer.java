package clusterproject.clustergenerator.userInterface;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import clusterproject.clustergenerator.data.ClusteringResult;
import clusterproject.clustergenerator.data.PointContainer;
import clusterproject.clustergenerator.userInterface.MetaClustering.ClusteringWithDistance;
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
			newViewer.setSelectedDimX(visibleViewer.getSelectedDimX());
			newViewer.setSelectedDimY(visibleViewer.getSelectedDimY());
			newViewer.setIntervalX(visibleViewer.getIntervalX());
			newViewer.setIntervalY(visibleViewer.getIntervalY());
			mainPanel.remove(visibleViewer);
			visibleViewer = null;
		}
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

}
