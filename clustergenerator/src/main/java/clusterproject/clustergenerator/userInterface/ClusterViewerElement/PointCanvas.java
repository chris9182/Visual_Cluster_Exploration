package clusterproject.clustergenerator.userInterface.ClusterViewerElement;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

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

		for (final double[] position : pointContainer.getPoints()) {
			final int[] pixel = clusterViewer.getPixel(position);// TODO: this could be fetched in parallel
			if (pixel == null)
				continue;
			g2.setColor(Color.GRAY);
			g2.fillOval(pixel[0] - pointWidth / 2, pixel[1] - pointWidth / 2, pointWidth, pointWidth);
			g2.setColor(Color.BLACK);
			g2.drawOval(pixel[0] - pointWidth / 2, pixel[1] - pointWidth / 2, pointWidth, pointWidth);
		}

	}

}
