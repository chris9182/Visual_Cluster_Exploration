package clusterproject.clustergenerator.userInterface.Generator;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import clusterproject.clustergenerator.userInterface.Generator.Panel.NoOptionsPanel;

public class SinglePointGenerator implements IGenerator{

	NoOptionsPanel optionsPanel=new NoOptionsPanel();

	public JPanel getPanel() {
		return optionsPanel;
	}

	public String getName() {
		return "SinglePoint";
	}

	public boolean canSimpleGenerate() {
		return false;
	}

	public List<Double[]> generate() {
		return null;
	}



	public boolean canClickGenerate() {
		return true;
	}

	public List<Double[]> generate(Double[] point) {
		final List<Double[]> points=new ArrayList<Double[]>();
		points.add(point);
		return points;
	}

}
