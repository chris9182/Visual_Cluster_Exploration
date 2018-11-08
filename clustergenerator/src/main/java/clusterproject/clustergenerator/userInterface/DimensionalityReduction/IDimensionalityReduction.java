package clusterproject.clustergenerator.userInterface.DimensionalityReduction;

import javax.swing.JPanel;

import clusterproject.clustergenerator.data.PointContainer;

public interface IDimensionalityReduction {
	JPanel getOptionsPanel();

	String getName();

	boolean reduce(PointContainer container);
}
