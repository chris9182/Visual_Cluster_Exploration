package clusterproject.clustergenerator.program.ClusteringResultsViewer;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.SpringLayout;

import clusterproject.clustergenerator.program.MainWindow;

public class ConsensusWindow extends JFrame {

	private static final long serialVersionUID = 3528609759641911967L;

	private final JLayeredPane mainPanel = new JLayeredPane();
	private final SpringLayout mainLayout = new SpringLayout();

	public ConsensusWindow(ClusteringViewer clusteringViewer) {
		add(mainPanel);
		getContentPane().setBackground(MainWindow.BACKGROUND_COLOR);
		mainPanel.setLayout(mainLayout);
		final JLabel betaLabel = new JLabel("BETA");
		mainPanel.add(betaLabel, new Integer(1));
		mainLayout.putConstraint(SpringLayout.NORTH, betaLabel, 0, SpringLayout.NORTH, this);
		mainLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, betaLabel, 0, SpringLayout.HORIZONTAL_CENTER,
				mainPanel);
	}

}
