package clusterproject.program.Generator;

import javax.swing.JPanel;

import clusterproject.data.PointContainer;

public interface IGenerator {
	JPanel getOptionsPanel();

	String getName();

	boolean canSimpleGenerate();

	boolean generate(PointContainer container);

	boolean canClickGenerate();

	boolean generate(double[] point, PointContainer container);
}
