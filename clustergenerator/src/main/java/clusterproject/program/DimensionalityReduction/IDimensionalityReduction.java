package clusterproject.program.DimensionalityReduction;

import javax.swing.JPanel;

import clusterproject.data.PointContainer;

public interface IDimensionalityReduction {
	JPanel getOptionsPanel();

	String getName();

	boolean reduce(PointContainer container);
}
