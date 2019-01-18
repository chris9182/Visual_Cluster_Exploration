package clusterproject.clustergenerator.program.ClusteringResultsViewer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import clusterproject.clustergenerator.Util;
import clusterproject.clustergenerator.program.MetaClustering.ClusteringWithDistance;

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

	public HeatMap(double[][] distances, ClusteringViewer clusteringViewer,
			List<ClusteringWithDistance> clusteringList) {
		this.distances = distances;
		this.clusteringViewer = clusteringViewer;
		heatMap = new JPanel();
		layout = new SpringLayout();
		setLayout(layout);
		setOpaque(false);
		heatMap.setOpaque(false);
		heatMap.setLayout(new GridLayout(distances.length, distances.length));
		double maxDistance = -Double.MAX_VALUE;
		for (int i = 0; i < distances.length; ++i)
			for (int j = i; j < distances.length; ++j) {
				if (distances[i][j] > maxDistance)
					maxDistance = distances[i][j];
			}
		for (int i = distances.length - 1; i >= 0; --i)
			for (int j = 0; j < distances.length; ++j) {
				final HeatCell cell = new HeatCell(this, distances[i][j] / maxDistance, clusteringList.get(i).inIndex,
						clusteringList.get(j).inIndex);
				heatMap.add(cell);
				final int selected = clusteringList.get(j).inIndex;
				cell.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						highlight(selected, !e.isControlDown());

					}
				});
			}

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

	public void highlight(int selection, boolean replace) {
		final List<Integer> highlighted = new ArrayList<Integer>();
		highlighted.add(selection);
		clusteringViewer.highlight(highlighted, replace);
	}

	public Set<Integer> getFilteredIndexes() {
		return clusteringViewer.getFilteredIndexes();
	}

	public int getTruth() {
		return clusteringViewer.getGroundTruth();
	}

	private class HeatCell extends JComponent {
		private static final long serialVersionUID = -1175380889963981647L;

		private final int i;
		private final int j;
		private final Color myColor;

		private final HeatMap heatMap;

		public HeatCell(HeatMap heatMap, double percent, int i, int j) {
			myColor = (getColor(percent, MIN_COLOR, MAX_COLOR));
			this.heatMap = heatMap;
			this.i = i;
			this.j = j;
		}

		@Override
		public void paint(Graphics g) {
			final Graphics2D g2 = (Graphics2D) g;
			final Set<Integer> filtered = heatMap.getFilteredIndexes();
			final LinkedHashSet<Integer> highlighted = heatMap.getHighlighted();
			final int truth = heatMap.getTruth();
			g2.setColor(myColor);
			if (i == j) {
				if (filtered != null && !filtered.contains(i) && i != truth)
					g2.setComposite(AlphaComposite.SrcOver.derive(Util.FILTER_ALPHA));
				else
					g2.setComposite(AlphaComposite.SrcOver);

				if (highlighted.contains(i))
					g2.setColor(Util.HIGHLIGHT_COLOR);
			}
			g.fillRect(0, 0, getWidth(), getHeight());
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
			Util.drawRotate(g2, LABLE_OFFSET, LABLE_OFFSET, 90, df.format(maxValue));
			final int endTickWidth = g.getFontMetrics().stringWidth(df.format(0));
			g2.setColor(MIN_COLOR);
			Util.drawRotate(g2, LABLE_OFFSET, getHeight() - LABLE_OFFSET - endTickWidth, 90, df.format(0));

		}
	}

}
