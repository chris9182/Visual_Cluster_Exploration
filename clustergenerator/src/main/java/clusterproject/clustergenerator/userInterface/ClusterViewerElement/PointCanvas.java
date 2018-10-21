package clusterproject.clustergenerator.userInterface.ClusterViewerElement;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.JPanel;

import clusterproject.clustergenerator.data.PointContainer;
import clusterproject.clustergenerator.userInterface.ClusterViewer;

public class PointCanvas extends JPanel{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final int POINT_WIDTH=6;
	private final PointContainer pointContainer;
	private final ClusterViewer clusterViewer;

	public PointCanvas(PointContainer pointContainer,ClusterViewer clusterViewer) {
		this.pointContainer=pointContainer;
		this.clusterViewer=clusterViewer;
		setOpaque(false);
	}

	@Override
	public void paint(Graphics g) {

		super.paint(g);
		final Graphics2D g2 = (Graphics2D) g;

		for(final double[] position:pointContainer.getPoints()){
			final Point pixel=clusterViewer.getPixel(position);
			g2.drawOval(pixel.x-POINT_WIDTH/2, pixel.y-POINT_WIDTH/2, POINT_WIDTH, POINT_WIDTH);
			final Color color=g2.getColor();
			g2.setColor(Color.GRAY);
			g2.fillOval(pixel.x-POINT_WIDTH/2, pixel.y-POINT_WIDTH/2, POINT_WIDTH, POINT_WIDTH);
			g2.setColor(color);
		}

	}




}
