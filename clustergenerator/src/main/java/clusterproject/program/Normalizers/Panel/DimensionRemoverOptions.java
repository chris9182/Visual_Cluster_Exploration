package clusterproject.program.Normalizers.Panel;

import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class DimensionRemoverOptions extends JPanel {
	private static final long serialVersionUID = 1L;
	private final JFormattedTextField dimensionField;

	public DimensionRemoverOptions() {

		setVisible(false);
		setOpaque(false);
		final SpringLayout layout = new SpringLayout();
		setLayout(layout);
		final JLabel dimLbl = new JLabel("Dimension to remove:");
		layout.putConstraint(SpringLayout.NORTH, dimLbl, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST, dimLbl, 0, SpringLayout.WEST, this);
		add(dimLbl);

		final NumberFormat integerFieldFormatter = NumberFormat.getIntegerInstance();
		integerFieldFormatter.setGroupingUsed(false);
		dimensionField = new JFormattedTextField(integerFieldFormatter);
		dimensionField.setValue(0);
		dimensionField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, dimensionField, 0, SpringLayout.SOUTH, dimLbl);
		layout.putConstraint(SpringLayout.WEST, dimensionField, 0, SpringLayout.WEST, dimLbl);
		add(dimensionField);
	}

	public int getDim() {
		return Integer.parseInt(dimensionField.getText());
	}

}
