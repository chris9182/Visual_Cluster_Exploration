package clusterproject.program.ClusteringResultsViewer;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.SpringLayout;

import clusterproject.data.PointContainer;
import clusterproject.program.StartWindow;
import clusterproject.program.ClusterViewerElement.ScatterPlot;
import clusterproject.program.Consensus.CoAssociationMatrixAverageLink;

public class ConsensusWindow extends JFrame {

	private static final long serialVersionUID = 3528609759641911967L;

	private final JLayeredPane mainPanel = new JLayeredPane();
	private final SpringLayout mainLayout = new SpringLayout();

	public ConsensusWindow(ClusteringViewer clusteringViewer) {
		add(mainPanel);
		getContentPane().setBackground(StartWindow.BACKGROUND_COLOR);
		mainPanel.setLayout(mainLayout);
		final JLabel betaLabel = new JLabel(
				"BETA - should work well in most cases (magic threshhold in CoAssociationMatrixAverageLink.java)");
		mainPanel.add(betaLabel, new Integer(1));
		mainLayout.putConstraint(SpringLayout.NORTH, betaLabel, 0, SpringLayout.NORTH, this);
		mainLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, betaLabel, 0, SpringLayout.HORIZONTAL_CENTER,
				mainPanel);

		final CoAssociationMatrixAverageLink function = new CoAssociationMatrixAverageLink();

		final List<PointContainer> pointContainers = clusteringViewer.getRelevantContainers();
//		System.err.println(pointContainers.size());
		final List<Double> weights = null;
		// weights = clusteringViewer.getRelevantWeightsAcrossMethods();
		// weights = clusteringViewer.getRelevantWeightsAcrossMetaClusters();
		// System.err.println(pointContainers.size() + " " + weights.size());
		final ScatterPlot plot = new ScatterPlot(function.calculateConsensus(pointContainers, weights), true);
		plot.setSelectedDimX(clusteringViewer.getVisibleViewer().getSelectedDimX());
		plot.setSelectedDimY(clusteringViewer.getVisibleViewer().getSelectedDimY());
		plot.autoAdjust();
		mainPanel.add(plot, new Integer(1));
		mainLayout.putConstraint(SpringLayout.NORTH, plot, 0, SpringLayout.SOUTH, betaLabel);
		mainLayout.putConstraint(SpringLayout.WEST, plot, 0, SpringLayout.WEST, mainPanel);
		mainLayout.putConstraint(SpringLayout.EAST, plot, 0, SpringLayout.EAST, mainPanel);
		mainLayout.putConstraint(SpringLayout.SOUTH, plot, 0, SpringLayout.SOUTH, mainPanel);

	}

}
