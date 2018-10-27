package clusterproject.clustergenerator.userInterface.Generator.Panel;

import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import clusterproject.clustergenerator.userInterface.MainWindow;

public class ELKIOptions extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private final JTextArea inputString;
	private static final int DEFAULT_TEXT_HEIGHT = 400;
	private static final String DEFAULT_TEXT = "<dataset random-seed=\"5\">\r\n"
			+ "  <cluster name=\"Cluster1\" size=\"50\">\r\n" + "    <normal mean=\"0.1\" stddev=\"0.02\" />\r\n"
			+ "    <normal mean=\"0.1\" stddev=\"0.02\" />\r\n" + "  </cluster>\r\n"
			+ "  <cluster name=\"Cluster2\" size=\"50\">\r\n" + "    <normal mean=\"0.28\" stddev=\"0.08\" />\r\n"
			+ "    <normal mean=\"0.28\" stddev=\"0.08\" />\r\n" + "  </cluster>\r\n"
			+ "  <cluster name=\"Cluster3\" size=\"50\">\r\n" + "    <normal mean=\"0.65\" stddev=\"0.13\" />\r\n"
			+ "    <normal mean=\"0.65\" stddev=\"0.13\" />\r\n" + "  </cluster>\r\n" + "</dataset>";

	public ELKIOptions() {
		setOpaque(false);
		inputString = new JTextArea(DEFAULT_TEXT);
		final JScrollPane scroll = new JScrollPane(inputString, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setPreferredSize(new Dimension(MainWindow.OPTIONS_WIDTH, DEFAULT_TEXT_HEIGHT));

		add(scroll);
	}

	public String getTemplate() {
		return inputString.getText();
	}

	public boolean replacePoints() {
		return true;
	}

}
