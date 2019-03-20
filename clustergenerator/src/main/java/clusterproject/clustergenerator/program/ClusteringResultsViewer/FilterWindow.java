package clusterproject.clustergenerator.program.ClusteringResultsViewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.util.SortOrder;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtils;

import clusterproject.clustergenerator.Util;
import clusterproject.clustergenerator.data.ClusteringResult;
import clusterproject.clustergenerator.program.MainWindow;
import clusterproject.clustergenerator.program.Slider.RangeSlider;
import clusterproject.clustergenerator.program.Slider.RangeSliderUI;
import smile.math.Math;

public class FilterWindow extends JPanel {

	private static final long serialVersionUID = 1052960199516074256L;
	private static final int SPACING = 10;
	private static final int ABOVE_BAR_SPACE = 100;
	private static final int SLIDERHEIGHT = 30;
	private static final int MAX_BINS = 21;

	private SpringLayout mainLayout = new SpringLayout();

	private List<ClusteringResult> clusteringResults;
	private final Map<String, Object> selectors;
	private final Map<String, double[]> allParametersMap;
	private final Map<String, Double> allParametersMinMap;
	private final Map<String, Double> allParametersMaxMap;
	private final Map<String, Integer> bucketsMap;
	private final ClusteringViewer clusteringViewer;

	private final Set<ClusteringResult> filteredSet = new HashSet<ClusteringResult>();
	private List<List<List<Object>>> filteredParameters;
	private final LinkedHashSet<String> clusteringNames;
	private final List<LinkedHashSet<String>> parameterNames;
	private List<List<List<Object>>> parameters;
	private final Map<String, JFreeChart> charts;
	private final List<ClusteringResult> clusteringBaseResults;
	private boolean ignoreChange = false;

	public FilterWindow(List<ClusteringResult> clusteringResults, ClusteringViewer clusteringViewer) {
		mainLayout = new SpringLayout();
		charts = new HashMap<String, JFreeChart>();
		this.clusteringViewer = clusteringViewer;
		allParametersMap = new HashMap<String, double[]>();
		allParametersMaxMap = new HashMap<String, Double>();
		allParametersMinMap = new HashMap<String, Double>();
		bucketsMap = new HashMap<String, Integer>();
		selectors = new HashMap<String, Object>();
		setBackground(MainWindow.BACKGROUND_COLOR);
		setLayout(mainLayout);
		clusteringBaseResults = new ArrayList<ClusteringResult>(clusteringResults);
		final List<ClusteringResult> removeTruth = new ArrayList<ClusteringResult>();
		for (final ClusteringResult result : clusteringBaseResults) {
			if (result.getParameter().getName().equals(Util.GROUND_TRUTH))
				removeTruth.add(result);
		}
		clusteringBaseResults.removeAll(removeTruth);

		final List<String> cList = new ArrayList<String>();
		parameterNames = new ArrayList<LinkedHashSet<String>>();
		for (final ClusteringResult result : clusteringBaseResults) {
			final String clusteringName = result.getParameter().getName();
			if (!cList.contains(clusteringName)) {
				cList.add(clusteringName);
				final List<String> pList = new ArrayList<>(result.getParameter().getParameters().keySet());
				Collections.sort(pList);
				final List<String> pListAdditional = new ArrayList<>(
						result.getParameter().getAdditionalParameters().keySet());
				Collections.sort(pListAdditional);
				final LinkedHashSet<String> clusteringParameterNames = new LinkedHashSet<String>();
				clusteringParameterNames.addAll(pList);
				clusteringParameterNames.addAll(pListAdditional);

				parameterNames.add(clusteringParameterNames);
			}
		}
		clusteringNames = new LinkedHashSet<String>(cList);

		rebuild(clusteringBaseResults);
		final Iterator<String> clusteringNamesIt = clusteringNames.iterator();
		final Component lastComponent = Box.createVerticalStrut(0);
		mainLayout.putConstraint(SpringLayout.NORTH, lastComponent, 0, SpringLayout.NORTH, this);
		add(lastComponent, new Integer(0));

		for (int i = 0; i < clusteringNames.size(); ++i) {
			final String clusteringName = clusteringNamesIt.next();
			final Iterator<String> parameterNamesIt = parameterNames.get(i).iterator();
			for (int j = 0; j < parameterNames.get(i).size(); ++j) {
				final String parameterName = parameterNamesIt.next();
				double max = -Double.MAX_VALUE;
				double min = Double.MAX_VALUE;
				final double[] allParameters = new double[parameters.get(i).get(j).size()];
				for (int k = 0; k < parameters.get(i).get(j).size(); ++k) {
					final Object parameter = parameters.get(i).get(j).get(k);
					if (!(parameter instanceof Double) && !(parameter instanceof Integer)
							&& !(parameter instanceof Boolean)) {// TODO: handle other types
						System.err.println("unexpected value type");
						continue;
					}
					Double value = Double.NaN;
					if (parameter instanceof Double)
						value = (Double) parameter;
					if (parameter instanceof Integer)
						value = (double) (((Integer) parameter));
					if (parameter instanceof Boolean)
						value = (double) (((Boolean) parameter) ? 1 : 0);
					if (value == Double.NaN) {
						System.err.println("unexpected value type");
						continue;
					}
					allParameters[k] = value;
					if (value < min)
						min = value;
					if (value > max)
						max = value;
				}
				int bins = (int) Math.sqrt(allParameters.length);// TODO check if this is good
				if (parameters.get(i).get(j).size() <= 1)
					bins = 1;
				else if (parameters.get(i).get(j).get(0) instanceof Integer)
					bins = (int) (max - min + 1);// TODO check if this is good
				else if (parameters.get(i).get(j).get(0) instanceof Boolean)
					bins = 2;
				if (bins < 1)
					bins = 1;
				if (bins > MAX_BINS)
					bins = MAX_BINS;
				bucketsMap.put(clusteringName + " " + parameterName, bins);
				allParametersMaxMap.put(clusteringName + " " + parameterName, max);
				allParametersMinMap.put(clusteringName + " " + parameterName, min);
			}
		}
		adjust();

	}

