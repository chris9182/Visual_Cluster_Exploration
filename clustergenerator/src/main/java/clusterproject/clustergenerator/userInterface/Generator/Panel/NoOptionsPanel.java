package clusterproject.clustergenerator.userInterface.Generator.Panel;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;

import clusterproject.clustergenerator.userInterface.Generator.SinglePointGenerator;

public class NoOptionsPanel extends JPanel{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final SinglePointGenerator generator;
	public NoOptionsPanel(SinglePointGenerator generator) {
		this.generator=generator;
		add(new JLabel("no Options"));
		setBackground(Color.YELLOW);
	}

}
