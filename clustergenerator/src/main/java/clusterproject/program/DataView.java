package clusterproject.program;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import clusterproject.data.PointContainer;
import clusterproject.program.ClusterViewerElement.ScatterPlot;
import clusterproject.program.ClusterViewerElement.ScatterPlotMatrix;
import clusterproject.program.ComboBox.BlockComboListener;
import clusterproject.program.ComboBox.ComboBoxRenderer;
import clusterproject.program.DimensionalityReduction.DimensionRemover;
import clusterproject.program.DimensionalityReduction.IReducer;
import clusterproject.program.DimensionalityReduction.PCAReducer;
import clusterproject.program.DimensionalityReduction.TSNEReducer;
import clusterproject.program.Generator.ELKIGenerator;
import clusterproject.program.Generator.IGenerator;
import clusterproject.program.Generator.SinglePointGenerator;
import clusterproject.program.Normalizers.INormalizer;
import clusterproject.program.Normalizers.Normalize;
import clusterproject.program.Normalizers.Standardize;
import clusterproject.util.FileUtils;

public class DataView extends JFrame {

	public static final int INNER_SPACE = 4;
	public static final int OPTIONS_WIDTH = 200;

	public static final int ADJUST_BUTTON_DIM = 16;
	public static final int GENERATOR_BUTTON_HEIGHT = 200;
	public static final Color BACKGROUND_COLOR = Color.white;

	private final static List<IGenerator> generators = new ArrayList<IGenerator>();
	private final static List<IReducer> reducers = new ArrayList<IReducer>();
	private final static List<INormalizer> normalizers = new ArrayList<INormalizer>();
	static {
		initGenerators();
		initReducers();
		initNormalizers();
	}
	private final JLayeredPane mainPanel;
	private final SpringLayout mainLayout;
	private IGenerator activeGenerator = null;
	private IReducer activeReducer = null;
	private INormalizer activeNormalizer = null;
	final JComboBox<String> selector;
	private final PointContainer pointContainer;
	final ScatterPlot clusterViewer;
	private final JButton activationButton;
	private final JButton importButton;
	private final JButton exportButton;
	private final JButton scatterMatrixButton;
	private final JButton clusterButton;

	private static final long serialVersionUID = 1L;

	public DataView() {
		this(new PointContainer(2));
	}

