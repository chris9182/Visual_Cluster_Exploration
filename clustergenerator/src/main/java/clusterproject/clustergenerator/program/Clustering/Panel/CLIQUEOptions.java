package clusterproject.clustergenerator.program.Clustering.Panel;

import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class CLIQUEOptions extends JPanel {
	private static final long serialVersionUID = -3850715094627642884L;

	private final JFormattedTextField lowerxsiField;
	private final JFormattedTextField stepxsiField;
	private final JFormattedTextField upperxsiField;

	private final JFormattedTextField lowertauField;
	private final JFormattedTextField steptauField;
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

		// step
		stepxsiField = new JFormattedTextField(integerFieldFormatter);
		stepxsiField.setValue(1);
		stepxsiField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, stepxsiField, INNER_PAD, SpringLayout.SOUTH, lowerxsiField);
		layout.putConstraint(SpringLayout.EAST, stepxsiField, 0, SpringLayout.EAST, this);
		add(stepxsiField);
		final JLabel sminplbl = new JLabel("step size:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, sminplbl, 0, SpringLayout.VERTICAL_CENTER, stepxsiField);
		layout.putConstraint(SpringLayout.WEST, sminplbl, 0, SpringLayout.WEST, this);
		add(sminplbl);

		// upperBound
		upperxsiField = new JFormattedTextField(integerFieldFormatter);
		upperxsiField.setValue(1);
		upperxsiField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, upperxsiField, INNER_PAD, SpringLayout.SOUTH, stepxsiField);
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

		// step
		steptauField = new JFormattedTextField(doubleFieldFormatter);
		steptauField.setValue(new Double(1.0));
		steptauField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, steptauField, INNER_PAD, SpringLayout.SOUTH, lowertauField);
		layout.putConstraint(SpringLayout.EAST, steptauField, 0, SpringLayout.EAST, this);
		add(steptauField);
		final JLabel staulbl = new JLabel("step size:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, staulbl, 0, SpringLayout.VERTICAL_CENTER, steptauField);
		layout.putConstraint(SpringLayout.WEST, staulbl, 0, SpringLayout.WEST, this);
		add(staulbl);

		// upperBound
		uppertauField = new JFormattedTextField(doubleFieldFormatter);
		uppertauField.setValue(new Double(1.0));
		uppertauField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, uppertauField, INNER_PAD, SpringLayout.SOUTH, steptauField);
		layout.putConstraint(SpringLayout.EAST, uppertauField, 0, SpringLayout.EAST, this);
		add(uppertauField);
		final JLabel utaulbl = new JLabel("upper bound:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, utaulbl, 0, SpringLayout.VERTICAL_CENTER, uppertauField);
		layout.putConstraint(SpringLayout.WEST, utaulbl, 0, SpringLayout.WEST, this);
		add(utaulbl);

	}

	public int getLBxsi() {
		return Integer.parseInt(lowerxsiField.getText());
	}

	public int getStepxsi() {
		return Integer.parseInt(stepxsiField.getText());
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

	public double getSteptau() {
		final NumberFormat format = NumberFormat.getInstance();
		Number number;
		try {
			number = format.parse(steptauField.getText());
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
