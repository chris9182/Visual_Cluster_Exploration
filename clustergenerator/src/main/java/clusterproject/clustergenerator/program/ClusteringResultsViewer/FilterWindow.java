package clusterproject.clustergenerator.program.ClusteringResultsViewer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import clusterproject.clustergenerator.data.ClusteringResult;
import clusterproject.clustergenerator.program.MainWindow;
import clusterproject.clustergenerator.program.slider.RangeSlider;
import clusterproject.clustergenerator.program.slider.RangeSliderUI;

public class FilterWindow extends JFrame {

	private static final long serialVersionUID = 1052960199516074256L;
	private static final int SPACING = 10;
	private static final int ABOVE_BAR_SPACE = 100;
	private static final int SLIDERHEIGHT = 30;

	private final JLayeredPane mainPane = new JLayeredPane();
	private final SpringLayout mainLayout = new SpringLayout();

	private final List<ClusteringResult> clusteringResults;
	private Map<String, Object> selectors;
	private final ClusteringViewer clusteringViewer;
	private final JButton filterButton;
	private final LinkedHashSet<String> clusteringNames;
	private final List<LinkedHashSet<String>> parameterNames;
	private final List<List<LinkedHashSet<Object>>> parameters;

	public FilterWindow(List<ClusteringResult> clusteringResults, ClusteringViewer clusteringViewer) {
		this.clusteringViewer = clusteringViewer;
		selectors = new HashMap<String, Object>();
		getContentPane().setBackground(MainWindow.BACKGROUND_COLOR);
		add(mainPane);
		mainPane.setLayout(mainLayout);
		this.clusteringResults = clusteringResults;

		filterButton = new JButton("Filter");
		filterButton.addActionListener(e -> {
			final List<ClusteringResult> filteredList = new ArrayList<ClusteringResult>();
			for (final ClusteringResult result : clusteringResults) {
				final String clusteringName = result.getParameter().getName();
				final Map<String, Object> params = result.getParameter().getParameters();
				boolean add = true;
				for (final String param : params.keySet()) {
					final Object selector = selectors.get(clusteringName + " " + param);
					if (selector == null) {
						System.err.println("not implemented type");
						continue;
					}
					if (selector instanceof MyRangeSlider) {// TODO: other selectors
						final Object paramVal = params.get(param);
						Double value = Double.NaN;
						if (paramVal instanceof Double)
							value = (Double) paramVal;
						if (paramVal instanceof Integer)
							value = (double) (((Integer) paramVal));
						if (value == Double.NaN) {
							System.err.println("unexpected value type");
							continue;
						}
						if (value > ((MyRangeSlider) selector).getUpperValue()
								|| value < ((MyRangeSlider) selector).getLowerValue()) {
							add = false;
						}
					} else {
						System.err.println("not implemented type");
					}
				}
				if (add)
					filteredList.add(result);
			}
			final ClusteringViewer cv = new ClusteringViewer(filteredList, clusteringViewer.getDistanceMeasure(), 1,
					Double.MAX_VALUE);// TODO:
			cv.setSize(new Dimension(800, 600));
			cv.setExtendedState(JFrame.MAXIMIZED_BOTH);
			cv.setLocationRelativeTo(null);
			cv.setVisible(true);
			setVisible(false);
			dispose();
		});

		clusteringNames = new LinkedHashSet<String>();
		for (final ClusteringResult result : clusteringResults) {
			clusteringNames.add(result.getParameter().getName());
		}
		parameterNames = new ArrayList<LinkedHashSet<String>>();
		parameters = new ArrayList<List<LinkedHashSet<Object>>>();
		for (final String clusteringName : clusteringNames) {
			final LinkedHashSet<String> clusteringParameterNames = new LinkedHashSet<String>();
			for (final ClusteringResult result : clusteringResults) {
				if (result.getParameter().getName().equals(clusteringName))
					clusteringParameterNames.addAll(result.getParameter().getParameters().keySet());
			}
			parameterNames.add(clusteringParameterNames);
			final List<LinkedHashSet<Object>> clusteringParameters = new ArrayList<LinkedHashSet<Object>>();

			for (final String clusteringParameterName : clusteringParameterNames) {
				final LinkedHashSet<Object> nameParameters = new LinkedHashSet<Object>();
				for (final ClusteringResult result : clusteringResults) {
					if (result.getParameter().getName().equals(clusteringName)) {
						nameParameters.add(result.getParameter().getParameters().get(clusteringParameterName));
					}
				}
				clusteringParameters.add(nameParameters);
			}
			parameters.add(clusteringParameters);
		}

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				adjust();
			}
		});
	}

	public void adjust() {
		mainPane.removeAll();
		mainLayout.putConstraint(SpringLayout.SOUTH, filterButton, -SPACING, SpringLayout.SOUTH, mainPane);
		mainLayout.putConstraint(SpringLayout.EAST, filterButton, -SPACING, SpringLayout.EAST, mainPane);
		mainPane.add(filterButton, new Integer(2));
		selectors = new HashMap<String, Object>();

		final Iterator<String> clusteringNamesIt = clusteringNames.iterator();
		final int clusteringNameWidth = getHeight() / clusteringNames.size();
		Component lastComponent = Box.createVerticalStrut(0);
		mainLayout.putConstraint(SpringLayout.NORTH, lastComponent, 0, SpringLayout.NORTH, mainPane);
		mainPane.add(lastComponent, new Integer(0));

		for (int i = 0; i < clusteringNames.size(); ++i) {
			final String clusteringName = clusteringNamesIt.next();
			final JLabel clusteringNameLabel = new JLabel(clusteringName);
			final int clusteringNameCenter = getHeight() / (clusteringNames.size()) * (i)
					+ getHeight() / (clusteringNames.size()) / 2;
			mainLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, clusteringNameLabel, 0,
					SpringLayout.HORIZONTAL_CENTER, mainPane);
			mainLayout.putConstraint(SpringLayout.NORTH, clusteringNameLabel, 2 * SPACING, SpringLayout.SOUTH,
					lastComponent);
			lastComponent = clusteringNameLabel;
			mainPane.add(clusteringNameLabel, new Integer(2));
			// System.err.println(clusteringName);
			final Iterator<String> parameterNamesIt = parameterNames.get(i).iterator();
			final int parameterStart = clusteringNameCenter - clusteringNameWidth / 2;
			for (int j = 0; j < parameterNames.get(i).size(); ++j) {
				final String parameterName = parameterNamesIt.next();
				final JLabel parameterNameLabel = new JLabel(parameterName);
				final int clusteringParameterCenter = clusteringNameWidth / (parameterNames.get(i).size()) * (j)
						+ parameterStart;
				mainLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, parameterNameLabel, 0,
						SpringLayout.HORIZONTAL_CENTER, clusteringNameLabel);
				mainLayout.putConstraint(SpringLayout.NORTH, parameterNameLabel, SPACING, SpringLayout.SOUTH,
						lastComponent);
				lastComponent = parameterNameLabel;
				mainPane.add(parameterNameLabel, new Integer(2));
				// System.err.println(" " + parameterName);
				double max = Double.MIN_VALUE;
				double min = Double.MAX_VALUE;
				final Iterator<Object> parametersIt = parameters.get(i).get(j).iterator();
				for (int k = 0; k < parameters.get(i).get(j).size(); ++k) {
					final Object parameter = parametersIt.next();
					if (!(parameter instanceof Double) && !(parameter instanceof Integer)) {// TODO: handle other types
						selectors.put(clusteringName + " " + parameterName, null);
						System.err.println("unexpected value type");
						continue;
					}
					Double value = Double.NaN;
					if (parameter instanceof Double)
						value = (Double) parameter;
					if (parameter instanceof Integer)
						value = (double) (((Integer) parameter));
					if (value == Double.NaN) {
						System.err.println("unexpected value type");
						continue;
					}
					if (value < min)
						min = value;
					if (value > max)
						max = value;
				}
				if (max != Double.MIN_VALUE) {
					final RangeSlider slider = new MyRangeSlider(min, max);
					((RangeSliderUI) slider.getUI()).getTrackRectangle();// TODO use for bounds with plot
					slider.setSize(new Dimension(getWidth(), SLIDERHEIGHT));
					if (min == max)
						slider.setEnabled(false);
					selectors.put(clusteringName + " " + parameterName, slider);
					mainLayout.putConstraint(SpringLayout.NORTH, slider, ABOVE_BAR_SPACE, SpringLayout.SOUTH,
							parameterNameLabel);
					mainLayout.putConstraint(SpringLayout.SOUTH, slider, ABOVE_BAR_SPACE + SLIDERHEIGHT,
							SpringLayout.SOUTH, parameterNameLabel);
					mainLayout.putConstraint(SpringLayout.WEST, slider, 0, SpringLayout.WEST, mainPane);
					mainLayout.putConstraint(SpringLayout.EAST, slider, 0, SpringLayout.EAST, mainPane);
					lastComponent = slider;
					mainPane.add(slider, new Integer(3));
				} else {
					System.err.println("no values found");
				}
			}
		}
		SwingUtilities.invokeLater(() -> {
			revalidate();
			repaint();
			toFront();
		});
	}

	private class MyRangeSlider extends RangeSlider {
		private static final long serialVersionUID = -1145841853132161271L;
		private static final int LABEL_COUNT = 6;
		private static final int TICK_COUNT = 1000;
		private final double minLbl;
		private final double maxLbl;
		private final JLabel tooltip = new JLabel();

		public MyRangeSlider(double minLbl, double maxLbl) {
			this.minLbl = minLbl;
			this.maxLbl = maxLbl;
			setOpaque(false);
			setMinimum(0);
			setMaximum(TICK_COUNT);
			setValue(0);
			setUpperValue(TICK_COUNT);
			setFocusable(false);
			setPaintLabels(true);
			final Dictionary<Integer, JComponent> dict = new Hashtable<Integer, JComponent>();
			for (int i = 0; i < LABEL_COUNT; ++i) {
				final JLabel label = new JLabel("   " + (float) ((maxLbl - minLbl) * (i) / (LABEL_COUNT - 1) + minLbl));
				// String.format("%.3f", (maxLbl - minLbl) * i + minLbl));
				dict.put(i * TICK_COUNT / (LABEL_COUNT - 1), label);
			}
			setLabelTable(dict);
			final JFrame tooltipFrame = new JFrame();
			tooltipFrame.setType(javax.swing.JFrame.Type.UTILITY);
			tooltipFrame.setUndecorated(true);
			tooltipFrame.getContentPane().setBackground(MainWindow.BACKGROUND_COLOR);
			tooltip.setOpaque(false);
			tooltipFrame.add(tooltip);
			tooltip.setText((String.valueOf(((float) getLowerValue()) + " <-> " + (float) getUpperValueD())));
			addChangeListener(e -> {
				tooltip.setText((String.valueOf(((float) getLowerValue()) + " <-> " + (float) getUpperValueD())));
				final Point p = MouseInfo.getPointerInfo().getLocation();
				tooltipFrame.pack();
				tooltipFrame.setLocation((int) p.getX() - tooltipFrame.getWidth() / 2,
						(int) (getLocationOnScreen().getY() - getHeight() / 2));
				tooltipFrame.setVisible(true);
			});
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					tooltip.setText((String.valueOf(((float) getLowerValue()) + " <-> " + (float) getUpperValueD())));
					final Point p = MouseInfo.getPointerInfo().getLocation();
					tooltipFrame.pack();
					tooltipFrame.setLocation((int) p.getX() - tooltipFrame.getWidth() / 2,
							(int) (getLocationOnScreen().getY() - getHeight() / 2));
					tooltipFrame.setVisible(true);
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					tooltipFrame.setVisible(false);
				}

				@Override
				public void mouseExited(MouseEvent e) {
					tooltipFrame.setVisible(false);
				}
			});
		}

		public double getUpperValueD() {
			return (maxLbl - minLbl) * ((double) getUpperValue() / TICK_COUNT) + minLbl;
		}

		public double getLowerValue() {
			return (maxLbl - minLbl) * ((double) getValue() / TICK_COUNT) + minLbl;
		}

	};

}
