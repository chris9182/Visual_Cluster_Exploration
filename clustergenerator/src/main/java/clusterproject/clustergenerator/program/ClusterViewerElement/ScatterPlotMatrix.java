package clusterproject.clustergenerator.program.ClusterViewerElement;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import clusterproject.clustergenerator.data.PointContainer;
import clusterproject.clustergenerator.program.MainWindow;

public class ScatterPlotMatrix extends JFrame {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final int POINT_SIZE = 3;
	private static final int TEXT_WIDTH = 20;

	private final JLayeredPane mainPane;
	private final PointContainer pointContainer;
	private final List<String> headers;

	// XXX this could be improved to calculate the points WAY smarter
	// each scatterplot calculates each coordinate by itself for now
	public ScatterPlotMatrix(PointContainer pointContainer) {
		setTitle("Scatterplot Matrix");
		this.pointContainer = pointContainer;
		final SpringLayout springLayout = new SpringLayout();
		headers = pointContainer.getHeaders();
		final JLayeredPane pane = new JLayeredPane();
		pane.setLayout(springLayout);
		add(pane);
		mainPane = new JLayeredPane();
		getContentPane().setBackground(MainWindow.BACKGROUND_COLOR);
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
		final ScatterPlot[][] matr = new ScatterPlot[dim][dim];

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
				final ScatterPlot scatterPlot = new ScatterPlot(pointContainer, false);
				matr[i][j] = scatterPlot;
				scatterPlot.setSelectedDimX(j);
				scatterPlot.setSelectedDimY(i);
				scatterPlot.setPointDiameter(POINT_SIZE);
				scatterPlot.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				mainPane.add(scatterPlot);
			}
			final JLabel lbl = new JLabel(headers.get(i));
			lbl.setHorizontalAlignment(SwingConstants.CENTER);
			xTextPane.add(lbl);
			final JLabel lbl2 = new RotateLabel(headers.get(i));
			lbl2.setHorizontalAlignment(SwingConstants.CENTER);
			yTextPane.add(lbl2);
		}

		final double[][] intervals = new double[dim][];
		for (int i = 0; i < dim; i++)
			intervals[i] = pointContainer.getMinMaxFrom(i);

		for (int i = 0; i < dim; i++) {
			for (int j = 0; j < dim; j++) {
				matr[i][j].setIntervalX(intervals[j]);
				matr[i][j].setIntervalY(intervals[i]);
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
			gx.rotate(Math.PI / 2, getWidth() / 2, getHeight() / 2);
			super.paintComponent(g);

		}
	}
}
