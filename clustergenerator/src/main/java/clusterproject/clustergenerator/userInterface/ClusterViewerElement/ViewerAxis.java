package clusterproject.clustergenerator.userInterface.ClusterViewerElement;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

public class ViewerAxis extends JPanel{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final int LABLE_OFFSET=5;
	private final boolean isHorizontal;
	private double[] interval;
	private final String header;

	public ViewerAxis(boolean isHorizontal,double[] interval,String header) {
		this.isHorizontal=isHorizontal;
		this.interval=interval;
		this.header=header;
		setOpaque(false);
	}

	public double[] getInterval() {
		return interval;
	}

	public void setInterval(double[] interval) {
		this.interval = interval;
	}
	@Override
	public void paint(Graphics g) {

		super.paint(g);
		final Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		if(isHorizontal) {
			g2.drawLine(0, 0, getWidth(), 0);
			g2.drawString(Double.toString(interval[0]), LABLE_OFFSET, getHeight()-LABLE_OFFSET);
			final int endTickWidth=g.getFontMetrics().stringWidth(Double.toString(interval[1]));
			g2.drawString(Double.toString(interval[1]), getWidth()-LABLE_OFFSET-endTickWidth, getHeight()-LABLE_OFFSET);
			final int headerWidth=g.getFontMetrics().stringWidth(header);
			g2.drawString(header, getWidth()/2-headerWidth, getHeight()-LABLE_OFFSET);
		}
		else {
			g.drawLine(getWidth()-1, 0, getWidth()-1, getHeight());
			drawRotate(g2,LABLE_OFFSET, LABLE_OFFSET, 90, Double.toString(interval[1]));
			final int endTickWidth=g.getFontMetrics().stringWidth(Double.toString(interval[0]));
			drawRotate(g2,LABLE_OFFSET, getHeight()-LABLE_OFFSET-endTickWidth, 90, Double.toString(interval[0]));
			final int headerWidth=g.getFontMetrics().stringWidth(header);
			drawRotate(g2,LABLE_OFFSET, getHeight()/2-headerWidth, 90, header);
		}
	}

	private static void drawRotate(Graphics2D g2d, double x, double y, int angle, String text)
	{
		g2d.translate((float)x,(float)y);
		g2d.rotate(Math.toRadians(angle));
		g2d.drawString(text,0,0);
		g2d.rotate(-Math.toRadians(angle));
		g2d.translate(-(float)x,-(float)y);
	}

	public double getCoordinate(double pixel) {
		if(isHorizontal)
			return (pixel)/getWidth()*(interval[1]-interval[0]);
		else {
			return (1-(pixel)/getHeight())*(interval[1]-interval[0]);
		}
	}

	public double getPixel(double coordinate) {
		if(isHorizontal)
			return coordinate/(interval[1]-interval[0])*getWidth();
		else {
			return (-coordinate/(interval[1]-interval[0])+1)*getHeight();
		}
	}


}
