package other.diclens;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import javax.swing.JPanel;

//from https://www.cs.umb.edu/~smimarog/diclens/
//modified
final class VennDiagram extends JPanel {
	private static final long serialVersionUID = 9036583102626592576L;
	private final int numberOfClusters;
	private static final int padding = 25;
	private static int fontSize;
	private final Font font;
	private final BasicStroke stroke;
	private int numOfRows;
	private boolean drawingAvailable;

	static {
		VennDiagram.fontSize = 18;
	}

	public VennDiagram(final int numberOfClusters) {
		this.drawingAvailable = true;
		this.numberOfClusters = numberOfClusters;
		this.font = new Font("Sans", 1, VennDiagram.fontSize);
		this.stroke = new BasicStroke(2.0f);
		if (numberOfClusters <= 10) {
			this.numOfRows = 1;
		} else if (numberOfClusters <= 20) {
			this.numOfRows = 2;
		} else {
			this.drawingAvailable = false;
		}
	}

	@Override
	public void paint(final Graphics g) {
		super.paint(g);
		final Dimension diagramSize = this.getParent().getSize();
		final Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(this.stroke);
		g2.setFont(this.font);
		if (this.drawingAvailable) {
			final int clusterPerRow = this.numberOfClusters / this.numOfRows;
			final int[] clusterForRow = new int[this.numOfRows];
			for (int i = 0; i < clusterForRow.length; ++i) {
				clusterForRow[i] = clusterPerRow;
			}
			clusterForRow[clusterForRow.length - 1] = this.numberOfClusters
					- (clusterForRow.length - 1) * clusterPerRow;
			for (int row = 0; row < this.numOfRows; ++row) {
				final int setSize = (int) ((diagramSize.width - 50.0) / clusterForRow[row]);
				final int ovalWidth = (int) (setSize * 0.8);
				final int ovalHeight = (int) (diagramSize.height * (0.6 / this.numOfRows));
				final int textCenterOffset = (ovalWidth + 4 - VennDiagram.fontSize) / 2;
				final int rowHeight = 25 + ovalHeight + VennDiagram.fontSize;
				final int lineTop = row * rowHeight + 25;
				final int ovalTop = lineTop + VennDiagram.fontSize;
				final int clusterIxBase = row * clusterForRow[0];
				for (int j = 0; j < clusterForRow[row]; ++j) {
					final int x_Position = 25 + j * setSize;
					final int clusterIx = clusterIxBase + j;
					g2.drawString(this.clusterLabelFor(clusterIx), x_Position + textCenterOffset, lineTop);
					g2.drawOval(x_Position, ovalTop, ovalWidth, ovalHeight);
					g2.drawString(DiclensGUIController.setContentIdentifier[clusterIx], x_Position + textCenterOffset,
							ovalTop + ovalHeight / 2);
				}
			}
		} else {
			g2.drawString("Venn diagram is not available: Too manyclusters!", 50, 50);
		}
	}

	private AttributedCharacterIterator clusterLabelFor(final int i) {
		final String label = "C*" + (i + 1);
		final AttributedString as = new AttributedString(label);
		as.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUPER, 1, 2);
		as.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUB, 2, label.length());
		as.addAttribute(TextAttribute.SIZE, VennDiagram.fontSize);
		return as.getIterator();
	}
}
