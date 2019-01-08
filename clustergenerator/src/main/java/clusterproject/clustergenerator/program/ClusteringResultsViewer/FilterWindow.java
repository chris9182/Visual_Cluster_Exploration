package clusterproject.clustergenerator.program.ClusteringResultsViewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
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

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import com.jidesoft.swing.RangeSlider;

import clusterproject.clustergenerator.data.ClusteringResult;
import clusterproject.clustergenerator.program.MainWindow;

public class FilterWindow extends JFrame {

	private static final long serialVersionUID = 1052960199516074256L;
	private static final int BARWIDTH = 10;
	private static final int BARVOFFSET = 8;
	private static final int BARHOFFSET = 16;
	private static final int SPACING = 10;
	private static final int SLIDERWIDTH = 70;

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
		final int clusteringNameWidth = getWidth() / clusteringNames.size();
		for (int i = 0; i < clusteringNames.size(); ++i) {
			final String clusteringName = clusteringNamesIt.next();
			final JLabel clusteringNameLabel = new JLabel(clusteringName);
			final int clusteringNameCenter = getWidth() / (clusteringNames.size()) * (i)
					+ getWidth() / (clusteringNames.size()) / 2;
			mainLayout.putConstraint(SpringLayout.NORTH, clusteringNameLabel, SPACING, SpringLayout.NORTH, mainPane);
			mainLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, clusteringNameLabel, clusteringNameCenter,
					SpringLayout.WEST, mainPane);
			mainPane.add(clusteringNameLabel, new Integer(2));
			// System.err.println(clusteringName);
			final Iterator<String> parameterNamesIt = parameterNames.get(i).iterator();
			final int parameterStart = clusteringNameCenter - clusteringNameWidth / 2;
			for (int j = 0; j < parameterNames.get(i).size(); ++j) {
				final String parameterName = parameterNamesIt.next();
				final JLabel parameterNameLabel = new JLabel(parameterName);
				final int clusteringParameterCenter = clusteringNameWidth / (parameterNames.get(i).size()) * (j)
						+ clusteringNameWidth / (parameterNames.get(i).size()) / 2 + parameterStart;
				mainLayout.putConstraint(SpringLayout.NORTH, parameterNameLabel, SPACING, SpringLayout.SOUTH,
						clusteringNameLabel);
				mainLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, parameterNameLabel, clusteringParameterCenter,
						SpringLayout.WEST, mainPane);
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
					if (min == max)
						slider.setEnabled(false);
					selectors.put(clusteringName + " " + parameterName, slider);
					mainLayout.putConstraint(SpringLayout.NORTH, slider, SPACING, SpringLayout.SOUTH,
							parameterNameLabel);
					mainLayout.putConstraint(SpringLayout.SOUTH, slider, -SPACING, SpringLayout.NORTH, filterButton);
					mainLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, slider, 0, SpringLayout.HORIZONTAL_CENTER,
							parameterNameLabel);
					mainPane.add(slider, new Integer(2));
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
		private final List<JLabel> labels = new ArrayList<JLabel>();
		private final double minLbl;
		private final double maxLbl;
		private final JLabel tooltip = new JLabel();

		public MyRangeSlider(double minLbl, double maxLbl) {
			super(RangeSlider.VERTICAL);
			this.minLbl = minLbl;
			this.maxLbl = maxLbl;
			setOpaque(false);
			setMinimum(0);
			setMaximum(10000);
			setLowValue(0);
			setHighValue(10000);
			setRangeDraggable(true);
			setPaintTrack(false);
			setPaintLabels(true);
			final Dictionary<Integer, JComponent> dict = new Hashtable<Integer, JComponent>();
			for (int i = 0; i < 11; ++i) {
				final JLabel label = new JLabel("   " + (float) ((maxLbl - minLbl) * (i) / 10 + minLbl));
				// String.format("%.3f", (maxLbl - minLbl) * i + minLbl));
				labels.add(label);
				dict.put(i * 1000, label);
			}
			setLabelTable(dict);
			final JFrame tooltipFrame = new JFrame();

			tooltipFrame.setUndecorated(true);
			tooltipFrame.getContentPane().setBackground(MainWindow.BACKGROUND_COLOR);
			tooltip.setOpaque(false);
			tooltipFrame.add(tooltip);
			tooltip.setText((String.valueOf((float) getUpperValue())) + " <-> " + ((float) getLowerValue()));
			addChangeListener(e -> {
				tooltip.setText((String.valueOf((float) getUpperValue())) + " <-> " + ((float) getLowerValue()));
				final Point p = MouseInfo.getPointerInfo().getLocation();
				int labelwidth = 0;
				for (final JLabel label : labels)
					if (label.getWidth() > labelwidth)
						labelwidth = label.getWidth();
				tooltipFrame.pack();
				tooltipFrame.setLocation((int) (getLocationOnScreen().getX() + getWidth() / 2),
						(int) p.getY() - tooltipFrame.getHeight() / 2);
				tooltipFrame.setVisible(true);
			});
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					tooltip.setText((String.valueOf((float) getUpperValue())) + " <-> " + ((float) getLowerValue()));
					final Point p = MouseInfo.getPointerInfo().getLocation();
					int labelwidth = 0;
					for (final JLabel label : labels)
						if (label.getWidth() > labelwidth)
							labelwidth = label.getWidth();
					tooltipFrame.pack();
					tooltipFrame.setLocation((int) (getLocationOnScreen().getX() + getWidth() / 2),
							(int) p.getY() - tooltipFrame.getHeight() / 2);
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

		public double getUpperValue() {
			return (maxLbl - minLbl) * ((double) getHighValue() / 10000) + minLbl;
		}

		public double getLowerValue() {
			return (maxLbl - minLbl) * ((double) getLowValue() / 10000) + minLbl;
		}

		@Override
		public void paint(Graphics g) {
			final Color c = g.getColor();
			g.setColor(Color.black);
			int labelwidth = 0;
			for (final JLabel label : labels)
				if (label.getWidth() > labelwidth)
					labelwidth = label.getWidth();
			g.drawRect(getWidth() / 2 - BARWIDTH + BARHOFFSET - labelwidth / 2, BARVOFFSET, BARWIDTH / 2,
					getHeight() - 2 * BARVOFFSET);
			g.setColor(c);
			super.paint(g);

		}
	};

}
