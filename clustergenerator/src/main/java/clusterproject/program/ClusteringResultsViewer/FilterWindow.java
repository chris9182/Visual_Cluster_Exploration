package clusterproject.program.ClusteringResultsViewer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

import clusterproject.data.ClusteringResult;
import clusterproject.program.MainWindow;
import clusterproject.program.Clustering.Parameters.Parameter;
import clusterproject.program.Slider.RangeSlider;
import clusterproject.program.Slider.RangeSliderUI;
import clusterproject.util.MinMax;
import clusterproject.util.Util;
import smile.math.Math;

public class FilterWindow extends JPanel {

	private static final long serialVersionUID = 1052960199516074256L;
	private static final int SPACING = 10;
	private static final int ABOVE_BAR_SPACE = 120;
	private static final int SLIDERHEIGHT = 30;
	private static final int MAX_BINS = 31;
	private static final int MIN_BINS = 7;
	private static final int CHART_SLIDER_OVERLAP = 8;

	private SpringLayout mainLayout = new SpringLayout();

	private List<ClusteringResult> clusteringResults;
	private final Map<String, Object> selectors;
	private final Map<String, double[]> allParametersMap;
	private final Map<String, MinMax> allParametersRangeMap;

	private final Map<String, Integer> bucketsMap;
	private final ClusteringViewer clusteringViewer;
	private final JLabel headerLabel = new JLabel(HistogramData.All.toString());

	private final Set<ClusteringResult> filteredSet = new HashSet<ClusteringResult>();
	private List<List<List<Object>>> filteredParameters;
	private final LinkedHashSet<String> clusteringNames;
	private final List<LinkedHashSet<String>> parameterNames;
	private List<List<List<Object>>> parameters;
	private final Map<String, HistogramDataPainter> charts;
	private final List<ClusteringResult> clusteringBaseResults;
	private boolean ignoreChange = false;
	private HistogramData histogramData = HistogramData.All;

