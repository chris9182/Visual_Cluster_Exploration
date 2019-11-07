package clusterproject.program.ClusteringResultsViewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import clusterproject.program.MetaClustering.OpticsContainer;
import clusterproject.program.MetaClustering.OpticsResult;
import clusterproject.util.Util;

public class HeatMap extends JLayeredPane {

	private static final long serialVersionUID = 4325048897002003982L;
	private static final int BAR_OFFSET = 4;
	public static final Color MIN_COLOR = Color.WHITE;
	public static final Color MAX_COLOR = new Color(102, 51, 0);
	private static final int BAR_WIDTH = 30;
	private static final int LABLE_OFFSET = 10;
	private final SpringLayout layout;
	protected final JPanel heatMap;
	protected final double maxDistance;
	protected final ClusteringViewer clusteringViewer;
	Map<Integer, Integer> indexMap;

	public HeatMap(double[][] distances, ClusteringViewer clusteringViewer, OpticsResult<?> clusteredList) {
		this.clusteringViewer = clusteringViewer;
		this.indexMap = new HashMap<Integer, Integer>();
		int key = 0;
		for (final OpticsContainer<?> clu : clusteredList)
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
		this.maxDistance = maxDistance;
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

	protected int getInIndex(int selected) {
		return indexMap.get(selected);
	}

}
