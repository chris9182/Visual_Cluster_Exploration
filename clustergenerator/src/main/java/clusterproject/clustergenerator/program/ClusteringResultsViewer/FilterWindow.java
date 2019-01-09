package clusterproject.clustergenerator.program.ClusteringResultsViewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
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
import javax.swing.JLayeredPane;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtils;

import clusterproject.clustergenerator.data.ClusteringResult;
import clusterproject.clustergenerator.program.MainWindow;
import clusterproject.clustergenerator.program.slider.RangeSlider;
import clusterproject.clustergenerator.program.slider.RangeSliderUI;

public class FilterWindow extends JLayeredPane {

	private static final long serialVersionUID = 1052960199516074256L;
	private static final int SPACING = 10;
	private static final int ABOVE_BAR_SPACE = 100;
	private static final int SLIDERHEIGHT = 30;

	private final SpringLayout mainLayout = new SpringLayout();

	private final List<ClusteringResult> clusteringResults;
	private Map<String, Object> selectors;
	private final ClusteringViewer clusteringViewer;

	private final Set<ClusteringResult> filteredSet = new HashSet<ClusteringResult>();
	private List<List<List<Object>>> filteredParameters;
	private final LinkedHashSet<String> clusteringNames;
	private final List<LinkedHashSet<String>> parameterNames;
	private final List<List<List<Object>>> parameters;
	private final List<JFreeChart> charts;

	public FilterWindow(List<ClusteringResult> clusteringResults, ClusteringViewer clusteringViewer) {
		this.clusteringViewer = clusteringViewer;
		selectors = new HashMap<String, Object>();
		// getContentPane().setBackground(MainWindow.BACKGROUND_COLOR);
		setLayout(mainLayout);
		this.clusteringResults = clusteringResults;
		charts = new ArrayList<JFreeChart>();

		clusteringNames = new LinkedHashSet<String>();
		for (final ClusteringResult result : clusteringResults) {
			clusteringNames.add(result.getParameter().getName());
		}
		parameterNames = new ArrayList<LinkedHashSet<String>>();
		parameters = new ArrayList<List<List<Object>>>();
		for (final String clusteringName : clusteringNames) {
			final LinkedHashSet<String> clusteringParameterNames = new LinkedHashSet<String>();
			for (final ClusteringResult result : clusteringResults) {
				if (result.getParameter().getName().equals(clusteringName))
					clusteringParameterNames.addAll(result.getParameter().getParameters().keySet());
			}
			parameterNames.add(clusteringParameterNames);
			final List<List<Object>> clusteringParameters = new ArrayList<List<Object>>();

			for (final String clusteringParameterName : clusteringParameterNames) {
				final List<Object> nameParameters = new ArrayList<Object>();
				for (final ClusteringResult result : clusteringResults) {
					if (result.getParameter().getName().equals(clusteringName)) {
						nameParameters.add(result.getParameter().getParameters().get(clusteringParameterName));
					}
				}
				clusteringParameters.add(nameParameters);
			}
			parameters.add(clusteringParameters);
		}
	}

	private int oldwidth = -1;

	@Override
	public void setBounds(int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		super.setBounds(x, y, width, height);
		if (oldwidth != width) {
			oldwidth = width;
			adjust();

		}
	}

