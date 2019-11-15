package clusterproject.program.ClusterViewerElement;

import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import clusterproject.data.PointContainer;
import clusterproject.program.MainWindow;
import clusterproject.util.MinMax;

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
	final ViewerAxis xAxis;
	final ViewerAxis yAxis;
	final PointCanvas canvas;
	private int selectedDimX = 1;
	private int selectedDimY = 0;
	private int pointDiameter = 6;

	public ScatterPlot(PointContainer pointContainer, boolean showAxies) {
		this.pointContainer = pointContainer;
		if (!showAxies) {
			axisWidth = AXIS_WIDTH_NONE;
			axisPadding = AXIS_PADDING_NONE;
		}

		layout = new SpringLayout();
		setLayout(layout);

		final MinMax xInterval = new MinMax(0, 100);
		final MinMax yInterval = new MinMax(0, 100);
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

	public void addAutoColor() {
		final JButton autoColor = new JButton("C");
		autoColor.setToolTipText("Base Colors");
		// autoColor.setPreferredSize(new Dimension(MainWindow.ADJUST_BUTTON_DIM,
		// MainWindow.ADJUST_BUTTON_DIM));
		autoColor.addActionListener(e -> {
			if (pointContainer.hasClusters()) {
				pointContainer.getClusterInformation()
						.setClusterIDs(pointContainer.getClusterInformation().getOriginalClusterIDs());
				pointContainer.getClusterInformation().setIDMap(null);
				// TODO: maybe something with cluster size for color selection?
			}
			SwingUtilities.invokeLater(() -> repaint());

		});

		layout.putConstraint(SpringLayout.NORTH, autoColor, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, autoColor, 0, SpringLayout.EAST, this);
		add(autoColor, new Integer(100));

	}

	public double[] getCoordinates(Point point) {
		final double[] position = new double[pointContainer.getDim()];
		for (int i = 0; i < position.length; ++i)
			position[i] = Double.NaN;
		position[selectedDimX] = xAxis.getCoordinate(point.getX() - axisWidth);
		position[selectedDimY] = yAxis.getCoordinate(point.getY() - axisPadding);
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
		return yAxis.getPixelY(py) + axisPadding;
	}

	public double getPixelX(double[] position) {
		final double px = position[selectedDimX];
		if (Double.isNaN(px))
			return px;
		return xAxis.getPixelX(px) + axisWidth;
	}

	public void autoAdjust() {
		if (pointContainer.getPoints().size() < 2) {
			final MinMax xInterval = new MinMax(0, 100);
			final MinMax yInterval = new MinMax(0, 100);
			xAxis.setInterval(xInterval);
			yAxis.setInterval(yInterval);
			return;
		}
		update(false);

		xAxis.setInterval(pointContainer.getMinMaxFrom(selectedDimX));
		yAxis.setInterval(pointContainer.getMinMaxFrom(selectedDimY));
	}

	public void setIntervalX(MinMax interval) {
		xAxis.setInterval(interval);
	}

	public void setIntervalY(MinMax interval) {
		yAxis.setInterval(interval);
	}

	public MinMax getIntervalX() {
		return xAxis.getInterval();
	}

	public MinMax getIntervalY() {
		return yAxis.getInterval();
	}

	public void update() {
		update(true);
	}

	public void update(boolean repaint) {
		if (pointContainer.getDim() <= selectedDimX || pointContainer.getDim() <= selectedDimY //
				|| selectedDimX < 0 || selectedDimY < 0)
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

	public void setSelection(Point down, Point current) {
		canvas.setSelection(down, current);

	}

	public PointCanvas getCanvas() {
		return canvas;
	}
}
