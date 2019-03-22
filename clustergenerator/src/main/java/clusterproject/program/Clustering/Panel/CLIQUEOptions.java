package clusterproject.program.Clustering.Panel;

import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class CLIQUEOptions extends JPanel {
	private static final long serialVersionUID = -3850715094627642884L;

	private final JFormattedTextField lowerxsiField;
	private final JFormattedTextField NField;
	private final JFormattedTextField upperxsiField;

	private final JFormattedTextField lowertauField;
	private final JFormattedTextField uppertauField;

	private static final int INNER_PAD = 2;

	public CLIQUEOptions() {
		setOpaque(false);
		final SpringLayout layout = new SpringLayout();
		setLayout(layout);
		final NumberFormat integerFieldFormatter = NumberFormat.getIntegerInstance();
		integerFieldFormatter.setGroupingUsed(false);

		// xsi
		final JLabel xsiLbl = new JLabel("xsi");
		layout.putConstraint(SpringLayout.NORTH, xsiLbl, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST, xsiLbl, 0, SpringLayout.WEST, this);
		add(xsiLbl);

		// lowerBound
		lowerxsiField = new JFormattedTextField(integerFieldFormatter);
		lowerxsiField.setValue(1);
		lowerxsiField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, lowerxsiField, INNER_PAD, SpringLayout.SOUTH, xsiLbl);
		layout.putConstraint(SpringLayout.EAST, lowerxsiField, 0, SpringLayout.EAST, this);
		add(lowerxsiField);
		final JLabel lminplbl = new JLabel("lower bound:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, lminplbl, 0, SpringLayout.VERTICAL_CENTER, lowerxsiField);
		layout.putConstraint(SpringLayout.WEST, lminplbl, 0, SpringLayout.WEST, this);
		add(lminplbl);

		// upperBound
		upperxsiField = new JFormattedTextField(integerFieldFormatter);
		upperxsiField.setValue(1);
		upperxsiField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, upperxsiField, INNER_PAD, SpringLayout.SOUTH, lowerxsiField);
		layout.putConstraint(SpringLayout.EAST, upperxsiField, 0, SpringLayout.EAST, this);
		add(upperxsiField);
		final JLabel uminplbl = new JLabel("upper bound:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, uminplbl, 0, SpringLayout.VERTICAL_CENTER, upperxsiField);
		layout.putConstraint(SpringLayout.WEST, uminplbl, 0, SpringLayout.WEST, this);
		add(uminplbl);

		final NumberFormat doubleFieldFormatter = NumberFormat.getNumberInstance();
		// tau
		final JLabel tauLbl = new JLabel("tau");
		layout.putConstraint(SpringLayout.NORTH, tauLbl, 5 * INNER_PAD, SpringLayout.SOUTH, upperxsiField);
		layout.putConstraint(SpringLayout.WEST, tauLbl, 0, SpringLayout.WEST, this);
		add(tauLbl);

		// lowerBound
		lowertauField = new JFormattedTextField(doubleFieldFormatter);
		lowertauField.setValue(new Double(1.0));
		lowertauField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, lowertauField, INNER_PAD, SpringLayout.SOUTH, tauLbl);
		layout.putConstraint(SpringLayout.EAST, lowertauField, 0, SpringLayout.EAST, this);
		add(lowertauField);
		final JLabel ltaulbl = new JLabel("lower bound:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, ltaulbl, 0, SpringLayout.VERTICAL_CENTER, lowertauField);
		layout.putConstraint(SpringLayout.WEST, ltaulbl, 0, SpringLayout.WEST, this);
		add(ltaulbl);

		// upperBound
		uppertauField = new JFormattedTextField(doubleFieldFormatter);
		uppertauField.setValue(new Double(1.0));
		uppertauField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, uppertauField, INNER_PAD, SpringLayout.SOUTH, lowertauField);
		layout.putConstraint(SpringLayout.EAST, uppertauField, 0, SpringLayout.EAST, this);
		add(uppertauField);
		final JLabel utaulbl = new JLabel("upper bound:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, utaulbl, 0, SpringLayout.VERTICAL_CENTER, uppertauField);
		layout.putConstraint(SpringLayout.WEST, utaulbl, 0, SpringLayout.WEST, this);
		add(utaulbl);

		NField = new JFormattedTextField(integerFieldFormatter);
		NField.setValue(1);
		NField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, NField, 5 * INNER_PAD, SpringLayout.SOUTH, uppertauField);
		layout.putConstraint(SpringLayout.EAST, NField, 0, SpringLayout.EAST, this);
		add(NField);
		final JLabel sampleslbl = new JLabel("Samples:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, sampleslbl, 0, SpringLayout.VERTICAL_CENTER, NField);
		layout.putConstraint(SpringLayout.WEST, sampleslbl, 0, SpringLayout.WEST, this);
		add(sampleslbl);

	}

	public int getLBxsi() {
		return Integer.parseInt(lowerxsiField.getText());
	}

	public int getNSamples() {
		return Integer.parseInt(NField.getText());
	}

	public int getUBxsi() {
		return Integer.parseInt(upperxsiField.getText());
	}

	public double getLBtau() {
		final NumberFormat format = NumberFormat.getInstance();
		Number number;
		try {
			number = format.parse(lowertauField.getText());
		} catch (final ParseException e1) {
			e1.printStackTrace();
			return 0;
		}
		return number.doubleValue();
	}

	public double getUBtau() {
		final NumberFormat format = NumberFormat.getInstance();
		Number number;
		try {
			number = format.parse(uppertauField.getText());
		} catch (final ParseException e1) {
			e1.printStackTrace();
			return 0;
		}
		return number.doubleValue();
	}

}
