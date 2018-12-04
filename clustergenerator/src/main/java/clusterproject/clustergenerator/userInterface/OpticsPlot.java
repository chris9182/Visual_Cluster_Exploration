package clusterproject.clustergenerator.userInterface;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import clusterproject.clustergenerator.userInterface.MetaClustering.ClusteringWithDistance;

public class OpticsPlot extends JLayeredPane {

	/**
	 *
	 */
	private final static int BAR_OFFSET = 20;

	private static final long serialVersionUID = 1L;
	private final List<ClusteringWithDistance> clusteringList;
	private final JPanel opticsBars;
	private final SpringLayout layout;

	public OpticsPlot(ClusteringViewer clusteringViewer, List<ClusteringWithDistance> clusteringList) {
		this.clusteringList = clusteringList;
		setOpaque(false);
		opticsBars = new JPanel();
		opticsBars.setOpaque(false);
		final GridLayout gridLayout = new GridLayout(0, clusteringList.size());
		opticsBars.setLayout(gridLayout);
		opticsBars.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.gray));
		layout = new SpringLayout();
		setLayout(layout);
		layout.putConstraint(SpringLayout.NORTH, opticsBars, BAR_OFFSET, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, opticsBars, -BAR_OFFSET, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.SOUTH, opticsBars, -BAR_OFFSET, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.WEST, opticsBars, BAR_OFFSET, SpringLayout.WEST, this);

		for (int i = 0; i < clusteringList.size(); ++i) {
			final int selection = clusteringList.get(i).inIndex;
			final OpticsBar bar = new OpticsBar(Math.min(clusteringList.get(i).distance, 1));// XXX distances!=0?
			bar.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					clusteringViewer.showViewer(selection);
				}
			});
			opticsBars.add(bar);
		}
		add(opticsBars, new Integer(20));
	}

	private class OpticsBar extends JComponent {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private final static int INNER_SPACE = 2;
		private final static int BORDER_MIN_SIZE = 2;
		double heightPercent;

		public OpticsBar(double heightPercent) {
			this.heightPercent = Math.sqrt(heightPercent);
			setBackground(Color.green);
		}

		@Override
		public void paint(Graphics g) {

			super.paint(g);
			if (getWidth() - INNER_SPACE > BORDER_MIN_SIZE) {

				g.setColor(Color.blue);
				g.fillRect(INNER_SPACE / 2, (int) (getHeight() * (1 - heightPercent)) + 1, getWidth() - INNER_SPACE,
						(getHeight() - (int) (getHeight() * (1 - heightPercent))) + 1);
				g.setColor(Color.black);
				g.drawRect(INNER_SPACE / 2, (int) Math.min((getHeight() * (1 - heightPercent)), getHeight() - 2),
						getWidth() - INNER_SPACE,
						(getHeight() - (int) Math.min((getHeight() * (1 - heightPercent)), getHeight() - 2)) - 1);

			} else {
				g.setColor(Color.blue);
				g.fillRect(0, (int) (getHeight() * (1 - heightPercent)), getWidth(),
						(getHeight() - (int) (getHeight() * (1 - heightPercent))) + 1);
			}

		}
	}

}
