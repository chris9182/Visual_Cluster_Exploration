package clusterproject.clustergenerator.program.Normalizers;

import javax.swing.JPanel;

import clusterproject.clustergenerator.data.PointContainer;

public interface INormalizer {

	JPanel getOptionsPanel();

	String getName();

	boolean normalize(PointContainer container);
}
