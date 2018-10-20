package clusterproject.clustergenerator.userInterface.Generator;

import java.util.List;

import javax.swing.JPanel;

public interface IGenerator {
	JPanel getPanel();
	String getName();
	boolean canSimpleGenerate();
	List<Double[]> generate();
	boolean canClickGenerate();
	List<Double[]> generate(Double[] point);
}
