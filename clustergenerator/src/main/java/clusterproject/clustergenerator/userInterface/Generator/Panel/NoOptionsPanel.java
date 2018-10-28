package clusterproject.clustergenerator.userInterface.Generator.Panel;

import javax.swing.JLabel;
import javax.swing.JPanel;

import clusterproject.clustergenerator.userInterface.Generator.IGenerator;

public class NoOptionsPanel extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final IGenerator generator;

	public NoOptionsPanel(IGenerator generator) {
		setVisible(false);
		setOpaque(false);
		this.generator = generator;
		add(new JLabel("no Options"));
	}

}