	public void rebuild(List<ClusteringResult> clusteringResults) {
		filteredSet.clear();
		this.clusteringResults = new ArrayList<ClusteringResult>(clusteringResults);
		final List<ClusteringResult> removeTruth = new ArrayList<ClusteringResult>();
		for (final ClusteringResult result : this.clusteringResults) {
			if (result.getParameter().getName().equals(Util.GROUND_TRUTH))
				removeTruth.add(result);
		}
		this.clusteringResults.removeAll(removeTruth);
		allParametersMap.clear();

		parameters = new ArrayList<List<List<Object>>>();
		int parameterIndex = 0;
		for (final String clusteringName : clusteringNames) {
			final LinkedHashSet<String> clusteringParameterNames = parameterNames.get(parameterIndex);
			final List<List<Object>> clusteringParameters = new ArrayList<List<Object>>();
			for (final String clusteringParameterName : clusteringParameterNames) {
				final List<Object> nameParameters = new ArrayList<Object>();
				for (final ClusteringResult result : this.clusteringResults) {
					if (result.getParameter().getName().equals(clusteringName)) {
						nameParameters.add(result.getParameter().getAllParameters().get(clusteringParameterName));
					}
				}
				clusteringParameters.add(nameParameters);
			}
			parameters.add(clusteringParameters);
			parameterIndex++;
		}

		final Iterator<String> clusteringNamesIt = clusteringNames.iterator();

		for (int i = 0; i < clusteringNames.size(); ++i) {
			final String clusteringName = clusteringNamesIt.next();
			final Iterator<String> parameterNamesIt = parameterNames.get(i).iterator();
			for (int j = 0; j < parameterNames.get(i).size(); ++j) {
				final String parameterName = parameterNamesIt.next();
				final double[] allParameters = new double[parameters.get(i).get(j).size()];
				allParametersMap.put(clusteringName + " " + parameterName, allParameters);
				for (int k = 0; k < parameters.get(i).get(j).size(); ++k) {
					final Object parameter = parameters.get(i).get(j).get(k);
					if (!(parameter instanceof Double) && !(parameter instanceof Integer)
							&& !(parameter instanceof Boolean)) {// TODO: handle other types
						System.err.println("unexpected value type");
						continue;
					}
					Double value = Double.NaN;
					if (parameter instanceof Double)
						value = (Double) parameter;
					if (parameter instanceof Integer)
						value = (double) (((Integer) parameter));
					if (parameter instanceof Boolean)
						value = (double) (((Boolean) parameter) ? 1 : 0);
					if (value == Double.NaN) {
						System.err.println("unexpected value type");
						continue;
					}
					allParameters[k] = value;
				}
			}
		}

	}

