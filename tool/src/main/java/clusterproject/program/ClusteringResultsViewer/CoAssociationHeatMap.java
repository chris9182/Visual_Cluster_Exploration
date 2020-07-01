package clusterproject.program.ClusteringResultsViewer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

import clusterproject.program.MetaClustering.OpticsResult;
import smile.math.Math;

public class CoAssociationHeatMap extends HeatMap {
	private static final long serialVersionUID = -3268593863313504815L;

	public CoAssociationHeatMap(double[][] distances, MetaViewer clusteringViewer, OpticsResult clusteringList) {
		super(distances, clusteringViewer, clusteringList);

		heatMap.add(new HeatMapDataPainter(this, distances, maxDistance));

	}

	private class HeatMapDataPainter extends JComponent {
		private static final long serialVersionUID = 6140315256167371111L;
		final double[][] distances;
		final double maxDistance;
		final HeatMap heatMap;

		public HeatMapDataPainter(HeatMap heatMap, double[][] distances, double maxDistance) {
			this.distances = distances;
			this.maxDistance = maxDistance;
			this.heatMap = heatMap;
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					final int width = getWidth();
					final int length = distances.length;
					final double singleWidth = width / (double) length;
					final int selected = (int) ((e.getX()) / singleWidth);
					// TODO
				}
			});
		}

		@Override
		public void paint(Graphics g) {
			final Graphics2D g2 = (Graphics2D) g;
			final int width = getWidth();
			final int height = getHeight();
			final int length = distances.length;
			double startx;
			double starty = 0;
			final double singleWidth = width / (double) length;
			final double singleHeight = height / (double) length;
			for (int i = length - 1; i >= 0; --i) {
				startx = 0;
				for (int j = 0; j < length; ++j) {
					g2.setColor(getColor(distances[i][j] / maxDistance, MIN_COLOR, MAX_COLOR));
					final int iStartx = (int) Math.round(startx);
					final int iEndx = (int) Math.round(startx + singleWidth);
					final int iStarty = (int) Math.round(starty);
					final int iEndy = (int) Math.round(starty + singleHeight);
					g2.fillPolygon(new int[] { iStartx, iEndx, iEndx, iStartx },
							new int[] { iStarty, iStarty, iEndy, iEndy }, 4);
					startx += singleWidth;
				}
				starty += singleHeight;
			}

			super.paint(g);
		}

		public Color getColor(double x, Color c1, Color c2) {
			final double y = 1 - x;
			final int red = (int) (x * c1.getRed() + y * c2.getRed());
			final int grn = (int) (x * c1.getGreen() + y * c2.getGreen());
			final int blu = (int) (x * c1.getBlue() + y * c2.getBlue());
			return new Color(red, grn, blu);
		}
	}

}
