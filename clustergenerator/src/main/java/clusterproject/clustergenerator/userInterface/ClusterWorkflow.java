package clusterproject.clustergenerator.userInterface;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import clusterproject.clustergenerator.data.ClusteringResult;
import clusterproject.clustergenerator.data.PointContainer;
import clusterproject.clustergenerator.userInterface.Clustering.DBScan;
import clusterproject.clustergenerator.userInterface.Clustering.DiSHClustering;
import clusterproject.clustergenerator.userInterface.Clustering.IClusterer;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;

public class ClusterWorkflow extends JFrame {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final int OUTER_SPACE = 20;
	private static final int OPTIONS_WIDTH = 200;

	private final SpringLayout layout;

	private final PointContainer pointContainer;

	private IClusterer selectedClusterer;

	private final List<IClusterer> clusterers;

	private final List<IClusterer> workflow;

	private final JComboBox<String> clustererSelector;
	private final JLabel wfLabel;

	private final JButton confirmClustererButton;
	private final JButton executeClusterersButton;
	private final JLayeredPane mainPanel;
	private JScrollPane wfScrollPane;

	public ClusterWorkflow(PointContainer container) {
		pointContainer = container;
		mainPanel = new JLayeredPane();
		add(mainPanel);

		getContentPane().setBackground(MainWindow.BACKGROUND_COLOR);
		clusterers = new ArrayList<IClusterer>();
		workflow = new ArrayList<IClusterer>();
		layout = new SpringLayout();
		mainPanel.setLayout(layout);
		final JLabel addLabel = new JLabel("Add");
		mainPanel.add(addLabel, new Integer(1));
		layout.putConstraint(SpringLayout.NORTH, addLabel, OUTER_SPACE, SpringLayout.NORTH, mainPanel);
		layout.putConstraint(SpringLayout.WEST, addLabel, OUTER_SPACE, SpringLayout.WEST, mainPanel);

		initClusterers();
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

		wfLabel = new JLabel("Workflow:");

		layout.putConstraint(SpringLayout.NORTH, wfLabel, OUTER_SPACE, SpringLayout.SOUTH, clustererSelector);
		layout.putConstraint(SpringLayout.WEST, wfLabel, OUTER_SPACE, SpringLayout.WEST, mainPanel);
		wfLabel.setVisible(false);
		mainPanel.add(wfLabel, new Integer(1));

		executeClusterersButton = new JButton("Execute Workflow");
		executeClusterersButton.addActionListener(e -> executeWorkflow());
		executeClusterersButton.setVisible(false);

		layout.putConstraint(SpringLayout.SOUTH, executeClusterersButton, -OUTER_SPACE, SpringLayout.SOUTH, mainPanel);
		layout.putConstraint(SpringLayout.EAST, executeClusterersButton, -OPTIONS_WIDTH - 3 * OUTER_SPACE,
				SpringLayout.EAST, mainPanel);
		mainPanel.add(executeClusterersButton, new Integer(1));

		confirmClustererButton = new JButton("Confirm");
		confirmClustererButton.addActionListener(e -> addToWorkflow());
		layout.putConstraint(SpringLayout.SOUTH, confirmClustererButton, -OUTER_SPACE, SpringLayout.SOUTH, mainPanel);
		layout.putConstraint(SpringLayout.WEST, confirmClustererButton, -OPTIONS_WIDTH - OUTER_SPACE, SpringLayout.EAST,
				mainPanel);
		layout.putConstraint(SpringLayout.EAST, confirmClustererButton, -OUTER_SPACE, SpringLayout.EAST, mainPanel);
		mainPanel.add(confirmClustererButton, new Integer(1));
		openClustererSettings(clusterers.get(0).getName());

	}

	private void addToWorkflow() {
		workflow.add(selectedClusterer);
		showWorkflow();
		openClustererSettings(selectedClusterer.getName());

	}

	private void executeWorkflow() {
		final List<ClusteringResult> clusterings = new ArrayList<ClusteringResult>();
		double[][] data = new double[pointContainer.getPoints().size()][];
		data = pointContainer.getPoints().toArray(data);
		final DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data);
		final Database db = new StaticArrayDatabase(dbc, null);
		db.initialize();
		for (final IClusterer clusterer : workflow) {
			final List<ClusteringResult> results = clusterer.cluster(db);
			clusterings.addAll(results);
		}

		final ClusteringViewer cv = new ClusteringViewer(clusterings, pointContainer);
		cv.setSize(new Dimension(800, 600));
		cv.setExtendedState(JFrame.MAXIMIZED_BOTH);
		cv.setLocationRelativeTo(null);
		cv.setVisible(true);

	}

	private void openClustererSettings(String name) {
		// confirmClustererButton.setVisible(false);
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
		if (workflow.isEmpty()) {
			mainPanel.remove(wfScrollPane);
			wfLabel.setVisible(false);
			executeClusterersButton.setVisible(false);
			return;
		}
		wfLabel.setVisible(true);
		executeClusterersButton.setVisible(true);
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
		layout.putConstraint(SpringLayout.SOUTH, wfScrollPane, -OUTER_SPACE, SpringLayout.SOUTH, mainPanel);
		layout.putConstraint(SpringLayout.WEST, wfScrollPane, OUTER_SPACE, SpringLayout.WEST, mainPanel);
		layout.putConstraint(SpringLayout.EAST, wfScrollPane, -2 * OUTER_SPACE - OPTIONS_WIDTH, SpringLayout.EAST,
				mainPanel);

		// layout.putConstraint(SpringLayout.NORTH, wfPanel, 0, SpringLayout.NORTH,
		// wfScrollPane);
		// layout.putConstraint(SpringLayout.SOUTH, wfPanel, 0, SpringLayout.SOUTH,
		// wfScrollPane);
		// layout.putConstraint(SpringLayout.WEST, wfPanel, 0, SpringLayout.WEST,
		// wfScrollPane);
		// layout.putConstraint(SpringLayout.EAST, wfPanel, 0, SpringLayout.EAST,
		// wfScrollPane);

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

	}

	private void removeFromWorkflow(IClusterer clusterer) {
		workflow.remove(clusterer);
		showWorkflow();
		SwingUtilities.invokeLater(() -> {
			revalidate();
			repaint();
		});

	}

	private void initClusterers() {
		clusterers.add(new DBScan());
		clusterers.add(new DiSHClustering());
	}

}
