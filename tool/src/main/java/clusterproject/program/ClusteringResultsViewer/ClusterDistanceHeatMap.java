package clusterproject.program.ClusteringResultsViewer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JComponent;

import clusterproject.data.ClusteringResult;
import clusterproject.program.MetaClustering.OpticsResult;
import clusterproject.util.Util;
import smile.math.Math;

public class ClusterDistanceHeatMap extends HeatMap {
	private static final long serialVersionUID = -3268593863313504815L;

	public ClusterDistanceHeatMap(double[][] distances, MetaViewer clusteringViewer,
			OpticsResult<ClusteringResult> clusteredList) {
		super(distances, clusteringViewer, clusteredList);

		heatMap.add(new HeatMapDataPainter(this, distances, maxDistance));

	}

	public LinkedHashSet<Integer> getHighlighted() {
		return clusteringViewer.getHighlighted();
	}

	public void highlight(int selection, boolean replace, boolean singleClick) {
		clusteringViewer.mouseHighlight(selection, replace, singleClick);
	}

	public Set<Integer> getFilteredIndexes() {
		return clusteringViewer.getFilteredIndexes();
	}

	public int getTruth() {
		return clusteringViewer.getGroundTruth();
	}

	private class HeatMapDataPainter extends JComponent {
		private static final long serialVersionUID = 6140315256167371111L;
		final double[][] distances;
		final double maxDistance;
		final ClusterDistanceHeatMap heatMap;

		public HeatMapDataPainter(ClusterDistanceHeatMap heatMap, double[][] distances, double maxDistance) {
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
					highlight(heatMap.getInIndex(selected), !e.isControlDown(), e.getClickCount() == 1);
				}
			});
		}

		@Override
		public void paint(Graphics g) {
			final Graphics2D g2 = (Graphics2D) g;
			final Set<Integer> filtered = heatMap.getFilteredIndexes();
			final LinkedHashSet<Integer> highlighted = heatMap.getHighlighted();
			final int truth = heatMap.getTruth();
			final int width = getWidth();
			final int height = getHeight();
			final int length = distances.length;
			double startx;
			double starty = 0;
			final double singleWidth = width / (double) length;
			final double singleHeight = height / (double) length;
			for (int i = length - 1; i >= 0; --i) {
				final int myIndex = heatMap.getInIndex(i);
				startx = 0;
				for (int j = 0; j < length; ++j) {
					final int myIndex2 = heatMap.getInIndex(j);

					if (filtered != null && (!filtered.contains(myIndex) || !filtered.contains(myIndex2))
							&& myIndex != truth)
						g2.setComposite(AlphaComposite.SrcOver.derive(Util.FILTER_ALPHA));
					else
						g2.setComposite(AlphaComposite.SrcOver);

					if (i == j && highlighted.contains(myIndex))
						g2.setColor(Util.HIGHLIGHT_COLOR);
					else if (highlighted.contains(myIndex) || highlighted.contains(heatMap.getInIndex(j))) {
						g2.setColor(getColor(distances[i][j] / maxDistance, MIN_COLOR, MAX_COLOR).brighter());
					} else
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
