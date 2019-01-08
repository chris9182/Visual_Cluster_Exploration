package clusterproject.clustergenerator.program.DimensionalityReduction.Panel;

import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class PCAOptions extends JPanel {

	private static final long serialVersionUID = 1L;

	private final JFormattedTextField dimField;

	public PCAOptions() {
		setOpaque(false);
		final SpringLayout layout = new SpringLayout();
		setLayout(layout);
		final NumberFormat integerFieldFormatter = NumberFormat.getIntegerInstance();
		integerFieldFormatter.setGroupingUsed(false);
		dimField = new JFormattedTextField(integerFieldFormatter);
		dimField.setValue(2);
		dimField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, dimField, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, dimField, 0, SpringLayout.EAST, this);
		add(dimField);
		final JLabel dimLabel = new JLabel("Dimensions:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, dimLabel, 0, SpringLayout.VERTICAL_CENTER, dimField);
		layout.putConstraint(SpringLayout.WEST, dimLabel, 0, SpringLayout.WEST, this);
		add(dimLabel);

	}

	public int getDim() {
		return Integer.parseInt(dimField.getText());
	}
}
