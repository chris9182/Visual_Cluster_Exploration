package clusterproject.clustergenerator.userInterface.Generator;

import javax.swing.JPanel;

import clusterproject.clustergenerator.data.PointContainer;

public interface IGenerator {
	JPanel getOptionsPanel();

	String getName();

	boolean canSimpleGenerate();

	boolean generate(PointContainer container);

	boolean canClickGenerate();

	boolean generate(double[] point, PointContainer container);
}
