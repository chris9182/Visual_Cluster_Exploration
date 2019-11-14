package clusterproject.program.ClusterViewerElement;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import clusterproject.data.PointContainer;
import clusterproject.util.MinMax;
import clusterproject.util.Util;

public class ViewerAxis extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final int LABLE_OFFSET = 5;
	private final boolean isHorizontal;
	private MinMax interval;

	private final ScatterPlot scatterPlot;

	public ViewerAxis(boolean horizontal, final MinMax interval, ScatterPlot scatterPlot) {
		this.isHorizontal = horizontal;
		this.interval = interval;
		this.scatterPlot = scatterPlot;
		setOpaque(false);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				final Point point = e.getLocationOnScreen();
				if (isHorizontal) {
					if (e.getPoint().getX() < getWidth() / 4) {
						editBound(point, false);
					} else if (e.getPoint().getX() < getWidth() / 4 * 3) {
						changeDimension(point);
					} else {
						editBound(point, true);
					}
				} else {
					if (e.getPoint().getY() < getHeight() / 4) {
						editBound(point, true);
					} else if (e.getPoint().getY() < getHeight() / 4 * 3) {
						changeDimension(point);
					} else {
						editBound(point, false);
					}
				}
			}
		});
	}

	private void editBound(Point point, final boolean isMin) {
		final JFrame editFrame = new JFrame();
		editFrame.setUndecorated(true);
		final DecimalFormat df = new DecimalFormat("#.##########");
		final JFormattedTextField amountField = new JFormattedTextField(df);
		editFrame.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				final NumberFormat format = NumberFormat.getInstance();
				Number number;
				try {
					number = format.parse(amountField.getText());
					final double d = number.doubleValue();
					editAxis(isMin, d);
				} catch (final ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				editFrame.setVisible(false);
			}
		});
		amountField.setValue(new Double(interval.getByBoolean(isMin)));
		amountField.setColumns(10);
		amountField.addPropertyChangeListener("value", evt -> editFrame.setVisible(false));
		editFrame.add(amountField);
		editFrame.pack();
		editFrame.setLocation(point);
		editFrame.setVisible(true);
		editFrame.transferFocus();
	}

	private void editAxis(boolean isMin, double value) {
		final double newVal = value;
		if (newVal == interval.getByBoolean(isMin))
			return;
		interval.setByBoolean(isMin, newVal);
		SwingUtilities.invokeLater(() -> scatterPlot.repaint());

	}

	private void changeDimension(Point point) {
		final JFrame editFrame = new JFrame();
		editFrame.setUndecorated(true);
		final List<String> names = scatterPlot.getPointContainer().getHeaders();
		final JComboBox<String> selector = new JComboBox<String>(names.toArray(new String[names.size()]));
		selector.setSelectedItem(
				names.get(isHorizontal ? scatterPlot.getSelectedDimX() : scatterPlot.getSelectedDimY()));
		editFrame.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				changeAxis((String) selector.getSelectedItem());
				editFrame.setVisible(false);
			}
		});
		selector.addActionListener(e -> editFrame.setVisible(false));
		editFrame.add(selector);
		editFrame.pack();
		editFrame.setLocation(point);
		editFrame.setVisible(true);
		editFrame.transferFocus();

	}

	private void changeAxis(String string) {
		if (isHorizontal) {
			if (scatterPlot.getSelectedDimX() == scatterPlot.getPointContainer().getHeaders().indexOf(string))
				return;
			scatterPlot.setSelectedDimX(scatterPlot.getPointContainer().getHeaders().indexOf(string));
		} else {
			if (scatterPlot.getSelectedDimY() == scatterPlot.getPointContainer().getHeaders().indexOf(string))
				return;
			scatterPlot.setSelectedDimY(scatterPlot.getPointContainer().getHeaders().indexOf(string));
		}
		SwingUtilities.invokeLater(() -> {
			scatterPlot.autoAdjust();
			scatterPlot.update();
			repaint();
		});
	}

	public MinMax getInterval() {
		return interval;
	}

	public void setInterval(MinMax minMax) {
		this.interval = minMax;
	}

	@Override
	public void paint(Graphics g) {

		super.paint(g);
		final PointContainer container = scatterPlot.getPointContainer();
		final String header = container.getHeaders()
				.get(isHorizontal ? scatterPlot.getSelectedDimX() : scatterPlot.getSelectedDimY());
		final Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		final DecimalFormat df = new DecimalFormat("#.##");
		if (isHorizontal) {
			g2.drawLine(0, 0, getWidth(), 0);

			g2.drawString(df.format(interval.min), LABLE_OFFSET, getHeight() - LABLE_OFFSET);
			final int endTickWidth = g.getFontMetrics().stringWidth(df.format(interval.max));
			g2.drawString(df.format(interval.max), getWidth() - LABLE_OFFSET - endTickWidth,
					getHeight() - LABLE_OFFSET);
			final int headerWidth = g.getFontMetrics().stringWidth(header);
			g2.drawString(header, getWidth() / 2 - headerWidth, getHeight() - LABLE_OFFSET);
		} else {
			g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
			Util.drawRotatedString(g2, LABLE_OFFSET, LABLE_OFFSET, 90, df.format(interval.max));
			final int endTickWidth = g.getFontMetrics().stringWidth(df.format(interval.min));
			Util.drawRotatedString(g2, LABLE_OFFSET, getHeight() - LABLE_OFFSET - endTickWidth, 90,
					df.format(interval.min));
			final int headerWidth = g.getFontMetrics().stringWidth(header);
			Util.drawRotatedString(g2, LABLE_OFFSET, getHeight() / 2 - headerWidth, 90, header);
		}
	}

	public double getCoordinate(double pixel) {
		if (isHorizontal)
			return (pixel) / getWidth() * (interval.getRange()) + interval.min;
		else {
			return (1 - (pixel) / getHeight()) * (interval.getRange()) + interval.min;
		}
	}

	public double getPixel(double coordinate) {
		if (isHorizontal)
			return (coordinate - interval.min) / (interval.getRange()) * getWidth();
		else {
			return (-(coordinate - interval.min) / (interval.getRange()) + 1) * getHeight();
		}
	}

	// ONLY VALID FOR Y AXIS
	// consider refactoring into child classes
	public double getPixelY(double coordinate) {
		return (-(coordinate - interval.min) / (interval.getRange()) + 1) * getHeight();
	}

	// ONLY VALID FOR X AXIS
	// consider refactoring into child classes
	public double getPixelX(double coordinate) {
		return (coordinate - interval.min) / (interval.getRange()) * getWidth();
	}

}
