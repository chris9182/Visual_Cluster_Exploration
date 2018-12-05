package clusterproject.clustergenerator.userInterface;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import clusterproject.clustergenerator.Util;
import clusterproject.clustergenerator.userInterface.MetaClustering.ClusteringWithDistance;

public class OpticsPlot extends JLayeredPane implements IClickHandler {

	/**
	 *
	 */
	private final static int BAR_OFFSET = 20;
	private static final int LABLE_OFFSET = 5;

	private static final long serialVersionUID = 1L;
	private final List<ClusteringWithDistance> clusteringList;
	private final JPanel opticsBars;
	private final SpringLayout layout;
	private final List<OpticsBar> bars;
	private double threshhold = 0.5;
	private double max = 0;

	private final ClusteringViewer clusteringViewer;
	private final int NOISE_TAG = -2;

	public OpticsPlot(ClusteringViewer clusteringViewer, List<ClusteringWithDistance> clusteringList) {
		this.clusteringViewer = clusteringViewer;
		this.clusteringList = clusteringList;
		bars = new ArrayList<OpticsBar>(clusteringList.size());
		setOpaque(false);
		opticsBars = new JPanel();
		opticsBars.setOpaque(false);
		final GridLayout gridLayout = new GridLayout(0, clusteringList.size());
		opticsBars.setLayout(gridLayout);
		opticsBars.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.gray));
		layout = new SpringLayout();
		setLayout(layout);
		layout.putConstraint(SpringLayout.NORTH, opticsBars, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, opticsBars, 0, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.SOUTH, opticsBars, 0, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.WEST, opticsBars, BAR_OFFSET, SpringLayout.WEST, this);

		for (int i = 1; i < clusteringList.size(); ++i) {
			if (clusteringList.get(i).distance > max)
				max = clusteringList.get(i).distance;
		}
		max *= 1.1;

		for (int i = 0; i < clusteringList.size(); ++i) {
			final int selection = clusteringList.get(i).inIndex;
			final OpticsBar bar = new OpticsBar(this, Math.min(clusteringList.get(i).distance / max, 1),
					clusteringList.get(i).inIndex);// XXX
			// distances!=0?
			bar.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					highlight(selection);

				}
			});
			bars.add(bar);
			opticsBars.add(bar);
		}
		add(opticsBars, new Integer(20));
		final JPanel threshholdClicker = new JPanel() {
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				final Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				final DecimalFormat df = new DecimalFormat("#.##");
				g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
				Util.drawRotate(g2, LABLE_OFFSET, LABLE_OFFSET, 90, df.format(max));
				final int endTickWidth = g.getFontMetrics().stringWidth(df.format(0));
				Util.drawRotate(g2, LABLE_OFFSET, getHeight() - LABLE_OFFSET - endTickWidth, 90, df.format(0));
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

	public int getHighlighted() {
		return clusteringViewer.getHighlighted();
	}

	void adaptThreshhold(double newthreshhold) {

		if (newthreshhold < 0)
			newthreshhold = -0.0000000001;// TODO: nicer way for "epsilon"
		if (newthreshhold > max)
			newthreshhold = max;
		if (newthreshhold == threshhold)
			return;
		threshhold = newthreshhold;
		calculateClusters(threshhold);
		clusteringViewer.updateMDSPlot(clusteringList);
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

	public void highlight(int selection) {
		clusteringViewer.highlight(selection);
	}

	@Override
	public void handleClick(double[] point) {
		final int closest = clusteringViewer.getClosestPoint(point);
		if (closest != -1)
			highlight(closest);

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
		}

		@Override
		public void paint(Graphics g) {

			super.paint(g);
			final boolean highlighted = myid == plot.getHighlighted();
			if (getWidth() - INNER_SPACE > BORDER_MIN_SIZE && !highlighted) {
				g.setColor(color);
				g.fillRect(INNER_SPACE / 2, (int) (getHeight() * (1 - heightPercent)) + 1, getWidth() - INNER_SPACE,
						(getHeight() - (int) (getHeight() * (1 - heightPercent))) + 1);
				g.setColor(Color.black);
				g.drawRect(INNER_SPACE / 2, (int) Math.min((getHeight() * (1 - heightPercent)), getHeight() - 2),
						getWidth() - INNER_SPACE,
						(getHeight() - (int) Math.min((getHeight() * (1 - heightPercent)), getHeight() - 2)) - 1);
			} else {
				if (highlighted) {
					g.setColor(Color.lightGray);
					g.fillRect(0, 0, getWidth(), getHeight());
					g.setColor(Color.ORANGE);
					g.fillRect(0, (int) (getHeight() * (1 - heightPercent)), getWidth(),
							(getHeight() - (int) (getHeight() * (1 - heightPercent))) + 1);
				} else {
					g.setColor(color);
					g.fillRect(0, (int) (getHeight() * (1 - heightPercent)), getWidth(),
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
