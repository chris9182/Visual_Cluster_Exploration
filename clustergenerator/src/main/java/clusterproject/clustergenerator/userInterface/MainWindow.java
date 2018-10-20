package clusterproject.clustergenerator.userInterface;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.SpringLayout;

import clusterproject.clustergenerator.data.PointContainer;
import clusterproject.clustergenerator.userInterface.Generator.IGenerator;
import clusterproject.clustergenerator.userInterface.Generator.SinglePointGenerator;

public class MainWindow extends JFrame implements IClickHandler{

	/**
	 *
	 */
	private static final int INNER_SPACE=4;
	public static final int OPTIONS_WIDTH=200;

	private final JLayeredPane mainFrame;
	private final SpringLayout mainLayout;
	private final List<IGenerator> generators;
	private IGenerator activeGenerator=null;
	final JComboBox<String> selector;
	private final PointContainer pointContainer;
	final ClusterViewer clusterViewer;
	private final int dim=2;

	private static final long serialVersionUID = 1L;
	public MainWindow() {
		pointContainer=new PointContainer(2);
		final List<String> headers=new ArrayList<String>();
		headers.add("y");
		headers.add("x");
		pointContainer.setHeaders(headers);
		mainFrame= new JLayeredPane();
		generators= new ArrayList<IGenerator>();
		add(mainFrame);
		mainLayout=new SpringLayout();
		mainFrame.setLayout(mainLayout);
		initGenerators();

		clusterViewer= new ClusterViewer(this,pointContainer);

		final List<String> names = new ArrayList<String>();
		for(final IGenerator generator:generators)
			names.add(generator.getName());
		selector = new JComboBox<String>(names.toArray(new String[names.size()]));

		mainLayout.putConstraint(SpringLayout.NORTH, selector, INNER_SPACE, SpringLayout.NORTH,mainFrame);
		mainLayout.putConstraint(SpringLayout.EAST, selector, -INNER_SPACE, SpringLayout.EAST,mainFrame);

		mainLayout.putConstraint(SpringLayout.NORTH, clusterViewer, INNER_SPACE, SpringLayout.NORTH,mainFrame);
		mainLayout.putConstraint(SpringLayout.EAST, clusterViewer, 2*-INNER_SPACE-OPTIONS_WIDTH, SpringLayout.EAST,mainFrame);
		mainLayout.putConstraint(SpringLayout.WEST, clusterViewer, INNER_SPACE, SpringLayout.WEST,mainFrame);
		mainLayout.putConstraint(SpringLayout.SOUTH, clusterViewer, -INNER_SPACE, SpringLayout.SOUTH,mainFrame);


		mainFrame.add(selector,new Integer(100));
		mainFrame.add(clusterViewer, new Integer(1));
		showOptionsPanel(generators.get(0).getName());
	}
	private void showOptionsPanel(String name) {
		IGenerator newGenerator=null;
		for(final IGenerator generator:generators)
			if(generator.getName().equals(name))
				newGenerator=generator;
		if(newGenerator==null)return;
		if(activeGenerator!=null)
			mainFrame.remove(activeGenerator.getPanel());
		activeGenerator=newGenerator;

		mainLayout.putConstraint(SpringLayout.NORTH, activeGenerator.getPanel(), INNER_SPACE, SpringLayout.SOUTH, selector);
		mainLayout.putConstraint(SpringLayout.EAST, activeGenerator.getPanel(), -INNER_SPACE, SpringLayout.EAST,mainFrame);
		mainLayout.putConstraint(SpringLayout.WEST, activeGenerator.getPanel(), -INNER_SPACE-OPTIONS_WIDTH, SpringLayout.EAST,mainFrame);

		mainFrame.add(activeGenerator.getPanel(), new Integer(1));
	}

	private void initGenerators() {
		final IGenerator generator1= new SinglePointGenerator();
		generators.add(generator1);

	}
	public void handleClick(double[] point) {
		if(activeGenerator.canClickGenerate()) {
			final boolean done=activeGenerator.generate(point,pointContainer);
			if(done)
				clusterViewer.update();
			else
			{
				//TODO:error
			}
		}
	}

}
