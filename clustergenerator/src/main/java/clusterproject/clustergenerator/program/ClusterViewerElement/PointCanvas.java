package clusterproject.clustergenerator.program.ClusterViewerElement;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.JPanel;

import clusterproject.clustergenerator.Util;
import clusterproject.clustergenerator.data.PointContainer;

public class PointCanvas extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static final int HIGHLIGHT_FACTOR = 2;

	private static final int MIN_WIDTH_FOR_BORDER = 5;

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
		final Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final int pointWidth = clusterViewer.getPointDiameter();
		final int pointCount = pointContainer.getPoints().size();
		final double[] yCoordinates = new double[pointCount];
		final double[] xCoordinates = new double[pointCount];

		final List<double[]> points = pointContainer.getPoints();

		IntStream.range(0, yCoordinates.length).forEach(i -> yCoordinates[i] = clusterViewer.getPixelY(points.get(i)));
		IntStream.range(0, xCoordinates.length).forEach(i -> xCoordinates[i] = clusterViewer.getPixelX(points.get(i)));

		if (!pointContainer.hasClusters()) {
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
		} else {
			final List<Integer> clusterIDs = pointContainer.getClusterIDs();
			final Stream<Integer> stream = clusterIDs.stream().distinct();
			final Map<Integer, Color> colorMap = new HashMap<Integer, Color>();
			final Set<Integer> filtered = pointContainer.getFilteredIndexes();
			final Iterator<Integer> iter = stream.iterator();
			while (iter.hasNext()) {
				final int i = iter.next();
				colorMap.put(i, Util.getColor(i + 2));
			}

			for (int i = 0; i < pointCount; ++i) {
				if (Double.isNaN(xCoordinates[i]) || Double.isNaN(yCoordinates[i]))
					continue;

				if (filtered == null) {
					g2.setColor(colorMap.get(clusterIDs.get(i)));
				} else {
					if (filtered.contains(i)) {
						g2.setColor(colorMap.get(clusterIDs.get(i)));
					} else {
						g2.setColor(Color.WHITE);
					}
				}

				g2.fillOval((int) xCoordinates[i] - pointWidth / 2, (int) yCoordinates[i] - pointWidth / 2, pointWidth,
						pointWidth);
				if (pointWidth >= MIN_WIDTH_FOR_BORDER) {
					if (filtered == null) {
						g2.setColor(Color.BLACK);
					} else {
						if (filtered.contains(i)) {
							g2.setColor(Color.BLACK);
						} else {
							g2.setColor(Color.GRAY);
						}
					}

					g2.drawOval((int) xCoordinates[i] - pointWidth / 2, (int) yCoordinates[i] - pointWidth / 2,
							pointWidth, pointWidth);
				}

			}
		}
		final LinkedHashSet<Integer> highlighted = pointContainer.getHighlighted();
		if (highlighted.size() <= 1 && highlighted.iterator().next() == -1)
			return;
		for (final int hIndex : highlighted) {
			g2.setColor(HIGHLIGHT_COLOR);
			g2.fillOval((int) (xCoordinates[hIndex] - pointWidth * HIGHLIGHT_FACTOR / 2),
					(int) (yCoordinates[hIndex] - pointWidth * HIGHLIGHT_FACTOR / 2), pointWidth * HIGHLIGHT_FACTOR,
					pointWidth * HIGHLIGHT_FACTOR);
			if (pointWidth >= MIN_WIDTH_FOR_BORDER) {
				g2.setColor(Color.BLACK);
				g2.drawOval((int) (xCoordinates[hIndex] - pointWidth * HIGHLIGHT_FACTOR / 2),
						(int) (yCoordinates[hIndex] - pointWidth * HIGHLIGHT_FACTOR / 2), pointWidth * HIGHLIGHT_FACTOR,
						pointWidth * HIGHLIGHT_FACTOR);
			}
		}
	}

}
