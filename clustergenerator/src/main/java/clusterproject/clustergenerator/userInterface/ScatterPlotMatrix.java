package clusterproject.clustergenerator.userInterface;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

import clusterproject.clustergenerator.data.PointContainer;

public class ScatterPlotMatrix extends JFrame {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final int POINT_SIZE = 3;

	JPanel mainPane;

	// XXX this could be improved to calculate the points WAY smarter
	// each scatterplot calculates each coordinate by itself for now
	public ScatterPlotMatrix(PointContainer pointContainer) {
		mainPane = new JPanel();
		mainPane.setBackground(MainWindow.BACKGROUND_COLOR);
		add(mainPane);
		final int dim = pointContainer.getDim();
		final GridLayout layout = new GridLayout(dim, dim);
		mainPane.setLayout(layout);
		mainPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		final ScatterPlot[][] matr = new ScatterPlot[dim][dim];
		for (int i = 0; i < dim; i++) {
			for (int j = 0; j < dim; j++) {
				final ScatterPlot scatterPlot = new ScatterPlot(null, pointContainer, false);
				matr[i][j] = scatterPlot;
				scatterPlot.setSelectedDimX(j);
				scatterPlot.setSelectedDimY(i);
				scatterPlot.setPointDiameter(POINT_SIZE);
				scatterPlot.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				mainPane.add(scatterPlot);
			}
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
}
