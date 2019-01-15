package clusterproject.clustergenerator.program.Normalizers;

import javax.swing.JPanel;

import clusterproject.clustergenerator.data.PointContainer;
import clusterproject.clustergenerator.program.Normalizers.Panel.NormalizeOptions;

public class Normalize implements INormalizer {

	NormalizeOptions options = new NormalizeOptions();

	@Override
	public JPanel getOptionsPanel() {
		// TODO Auto-generated method stub
		return options;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Normalize";
	}

	@Override
	public boolean normalize(PointContainer container) {
		// TODO Auto-generated method stub
		return false;
	}

}