	public DataView(PointContainer container) {
		setTitle("Data-View");
		pointContainer = container;
		clusterViewer = new ScatterPlot(pointContainer, true);
		clusterViewer.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.gray));
		clusterViewer.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				final double[] translation = clusterViewer.getCoordinates(e.getPoint());
				handleClick(translation);
			}

		});

		clusterButton = new JButton("Clustering");
		clusterButton.addActionListener(e -> {
			final ClusterWorkflow cw = new ClusterWorkflow(pointContainer);
			cw.setSize(new Dimension(800, 600));
			cw.setExtendedState(Frame.MAXIMIZED_BOTH);
			cw.setLocationRelativeTo(null);
			cw.setVisible(true);
		});

		scatterMatrixButton = new JButton("Matrix");
		scatterMatrixButton.addActionListener(e -> {
			final ScatterPlotMatrix ms = new ScatterPlotMatrix(pointContainer);
			ms.setSize(new Dimension(800, 600));
			ms.setExtendedState(Frame.MAXIMIZED_BOTH);
			ms.setLocationRelativeTo(null);
			ms.setVisible(true);
		});

		importButton = new JButton("Import");
		importButton.addActionListener(e -> {
			final JFrame importerFrame = new ImporterWindow(pointContainer, DataView.this);
			importerFrame.setSize(new Dimension(450, 400));
			importerFrame.setResizable(false);
			importerFrame.setLocationRelativeTo(null);
			importerFrame.setVisible(true);
		});

		exportButton = new JButton("Export");
		exportButton.addActionListener(e -> {
			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.addChoosableFileFilter(FileUtils.csvfilter);
			fileChooser.setApproveButtonText("Save");
			fileChooser.setFileFilter(FileUtils.csvfilter);
			final JFrame chooserFrame = new JFrame();
			chooserFrame.setTitle("Export");
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

				if (FileUtils.csvfilter.accept(selectedFile)) {
					saveCSVFile(selectedFile);

				} else if (!selectedFile.getName().contains(".")
						&& fileChooser.getFileFilter().equals(FileUtils.csvfilter)) {
					saveCSVFile(new File(selectedFile.getPath() + "." + FileUtils.csvfilter.getExtensions()[0]));
				} else {
					return;
				}
				chooserFrame.setVisible(false);
				chooserFrame.dispose();
			});
		});

		activationButton = new JButton();
		activationButton.addActionListener(e -> {
			boolean done = false;
			if (activeGenerator != null) {
				done = activeGenerator.generate(pointContainer);
				pointContainer.checkRemoveClusterInfo();
			}
			if (activeReducer != null)
				done = activeReducer.reduce(pointContainer);
			if (activeNormalizer != null)
				done = activeNormalizer.normalize(pointContainer);
			if (done) {
				clusterViewer.autoAdjust();
				update();
			} else {
				// TODO:error
			}
		});

		mainPanel = new JLayeredPane();

		add(mainPanel);
		mainLayout = new SpringLayout();
		mainPanel.setLayout(mainLayout);

		getContentPane().setBackground(BACKGROUND_COLOR);

		final List<String> generatorNames = new ArrayList<String>();
		for (final IGenerator generator : generators)
			generatorNames.add(generator.getName());

		final List<String> reducerNames = new ArrayList<String>();
		for (final IReducer reducer : reducers)
			reducerNames.add(reducer.getName());

		final List<String> normalizerNames = new ArrayList<String>();
		for (final INormalizer normalizer : normalizers)
			normalizerNames.add(normalizer.getName());

		final String[][] elements = new String[3][];
		elements[0] = generatorNames.toArray(new String[generatorNames.size()]);
		elements[1] = reducerNames.toArray(new String[reducerNames.size()]);
		elements[2] = normalizerNames.toArray(new String[normalizerNames.size()]);

		selector = new JComboBox<String>(ComboBoxRenderer.makeVectorData(elements));
		selector.setRenderer(new ComboBoxRenderer());
		selector.addActionListener(new BlockComboListener(selector));
		selector.addActionListener(e -> setActiveElement());

		mainLayout.putConstraint(SpringLayout.NORTH, selector, INNER_SPACE, SpringLayout.NORTH, mainPanel);
		mainLayout.putConstraint(SpringLayout.EAST, selector, -INNER_SPACE, SpringLayout.EAST, mainPanel);

		mainLayout.putConstraint(SpringLayout.NORTH, clusterViewer, INNER_SPACE, SpringLayout.NORTH, mainPanel);
		mainLayout.putConstraint(SpringLayout.EAST, clusterViewer, 2 * -INNER_SPACE - OPTIONS_WIDTH, SpringLayout.EAST,
				mainPanel);
		mainLayout.putConstraint(SpringLayout.WEST, clusterViewer, INNER_SPACE, SpringLayout.WEST, mainPanel);
		mainLayout.putConstraint(SpringLayout.SOUTH, clusterViewer, -INNER_SPACE, SpringLayout.SOUTH, mainPanel);

		mainLayout.putConstraint(SpringLayout.NORTH, scatterMatrixButton, INNER_SPACE, SpringLayout.NORTH, mainPanel);
		mainLayout.putConstraint(SpringLayout.EAST, scatterMatrixButton, -INNER_SPACE, SpringLayout.WEST, selector);

		mainLayout.putConstraint(SpringLayout.SOUTH, exportButton, -INNER_SPACE, SpringLayout.NORTH, clusterButton);
		mainLayout.putConstraint(SpringLayout.EAST, exportButton, -INNER_SPACE, SpringLayout.EAST, mainPanel);
		mainLayout.putConstraint(SpringLayout.WEST, exportButton, -INNER_SPACE - OPTIONS_WIDTH / 2, SpringLayout.EAST,
				mainPanel);

		mainLayout.putConstraint(SpringLayout.SOUTH, importButton, -INNER_SPACE, SpringLayout.NORTH, clusterButton);
		mainLayout.putConstraint(SpringLayout.EAST, importButton, -INNER_SPACE - OPTIONS_WIDTH / 2, SpringLayout.EAST,
				mainPanel);
		mainLayout.putConstraint(SpringLayout.WEST, importButton, -INNER_SPACE - OPTIONS_WIDTH, SpringLayout.EAST,
				mainPanel);

		mainLayout.putConstraint(SpringLayout.SOUTH, clusterButton, -INNER_SPACE, SpringLayout.SOUTH, mainPanel);
		mainLayout.putConstraint(SpringLayout.EAST, clusterButton, -INNER_SPACE, SpringLayout.EAST, mainPanel);
		mainLayout.putConstraint(SpringLayout.WEST, clusterButton, -INNER_SPACE - OPTIONS_WIDTH, SpringLayout.EAST,
				mainPanel);

		mainLayout.putConstraint(SpringLayout.SOUTH, activationButton, -INNER_SPACE, SpringLayout.NORTH, importButton);
		mainLayout.putConstraint(SpringLayout.EAST, activationButton, -INNER_SPACE, SpringLayout.EAST, mainPanel);
		mainLayout.putConstraint(SpringLayout.WEST, activationButton, -INNER_SPACE - OPTIONS_WIDTH, SpringLayout.EAST,
				mainPanel);

		mainPanel.add(selector, new Integer(100));
		mainPanel.add(importButton, new Integer(101));
		mainPanel.add(exportButton, new Integer(101));
		mainPanel.add(scatterMatrixButton, new Integer(101));
		mainPanel.add(clusterButton, new Integer(101));
		mainPanel.add(activationButton, new Integer(101));
		mainPanel.add(clusterViewer, new Integer(1));

		clusterViewer.addAutoAdjust();

		setActiveElement();

	}

	private void setActiveElement() {
		// System.err.println(selector.getSelectedIndex() + " " + (generators.size() +
		// reducers.size()));
		if (selector.getSelectedIndex() < generators.size()) {
			setActiveGenerator((String) selector.getSelectedItem());
		} else if (selector.getSelectedIndex() < generators.size() + reducers.size() + 1
				&& selector.getSelectedIndex() > generators.size()) {
			setActiveDimReduction((String) selector.getSelectedItem());
		} else if (selector.getSelectedIndex() < generators.size() + reducers.size() + normalizers.size() + 2
				&& selector.getSelectedIndex() > generators.size() + reducers.size() + 1) {
			setActiveNormalizer((String) selector.getSelectedItem());
		}
		SwingUtilities.invokeLater(() -> {
			revalidate();
			repaint();
		});
	}

	private void setActiveNormalizer(String name) {
		INormalizer newNormalizer = null;
		for (final INormalizer normalizer : normalizers)
			if (normalizer.getName().equals(name))
				newNormalizer = normalizer;
		if (newNormalizer == null)
			return;

		removeActive();

		activeNormalizer = newNormalizer;

		mainLayout.putConstraint(SpringLayout.NORTH, activeNormalizer.getOptionsPanel(), INNER_SPACE,
				SpringLayout.SOUTH, selector);
		mainLayout.putConstraint(SpringLayout.EAST, activeNormalizer.getOptionsPanel(), -INNER_SPACE, SpringLayout.EAST,
				mainPanel);
		mainLayout.putConstraint(SpringLayout.WEST, activeNormalizer.getOptionsPanel(), -INNER_SPACE - OPTIONS_WIDTH,
				SpringLayout.EAST, mainPanel);
		mainLayout.putConstraint(SpringLayout.SOUTH, activeNormalizer.getOptionsPanel(), -INNER_SPACE,
				SpringLayout.NORTH, activationButton);

		mainPanel.add(activeNormalizer.getOptionsPanel(), new Integer(1));
		activeNormalizer.getOptionsPanel().setVisible(true);
		activationButton.setText("Apply");
		activationButton.setVisible(true);

	}

	private void removeActive() {
		if (activeGenerator != null) {
			mainPanel.remove(activeGenerator.getOptionsPanel());
			activeGenerator.getOptionsPanel().setVisible(false);
			activeGenerator = null;
		}
		if (activeReducer != null) {
			mainPanel.remove(activeReducer.getOptionsPanel());
			activeReducer.getOptionsPanel().setVisible(false);
			activeReducer = null;
		}
		if (activeNormalizer != null) {
			mainPanel.remove(activeNormalizer.getOptionsPanel());
			activeNormalizer.getOptionsPanel().setVisible(false);
			activeNormalizer = null;
		}

	}

	private void setActiveDimReduction(String name) {
		IReducer newReducer = null;
		for (final IReducer reducer : reducers)
			if (reducer.getName().equals(name))
				newReducer = reducer;
		if (newReducer == null)
			return;
		removeActive();
		activeReducer = newReducer;

		mainLayout.putConstraint(SpringLayout.NORTH, activeReducer.getOptionsPanel(), INNER_SPACE, SpringLayout.SOUTH,
				selector);
		mainLayout.putConstraint(SpringLayout.EAST, activeReducer.getOptionsPanel(), -INNER_SPACE, SpringLayout.EAST,
				mainPanel);
		mainLayout.putConstraint(SpringLayout.WEST, activeReducer.getOptionsPanel(), -INNER_SPACE - OPTIONS_WIDTH,
				SpringLayout.EAST, mainPanel);
		mainLayout.putConstraint(SpringLayout.SOUTH, activeReducer.getOptionsPanel(), -INNER_SPACE, SpringLayout.NORTH,
				activationButton);

		mainPanel.add(activeReducer.getOptionsPanel(), new Integer(1));
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
		removeActive();
		activeGenerator = newGenerator;

		mainLayout.putConstraint(SpringLayout.NORTH, activeGenerator.getOptionsPanel(), INNER_SPACE, SpringLayout.SOUTH,
				selector);
		mainLayout.putConstraint(SpringLayout.EAST, activeGenerator.getOptionsPanel(), -INNER_SPACE, SpringLayout.EAST,
				mainPanel);
		mainLayout.putConstraint(SpringLayout.WEST, activeGenerator.getOptionsPanel(), -INNER_SPACE - OPTIONS_WIDTH,
				SpringLayout.EAST, mainPanel);
		mainLayout.putConstraint(SpringLayout.SOUTH, activeGenerator.getOptionsPanel(), -INNER_SPACE,
				SpringLayout.NORTH, activationButton);

		mainPanel.add(activeGenerator.getOptionsPanel(), new Integer(1));
		activeGenerator.getOptionsPanel().setVisible(true);
		activationButton.setText("Generate");
		activationButton.setVisible(activeGenerator.canSimpleGenerate());

	}

	public void handleClick(double[] point) {
		if (activeGenerator != null && activeGenerator.canClickGenerate()) {
			final boolean done = activeGenerator.generate(point, pointContainer);
			if (done) {
				pointContainer.removeClusterInfo();
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

	private void saveCSVFile(File selectedFile) {
		if (pointContainer.getPointCount() < 1 || pointContainer.getDim() < 1)
			return;
		if (pointContainer.hasClusters())
			FileUtils.saveCSVFileWithClass(selectedFile, pointContainer.toData(), pointContainer.getHeaders());
		else
			FileUtils.saveCSVFileWithoutClass(selectedFile, pointContainer.toData(), pointContainer.getHeaders());
	}

	public void update() {
		clusterViewer.autoAdjust();
		clusterViewer.update();
	}

	private static void initGenerators() {
		generators.add(new SinglePointGenerator());
		generators.add(new ELKIGenerator());

	}

	private static void initReducers() {
		reducers.add(new PCAReducer());
		reducers.add(new TSNEReducer());
		reducers.add(new DimensionRemover());
	}

	private static void initNormalizers() {
		normalizers.add(new Normalize());
		normalizers.add(new Standardize());

	}

}
