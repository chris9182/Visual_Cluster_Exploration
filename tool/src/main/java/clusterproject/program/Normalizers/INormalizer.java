package clusterproject.program.Normalizers;

import javax.swing.JPanel;

import clusterproject.data.PointContainer;

public interface INormalizer {

	JPanel getOptionsPanel();

	String getName();

	boolean normalize(PointContainer container);
}