	public void adjust() {
		this.removeAll();
		selectors = new HashMap<String, Object>();

		final Iterator<String> clusteringNamesIt = clusteringNames.iterator();
		Component lastComponent = Box.createVerticalStrut(0);
		mainLayout.putConstraint(SpringLayout.NORTH, lastComponent, 0, SpringLayout.NORTH, this);
		this.add(lastComponent, new Integer(0));

		for (int i = 0; i < clusteringNames.size(); ++i) {
			final String clusteringName = clusteringNamesIt.next();
			final JLabel clusteringNameLabel = new JLabel(clusteringName);
			mainLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, clusteringNameLabel, 0,
					SpringLayout.HORIZONTAL_CENTER, this);
			mainLayout.putConstraint(SpringLayout.NORTH, clusteringNameLabel, 2 * SPACING, SpringLayout.SOUTH,
					lastComponent);
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
				lastComponent = parameterNameLabel;
				this.add(parameterNameLabel, new Integer(2));
				// System.err.println(" " + parameterName);
				double max = Double.MIN_VALUE;
				double min = Double.MAX_VALUE;
				final double[] allParameters = new double[parameters.get(i).get(j).size()];
				Object parameter = null;
				for (int k = 0; k < parameters.get(i).get(j).size(); ++k) {
					parameter = parameters.get(i).get(j).get(k);
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
					allParameters[k] = value;
					if (value < min)
						min = value;
					if (value > max)
						max = value;
				}
				if (max != Double.MIN_VALUE) {
					final RangeSlider slider = new MyRangeSlider(min, max, this);
					slider.setSize(new Dimension(getWidth(), SLIDERHEIGHT));
					if (min == max)
						slider.setEnabled(false);
					selectors.put(clusteringName + " " + parameterName, slider);
					mainLayout.putConstraint(SpringLayout.NORTH, slider, ABOVE_BAR_SPACE, SpringLayout.SOUTH,
							parameterNameLabel);
					mainLayout.putConstraint(SpringLayout.SOUTH, slider, ABOVE_BAR_SPACE + SLIDERHEIGHT,
							SpringLayout.SOUTH, parameterNameLabel);
					mainLayout.putConstraint(SpringLayout.WEST, slider, 0, SpringLayout.WEST, this);
					mainLayout.putConstraint(SpringLayout.EAST, slider, 0, SpringLayout.EAST, this);
					lastComponent = slider;
					this.add(slider, new Integer(3));

					final Rectangle sliderRectangle = ((RangeSliderUI) slider.getUI()).getTrackRectangle();// TODO use
																											// for
																											// bounds
																											// with plot
					int bins = clusteringResults.size() / 5;
					if (parameter instanceof Integer)
						bins = (int) (max - min + 1);// TODO check if this is good
					if (bins < 1)
						bins = 1;
					if (bins > 30)
						bins = 30;
					final CategoryDataset dataset = createDataset(allParameters, null, bins, min, max);
					final JFreeChart chart = createChart(dataset);
					charts.add(chart);

					final ChartPanel chartPanel = new ChartPanel(chart);
					chartPanel.setRangeZoomable(false);
					chartPanel.setDomainZoomable(false);
					chartPanel.setPopupMenu(null);
					mainLayout.putConstraint(SpringLayout.NORTH, chartPanel, -ABOVE_BAR_SPACE, SpringLayout.NORTH,
							slider);
					mainLayout.putConstraint(SpringLayout.SOUTH, chartPanel, 0, SpringLayout.NORTH, slider);
					mainLayout.putConstraint(SpringLayout.WEST, chartPanel, (int) sliderRectangle.getX() - 11,
							SpringLayout.WEST, this);
					mainLayout.putConstraint(SpringLayout.EAST, chartPanel, (int) (-sliderRectangle.getX() + 10),
							SpringLayout.EAST, this);
					this.add(chartPanel, new Integer(4));
				} else {
					System.err.println("no values found");
				}
			}
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

		final CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setLowerMargin(0.0);
		domainAxis.setUpperMargin(0.0);
		domainAxis.setVisible(false);
		domainAxis.setCategoryMargin(0);

		// change the auto tick unit selection to integer units only...
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setVisible(false);

		final CategoryItemRenderer renderer = plot.getRenderer();
		renderer.setDefaultItemLabelsVisible(false);

