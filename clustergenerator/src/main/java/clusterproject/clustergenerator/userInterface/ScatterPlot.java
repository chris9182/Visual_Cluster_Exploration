package clusterproject.clustergenerator.userInterface;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import clusterproject.clustergenerator.data.PointContainer;
import clusterproject.clustergenerator.userInterface.ClusterViewerElement.PointCanvas;
import clusterproject.clustergenerator.userInterface.ClusterViewerElement.ViewerAxis;

public class ScatterPlot extends JLayeredPane {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private int axisWidth = 20; // default
	private int axisPadding = 20; // default
	private static final int AXIS_WIDTH_NONE = 5;
	private static final int AXIS_PADDING_NONE = 5;
	private PointContainer pointContainer;
	private final SpringLayout layout;
	private final IClickHandler clickHandler;
	final ViewerAxis xAxis;
	final ViewerAxis yAxis;
	final PointCanvas canvas;
	private int selectedDimX = 1;
	private int selectedDimY = 0;
	private int pointDiameter = 6;

	public ScatterPlot(IClickHandler handler, PointContainer pointContainer, boolean showAxies) {
		this.pointContainer = pointContainer;
		clickHandler = handler;

		if (!showAxies) {
			axisWidth = AXIS_WIDTH_NONE;
			axisPadding = AXIS_PADDING_NONE;
		}

		layout = new SpringLayout();
		setLayout(layout);
		if (clickHandler != null)
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					final double[] translation = getCoordinates(e.getPoint());
					clickHandler.handleClick(translation);
				}
			});
		final double[] xInterval = new double[2];
		final double[] yInterval = new double[2];
		xInterval[0] = 0;
		xInterval[1] = 100;
		yInterval[0] = 0;
		yInterval[1] = 100;
		xAxis = new ViewerAxis(true, xInterval, this);
		yAxis = new ViewerAxis(false, yInterval, this);
		layout.putConstraint(SpringLayout.NORTH, yAxis, axisPadding, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, yAxis, axisWidth, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.WEST, yAxis, 0, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.SOUTH, yAxis, -axisWidth, SpringLayout.SOUTH, this);

		layout.putConstraint(SpringLayout.NORTH, xAxis, -axisWidth, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, xAxis, -axisPadding, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, xAxis, axisWidth, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.SOUTH, xAxis, 0, SpringLayout.SOUTH, this);

		add(xAxis, new Integer(2));
		add(yAxis, new Integer(2));
		if (!showAxies) {
			xAxis.setVisible(false);
			yAxis.setVisible(false);
		}

		canvas = new PointCanvas(pointContainer, this);
		add(canvas, new Integer(1));
		layout.putConstraint(SpringLayout.NORTH, canvas, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, canvas, 0, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, canvas, 0, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.SOUTH, canvas, 0, SpringLayout.SOUTH, this);
	}

	public void addAutoAdjust() {
		final JButton autoAdjust = new JButton("");
		autoAdjust.setToolTipText("Auto-Adjust Axies");
		autoAdjust.setPreferredSize(new Dimension(MainWindow.ADJUST_BUTTON_DIM, MainWindow.ADJUST_BUTTON_DIM));
		autoAdjust.addActionListener(e -> {
			autoAdjust();
			SwingUtilities.invokeLater(() -> repaint());

		});

		layout.putConstraint(SpringLayout.SOUTH, autoAdjust, 0, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.WEST, autoAdjust, 0, SpringLayout.WEST, this);

		add(autoAdjust, new Integer(100));
	}

	public double[] getCoordinates(Point point) {
		final double[] position = new double[pointContainer.getDim()];
		for (int i = 0; i < position.length; ++i)
			position[i] = Double.NaN;// TODO: set NaN
		position[selectedDimX] = xAxis.getCoordinate(point.getX() - axisWidth);
		position[selectedDimY] = yAxis.getCoordinate(point.getY() - axisPadding);
		// System.err.println(position[0] + " " + position[1]);
		return position;
	}

	public double[] getPixel(double[] position) {
		final double px = position[selectedDimX];
		final double py = position[selectedDimY];
		return new double[] { xAxis.getPixel(px) + axisWidth, yAxis.getPixel(py) + axisPadding };
	}

	public double getPixelY(double[] position) {
		final double py = position[selectedDimY];
		if (Double.isNaN(py))
			return py;
		return yAxis.getPixel(py) + axisPadding;
	}

	public double getPixelX(double[] position) {
		final double px = position[selectedDimX];
		if (Double.isNaN(px))
			return px;
		return xAxis.getPixel(px) + axisWidth;
	}

	public void autoAdjust() {
		if (pointContainer.getPoints().size() < 2) {
			final double[] xInterval = new double[2];
			final double[] yInterval = new double[2];
			xInterval[0] = 0;
			xInterval[1] = 100;
			yInterval[0] = 0;
			yInterval[1] = 100;
			xAxis.setInterval(xInterval);
			yAxis.setInterval(yInterval);
			return;
		}
		update(false);

		xAxis.setInterval(pointContainer.getMinMaxFrom(selectedDimX));
		yAxis.setInterval(pointContainer.getMinMaxFrom(selectedDimY));
	}

	public void setIntervalX(double[] interval) {
		xAxis.setInterval(interval);
	};

	public void setIntervalY(double[] interval) {
		yAxis.setInterval(interval);
	};

	public void update() {
		update(true);
	}

	public void update(boolean repaint) {
		if (pointContainer.getDim() <= selectedDimX || pointContainer.getDim() <= selectedDimY)
			if (pointContainer.getDim() > 1) {
				selectedDimX = 1;
				selectedDimY = 0;
			} else if (pointContainer.getDim() == 1) {
				selectedDimX = 0;
				selectedDimY = 0;
			} else {
				selectedDimX = -1;
				selectedDimY = -1;
			}
		if (repaint)
			SwingUtilities.invokeLater(() -> canvas.repaint());
	}

	public PointContainer getPointContainer() {
		return pointContainer;
	}

	public void setPointContainer(PointContainer pointContainer) {
		this.pointContainer = pointContainer;
	}

	public int getSelectedDimX() {
		return selectedDimX;
	}

	public int getSelectedDimY() {
		return selectedDimY;
	}

	public void setSelectedDimX(int selectedDimX) {
		this.selectedDimX = selectedDimX;

	}

	public void setSelectedDimY(int selectedDimY) {
		this.selectedDimY = selectedDimY;

	}

	public int getPointDiameter() {
		return pointDiameter;
	}

	public void setPointDiameter(int pointDiameter) {
		this.pointDiameter = pointDiameter;
	}

}