	public FilterWindow(List<ClusteringResult> clusteringResults, ClusteringViewer clusteringViewer) {
		mainLayout = new SpringLayout();
		charts = new HashMap<String, HistogramDataPainter>();
		this.clusteringViewer = clusteringViewer;
		allParametersMap = new HashMap<String, double[]>();
		allParametersRangeMap = new HashMap<String, MinMax>();

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
				final MinMax minMax = new MinMax();
				final double[] allParameters = new double[parameters.get(i).get(j).size()];
				for (int k = 0; k < parameters.get(i).get(j).size(); ++k) {
					final Double value = Parameter.getParameterDoubleValue(parameters.get(i).get(j).get(k));
					if (value == Double.NaN) {
						System.err.println("unexpected value type");
						continue;
					}
					allParameters[k] = value;
					minMax.add(value);
				}
				int bins = (int) Math.ceil(Math.sqrt(allParameters.length)) + 1;

				if (parameters.get(i).get(j).size() <= 1)
					bins = 1;
				else if (parameters.get(i).get(j).get(0) instanceof Integer)
					bins = (int) (minMax.getRange() + 1);// TODO check if this is good

				else if (parameters.get(i).get(j).get(0) instanceof Boolean)
					bins = 3;
				if (bins < 1)
					bins = 1;
				if (bins > MAX_BINS)
					bins = MAX_BINS;
				if ((parameters.get(i).get(j).get(0) instanceof Double
						|| parameters.get(i).get(j).get(0) instanceof Integer) && bins < MIN_BINS && bins != 1) {
					bins = MIN_BINS;
				}
				bucketsMap.put(clusteringName + " " + parameterName, bins);
				allParametersRangeMap.put(clusteringName + " " + parameterName, minMax);
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
					final Double value = Parameter.getParameterDoubleValue(parameters.get(i).get(j).get(k));
					if (value == Double.NaN) {
						System.err.println("unexpected value type");
						continue;
					}
					allParameters[k] = value;
				}
			}
		}
		SwingUtilities.invokeLater(() -> repaint());

	}

	private void adjust() {
		final Iterator<String> clusteringNamesIt = clusteringNames.iterator();
		mainLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, headerLabel, 0, SpringLayout.HORIZONTAL_CENTER, this);
		mainLayout.putConstraint(SpringLayout.NORTH, headerLabel, 0, SpringLayout.NORTH, this);
		Component lastComponent = headerLabel;
		this.add(lastComponent, new Integer(0));

		int prefHeight = (int) headerLabel.getPreferredSize().getHeight();

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
				final MinMax minMax = allParametersRangeMap.get(clusteringName + " " + parameterName);
				final double[] allParameters = allParametersMap.get(clusteringName + " " + parameterName);
				for (int k = 0; k < parameters.get(i).get(j).size(); ++k) {
					final Double value = Parameter.getParameterDoubleValue(parameters.get(i).get(j).get(k));
					if (value == Double.NaN) {
						System.err.println("unexpected value type");
						continue;
					}
				}
				if (minMax.max != -Double.MAX_VALUE) {
					final RangeSlider slider = new MyRangeSlider(minMax, this);
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

					final HistogramDataPainter painter = new HistogramDataPainter(
							createDataset(allParameters, null, bins, minMax));

					charts.put(clusteringName + " " + parameterName, painter);

					mainLayout.putConstraint(SpringLayout.NORTH, painter, -ABOVE_BAR_SPACE, SpringLayout.NORTH, slider);
					mainLayout.putConstraint(SpringLayout.SOUTH, painter, CHART_SLIDER_OVERLAP, SpringLayout.NORTH,
							slider);
					mainLayout.putConstraint(SpringLayout.WEST, painter, (int) sliderRectangle.getX() - 2,
							SpringLayout.WEST, this);
					mainLayout.putConstraint(SpringLayout.EAST, painter, (int) (-sliderRectangle.getX() + 2),
							SpringLayout.EAST, this);
					this.add(painter, new Integer(4));
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

	public int[][] createDataset(double[] allParameters, double[] filteredParametersD, int bins, MinMax minMax) {
		final int[] activeParametersBins = new int[bins];
		for (int i = 0; i < bins; ++i) {
			activeParametersBins[i] = 0;
		}
		int[] filteredParametersBins = null;
		final double start = minMax.getRange();
		final double width = start / (bins - 1);

		if (filteredParametersD == null) {
			for (int i = 0; i < allParameters.length; ++i) {
				activeParametersBins[(int) Math.round((allParameters[i] - minMax.min) / width)] += 1;
			}
		} else {
			filteredParametersBins = new int[bins];
			for (int i = 0; i < bins; ++i) {
				filteredParametersBins[i] = 0;
			}
			for (int i = 0; i < filteredParametersD.length; ++i) {
				final int idx = (int) Math.round((filteredParametersD[i] - minMax.min) / width);
				// filteredParametersBins[idx] -= 1;
				activeParametersBins[idx] += 1;
			}
			for (int i = 0; i < allParameters.length; ++i) {
				filteredParametersBins[(int) Math.round((allParameters[i] - minMax.min) / width)] += 1;
			}
		}

		return new int[][] { activeParametersBins, filteredParametersBins };
	}

	private class HistogramDataPainter extends JComponent {
		private static final long serialVersionUID = 3116762462606134991L;
		final int max;
		int[] activeParametersBins;
		int[] filteredParametersBins;

		public HistogramDataPainter(int[][] bins) {
			int max = -Integer.MAX_VALUE;
			for (int i = 0; i < bins[0].length; ++i)
				if (bins[0][i] > max)
					max = bins[0][i];
			this.max = max;
			setData(bins);
		}

		public void setData(int[][] bins) {
			this.activeParametersBins = bins[0];
			this.filteredParametersBins = bins[1];
		}

		@Override
		public void paint(Graphics g) {
			final Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
			g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
			final int width = getWidth();
			final int height = getHeight();
			g2.setComposite(AlphaComposite.SrcOver.derive(.5f));
			g2.setColor(Color.RED);
			final List<Integer> xpoints = new ArrayList<Integer>();
			xpoints.add(width);
			xpoints.add(0);
			final List<Integer> ypoints = new ArrayList<Integer>();
			ypoints.add(height);
			ypoints.add(height);
			final double singleWidth = width / (double) (activeParametersBins.length - 1);
			double curx = 0;
			if (activeParametersBins.length == 1) {
				g2.fillRect(0, (int) Math.round(height - (height * (activeParametersBins[0] / (double) max))), width,
						height - (int) Math.round(height - (height * (activeParametersBins[0] / (double) max))));
			} else {
				for (int i = 0; i < activeParametersBins.length; ++i) {
					xpoints.add((int) Math.round(curx));
//				xpoints.add((int) Math.round(curx - singleWidth / 2));
					ypoints.add((int) Math.round(height - (height * (activeParametersBins[i] / (double) max))));
//				xpoints.add((int) Math.round(curx + singleWidth / 2));
//				ypoints.add((int) Math.round(height - (height * (activeParametersBins[i] / (double) max))));
					curx += singleWidth;
				}
				final int[] xarray = xpoints.stream().mapToInt(i -> i).toArray();
				final int[] yarray = ypoints.stream().mapToInt(i -> i).toArray();
				g2.fillPolygon(xarray, yarray, xarray.length);
			}
			g2.setColor(Color.BLUE);

			curx -= singleWidth;
			if (filteredParametersBins != null && filteredParametersBins.length == 1) {
				if (activeParametersBins.length == 1) {
					g2.fillRect(0, (int) Math.round((height - (height * (filteredParametersBins[0] / (double) max)))),
							width,
							height - (int) Math.round((height - (height * (filteredParametersBins[0] / (double) max))))
									- (int) Math.round((height * (activeParametersBins[0] / (double) max))));
				}
			} else {
//				curx = 0;
//				xpoints.clear();
//				ypoints.clear();
//				xpoints.add(width);
//				xpoints.add(0);
//				ypoints.add(height);
//				ypoints.add(height);
				xpoints.remove(0);
				xpoints.remove(0);
				ypoints.remove(0);
				ypoints.remove(0);
				if (filteredParametersBins != null) {
					for (int i = filteredParametersBins.length - 1; i >= 0; --i) {
						xpoints.add((int) Math.round(curx));
//				xpoints.add((int) Math.round(curx - singleWidth / 2));
						ypoints.add((int) Math.round((height - (height * (filteredParametersBins[i] / (double) max)))));
//				xpoints.add((int) Math.round(curx + singleWidth / 2));
//				ypoints.add((int) Math.round(height - (height * (filteredParametersBins[i] / (double) max))));
						curx -= singleWidth;
					}

					final int[] xarray = xpoints.stream().mapToInt(i -> i).toArray();
					final int[] yarray = ypoints.stream().mapToInt(i -> i).toArray();
					g2.fillPolygon(xarray, yarray, xarray.length);
				}
			}

		}
	}

	protected void comitFilteredData() {
		if (filteredSet.size() == clusteringBaseResults.size())
			clusteringViewer.setFilteredData(null);
		else
			clusteringViewer.setFilteredData(filteredSet);

	}

	private class MyRangeSlider extends RangeSlider {
		private static final long serialVersionUID = -1145841853132161271L;
		private static final int LABEL_COUNT = 3;
		final Map<Integer, String> dict;
		private static final int TICK_COUNT = 500;
		private final MinMax minMax;
		private final JLabel tooltip = new JLabel();
		private final FilterWindow filterWindow;
		private int oldMin = 0;
		private int oldMax = TICK_COUNT;
		private final JFrame tooltipFrame;

		public MyRangeSlider(MinMax minMax, FilterWindow mainPanel) {
			this.minMax = minMax;
			this.filterWindow = mainPanel;
			setOpaque(false);
			setMinimum(0);
			setMaximum(TICK_COUNT);
			setValue(0);
			setUpperValue(TICK_COUNT);
			setFocusable(false);
			setPaintLabels(true);
			dict = new HashMap<Integer, String>();
			// XXX: paint this manually
			for (int i = 0; i < LABEL_COUNT; ++i) {
				// String.format("%.3f", (maxLbl - minLbl) * i + minLbl));
				dict.put(i, Float.toString((float) ((minMax.getRange()) * (i) / (LABEL_COUNT - 1) + minMax.min)));
			}

			tooltipFrame = new JFrame();
			tooltipFrame.setType(javax.swing.JFrame.Type.UTILITY);
			tooltipFrame.setUndecorated(true);
			tooltipFrame.getContentPane().setBackground(MainWindow.BACKGROUND_COLOR);
			tooltip.setOpaque(false);
			tooltipFrame.add(tooltip);
			tooltip.setText(getTooltipText());
			addMouseMotionListener(new MouseMotionAdapter() {
			});

			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (e.isConsumed())
						return;
					e.consume();
					tooltip.setText(getTooltipText());
					final Point p = MouseInfo.getPointerInfo().getLocation();
					tooltipFrame.pack();
					int x = (int) p.getX() - tooltipFrame.getWidth() / 2;
					x = (int) Math.min(x, getLocationOnScreen().getX() + getWidth() - tooltipFrame.getWidth());
					x = (int) Math.max(x, getLocationOnScreen().getX());
					tooltipFrame.setLocation(x, (int) (getLocationOnScreen().getY() + getHeight() / 2));
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
			});

			addChangeListener(e -> {
				handleChange();
			});
		}

		@Override
		public void paint(Graphics g) {
			final Graphics2D g2 = (Graphics2D) g;
			super.paint(g);
			g2.setColor(Color.DARK_GRAY);
			final int width = getWidth();
			g2.drawString(dict.get(0), 0, 30);
			for (int i = 1; i < LABEL_COUNT - 1; ++i) {
				final int x = (int) Math.round(width * i / ((double) LABEL_COUNT - 1));
				final int widthi = g2.getFontMetrics().stringWidth(dict.get(i));
				g2.drawString(dict.get(i), (int) Math.round(x - widthi / (double) 2), 30);
			}
			final int widthLast = g2.getFontMetrics().stringWidth(dict.get(LABEL_COUNT - 1));
			g2.drawString(dict.get(LABEL_COUNT - 1), getWidth() - widthLast, 30);
		}

		private String getTooltipText() {
			if (getLowerValueD() == -Double.MAX_VALUE && getUpperValueD() == Double.MAX_VALUE)
				return " All ";
			if (getLowerValueD() == -Double.MAX_VALUE)
				return " < " + (float) getUpperValueD() + " ";
			if (getUpperValueD() == Double.MAX_VALUE)
				return (" " + (float) getLowerValueD() + " < ");
			return (" " + String.valueOf(((float) getLowerValueD()) + " <-> " + (float) getUpperValueD()) + " ");
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
						final Double value = Parameter.getParameterDoubleValue(params.get(param));
						if (value == Double.NaN) {
							System.err.println("unexpected value type");
							continue;
						}
						if (value * 0.99999 >= ((MyRangeSlider) selector).getUpperValueD()
								|| value * 1.00001 <= ((MyRangeSlider) selector).getLowerValueD()) {
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
					final MinMax minMax = allParametersRangeMap.get(clusteringName + " " + parameterName);

					double[] filteredParametersD = null;
					if (parameters.get(i).get(j).size() != filteredParameters.get(i).get(j).size()) {
						filteredParametersD = new double[filteredParameters.get(i).get(j).size()];
						for (int k = 0; k < filteredParameters.get(i).get(j).size(); ++k) {
							final Double value = Parameter.getParameterDoubleValue(filteredParameters.get(i).get(j).get(k));
							if (value == Double.NaN) {
								System.err.println("unexpected value type");
								continue;
							}
							filteredParametersD[k] = value;
						}
					}
					if (minMax.max != -Double.MAX_VALUE) {
						final int bins = bucketsMap.get(clusteringName + " " + parameterName);

						final HistogramDataPainter chart = charts.get(clusteringName + " " + parameterName);
						chart.setData(createDataset(allParameters, filteredParametersD, bins, minMax));
					} else {
						System.err.println("no values found");
					}
				}
			}

			if (!forceUpdate) {
				tooltip.setText(getTooltipText());
				final Point p = MouseInfo.getPointerInfo().getLocation();
				tooltipFrame.pack();
				int x = (int) p.getX() - tooltipFrame.getWidth() / 2;
				x = (int) Math.min(x, getLocationOnScreen().getX() + getWidth() - tooltipFrame.getWidth());
				x = (int) Math.max(x, getLocationOnScreen().getX());
				tooltipFrame.setLocation(x, (int) (getLocationOnScreen().getY() + getHeight() / 2));
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
			if (getUpperValue() == TICK_COUNT)
				return Double.MAX_VALUE;
			return (minMax.getRange()) * ((double) getUpperValue() / TICK_COUNT) + minMax.min;
		}

		public double getLowerValueD() {
			if (getValue() == 0)
				return -Double.MAX_VALUE;
			return (minMax.getRange()) * ((double) getValue() / TICK_COUNT) + minMax.min;
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
					final Double value = Parameter.getParameterDoubleValue(params.get(param));
					if (value == Double.NaN) {
						System.err.println("unexpected value type");
						continue;
					}
					if (value * 0.99999 >= ((MyRangeSlider) selector).getUpperValueD()
							|| value * 1.00001 <= ((MyRangeSlider) selector).getLowerValueD()) {
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

	public void setHistogramData(List<ClusteringResult> newData, HistogramData histogramData) {
		this.histogramData = histogramData;

		headerLabel.setText(histogramData.toString());
		rebuild(newData);
		forceChange();

	}

	public HistogramData getHistogramData() {
		return histogramData;
	}

	public enum HistogramData {
		Colored("Colored Clusterings"), All("All Clusterings"), Highlited("Selected Clusterings");

		private String name;

		HistogramData(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

}
