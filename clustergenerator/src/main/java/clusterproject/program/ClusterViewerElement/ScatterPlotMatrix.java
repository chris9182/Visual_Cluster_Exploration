package clusterproject.program.ClusterViewerElement;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import clusterproject.data.PointContainer;
import clusterproject.program.DataView;
import clusterproject.util.MinMax;

public class ScatterPlotMatrix extends JFrame {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final int POINT_SIZE = 3;
	private static final int TEXT_WIDTH = 20;

	private final JLayeredPane mainPane;
	private final List<String> headers;

	// XXX this could be improved to calculate the points WAY smarter
	// each scatterplot calculates each coordinate by itself for now
	public ScatterPlotMatrix(PointContainer pointContainer) {
		setTitle("Scatterplot Matrix");
		final SpringLayout springLayout = new SpringLayout();
		headers = pointContainer.getHeaders();
		final JLayeredPane pane = new JLayeredPane();
		pane.setLayout(springLayout);
		add(pane);
		mainPane = new JLayeredPane();
		getContentPane().setBackground(DataView.BACKGROUND_COLOR);
		mainPane.setBorder(null);
		pane.add(mainPane, new Integer(20));

		springLayout.putConstraint(SpringLayout.NORTH, mainPane, TEXT_WIDTH, SpringLayout.NORTH, pane);
		springLayout.putConstraint(SpringLayout.SOUTH, mainPane, 0, SpringLayout.SOUTH, pane);
		springLayout.putConstraint(SpringLayout.EAST, mainPane, 0, SpringLayout.EAST, pane);
		springLayout.putConstraint(SpringLayout.WEST, mainPane, TEXT_WIDTH, SpringLayout.WEST, pane);

		final int dim = pointContainer.getDim();
		final GridLayout layout = new GridLayout(dim, dim, 0, 0);
		mainPane.setLayout(layout);
		mainPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		final JComponent[][] matr = new JComponent[dim][dim];

		final JPanel xTextPane = new JPanel();
		xTextPane.setOpaque(false);
		final GridLayout xTextPaneLayout = new GridLayout(1, dim, 0, 0);
		xTextPane.setLayout(xTextPaneLayout);
		pane.add(xTextPane, new Integer(21));
		springLayout.putConstraint(SpringLayout.NORTH, xTextPane, 0, SpringLayout.NORTH, pane);
		springLayout.putConstraint(SpringLayout.WEST, xTextPane, TEXT_WIDTH, SpringLayout.WEST, pane);
		springLayout.putConstraint(SpringLayout.EAST, xTextPane, 0, SpringLayout.EAST, pane);
		springLayout.putConstraint(SpringLayout.SOUTH, xTextPane, TEXT_WIDTH, SpringLayout.NORTH, pane);

		final JPanel yTextPane = new JPanel();
		yTextPane.setOpaque(false);
		final GridLayout yTextPaneLayout = new GridLayout(dim, 1, 0, 0);
		yTextPane.setLayout(yTextPaneLayout);
		pane.add(yTextPane, new Integer(21));
		springLayout.putConstraint(SpringLayout.NORTH, yTextPane, TEXT_WIDTH, SpringLayout.NORTH, pane);
		springLayout.putConstraint(SpringLayout.WEST, yTextPane, 0, SpringLayout.WEST, pane);
		springLayout.putConstraint(SpringLayout.EAST, yTextPane, TEXT_WIDTH, SpringLayout.WEST, pane);
		springLayout.putConstraint(SpringLayout.SOUTH, yTextPane, 0, SpringLayout.SOUTH, pane);

		for (int i = 0; i < dim; i++) {
			for (int j = 0; j < dim; j++) {
				if (i == j) {
					if (pointContainer.getPointCount() > 2) {
						final Map<double[], Integer> map = pointContainer.getLabelMap();
						if (map == null) {

							final double[] vals = new double[pointContainer.getPointCount()];
							final List<double[]> points = pointContainer.getPoints();
							for (int p = 0; p < points.size(); ++p)
								vals[p] = points.get(p)[i];
							final KernelDensityPlot densityPlot = new KernelDensityPlot(new double[][] { vals },
									new int[] { 1 });
							matr[i][j] = densityPlot;
							densityPlot.setBorder(BorderFactory.createLineBorder(Color.BLACK));
							mainPane.add(densityPlot);
						} else {
							final Map<Integer, List<Double>> imap = new HashMap<Integer, List<Double>>();
							for (final Entry<double[], Integer> e : map.entrySet()) {
								List<Double> curList = imap.get(e.getValue());
								if (curList == null) {
									curList = new ArrayList<Double>();
								}
								curList.add(e.getKey()[i]);
								imap.put(e.getValue(), curList);
							}
							final double[][] data = new double[imap.size()][];
							final int[] colors = new int[imap.size()];
							int ind = 0;
							for (final Entry<Integer, List<Double>> e : imap.entrySet()) {
								data[ind] = e.getValue().stream().mapToDouble(d -> d).toArray();
								colors[ind] = e.getKey();
								++ind;
							}
							final KernelDensityPlot densityPlot = new KernelDensityPlot(data, colors);
							matr[i][j] = densityPlot;
							densityPlot.setBorder(BorderFactory.createLineBorder(Color.BLACK));
							mainPane.add(densityPlot);
						}
					} else {
						final JPanel panel = new JPanel();
						panel.setBackground(DataView.BACKGROUND_COLOR);
						panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
						mainPane.add(panel);
					}
				} else {
					final ScatterPlot scatterPlot = new ScatterPlot(pointContainer, false);
					matr[i][j] = scatterPlot;
					scatterPlot.setSelectedDimX(j);
					scatterPlot.setSelectedDimY(i);
					scatterPlot.setPointDiameter(POINT_SIZE);
					scatterPlot.setBorder(BorderFactory.createLineBorder(Color.BLACK));
					mainPane.add(scatterPlot);
				}
			}
			final JLabel lbl = new JLabel(headers.get(i));
			lbl.setHorizontalAlignment(SwingConstants.CENTER);
			xTextPane.add(lbl);
			final JLabel lbl2 = new RotateLabel(headers.get(i));
			lbl2.setHorizontalAlignment(SwingConstants.CENTER);
			yTextPane.add(lbl2);
		}

		final MinMax[] intervals = new MinMax[dim];
		for (int i = 0; i < dim; i++)
			intervals[i] = pointContainer.getMinMaxFrom(i);

		for (int i = 0; i < dim; i++) {
			for (int j = 0; j < dim; j++) {
				if (matr[i][j] instanceof ScatterPlot) {
					final ScatterPlot plot = (ScatterPlot) matr[i][j];
					plot.setIntervalX(intervals[j]);
					plot.setIntervalY(intervals[i]);
				}
			}
		}
	}

	private class RotateLabel extends JLabel {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public RotateLabel(String text) {
			super(text);
		}

		@Override
		public void paintComponent(Graphics g) {
			final Graphics2D gx = (Graphics2D) g;
			final FontMetrics metrics = gx.getFontMetrics();
			String text = getText();
			final int height = getHeight();
			final boolean reduce = metrics.stringWidth(text) > height;
			while (metrics.stringWidth(text) > height) {
				text = text.substring(0, text.length() - 1).trim();
			}
			if (reduce && text.length() > 4)
				text = text.substring(0, text.length() - 3) + "...";
			else if (reduce)
				text = getText().substring(0, 1);
			final int width = getWidth();
			gx.rotate(Math.PI / 2, width / 2, height / 2);

			gx.drawString(text, (int) (width / (double) 2 - metrics.stringWidth(text) / (double) 2),
					(int) (-3 + height / (double) 2 + (metrics.getHeight()) / (double) 2));
		}
	}
}