		return chart;

	}

	protected void comitFilteredData() {
		if (filteredSet.size() == clusteringResults.size())
			clusteringViewer.setFilteredData(null);
		else
			clusteringViewer.setFilteredData(filteredSet);

	};

	private class MyRangeSlider extends RangeSlider {
		private static final long serialVersionUID = -1145841853132161271L;
		private static final int LABEL_COUNT = 3;
		private static final int TICK_COUNT = 1000;
		private final double minLbl;
		private final double maxLbl;
		private final JLabel tooltip = new JLabel();
		private final FilterWindow filterWindow;

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
			final JFrame tooltipFrame = new JFrame();
			tooltipFrame.setType(javax.swing.JFrame.Type.UTILITY);
			tooltipFrame.setUndecorated(true);
			tooltipFrame.getContentPane().setBackground(MainWindow.BACKGROUND_COLOR);
			tooltip.setOpaque(false);
			tooltipFrame.add(tooltip);
			tooltip.setText((String.valueOf(((float) getLowerValue()) + " <-> " + (float) getUpperValueD())));

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
					filterWindow.comitFilteredData();
				}

				@Override
				public void mouseExited(MouseEvent e) {
					tooltipFrame.setVisible(false);
				}
			});

			addChangeListener(e -> {
				filteredSet.clear();
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
							if (((MyRangeSlider) selector).isFullRange())
								continue;
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

				filteredParameters = new ArrayList<List<List<Object>>>();
				int index = 0;
				for (final String clusteringName : clusteringNames) {
					final List<List<Object>> clusteringParameters = new ArrayList<List<Object>>();

					for (final String clusteringParameterName : parameterNames.get(index)) {
						final List<Object> nameParameters = new ArrayList<Object>();
						for (final ClusteringResult result : filteredSet) {
							if (result.getParameter().getName().equals(clusteringName)) {
								nameParameters.add(result.getParameter().getParameters().get(clusteringParameterName));
							}
						}
						clusteringParameters.add(nameParameters);

					}
					filteredParameters.add(clusteringParameters);
					++index;
				}
				int chartIndex = 0;
				for (int i = 0; i < clusteringNames.size(); ++i) {
					for (int j = 0; j < parameterNames.get(i).size(); ++j) {
						double max = Double.MIN_VALUE;
						double min = Double.MAX_VALUE;
						final double[] allParameters = new double[parameters.get(i).get(j).size()];

						for (int k = 0; k < parameters.get(i).get(j).size(); ++k) {
							final Object parameter = parameters.get(i).get(j).get(k);
							if (!(parameter instanceof Double) && !(parameter instanceof Integer)) {// TODO: handle
																									// other types
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
							allParameters[k] = value;
							if (value < min)
								min = value;
							if (value > max)
								max = value;
						}
						double[] filteredParametersD = null;
						if (parameters.get(i).get(j).size() != filteredParameters.get(i).get(j).size()) {
							filteredParametersD = new double[filteredParameters.get(i).get(j).size()];
							for (int k = 0; k < filteredParameters.get(i).get(j).size(); ++k) {
								final Object parameter = filteredParameters.get(i).get(j).get(k);
								if (!(parameter instanceof Double) && !(parameter instanceof Integer)) {// TODO: handle
																										// other types
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
								// System.err.println(value);
								filteredParametersD[k] = value;
							}
						}
						if (max != Double.MIN_VALUE) {
							int bins = clusteringResults.size() / 5;
							if (parameters.get(i).get(j).get(0) instanceof Integer)
								bins = (int) (max - min + 1);// TODO check if this is good
							if (bins < 1)
								bins = 1;
							final CategoryDataset dataset = createDataset(allParameters, filteredParametersD, bins, min,
									max);
							final JFreeChart chart = charts.get(chartIndex++);
							chart.getCategoryPlot().setDataset(dataset);
						} else {
							System.err.println("no values found");
						}
					}
				}

				tooltip.setText((String.valueOf(((float) getLowerValue()) + " <-> " + (float) getUpperValueD())));
				final Point p = MouseInfo.getPointerInfo().getLocation();
				tooltipFrame.pack();
				tooltipFrame.setLocation((int) p.getX() - tooltipFrame.getWidth() / 2,
						(int) (getLocationOnScreen().getY() - getHeight() / 2));
				tooltipFrame.setVisible(true);

				SwingUtilities.invokeLater(() -> {
					mainPanel.repaint();
				});
			});
		}

		private boolean isFullRange() {
			// TODO Auto-generated method stub
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

}
