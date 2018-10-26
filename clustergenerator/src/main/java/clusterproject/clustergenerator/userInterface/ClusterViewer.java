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
	private PointContainer pointContainer;
	private final SpringLayout layout;
	private final IClickHandler clickHandler;
	final ViewerAxis xAxis;
	final ViewerAxis yAxis;
	final PointCanvas canvas;

	public ClusterViewer(IClickHandler clickHandler) {
		this(clickHandler, null);
	}

	public ClusterViewer(IClickHandler handler, PointContainer pointContainer) {
		this.pointContainer = pointContainer;
		clickHandler = handler;
		final double[] xInterval = new double[2];
		final double[] yInterval = new double[2];
		xInterval[0] = 0;
		xInterval[1] = 100;
		yInterval[0] = 0;
		yInterval[1] = 100;
		layout = new SpringLayout();
		setLayout(layout);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				final double[] translation = getCoordinates(e.getPoint());
				clickHandler.handleClick(translation);
			}
		});
		pointContainer.addPoint(new double[] { 15, 10 });// XXX
		xAxis = new ViewerAxis(true, xInterval, this);
		yAxis = new ViewerAxis(false, yInterval, this);

		add(xAxis, new Integer(2));
		add(yAxis, new Integer(2));

		layout.putConstraint(SpringLayout.NORTH, yAxis, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, yAxis, AXIS_WIDTH, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.WEST, yAxis, 0, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.SOUTH, yAxis, -AXIS_WIDTH, SpringLayout.SOUTH, this);

		layout.putConstraint(SpringLayout.NORTH, xAxis, -AXIS_WIDTH, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, xAxis, 0, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, xAxis, AXIS_WIDTH, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.SOUTH, xAxis, 0, SpringLayout.SOUTH, this);

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
		position[pointContainer.getSelectedDimX()] = xAxis.getCoordinate(point.getX() - AXIS_WIDTH);
		position[pointContainer.getSelectedDimY()] = yAxis.getCoordinate(point.getY());
		return position;
	}

	public Point getPixel(double[] position) {
		return new Point((int) xAxis.getPixel(position[pointContainer.getSelectedDimX()]) + AXIS_WIDTH,
				(int) yAxis.getPixel(position[pointContainer.getSelectedDimY()]));
	}

	private void autoAdjust() {
		xAxis.setInterval(pointContainer.getMinMaxFrom(pointContainer.getSelectedDimX()));
		yAxis.setInterval(pointContainer.getMinMaxFrom(pointContainer.getSelectedDimY()));
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

}
