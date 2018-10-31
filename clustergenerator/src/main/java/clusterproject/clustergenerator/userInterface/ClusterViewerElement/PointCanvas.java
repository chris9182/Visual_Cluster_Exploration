package clusterproject.clustergenerator.userInterface.ClusterViewerElement;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import java.util.stream.IntStream;

import javax.swing.JPanel;

import clusterproject.clustergenerator.data.PointContainer;
import clusterproject.clustergenerator.userInterface.ScatterPlot;

public class PointCanvas extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private final PointContainer pointContainer;
	private final ScatterPlot clusterViewer;

	public PointCanvas(PointContainer pointContainer, ScatterPlot clusterViewer) {
		this.pointContainer = pointContainer;
		this.clusterViewer = clusterViewer;
		setOpaque(false);
	}

	@Override
	public void paint(Graphics g) {

		super.paint(g);
		final Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final int pointWidth = clusterViewer.getPointDiameter();
		final int pointCount = pointContainer.getPoints().size();
		final int[] yCoordinates = new int[pointCount];
		final int[] xCoordinates = new int[pointCount];

		final List<double[]> points = pointContainer.getPoints();
		final IntStream stream = IntStream.range(0, pointCount);

		stream.parallel().forEach(value -> {
			final int[] pixel = clusterViewer.getPixel(points.get(value));
			xCoordinates[value] = pixel[0];
			yCoordinates[value] = pixel[1];
		});

		for (int i = 0; i < pointCount; ++i) {
			g2.setColor(Color.GRAY);
			g2.fillOval(xCoordinates[i] - pointWidth / 2, yCoordinates[i] - pointWidth / 2, pointWidth, pointWidth);
			g2.setColor(Color.BLACK);
			g2.drawOval(xCoordinates[i] - pointWidth / 2, yCoordinates[i] - pointWidth / 2, pointWidth, pointWidth);
		}
	}

}
