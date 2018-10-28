package clusterproject.clustergenerator.userInterface;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import clusterproject.clustergenerator.data.PointContainer;
import clusterproject.clustergenerator.userInterface.Generator.CSVGenerator;
import clusterproject.clustergenerator.userInterface.Generator.ELKIGenerator;
import clusterproject.clustergenerator.userInterface.Generator.IGenerator;
import clusterproject.clustergenerator.userInterface.Generator.SinglePointGenerator;

public class MainWindow extends JFrame implements IClickHandler {

	/**
	 *
	 */
	private static final int INNER_SPACE = 4;
	public static final int OPTIONS_WIDTH = 200;

	private static final int ADJUST_BUTTON_DIM = 20;
	public static final int GENERATOR_BUTTON_HEIGHT = 200;
	public static final Color BACKGROUND_COLOR = Color.white;

	private final JLayeredPane mainFrame;
	private final SpringLayout mainLayout;
	private final List<IGenerator> generators;
	private IGenerator activeGenerator = null;
	final JComboBox<String> selector;
	private final PointContainer pointContainer;
	final ClusterViewer clusterViewer;
	private final int dim = 2;
	private final JButton generateButton;

	private static final long serialVersionUID = 1L;

	public MainWindow() {
		pointContainer = new PointContainer(2);
		final List<String> headers = new ArrayList<String>();
		headers.add("y");
		headers.add("x");
		generateButton = new JButton("generate");
		generateButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final boolean done = activeGenerator.generate(pointContainer);
				if (done) {
					clusterViewer.autoAdjust();
					clusterViewer.update();
				} else {
					// TODO:error
				}
			}
		});
		pointContainer.setHeaders(headers);
		mainFrame = new JLayeredPane();
		generators = new ArrayList<IGenerator>();
		add(mainFrame);
		mainLayout = new SpringLayout();
		mainFrame.setLayout(mainLayout);

		final JPanel background = new JPanel();
		background.setBackground(BACKGROUND_COLOR);

		mainLayout.putConstraint(SpringLayout.NORTH, background, 0, SpringLayout.NORTH, mainFrame);
		mainLayout.putConstraint(SpringLayout.EAST, background, 0, SpringLayout.EAST, mainFrame);
		mainLayout.putConstraint(SpringLayout.WEST, background, 0, SpringLayout.WEST, mainFrame);
		mainLayout.putConstraint(SpringLayout.SOUTH, background, 0, SpringLayout.SOUTH, mainFrame);

		mainFrame.add(background, new Integer(0));

		initGenerators();

		clusterViewer = new ClusterViewer(this, pointContainer);

		final List<String> names = new ArrayList<String>();
		for (final IGenerator generator : generators)
			names.add(generator.getName());
		selector = new JComboBox<String>(names.toArray(new String[names.size()]));
		selector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setActiveGenerator((String) selector.getSelectedItem());
			}
		});

		mainLayout.putConstraint(SpringLayout.NORTH, selector, INNER_SPACE, SpringLayout.NORTH, mainFrame);
		mainLayout.putConstraint(SpringLayout.EAST, selector, -INNER_SPACE, SpringLayout.EAST, mainFrame);

		mainLayout.putConstraint(SpringLayout.NORTH, clusterViewer, INNER_SPACE, SpringLayout.NORTH, mainFrame);
		mainLayout.putConstraint(SpringLayout.EAST, clusterViewer, 2 * -INNER_SPACE - OPTIONS_WIDTH, SpringLayout.EAST,
				mainFrame);
		mainLayout.putConstraint(SpringLayout.WEST, clusterViewer, INNER_SPACE, SpringLayout.WEST, mainFrame);
		mainLayout.putConstraint(SpringLayout.SOUTH, clusterViewer, -INNER_SPACE, SpringLayout.SOUTH, mainFrame);

		mainLayout.putConstraint(SpringLayout.SOUTH, generateButton, -INNER_SPACE, SpringLayout.SOUTH, mainFrame);
		mainLayout.putConstraint(SpringLayout.EAST, generateButton, -INNER_SPACE, SpringLayout.EAST, mainFrame);
		mainLayout.putConstraint(SpringLayout.WEST, generateButton, -INNER_SPACE - OPTIONS_WIDTH, SpringLayout.EAST,
				mainFrame);

		mainFrame.add(selector, new Integer(100));
		mainFrame.add(generateButton, new Integer(101));
		mainFrame.add(clusterViewer, new Integer(1));

		final JButton autoAdjust = new JButton("");
		autoAdjust.setToolTipText("Auto-Adjust Axies");
		autoAdjust.setPreferredSize(new Dimension(ADJUST_BUTTON_DIM, ADJUST_BUTTON_DIM));
		autoAdjust.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				clusterViewer.autoAdjust();
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						clusterViewer.repaint();

					}
				});

			}
		});

		mainLayout.putConstraint(SpringLayout.SOUTH, autoAdjust, -1, SpringLayout.SOUTH, mainFrame);
		mainLayout.putConstraint(SpringLayout.WEST, autoAdjust, 1, SpringLayout.WEST, mainFrame);

		mainFrame.add(autoAdjust, new Integer(100));

		setActiveGenerator(generators.get(0).getName());
	}

	private void setActiveGenerator(String name) {
		IGenerator newGenerator = null;
		for (final IGenerator generator : generators)
			if (generator.getName().equals(name))
				newGenerator = generator;
		if (newGenerator == null)
			return;
		if (activeGenerator != null) {
			mainFrame.remove(activeGenerator.getOptionsPanel());
			activeGenerator.getOptionsPanel().setVisible(false);
		}
		activeGenerator = newGenerator;

		mainLayout.putConstraint(SpringLayout.NORTH, activeGenerator.getOptionsPanel(), INNER_SPACE, SpringLayout.SOUTH,
				selector);
		mainLayout.putConstraint(SpringLayout.EAST, activeGenerator.getOptionsPanel(), -INNER_SPACE, SpringLayout.EAST,
				mainFrame);
		mainLayout.putConstraint(SpringLayout.WEST, activeGenerator.getOptionsPanel(), -INNER_SPACE - OPTIONS_WIDTH,
				SpringLayout.EAST, mainFrame);

		mainFrame.add(activeGenerator.getOptionsPanel(), new Integer(1));
		activeGenerator.getOptionsPanel().setVisible(true);
		generateButton.setVisible(activeGenerator.canSimpleGenerate());
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				repaint();
			}
		});

	}

	private void initGenerators() {
		final IGenerator generator1 = new SinglePointGenerator();
		final IGenerator generator2 = new CSVGenerator();
		final IGenerator generator3 = new ELKIGenerator();
		generators.add(generator1);
		generators.add(generator2);
		generators.add(generator3);

	}

	@Override
	public void handleClick(double[] point) {
		if (activeGenerator.canClickGenerate()) {
			final boolean done = activeGenerator.generate(point, pointContainer);
			if (done)
				clusterViewer.update();
			else {
				// TODO:error
			}
		}
	}

}
