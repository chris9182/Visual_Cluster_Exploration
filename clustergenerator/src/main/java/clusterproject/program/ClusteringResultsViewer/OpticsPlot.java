package clusterproject.program.ClusteringResultsViewer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import clusterproject.data.ClusteringResult;
import clusterproject.program.ClusteringResultsViewer.FilterWindow.HistogramData;
import clusterproject.program.MetaClustering.ClusteringWithDistance;
import clusterproject.util.Util;

public class OpticsPlot extends JLayeredPane {

	private static final long serialVersionUID = 7515062269771305939L;

	private final static int BAR_OFFSET = 20;
	private static final int LABLE_OFFSET = 5;

	private final List<ClusteringWithDistance> clusteringList;
	private final JPanel opticsBars;
	private final SpringLayout layout;
	private final List<OpticsBar> bars;
	private double threshhold = -Double.MIN_NORMAL;
	private double max = 0;
	private final ClusteringViewer clusteringViewer;
	private final int NOISE_TAG = -2;
	private final JButton setHistogramDataButton;

	public OpticsPlot(ClusteringViewer clusteringViewer, List<ClusteringWithDistance> clusteringList) {
		this.clusteringViewer = clusteringViewer;
		this.clusteringList = clusteringList;
		layout = new SpringLayout();
		setLayout(layout);
		bars = new ArrayList<OpticsBar>(clusteringList.size());
		setOpaque(false);
		setHistogramDataButton = new JButton("Clusters to Histogram");
		setHistogramDataButton.addActionListener(e -> {
			adaptHistogramData();

		});
		add(setHistogramDataButton, new Integer(22));
		layout.putConstraint(SpringLayout.NORTH, setHistogramDataButton, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, setHistogramDataButton, 0, SpringLayout.EAST, this);
		setHistogramDataButton.setBackground(new Color(255, 255, 255, 0));// color doesnt matter just for transparancy
		// to kick in
		setHistogramDataButton.setOpaque(false);
		setHistogramDataButton.setFocusable(false);
		opticsBars = new JPanel();
		opticsBars.setOpaque(false);
		final GridLayout gridLayout = new GridLayout(0, clusteringList.size());
		opticsBars.setLayout(gridLayout);
		opticsBars.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.gray));

		layout.putConstraint(SpringLayout.NORTH, opticsBars, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, opticsBars, 0, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.SOUTH, opticsBars, 0, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.WEST, opticsBars, BAR_OFFSET, SpringLayout.WEST, this);
		for (int i = 1; i < clusteringList.size(); ++i) {
			if (clusteringList.get(i).distance > max && clusteringList.get(i).distance < Double.MAX_VALUE)
				max = clusteringList.get(i).distance;
		}
		if (max == 0)
			max = 1;
		else
			max *= 1.1;

		for (int i = 0; i < clusteringList.size(); ++i) {
			final int selection = clusteringList.get(i).inIndex;
			final OpticsBar bar = new OpticsBar(this, Math.min(clusteringList.get(i).distance / max, 1),
					clusteringList.get(i).inIndex);
			bar.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					// if(e.isShiftDown())
					// shiftHighlight(selection);//TODO: maybe shift selection?
					// else
					highlight(selection, !e.isControlDown(), e.getClickCount() == 1);

				}
			});
			bars.add(bar);
			opticsBars.add(bar);
		}
		add(opticsBars, new Integer(20));
		final JPanel threshholdClicker = new JPanel() {
			private static final long serialVersionUID = -343022910473819436L;

			@Override
			public void paint(Graphics g) {
				super.paint(g);
				final Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				final DecimalFormat df = new DecimalFormat("#.##");
				g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
				Util.drawRotatedString(g2, LABLE_OFFSET, LABLE_OFFSET, 90, df.format(max));
				final int endTickWidth = g.getFontMetrics().stringWidth(df.format(0));
				Util.drawRotatedString(g2, LABLE_OFFSET, getHeight() - LABLE_OFFSET - endTickWidth, 90, df.format(0));
			}
		};
		threshholdClicker.setBackground(Color.LIGHT_GRAY);
		layout.putConstraint(SpringLayout.NORTH, threshholdClicker, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, threshholdClicker, 0, SpringLayout.WEST, opticsBars);
		layout.putConstraint(SpringLayout.SOUTH, threshholdClicker, 0, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.WEST, threshholdClicker, 0, SpringLayout.WEST, this);
		add(threshholdClicker, new Integer(20));
		final MouseAdapter adapter = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				adaptThreshhold((((double) opticsBars.getHeight() - e.getY()) / opticsBars.getHeight()) * max);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				adaptThreshhold((((double) opticsBars.getHeight() - e.getY()) / opticsBars.getHeight()) * max);
			}
		};

		threshholdClicker.addMouseMotionListener(adapter);
		threshholdClicker.addMouseListener(adapter);
		adaptThreshhold(max / 2);

	}

	private void adaptHistogramData() {
		final List<ClusteringResult> newData = new ArrayList<ClusteringResult>();
		for (final ClusteringWithDistance c : clusteringList)
			if (c.tag >= 0)
				newData.add(c.getClustering());
		// if (newData.size() == clusteringList.size())
		// clusteringViewer.setHistogramData(newData, "All Clusterings");
		// else
		clusteringViewer.setHistogramData(newData, HistogramData.Colored);

	}

	public LinkedHashSet<Integer> getHighlighted() {
		return clusteringViewer.getHighlighted();
	}

	public Set<Integer> getFilteredIndexes() {
		return clusteringViewer.getFilteredIndexes();
	}

	private void adaptThreshhold(double newthreshhold) {

		if (newthreshhold < 0)
			newthreshhold = -Double.MIN_VALUE;
		if (newthreshhold > max)
			newthreshhold = max;
		if (newthreshhold == threshhold)
			return;
		threshhold = newthreshhold;
		calculateClusters(threshhold);
		clusteringViewer.updateMDSPlot(clusteringList);
		if (HistogramData.Colored.equals(clusteringViewer.getHistogramData())) {
			adaptHistogramData();
		}
		SwingUtilities.invokeLater(() -> repaint());
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		final double height = (getHeight()) * (1 - threshhold / max);
		g.drawLine(0, (int) height, getWidth(), (int) height);

	}

	private void calculateClusters(double threshhold) {
		final List<ClusteringWithDistance> clusterOrder = clusteringList;
		final int datalength = clusterOrder.size();

		final int[] clusterer = new int[clusterOrder.size()];
		int curindex = 0;
		clusterer[0] = 1;
		for (int i = 1; i < datalength; ++i) {
			if (clusterOrder.get(i).distance > threshhold) {
				clusterer[++curindex] = 0;
			}
			++clusterer[curindex];
		}
		tag(clusterOrder, clusterer);
		for (int i = 0; i < datalength; ++i) {
			bars.get(i).setColor(Util.getColor(clusterOrder.get(i).tag + 2));
		}
	}

	private void tag(List<ClusteringWithDistance> clusterOrder, int[] clusterer) {
		int noise = 0;
		int index = 0;
		final int length = clusterer.length;
		for (int i = 0; i < length; ++i) {
			if (clusterer[i] == 1) {
				noise++;
				clusterOrder.get(index++).tag = NOISE_TAG;
			} else {
				final int count = clusterer[i];
				for (int j = 0; j < count; ++j)
					clusterOrder.get(index++).tag = i - noise;
			}
		}
	}

	public void highlight(int selection, boolean replace, boolean singleClick) {
		final List<Integer> highlighted = new ArrayList<Integer>();
		highlighted.add(selection);
		clusteringViewer.mouseHighlight(selection, replace, singleClick);
	}

	public int getTruth() {
		return clusteringViewer.getGroundTruth();
	}

	public Double getDistanceToTruth(int i) {
		return clusteringViewer.getDistanceToTruth(i);
	}

	public Double getNMIToTruth(int i) {
		return clusteringViewer.getNMIToTruth(i);
	}

	private class OpticsBar extends JComponent {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private final static int INNER_SPACE = 2;
		private final static int BORDER_MIN_SIZE = 2;
		private final double heightPercent;
		private final int myid;
		private Color color = Color.blue;
		private final OpticsPlot plot;

		public OpticsBar(OpticsPlot plot, double heightPercent, int myid) {
			this.plot = plot;
			this.heightPercent = heightPercent;
			this.myid = myid;
			final Double dist = plot.getDistanceToTruth(myid);
			if (!dist.equals(Double.NaN)) {
				if (Math.abs(dist) < Double.MIN_NORMAL)
					setToolTipText("<html>" + "Equal to Ground Truth" + "</html>");
				else
					setToolTipText("<html>" + "Distance to Ground Truth: " + Float.toString((float) (double) dist)
							+ "<br> NMI: " + Float.toString((float) (double) plot.getNMIToTruth(myid)) + "</html>");
			}
		}

		@Override
		public void paint(Graphics g) {
			final Graphics2D g2 = (Graphics2D) g;
			final boolean highlighted = plot.getHighlighted().contains(myid);
			final Set<Integer> filtered = plot.getFilteredIndexes();
			final int truth = plot.getTruth();
			if (filtered != null && !filtered.contains(myid) && myid != truth)
				g2.setComposite(AlphaComposite.SrcOver.derive(Util.FILTER_ALPHA));
			else
				g2.setComposite(AlphaComposite.SrcOver);

			if (getWidth() - INNER_SPACE > BORDER_MIN_SIZE && !highlighted) {
				g2.setColor(color);
				g2.fillRect(INNER_SPACE / 2, (int) (getHeight() * (1 - heightPercent)) + 1, getWidth() - INNER_SPACE,
						(getHeight() - (int) (getHeight() * (1 - heightPercent))) + 1);
				g2.setColor(Color.BLACK);
				g2.drawRect(INNER_SPACE / 2, (int) Math.min((getHeight() * (1 - heightPercent)), getHeight() - 2),
						getWidth() - INNER_SPACE,
						(getHeight() - (int) Math.min((getHeight() * (1 - heightPercent)), getHeight() - 2)) - 1);
			} else {
				if (highlighted) {
					g2.setColor(Color.lightGray);
					g2.fillRect(0, 0, getWidth(), getHeight());
					g2.setColor(Util.HIGHLIGHT_COLOR);
					g2.fillRect(0, (int) (getHeight() * (1 - heightPercent) - 1), getWidth(),
							(getHeight() - (int) (getHeight() * (1 - heightPercent))) + 1);
				} else {
					g2.setColor(color);
					g2.fillRect(0, (int) (getHeight() * (1 - heightPercent) - 1), getWidth(),
							(getHeight() - (int) (getHeight() * (1 - heightPercent))) + 1);
				}
			}

		}

		public Color getColor() {
			return color;
		}

		public void setColor(Color color) {
			this.color = color;
		}

		public double getHeightPercent() {
			return heightPercent;
		}
	}

}
