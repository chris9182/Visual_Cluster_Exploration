package clusterproject.clustergenerator.userInterface;

import java.awt.Color;
import java.awt.Dimension;
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
import clusterproject.clustergenerator.userInterface.ComboBox.BlockComboListener;
import clusterproject.clustergenerator.userInterface.ComboBox.ComboBoxRenderer;
import clusterproject.clustergenerator.userInterface.DimensionalityReduction.IDimensionalityReduction;
import clusterproject.clustergenerator.userInterface.DimensionalityReduction.PCAReducer;
import clusterproject.clustergenerator.userInterface.DimensionalityReduction.TSNEReducer;
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
	private final List<IDimensionalityReduction> reducers;
	private IGenerator activeGenerator = null;
	private IDimensionalityReduction activeReducer = null;
	final JComboBox<String> selector;
	private final PointContainer pointContainer;
	final ScatterPlot clusterViewer;
	private final JButton activationButton;
	private final JButton importButton;
	private final JButton scatterMatrixButton;

	private static final long serialVersionUID = 1L;

	public MainWindow() {
		this(new PointContainer(2));
	}

	public MainWindow(PointContainer container) {
		setTitle("Scatterplot Matrix");
		pointContainer = container;
		clusterViewer = new ScatterPlot(this, pointContainer, true);

		scatterMatrixButton = new JButton("Matrix");
		scatterMatrixButton.addActionListener(e -> {
			final ScatterPlotMatrix ms = new ScatterPlotMatrix(pointContainer);// XXX for testing
			ms.setSize(new Dimension(400, 400));
			ms.setLocationRelativeTo(null);
			ms.setVisible(true);
		});

		importButton = new JButton("Import");
		importButton.addActionListener(e -> {
			final JFrame importerFrame = new ImporterWindow(pointContainer, MainWindow.this);
			importerFrame.setSize(new Dimension(400, 400));
			importerFrame.setLocationRelativeTo(null);
			importerFrame.setVisible(true);
		});
		activationButton = new JButton();
		activationButton.addActionListener(e -> {
			boolean done = false;
			if (activeGenerator != null)
				done = activeGenerator.generate(pointContainer);
			if (activeReducer != null)
				done = activeReducer.reduce(pointContainer);
			if (done) {
				clusterViewer.autoAdjust();
				update();
			} else {
				// TODO:error
			}
		});

		mainFrame = new JLayeredPane();
		generators = new ArrayList<IGenerator>();
		reducers = new ArrayList<IDimensionalityReduction>();
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
		initReducers();

		final List<String> generatorNames = new ArrayList<String>();
		for (final IGenerator generator : generators)
			generatorNames.add(generator.getName());

		final List<String> reducerNames = new ArrayList<String>();
		for (final IDimensionalityReduction reducer : reducers)
			reducerNames.add(reducer.getName());

		final String[][] elements = new String[2][];
		elements[0] = generatorNames.toArray(new String[generatorNames.size()]);
		elements[1] = reducerNames.toArray(new String[reducerNames.size()]);

		selector = new JComboBox<String>(ComboBoxRenderer.makeVectorData(elements));
		selector.setRenderer(new ComboBoxRenderer());
		selector.addActionListener(new BlockComboListener(selector));
		selector.addActionListener(e -> setActiveElement());

		mainLayout.putConstraint(SpringLayout.NORTH, selector, INNER_SPACE, SpringLayout.NORTH, mainFrame);
		mainLayout.putConstraint(SpringLayout.EAST, selector, -INNER_SPACE, SpringLayout.EAST, mainFrame);

		mainLayout.putConstraint(SpringLayout.NORTH, clusterViewer, INNER_SPACE, SpringLayout.NORTH, mainFrame);
		mainLayout.putConstraint(SpringLayout.EAST, clusterViewer, 2 * -INNER_SPACE - OPTIONS_WIDTH, SpringLayout.EAST,
				mainFrame);
		mainLayout.putConstraint(SpringLayout.WEST, clusterViewer, INNER_SPACE, SpringLayout.WEST, mainFrame);
		mainLayout.putConstraint(SpringLayout.SOUTH, clusterViewer, -INNER_SPACE, SpringLayout.SOUTH, mainFrame);

		mainLayout.putConstraint(SpringLayout.NORTH, scatterMatrixButton, INNER_SPACE, SpringLayout.NORTH, mainFrame);
		mainLayout.putConstraint(SpringLayout.EAST, scatterMatrixButton, -INNER_SPACE, SpringLayout.WEST, selector);

		mainLayout.putConstraint(SpringLayout.SOUTH, importButton, -INNER_SPACE, SpringLayout.SOUTH, mainFrame);
		mainLayout.putConstraint(SpringLayout.EAST, importButton, -INNER_SPACE, SpringLayout.EAST, mainFrame);
		mainLayout.putConstraint(SpringLayout.WEST, importButton, -INNER_SPACE - OPTIONS_WIDTH, SpringLayout.EAST,
				mainFrame);

		mainLayout.putConstraint(SpringLayout.SOUTH, activationButton, -INNER_SPACE, SpringLayout.NORTH, importButton);
		mainLayout.putConstraint(SpringLayout.EAST, activationButton, -INNER_SPACE, SpringLayout.EAST, mainFrame);
		mainLayout.putConstraint(SpringLayout.WEST, activationButton, -INNER_SPACE - OPTIONS_WIDTH, SpringLayout.EAST,
				mainFrame);

		mainFrame.add(selector, new Integer(100));
		mainFrame.add(importButton, new Integer(101));
		mainFrame.add(scatterMatrixButton, new Integer(101));
		mainFrame.add(activationButton, new Integer(101));
		mainFrame.add(clusterViewer, new Integer(1));

		final JButton autoAdjust = new JButton("");
		autoAdjust.setToolTipText("Auto-Adjust Axies");
		autoAdjust.setPreferredSize(new Dimension(ADJUST_BUTTON_DIM, ADJUST_BUTTON_DIM));
		autoAdjust.addActionListener(e -> {
			clusterViewer.autoAdjust();
			SwingUtilities.invokeLater(() -> clusterViewer.repaint());

		});

		mainLayout.putConstraint(SpringLayout.SOUTH, autoAdjust, -1, SpringLayout.SOUTH, mainFrame);
		mainLayout.putConstraint(SpringLayout.WEST, autoAdjust, 1, SpringLayout.WEST, mainFrame);

		mainFrame.add(autoAdjust, new Integer(100));

		setActiveElement();

	}

	private void setActiveElement() {
		if (selector.getSelectedIndex() < generators.size()) {
			setActiveGenerator((String) selector.getSelectedItem());
			if (activeReducer != null)
				activeReducer.getOptionsPanel().setVisible(false);
		} else {
			setActiveDimReduction((String) selector.getSelectedItem());
			if (activeGenerator != null)
				activeGenerator.getOptionsPanel().setVisible(false);
		}
		SwingUtilities.invokeLater(() -> repaint());
	}

	private void setActiveDimReduction(String name) {
		IDimensionalityReduction newReducer = null;
		for (final IDimensionalityReduction reducer : reducers)
			if (reducer.getName().equals(name))
				newReducer = reducer;
		if (newReducer == null)
			return;
		if (activeGenerator != null) {
			mainFrame.remove(activeGenerator.getOptionsPanel());
			activeGenerator.getOptionsPanel().setVisible(false);
			activeGenerator = null;
		}
		if (activeReducer != null) {
			mainFrame.remove(activeReducer.getOptionsPanel());
			activeReducer.getOptionsPanel().setVisible(false);
		}
		activeReducer = newReducer;

		mainLayout.putConstraint(SpringLayout.NORTH, activeReducer.getOptionsPanel(), INNER_SPACE, SpringLayout.SOUTH,
				selector);
		mainLayout.putConstraint(SpringLayout.EAST, activeReducer.getOptionsPanel(), -INNER_SPACE, SpringLayout.EAST,
				mainFrame);
		mainLayout.putConstraint(SpringLayout.WEST, activeReducer.getOptionsPanel(), -INNER_SPACE - OPTIONS_WIDTH,
				SpringLayout.EAST, mainFrame);

		mainFrame.add(activeReducer.getOptionsPanel(), new Integer(1));
		activeReducer.getOptionsPanel().setVisible(true);
		activationButton.setText("Reduce");
		activationButton.setVisible(true);

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
		if (activeReducer != null) {
			mainFrame.remove(activeReducer.getOptionsPanel());
			activeReducer.getOptionsPanel().setVisible(false);
			activeReducer = null;
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
		activationButton.setText("Generate");
		activationButton.setVisible(activeGenerator.canSimpleGenerate());

	}

	private void initGenerators() {
		final IGenerator generator1 = new SinglePointGenerator();
		final IGenerator generator2 = new ELKIGenerator();
		generators.add(generator1);
		generators.add(generator2);

	}

	private void initReducers() {
		final IDimensionalityReduction reducer1 = new PCAReducer();
		final IDimensionalityReduction reducer2 = new TSNEReducer();
		reducers.add(reducer1);
		reducers.add(reducer2);

	}

	@Override
	public void handleClick(double[] point) {
		if (activeGenerator.canClickGenerate()) {
			final boolean done = activeGenerator.generate(point, pointContainer);
			if (done) {
				// special case, here we don't want auto-adjust of axies
				if (activeGenerator instanceof SinglePointGenerator)
					clusterViewer.update();
				else
					update();
			} else {
				// TODO:error
			}
		}
	}

	public void update() {
		clusterViewer.autoAdjust();
		clusterViewer.update();
	}

}
