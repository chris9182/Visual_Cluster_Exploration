package clusterproject.clustergenerator.userInterface;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.SpringLayout;

import clusterproject.clustergenerator.data.PointContainer;

public class ClusterViewer extends JPanel{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final PointContainer pointContainer;
	private double[] xInterval= new double[2];
	private double[] yInterval= new double[2];
	private final int selectedDimX=0;
	private final int selectedDimY=1;
	private final SpringLayout layout;
	private final IClickHandler clickHandler;

	public ClusterViewer(IClickHandler clickHandler) {
		this(clickHandler,null);
	}

	public ClusterViewer(IClickHandler handler,PointContainer pointContainer) {
		this.pointContainer=pointContainer;
		clickHandler=handler;
		xInterval[0]=0;
		xInterval[1]=0;
		yInterval[0]=0;
		yInterval[1]=0;
		layout=new SpringLayout();
		setLayout(layout);
		setBackground(Color.red);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				final Double[] translation=translatePosition(e.getPoint());
				clickHandler.handleClick(translation);
				super.mouseClicked(e);
			}
		});

	}

	protected Double[] translatePosition(Point point) {
		// TODO
		final Double[] position = new Double[2];
		position[0]=(double) 1;
		position[1]=(double) 1;
		return position;
	}

	private void autoAdjust() {
		xInterval=pointContainer.getMinMaxFrom(selectedDimX);
		yInterval=pointContainer.getMinMaxFrom(selectedDimY);
	}

	public void update() {
		//TODO:
	}

}