	private void adjust() {
		final Iterator<String> clusteringNamesIt = clusteringNames.iterator();
		Component lastComponent = Box.createVerticalStrut(0);
		mainLayout.putConstraint(SpringLayout.NORTH, lastComponent, 0, SpringLayout.NORTH, this);
		this.add(lastComponent, new Integer(0));

		int prefHeight = 0;

		for (int i = 0; i < clusteringNames.size(); ++i) {
			final String clusteringName = clusteringNamesIt.next();
			final JLabel clusteringNameLabel = new JLabel(clusteringName);
			mainLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, clusteringNameLabel, 0,
					SpringLayout.HORIZONTAL_CENTER, this);
			mainLayout.putConstraint(SpringLayout.NORTH, clusteringNameLabel, 2 * SPACING, SpringLayout.SOUTH,
					lastComponent);
			prefHeight += 2 * SPACING + clusteringNameLabel.getPreferredSize().getHeight();
			lastComponent = clusteringNameLabel;
			this.add(clusteringNameLabel, new Integer(2));
			// System.err.println(clusteringName);
			final Iterator<String> parameterNamesIt = parameterNames.get(i).iterator();
			for (int j = 0; j < parameterNames.get(i).size(); ++j) {
				final String parameterName = parameterNamesIt.next();
				final JLabel parameterNameLabel = new JLabel(parameterName);
				mainLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, parameterNameLabel, 0,
						SpringLayout.HORIZONTAL_CENTER, clusteringNameLabel);
				mainLayout.putConstraint(SpringLayout.NORTH, parameterNameLabel, SPACING, SpringLayout.SOUTH,
						lastComponent);
				prefHeight += SPACING + parameterNameLabel.getPreferredSize().getHeight();
				lastComponent = parameterNameLabel;
				this.add(parameterNameLabel, new Integer(2));
				// System.err.println(" " + parameterName);
				final double max = allParametersMaxMap.get(clusteringName + " " + parameterName);
				final double min = allParametersMinMap.get(clusteringName + " " + parameterName);
				final double[] allParameters = allParametersMap.get(clusteringName + " " + parameterName);
				for (int k = 0; k < parameters.get(i).get(j).size(); ++k) {
					final Object parameter = parameters.get(i).get(j).get(k);
					if (!(parameter instanceof Double) && !(parameter instanceof Integer)
							&& !(parameter instanceof Boolean)) {// TODO: handle other types
						selectors.put(clusteringName + " " + parameterName, null);
						System.err.println("unexpected value type");
						continue;
					}
					Double value = Double.NaN;
					if (parameter instanceof Double)
						value = (Double) parameter;
					if (parameter instanceof Integer)
						value = (double) (((Integer) parameter));
					if (parameter instanceof Boolean)
						value = (double) (((Boolean) parameter) ? 1 : 0);
					if (value == Double.NaN) {
						System.err.println("unexpected value type");
						continue;
					}
				}
				if (max != -Double.MAX_VALUE) {
					final RangeSlider slider = new MyRangeSlider(min, max, this);
					// slider.setSize(new Dimension(getWidth(), SLIDERHEIGHT));
					// if (min == max)
					// slider.setEnabled(false);
					selectors.put(clusteringName + " " + parameterName, slider);
					mainLayout.putConstraint(SpringLayout.NORTH, slider, ABOVE_BAR_SPACE, SpringLayout.SOUTH,
							parameterNameLabel);
					mainLayout.putConstraint(SpringLayout.SOUTH, slider, ABOVE_BAR_SPACE + SLIDERHEIGHT,
							SpringLayout.SOUTH, parameterNameLabel);
					mainLayout.putConstraint(SpringLayout.WEST, slider, 0, SpringLayout.WEST, this);
					mainLayout.putConstraint(SpringLayout.EAST, slider, 0, SpringLayout.EAST, this);
					prefHeight += ABOVE_BAR_SPACE + SLIDERHEIGHT;
					lastComponent = slider;
					this.add(slider, new Integer(3));

					final Rectangle sliderRectangle = ((RangeSliderUI) slider.getUI()).getTrackRectangle();
					final int bins = bucketsMap.get(clusteringName + " " + parameterName);
					final CategoryDataset dataset = createDataset(allParameters, null, bins, min, max);
					final JFreeChart chart = createChart(dataset);
					charts.put(clusteringName + " " + parameterName, chart);

					final ChartPanel chartPanel = new ChartPanel(chart);
					final MouseAdapter adapter = new MouseAdapter() {
						@Override
						public void mouseMoved(MouseEvent e) {

							// System.err.println(e.getX());
							super.mouseMoved(e);
						}
					};
					chartPanel.addMouseMotionListener(adapter);
					chartPanel.setRangeZoomable(false);
					chartPanel.setDomainZoomable(false);
					chartPanel.setPopupMenu(null);
					chartPanel.setBorder(null);
					mainLayout.putConstraint(SpringLayout.NORTH, chartPanel, -ABOVE_BAR_SPACE, SpringLayout.NORTH,
							slider);
					mainLayout.putConstraint(SpringLayout.SOUTH, chartPanel, 0, SpringLayout.NORTH, slider);
					mainLayout.putConstraint(SpringLayout.WEST, chartPanel, (int) sliderRectangle.getX() - 2,
							SpringLayout.WEST, this);
					mainLayout.putConstraint(SpringLayout.EAST, chartPanel, (int) (-sliderRectangle.getX() + 2),
							SpringLayout.EAST, this);
					this.add(chartPanel, new Integer(4));
				} else {
					System.err.println("no values found");
				}
			}
		}

		setPreferredSize(new Dimension(0, prefHeight));
	}

	public void forceChange() {
		for (final Object selector : selectors.values())
			if (selector instanceof MyRangeSlider) {
				((MyRangeSlider) selector).handleChange(true);
				break;
			}
	}

	public CategoryDataset createDataset(double[] allParameters, double[] filteredParametersD, int bins, double min,
			double max) {
		final double[] allParametersBins = new double[bins];
		for (int i = 0; i < bins; ++i) {
			allParametersBins[i] = 0;
		}
		double[] filteredParametersBins = new double[bins];
		for (int i = 0; i < bins; ++i) {
			filteredParametersBins[i] = 0;
		}
		final double start = max - min;
		final double width = start / (bins - 1);
		for (int i = 0; i < allParameters.length; ++i) {
			allParametersBins[(int) ((allParameters[i] - min) / width)] += 1;
		}
		if (filteredParametersD == null)
			filteredParametersBins = null;
		else {
			for (int i = 0; i < filteredParametersD.length; ++i) {
				filteredParametersBins[(int) ((filteredParametersD[i] - min) / width)] += 1;
			}
			for (int i = 0; i < filteredParametersBins.length; ++i) {
				allParametersBins[i] -= filteredParametersBins[i];
			}
		}

		double[][] data = null;
		if (filteredParametersD == null)
			data = new double[][] { allParametersBins };
		else
			data = new double[][] { filteredParametersBins, allParametersBins };
		final CategoryDataset dataset = DatasetUtils.createCategoryDataset("Series ", "Type ", data);

		return dataset;
	}

	public JFreeChart createChart(CategoryDataset dataset) {

		final JFreeChart chart = ChartFactory.createStackedAreaChart(null, // chart title
				"Category", // domain axis label
				"Value", // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // orientation
				false, // include legend
				false, false);

		chart.setBackgroundPaint(Color.white);

		final CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setForegroundAlpha(0.5f);
		plot.setBackgroundPaint(null);
		plot.setDomainGridlinesVisible(false);
		plot.setRangeGridlinesVisible(false);
		plot.setRangeMinorGridlinesVisible(false);
		plot.setRangeMinorGridlinesVisible(false);

		plot.setRenderer(new AreaRenderer());// XXX: remove this if stacked is better
		plot.setRowRenderingOrder(SortOrder.DESCENDING);

		final CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setLowerMargin(0.0);
		domainAxis.setUpperMargin(0.0);

		domainAxis.setCategoryMargin(0);
		domainAxis.setVisible(false);

		// change the auto tick unit selection to integer units only...
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		// rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setVisible(false);
		rangeAxis.setLowerMargin(0);
		rangeAxis.setUpperMargin(0);
		rangeAxis.setRange(rangeAxis.getLowerBound(), rangeAxis.getUpperBound());

		final CategoryItemRenderer renderer = plot.getRenderer();
		renderer.setDefaultItemLabelsVisible(false);

		plot.setInsets(new RectangleInsets(0, 0, 0, 0));
		chart.setPadding(new RectangleInsets(0, 0, 0, 0));

		return chart;

	}

	protected void comitFilteredData() {
		if (filteredSet.size() == clusteringBaseResults.size())
			clusteringViewer.setFilteredData(null);
		else
			clusteringViewer.setFilteredData(filteredSet);

	};

	private class MyRangeSlider extends RangeSlider {
		private static final long serialVersionUID = -1145841853132161271L;
		private static final int LABEL_COUNT = 3;
		private static final int TICK_COUNT = 500;
		private final double minLbl;
		private final double maxLbl;
		private final JLabel tooltip = new JLabel();
		private final FilterWindow filterWindow;
		private int oldMin = 0;
		private int oldMax = TICK_COUNT;
		private final JFrame tooltipFrame;

		public MyRangeSlider(double minLbl, double maxLbl, FilterWindow mainPanel) {
			this.minLbl = minLbl;
			this.maxLbl = maxLbl;
			this.filterWindow = mainPanel;
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
			tooltipFrame = new JFrame();
			tooltipFrame.setType(javax.swing.JFrame.Type.UTILITY);
			tooltipFrame.setUndecorated(true);
			tooltipFrame.getContentPane().setBackground(MainWindow.BACKGROUND_COLOR);
			tooltip.setOpaque(false);
			tooltipFrame.add(tooltip);
			tooltip.setText((String.valueOf(((float) getLowerValue()) + " <-> " + (float) getUpperValueD())));
			addMouseMotionListener(new MouseMotionAdapter() {
			});

			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (e.isConsumed())
						return;
					e.consume();
					tooltip.setText((String.valueOf(((float) getLowerValue()) + " <-> " + (float) getUpperValueD())));
					final Point p = MouseInfo.getPointerInfo().getLocation();
					tooltipFrame.pack();
					tooltipFrame.setLocation((int) p.getX() - tooltipFrame.getWidth() / 2,
							(int) (getLocationOnScreen().getY() + getHeight() / 2));
					tooltipFrame.setVisible(true);

				}

				@Override
				public void mouseReleased(MouseEvent e) {
					if (e.isConsumed())
						return;
					filterWindow.applyFilter();
					e.consume();
					tooltipFrame.setVisible(false);

				}

				@Override
				public void mouseExited(MouseEvent e) {
					if (e.isConsumed())
						return;
					e.consume();
					tooltipFrame.setVisible(false);

				}
			});

			addChangeListener(e -> {
				handleChange();
			});
		}

		public void handleChange() {
			handleChange(false);
		}

		public void reset() {
			setValue(0);
			setUpperValue(TICK_COUNT);
		}

		public void handleChange(boolean forceUpdate) {
			if (!forceUpdate) {
				if (getValue() == oldMin && getUpperValue() == oldMax)
					return;
			}
			oldMin = getValue();
			oldMax = getUpperValue();

			final Set<ClusteringResult> visualResult = new HashSet<ClusteringResult>();
			for (final ClusteringResult result : clusteringResults) {
				final String clusteringName = result.getParameter().getName();
				final Map<String, Object> params = result.getParameter().getAllParameters();
				boolean add = true;
				for (final String param : params.keySet()) {
					final Object selector = selectors.get(clusteringName + " " + param);
					if (selector == null) {
						System.err.println("not implemented type");
						continue;
					}
					if (selector instanceof MyRangeSlider) {// TODO: other selectors
						if (((MyRangeSlider) selector).isFullRange())
							continue;
						if (((MyRangeSlider) selector).isEmptyRange()) {
							add = false;
							continue;
						}
						final Object paramVal = params.get(param);
						Double value = Double.NaN;
						if (paramVal instanceof Double)
							value = (Double) paramVal;
						if (paramVal instanceof Integer)
							value = (double) (((Integer) paramVal));
						if (paramVal instanceof Boolean)
							value = (double) (((Boolean) paramVal) ? 1 : 0);
						if (value == Double.NaN) {
							System.err.println("unexpected value type");
							continue;
						}
						if (value * 0.99999 >= ((MyRangeSlider) selector).getUpperValueD()
								|| value * 1.00001 <= ((MyRangeSlider) selector).getLowerValue()) {
							add = false;
						}
					} else {
						System.err.println("not implemented type");
					}
				}
				if (add) {
					visualResult.add(result);
				}
			}

			filteredParameters = new ArrayList<List<List<Object>>>();
			int index = 0;
			for (final String clusteringName : clusteringNames) {
				final List<List<Object>> clusteringParameters = new ArrayList<List<Object>>();

				for (final String clusteringParameterName : parameterNames.get(index)) {
					final List<Object> nameParameters = new ArrayList<Object>();
					for (final ClusteringResult result : visualResult) {
						if (result.getParameter().getName().equals(clusteringName)) {
							nameParameters.add(result.getParameter().getAllParameters().get(clusteringParameterName));
						}
					}
					clusteringParameters.add(nameParameters);

				}
				filteredParameters.add(clusteringParameters);
				++index;
			}
			final Iterator<String> clusteringNamesIt = clusteringNames.iterator();
			for (int i = 0; i < clusteringNames.size(); ++i) {
				final String clusteringName = clusteringNamesIt.next();
				final Iterator<String> parameterNamesIt = parameterNames.get(i).iterator();
				for (int j = 0; j < parameterNames.get(i).size(); ++j) {
					final String parameterName = parameterNamesIt.next();

					final double[] allParameters = allParametersMap.get(clusteringName + " " + parameterName);
					final double max = allParametersMaxMap.get(clusteringName + " " + parameterName);
					final double min = allParametersMinMap.get(clusteringName + " " + parameterName);

					double[] filteredParametersD = null;
					if (parameters.get(i).get(j).size() != filteredParameters.get(i).get(j).size()) {
						filteredParametersD = new double[filteredParameters.get(i).get(j).size()];
						for (int k = 0; k < filteredParameters.get(i).get(j).size(); ++k) {
							final Object parameter = filteredParameters.get(i).get(j).get(k);
							if (!(parameter instanceof Double) && !(parameter instanceof Integer)
									&& !(parameter instanceof Boolean)) {// TODO: handle
								// other types
								System.err.println("unexpected value type");
								continue;
							}
							Double value = Double.NaN;
							if (parameter instanceof Double)
								value = (Double) parameter;
							if (parameter instanceof Integer)
								value = (double) (((Integer) parameter));
							if (parameter instanceof Boolean)
								value = (double) (((Boolean) parameter) ? 1 : 0);
							if (value == Double.NaN) {
								System.err.println("unexpected value type");
								continue;
							}
							filteredParametersD[k] = value;
						}
					}
					if (max != -Double.MAX_VALUE) {
						final int bins = bucketsMap.get(clusteringName + " " + parameterName);

						final CategoryDataset dataset = createDataset(allParameters, filteredParametersD, bins, min,
								max);
						final JFreeChart chart = charts.get(clusteringName + " " + parameterName);
						chart.getCategoryPlot().setDataset(dataset);
					} else {
						System.err.println("no values found");
					}
				}
			}

			if (!forceUpdate) {
				tooltip.setText((String.valueOf(((float) getLowerValue()) + " <-> " + (float) getUpperValueD())));
				final Point p = MouseInfo.getPointerInfo().getLocation();
				tooltipFrame.pack();
				tooltipFrame.setLocation((int) p.getX() - tooltipFrame.getWidth() / 2,
						(int) (getLocationOnScreen().getY() + getHeight() / 2));
				tooltipFrame.setVisible(true);
			}

			SwingUtilities.invokeLater(() -> {
				filterWindow.repaint();
			});

		}

		private boolean isEmptyRange() {
			return getValue() == getUpperValue();
		}

		private boolean isFullRange() {
			return getValue() == 0 && getUpperValue() == TICK_COUNT;
		}

		public double getUpperValueD() {
			// if (getUpperValue() == TICK_COUNT)
			// return maxLbl;
			return (maxLbl - minLbl) * ((double) getUpperValue() / TICK_COUNT) + minLbl;
		}

		public double getLowerValue() {
			// if (getValue() == 0)
			// return minLbl;
			return (maxLbl - minLbl) * ((double) getValue() / TICK_COUNT) + minLbl;
		}

	}

	public List<ClusteringResult> getClusterings() {
		return clusteringResults;
	}

	protected void applyFilter() {
		if (ignoreChange)
			return;
		filteredSet.clear();
		for (final ClusteringResult result : clusteringBaseResults) {
			final String clusteringName = result.getParameter().getName();
			final Map<String, Object> params = result.getParameter().getAllParameters();
			boolean add = true;
			for (final String param : params.keySet()) {
				final Object selector = selectors.get(clusteringName + " " + param);
				if (selector == null) {
					System.err.println("not implemented type");
					continue;
				}
				if (selector instanceof MyRangeSlider) {// TODO: other selectors
					if (((MyRangeSlider) selector).isFullRange())
						continue;
					if (((MyRangeSlider) selector).isEmptyRange()) {
						add = false;
						continue;
					}
					final Object paramVal = params.get(param);
					Double value = Double.NaN;
					if (paramVal instanceof Double)
						value = (Double) paramVal;
					if (paramVal instanceof Integer)
						value = (double) (((Integer) paramVal));
					if (paramVal instanceof Boolean)
						value = (double) (((Boolean) paramVal) ? 1 : 0);
					if (value == Double.NaN) {
						System.err.println("unexpected value type");
						continue;
					}
					if (value * 0.99999 >= ((MyRangeSlider) selector).getUpperValueD()
							|| value * 1.00001 <= ((MyRangeSlider) selector).getLowerValue()) {
						add = false;
					}
				} else {
					System.err.println("not implemented type");
				}
			}
			if (add)
				filteredSet.add(result);
		}
		comitFilteredData();
	}

	public void resetFilters() {
		if (selectors != null) {
			ignoreChange = true;
			selectors.values().forEach(t -> {
				if (t instanceof MyRangeSlider)
					((MyRangeSlider) t).reset();
			});
			ignoreChange = false;
			applyFilter();
		}

	}

}
