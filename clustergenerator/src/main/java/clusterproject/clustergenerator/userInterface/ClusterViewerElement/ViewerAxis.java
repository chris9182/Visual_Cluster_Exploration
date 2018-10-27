package clusterproject.clustergenerator.userInterface.ClusterViewerElement;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import clusterproject.clustergenerator.data.PointContainer;
import clusterproject.clustergenerator.userInterface.ClusterViewer;

public class ViewerAxis extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final int LABLE_OFFSET = 5;
	private final boolean isHorizontal;
	private final double[] interval;

	private final ClusterViewer clusterViewer;

	public ViewerAxis(boolean horizontal, final double[] defaultInterval, ClusterViewer clusterViewer) {
		this.isHorizontal = horizontal;
		this.interval = defaultInterval;
		this.clusterViewer = clusterViewer;
		setOpaque(false);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				final Point point = e.getLocationOnScreen();
				if (isHorizontal) {
					if (e.getPoint().getX() < getWidth() / 4) {
						editBound(point, 0);
					} else if (e.getPoint().getX() < getWidth() / 4 * 3) {
						changeDimension(point);
					} else {
						editBound(point, 1);
					}
				} else {
					if (e.getPoint().getY() < getHeight() / 4) {
						editBound(point, 1);
					} else if (e.getPoint().getY() < getHeight() / 4 * 3) {
						changeDimension(point);
					} else {
						editBound(point, 0);
					}
				}
			}
		});
	}

	private void editBound(Point point, final int boundary) {
		final JFrame editFrame = new JFrame();
		editFrame.setUndecorated(true);
		final JFormattedTextField amountField = new JFormattedTextField(NumberFormat.getNumberInstance());
		editFrame.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				final NumberFormat format = NumberFormat.getInstance();
				Number number;
				try {
					number = format.parse(amountField.getText());
					final double d = number.doubleValue();
					editAxis(boundary, d);
				} catch (final ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				editFrame.setVisible(false);
			}
		});
		amountField.setValue(new Double(interval[boundary]));
		amountField.setColumns(10);
		amountField.addPropertyChangeListener("value", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				editFrame.setVisible(false);
			}
		});
		editFrame.add(amountField);
		editFrame.pack();
		editFrame.setLocation(point);
		editFrame.setVisible(true);
		editFrame.transferFocus();
	}

	private void editAxis(int boundary, double value) {
		final double newVal = value;
		if (newVal == interval[boundary])
			return;
		interval[boundary] = newVal;
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				clusterViewer.repaint();
			}
		});

	}

	private void changeDimension(Point point) {
		final JFrame editFrame = new JFrame();
		editFrame.setUndecorated(true);
		final List<String> names = clusterViewer.getPointContainer().getHeaders();
		final JComboBox selector = new JComboBox<String>(names.toArray(new String[names.size()]));
		selector.setSelectedItem(names.get(isHorizontal ? clusterViewer.getPointContainer().getSelectedDimX()
				: clusterViewer.getPointContainer().getSelectedDimY()));
		editFrame.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				changeAxis((String) selector.getSelectedItem());
				editFrame.setVisible(false);
			}
		});
		selector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				editFrame.setVisible(false);
			}
		});
		editFrame.add(selector);
		editFrame.pack();
		editFrame.setLocation(point);
		editFrame.setVisible(true);
		editFrame.transferFocus();

	}

	private void changeAxis(String string) {
		if (isHorizontal) {
			clusterViewer.getPointContainer()
					.setSelectedDimX(clusterViewer.getPointContainer().getHeaders().indexOf(string));
		} else {
			clusterViewer.getPointContainer()
					.setSelectedDimY(clusterViewer.getPointContainer().getHeaders().indexOf(string));
		}
		// TODO: maybe change interval?
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				clusterViewer.repaint();
			}
		});
	}

	public double[] getInterval() {
		return interval;
	}

	public void setInterval(double[] interval) {
		this.interval[0] = interval[0];
		this.interval[1] = interval[1];
	}

	@Override
	public void paint(Graphics g) {

		super.paint(g);
		final PointContainer container = clusterViewer.getPointContainer();
		final String header = container.getHeaders()
				.get(isHorizontal ? container.getSelectedDimX() : container.getSelectedDimY());
		final Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		if (isHorizontal) {
			g2.drawLine(0, 0, getWidth(), 0);

			g2.drawString(Double.toString(interval[0]), LABLE_OFFSET, getHeight() - LABLE_OFFSET);
			final int endTickWidth = g.getFontMetrics().stringWidth(Double.toString(interval[1]));
			g2.drawString(Double.toString(interval[1]), getWidth() - LABLE_OFFSET - endTickWidth,
					getHeight() - LABLE_OFFSET);
			final int headerWidth = g.getFontMetrics().stringWidth(header);
			g2.drawString(header, getWidth() / 2 - headerWidth, getHeight() - LABLE_OFFSET);
		} else {
			g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
			drawRotate(g2, LABLE_OFFSET, LABLE_OFFSET, 90, Double.toString(interval[1]));
			final int endTickWidth = g.getFontMetrics().stringWidth(Double.toString(interval[0]));
			drawRotate(g2, LABLE_OFFSET, getHeight() - LABLE_OFFSET - endTickWidth, 90, Double.toString(interval[0]));
			final int headerWidth = g.getFontMetrics().stringWidth(header);
			drawRotate(g2, LABLE_OFFSET, getHeight() / 2 - headerWidth, 90, header);
		}
	}

	private static void drawRotate(Graphics2D g2d, double x, double y, int angle, String text) {
		g2d.translate((float) x, (float) y);
		g2d.rotate(Math.toRadians(angle));
		g2d.drawString(text, 0, 0);
		g2d.rotate(-Math.toRadians(angle));
		g2d.translate(-(float) x, -(float) y);
	}

	public double getCoordinate(double pixel) {
		if (isHorizontal)
			return (pixel) / getWidth() * (interval[1] - interval[0]) + interval[0];
		else {
			return (1 - (pixel) / getHeight()) * (interval[1] - interval[0]) + interval[0];
		}
	}

	public double getPixel(double coordinate) {
		if (isHorizontal)
			return (coordinate - interval[0]) / (interval[1] - interval[0]) * getWidth();
		else {
			return (-(coordinate - interval[0]) / (interval[1] - interval[0]) + 1) * getHeight();
		}
	}

}
