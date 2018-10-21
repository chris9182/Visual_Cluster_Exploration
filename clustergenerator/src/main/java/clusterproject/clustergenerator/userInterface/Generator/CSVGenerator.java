package clusterproject.clustergenerator.userInterface.Generator;

import javax.swing.JPanel;

import clusterproject.clustergenerator.data.PointContainer;
import clusterproject.clustergenerator.userInterface.Generator.Panel.CSVOptions;

public class CSVGenerator implements IGenerator{

	private final CSVOptions optionsPanel=new CSVOptions();

	public JPanel getPanel() {
		return optionsPanel;
	}

	public String getName() {
		return "CSVGenerator";
	}

	public boolean canSimpleGenerate() {
		return true;
	}

	public boolean generate(PointContainer container) {
		return false;
	}

	public boolean canClickGenerate() {
		return false;
	}

	public boolean generate(double[] point,PointContainer container) {
		return false;
	}

}
