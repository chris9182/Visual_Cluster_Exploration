package clusterproject.program.ClusteringResultsViewer;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import clusterproject.program.MetaClustering.ClusteringWithDistance;
import clusterproject.util.Util;
import smile.math.Math;

public class HeatMap extends JLayeredPane {

	private static final long serialVersionUID = 4325048897002003982L;
	private static final int BAR_OFFSET = 4;
	public static final Color MIN_COLOR = Color.WHITE;
	public static final Color MAX_COLOR = new Color(102, 51, 0);
	private static final int BAR_WIDTH = 30;
	private static final int LABLE_OFFSET = 10;
	private final double[][] distances;
	private final SpringLayout layout;
	private final JPanel heatMap;
	private final ClusteringViewer clusteringViewer;
	Map<Integer, Integer> indexMap;

	public HeatMap(double[][] distances, ClusteringViewer clusteringViewer,
			List<ClusteringWithDistance> clusteringList) {
		this.distances = distances;
		this.clusteringViewer = clusteringViewer;
		this.indexMap = new HashMap<Integer, Integer>();
		int key = 0;
		for (final ClusteringWithDistance clu : clusteringList)
			indexMap.put(key++, clu.inIndex);
		heatMap = new JPanel();
		layout = new SpringLayout();
		setLayout(layout);
		setOpaque(false);
		heatMap.setOpaque(false);
		heatMap.setLayout(new BorderLayout());
		// heatMap.setLayout(new GridLayout(distances.length, distances.length));
		double maxDistance = -Double.MAX_VALUE;
		for (int i = 0; i < distances.length; ++i)
			for (int j = i; j < distances.length; ++j) {
				if (distances[i][j] > maxDistance)
					maxDistance = distances[i][j];
			}
		heatMap.add(new HeatMapDataPainter(this, distances, maxDistance));

		final GradientBar bar = new GradientBar(maxDistance);
		layout.putConstraint(SpringLayout.NORTH, bar, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, bar, 0, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.SOUTH, bar, 0, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.WEST, bar, -BAR_WIDTH, SpringLayout.EAST, this);
		add(bar, new Integer(20));

		layout.putConstraint(SpringLayout.NORTH, heatMap, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, heatMap, -BAR_OFFSET, SpringLayout.WEST, bar);
		layout.putConstraint(SpringLayout.SOUTH, heatMap, 0, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.WEST, heatMap, 0, SpringLayout.WEST, this);
		add(heatMap, new Integer(20));

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

					else
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

	private class GradientBar extends JComponent {
		private static final long serialVersionUID = -4332030550181663631L;
		private final double maxValue;

		public GradientBar(double maxDistance) {
			this.maxValue = maxDistance;
		}

		@Override
		public void paint(Graphics g) {
			final Graphics2D g2 = (Graphics2D) g;
			final int startX = 0, startY = 0, endX = 0, endY = getHeight();
			final GradientPaint gradient = new GradientPaint(startX, startY, MIN_COLOR, endX, endY, MAX_COLOR);
			g2.setPaint(gradient);
			g2.fill(new Rectangle(startX, startY, getWidth(), getHeight()));

			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			final DecimalFormat df = new DecimalFormat("#.##");
			g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
			g2.setColor(MAX_COLOR);
			Util.drawRotatedString(g2, LABLE_OFFSET, LABLE_OFFSET, 90, df.format(maxValue));
			final int endTickWidth = g.getFontMetrics().stringWidth(df.format(0));
			g2.setColor(MIN_COLOR);
			Util.drawRotatedString(g2, LABLE_OFFSET, getHeight() - LABLE_OFFSET - endTickWidth, 90, df.format(0));

		}
	}

	private int getInIndex(int selected) {
		return indexMap.get(selected);
	}

}
