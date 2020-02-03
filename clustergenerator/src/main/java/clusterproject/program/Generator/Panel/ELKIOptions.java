package clusterproject.program.Generator.Panel;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import clusterproject.program.DataView;

public class ELKIOptions extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private final JTextArea inputStringField;
	private final JCheckBox addBox;
	private static final int DEFAULT_TEXT_HEIGHT = 400;
	private static final String DEFAULT_TEXT = "<dataset random-seed=\"5\">\r\n"
			+ "  <cluster name=\"Cluster1\" size=\"50\">\r\n" + "    <normal mean=\"0.1\" stddev=\"0.02\" />\r\n"
			+ "    <normal mean=\"0.1\" stddev=\"0.02\" />\r\n" + "  </cluster>\r\n"
			+ "  <cluster name=\"Cluster2\" size=\"50\">\r\n" + "    <normal mean=\"0.28\" stddev=\"0.08\" />\r\n"
			+ "    <normal mean=\"0.28\" stddev=\"0.08\" />\r\n" + "  </cluster>\r\n"
			+ "  <cluster name=\"Cluster3\" size=\"50\">\r\n" + "    <normal mean=\"0.65\" stddev=\"0.13\" />\r\n"
			+ "    <normal mean=\"0.65\" stddev=\"0.13\" />\r\n" + "  </cluster>\r\n" + "</dataset>";

	public ELKIOptions() {
		setVisible(false);
		setOpaque(false);
		final BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(layout);
		inputStringField = new JTextArea(DEFAULT_TEXT);

		final JScrollPane scroll = new JScrollPane(inputStringField, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setPreferredSize(new Dimension(DataView.OPTIONS_WIDTH, DEFAULT_TEXT_HEIGHT));

		add(scroll);
		// add(Box.createVerticalStrut(5));
		addBox = new JCheckBox("Add");
		add(addBox);

	}

	public String getTemplate() {
		return inputStringField.getText();
	}

	public boolean replacePoints() {
		return !addBox.isSelected();
	}

}
