package clusterproject.clustergenerator.userInterface.Generator;

import javax.swing.JPanel;

import clusterproject.clustergenerator.data.PointContainer;
import clusterproject.clustergenerator.userInterface.Generator.Panel.SinglePointOptions;

public class SinglePointGenerator implements IGenerator {

	private final SinglePointOptions optionsPanel = new SinglePointOptions();

	@Override
	public JPanel getOptionsPanel() {
		return optionsPanel;
	}

	@Override
	public String getName() {
		return "SinglePoint";
	}

	@Override
	public boolean canSimpleGenerate() {
		return false;
	}

	@Override
	public boolean generate(PointContainer container) {
		return false;
	}

	@Override
	public boolean canClickGenerate() {
		return true;
	}

	@Override
	public boolean generate(double[] point, PointContainer container) {
		final int dim = container.getDim();
		int i;
		for (i = 0; i < point.length && i < dim; ++i)
			if (point[i] == Double.NaN)
				point[i] = 0;// TODO: handle NaN
		container.addPoint(point);
		return true;
	}

}
