package clusterproject.clustergenerator.userInterface;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLayeredPane;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import clusterproject.clustergenerator.data.PointContainer;
import clusterproject.clustergenerator.userInterface.ClusterViewerElement.PointCanvas;
import clusterproject.clustergenerator.userInterface.ClusterViewerElement.ViewerAxis;

public class ClusterViewer extends JLayeredPane {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final int AXIS_WIDTH = 20;
	private static final int AXIS_PADDING = 20;
	private PointContainer pointContainer;
	private final SpringLayout layout;
	private final IClickHandler clickHandler;
	final ViewerAxis xAxis;
	final ViewerAxis yAxis;
	final PointCanvas canvas;
	private int selectedDimX = 1;
	private int selectedDimY = 0;

	public ClusterViewer(IClickHandler handler, PointContainer pointContainer, boolean showAxies) {
		this.pointContainer = pointContainer;
		clickHandler = handler;

		layout = new SpringLayout();
		setLayout(layout);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				final double[] translation = getCoordinates(e.getPoint());
				clickHandler.handleClick(translation);
			}
		});
		// pointContainer.addPoint(new double[] { 0, 0 });// XXX

		final double[] xInterval = new double[2];
		final double[] yInterval = new double[2];
		xInterval[0] = 0;
		xInterval[1] = 100;
		yInterval[0] = 0;
		yInterval[1] = 100;
		xAxis = new ViewerAxis(true, xInterval, this);
		yAxis = new ViewerAxis(false, yInterval, this);
		layout.putConstraint(SpringLayout.NORTH, yAxis, AXIS_PADDING, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, yAxis, AXIS_WIDTH, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.WEST, yAxis, 0, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.SOUTH, yAxis, -AXIS_WIDTH, SpringLayout.SOUTH, this);

		layout.putConstraint(SpringLayout.NORTH, xAxis, -AXIS_WIDTH, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, xAxis, -AXIS_PADDING, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, xAxis, AXIS_WIDTH, SpringLayout.WEST, this);
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

	public double[] getCoordinates(Point point) {
		final double[] position = new double[pointContainer.getDim()];
		for (int i = 0; i < position.length; ++i)
			position[i] = Double.NaN;// TODO: set NaN
		position[selectedDimX] = xAxis.getCoordinate(point.getX() - AXIS_WIDTH);
		position[selectedDimY] = yAxis.getCoordinate(point.getY() - AXIS_PADDING);
		// System.err.println(position[0] + " " + position[1]);
		return position;
	}

	public int[] getPixel(double[] position) {
		final double px = position[selectedDimX];
		final double py = position[selectedDimY];
		if (px == Double.NaN || py == Double.NaN)
			return null;
		return new int[] { (int) xAxis.getPixel(px) + AXIS_WIDTH, (int) yAxis.getPixel(py) + AXIS_PADDING };
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

		xAxis.setInterval(pointContainer.getMinMaxFrom(selectedDimX));
		yAxis.setInterval(pointContainer.getMinMaxFrom(selectedDimY));
	}

	public void update() {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				canvas.repaint();
			}
		});
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

}
