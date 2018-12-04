package clusterproject.clustergenerator.userInterface.ClusterViewerElement;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.JPanel;

import clusterproject.clustergenerator.Util;
import clusterproject.clustergenerator.data.PointContainer;
import clusterproject.clustergenerator.userInterface.ScatterPlot;

public class PointCanvas extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static final int HIGHLIGHT_FACTOR = 2;

	private static final Color HIGHLIGHT_COLOR = Color.ORANGE;

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
		final double[] yCoordinates = new double[pointCount];
		final double[] xCoordinates = new double[pointCount];

		final List<double[]> points = pointContainer.getPoints();

		IntStream.range(0, yCoordinates.length).forEach(i -> yCoordinates[i] = clusterViewer.getPixelY(points.get(i)));
		IntStream.range(0, xCoordinates.length).forEach(i -> xCoordinates[i] = clusterViewer.getPixelX(points.get(i)));

		if (!pointContainer.hasClusters())
			for (int i = 0; i < pointCount; ++i) {
				if (Double.isNaN(xCoordinates[i]) || Double.isNaN(yCoordinates[i]))
					continue;

				g2.setColor(Color.GRAY);
				g2.fillOval((int) xCoordinates[i] - pointWidth / 2, (int) yCoordinates[i] - pointWidth / 2, pointWidth,
						pointWidth);
				g2.setColor(Color.BLACK);
				g2.drawOval((int) xCoordinates[i] - pointWidth / 2, (int) yCoordinates[i] - pointWidth / 2, pointWidth,
						pointWidth);

			}
		else {
			final List<Integer> clusterIDs = pointContainer.getClusterIDs();
			final Stream<Integer> stream = clusterIDs.stream().distinct();
			final Map<Integer, Color> colorMap = new HashMap<Integer, Color>();
			final Iterator<Integer> iter = stream.iterator();
			while (iter.hasNext()) {
				final int i = iter.next();
				colorMap.put(i, Util.getColor(i + 2));
			}

			for (int i = 0; i < pointCount; ++i) {
				if (Double.isNaN(xCoordinates[i]) || Double.isNaN(yCoordinates[i]))
					continue;
				g2.setColor(colorMap.get(clusterIDs.get(i)));

				g2.fillOval((int) xCoordinates[i] - pointWidth / 2, (int) yCoordinates[i] - pointWidth / 2, pointWidth,
						pointWidth);
				g2.setColor(Color.BLACK);
				g2.drawOval((int) xCoordinates[i] - pointWidth / 2, (int) yCoordinates[i] - pointWidth / 2, pointWidth,
						pointWidth);

			}
		}
		final int highlighted = pointContainer.getHighlighted();
		if (highlighted != -1) {
			g2.setColor(HIGHLIGHT_COLOR);
			g2.fillOval((int) (xCoordinates[highlighted] - pointWidth * HIGHLIGHT_FACTOR / 2),
					(int) (yCoordinates[highlighted] - pointWidth * HIGHLIGHT_FACTOR / 2),
					pointWidth * HIGHLIGHT_FACTOR, pointWidth * HIGHLIGHT_FACTOR);
			g2.setColor(Color.BLACK);
			g2.drawOval((int) (xCoordinates[highlighted] - pointWidth * HIGHLIGHT_FACTOR / 2),
					(int) (yCoordinates[highlighted] - pointWidth * HIGHLIGHT_FACTOR / 2),
					pointWidth * HIGHLIGHT_FACTOR, pointWidth * HIGHLIGHT_FACTOR);
		}
	}

}
