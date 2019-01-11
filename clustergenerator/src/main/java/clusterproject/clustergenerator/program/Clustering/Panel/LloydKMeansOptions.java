package clusterproject.clustergenerator.program.Clustering.Panel;

import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class LloydKMeansOptions extends JPanel {
	private static final long serialVersionUID = 559822521256476978L;

	private final JFormattedTextField lowerKField;
	private final JFormattedTextField upperKField;

	private static final int INNER_PAD = 2;

	public LloydKMeansOptions() {
		setOpaque(false);
		final SpringLayout layout = new SpringLayout();
		setLayout(layout);
		final NumberFormat integerFieldFormatter = NumberFormat.getIntegerInstance();
		integerFieldFormatter.setGroupingUsed(false);

		// K
		final JLabel kLbl = new JLabel("k");
		layout.putConstraint(SpringLayout.NORTH, kLbl, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST, kLbl, 0, SpringLayout.WEST, this);
		add(kLbl);

		// lowerBound
		lowerKField = new JFormattedTextField(integerFieldFormatter);
		lowerKField.setValue(1);
		lowerKField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, lowerKField, INNER_PAD, SpringLayout.SOUTH, kLbl);
		layout.putConstraint(SpringLayout.EAST, lowerKField, 0, SpringLayout.EAST, this);
		add(lowerKField);
		final JLabel lklbl = new JLabel("lower bound:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, lklbl, 0, SpringLayout.VERTICAL_CENTER, lowerKField);
		layout.putConstraint(SpringLayout.WEST, lklbl, 0, SpringLayout.WEST, this);
		add(lklbl);

		// upperBound
		upperKField = new JFormattedTextField(integerFieldFormatter);
		upperKField.setValue(1);
		upperKField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, upperKField, INNER_PAD, SpringLayout.SOUTH, lowerKField);
		layout.putConstraint(SpringLayout.EAST, upperKField, 0, SpringLayout.EAST, this);
		add(upperKField);
		final JLabel uklbl = new JLabel("upper bound:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, uklbl, 0, SpringLayout.VERTICAL_CENTER, upperKField);
		layout.putConstraint(SpringLayout.WEST, uklbl, 0, SpringLayout.WEST, this);
		add(uklbl);

	}

	public int getLBK() {
		return Integer.parseInt(lowerKField.getText());
	}

	public int getUBK() {
		return Integer.parseInt(upperKField.getText());
	}

}
