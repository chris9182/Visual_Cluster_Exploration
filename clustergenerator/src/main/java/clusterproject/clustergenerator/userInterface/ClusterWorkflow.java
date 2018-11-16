package clusterproject.clustergenerator.userInterface;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import clusterproject.clustergenerator.data.PointContainer;
import clusterproject.clustergenerator.userInterface.Clustering.DBScan;
import clusterproject.clustergenerator.userInterface.Clustering.IClusterer;

public class ClusterWorkflow extends JFrame {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final int OUTER_SPACE = 20;
	private static final int OPTIONS_WIDTH = 200;

	private final SpringLayout layout;

	private IClusterer selectedClusterer;

	private final List<IClusterer> clusterers;

	private final List<IClusterer> workflow;

	private final JComboBox<String> clustererSelector;

	private final JButton confirmClustererButton;
	private final JLayeredPane mainFrame;

	public ClusterWorkflow(PointContainer container) {

		mainFrame = new JLayeredPane();
		add(mainFrame);

		getContentPane().setBackground(MainWindow.BACKGROUND_COLOR);
		clusterers = new ArrayList<IClusterer>();
		workflow = new ArrayList<IClusterer>();
		layout = new SpringLayout();
		mainFrame.setLayout(layout);
		final JLabel addLabel = new JLabel("Add");
		mainFrame.add(addLabel, new Integer(1));
		layout.putConstraint(SpringLayout.NORTH, addLabel, OUTER_SPACE, SpringLayout.NORTH, mainFrame);
		layout.putConstraint(SpringLayout.WEST, addLabel, OUTER_SPACE, SpringLayout.WEST, mainFrame);

		initClusterers();
		final String[] names = new String[clusterers.size()];
		for (int i = 0; i < names.length; ++i)
			names[i] = clusterers.get(i).getName();
		clustererSelector = new JComboBox<>(names);
		mainFrame.add(clustererSelector, new Integer(1));
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, clustererSelector, 0, SpringLayout.VERTICAL_CENTER,
				addLabel);
		layout.putConstraint(SpringLayout.WEST, clustererSelector, 2 * MainWindow.INNER_SPACE, SpringLayout.EAST,
				addLabel);

		clustererSelector.addActionListener(e -> openClustererSettings((String) clustererSelector.getSelectedItem()));

		confirmClustererButton = new JButton("Confirm");
		layout.putConstraint(SpringLayout.SOUTH, confirmClustererButton, -OUTER_SPACE, SpringLayout.SOUTH, mainFrame);
		layout.putConstraint(SpringLayout.WEST, confirmClustererButton, -OPTIONS_WIDTH - OUTER_SPACE, SpringLayout.EAST,
				mainFrame);
		layout.putConstraint(SpringLayout.EAST, confirmClustererButton, -OUTER_SPACE, SpringLayout.EAST, mainFrame);
		mainFrame.add(confirmClustererButton, new Integer(1));
		openClustererSettings(clusterers.get(0).getName());

	}

	private void openClustererSettings(String name) {
		// confirmClustererButton.setVisible(false);
		if (selectedClusterer != null) {
			mainFrame.remove(selectedClusterer.getOptionsPanel());
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

		layout.putConstraint(SpringLayout.NORTH, options, OUTER_SPACE, SpringLayout.NORTH, mainFrame);
		layout.putConstraint(SpringLayout.EAST, options, -OUTER_SPACE, SpringLayout.EAST, mainFrame);
		layout.putConstraint(SpringLayout.WEST, options, -OUTER_SPACE - OPTIONS_WIDTH, SpringLayout.EAST, mainFrame);
		layout.putConstraint(SpringLayout.SOUTH, options, -MainWindow.INNER_SPACE, SpringLayout.NORTH,
				confirmClustererButton);

		options.setVisible(true);

		mainFrame.add(options, new Integer(1));
		System.err.println(options);
		SwingUtilities.invokeLater(() -> {
			revalidate();
			repaint();
		});

	}

	private void initClusterers() {
		clusterers.add(new DBScan());
	}

}
