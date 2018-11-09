package clusterproject.clustergenerator.userInterface.DimensionalityReduction;

import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JPanel;

public class PCAOptions extends JPanel {

	private static final long serialVersionUID = 1L;

	private final JFormattedTextField amountField;

	public PCAOptions() {
		setOpaque(false);
		final NumberFormat integerFieldFormatter = NumberFormat.getIntegerInstance();
		integerFieldFormatter.setGroupingUsed(false);
		amountField = new JFormattedTextField(integerFieldFormatter);
		amountField.setValue(2);
		amountField.setColumns(5);
		add(amountField);
	}

	public int getDim() {
		return Integer.parseInt(amountField.getText());
	}
}
