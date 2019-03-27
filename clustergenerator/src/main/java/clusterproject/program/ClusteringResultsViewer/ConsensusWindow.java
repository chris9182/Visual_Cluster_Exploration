package clusterproject.program.ClusteringResultsViewer;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.SpringLayout;

import clusterproject.program.MainWindow;
import clusterproject.program.ClusterViewerElement.ScatterPlot;
import clusterproject.program.Consensus.CoAssociationMatrixThreshhold;

public class ConsensusWindow extends JFrame {

	private static final long serialVersionUID = 3528609759641911967L;

	private final JLayeredPane mainPanel = new JLayeredPane();
	private final SpringLayout mainLayout = new SpringLayout();

	public ConsensusWindow(ClusteringViewer clusteringViewer) {
		add(mainPanel);
		getContentPane().setBackground(MainWindow.BACKGROUND_COLOR);
		mainPanel.setLayout(mainLayout);
		final JLabel betaLabel = new JLabel(
				"BETA - currently only works with good preselection (magic threshhold in CoAssociationMatrixThreshhold.java)");
		mainPanel.add(betaLabel, new Integer(1));
		mainLayout.putConstraint(SpringLayout.NORTH, betaLabel, 0, SpringLayout.NORTH, this);
		mainLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, betaLabel, 0, SpringLayout.HORIZONTAL_CENTER,
				mainPanel);

		final CoAssociationMatrixThreshhold function = new CoAssociationMatrixThreshhold();
		final ScatterPlot plot = new ScatterPlot(function.calculateConsensus(clusteringViewer.getRelevantContainers()),
				true);
		plot.autoAdjust();
		mainPanel.add(plot, new Integer(1));
		mainLayout.putConstraint(SpringLayout.NORTH, plot, 0, SpringLayout.SOUTH, betaLabel);
		mainLayout.putConstraint(SpringLayout.WEST, plot, 0, SpringLayout.WEST, mainPanel);
		mainLayout.putConstraint(SpringLayout.EAST, plot, 0, SpringLayout.EAST, mainPanel);
		mainLayout.putConstraint(SpringLayout.SOUTH, plot, 0, SpringLayout.SOUTH, mainPanel);

	}

}
