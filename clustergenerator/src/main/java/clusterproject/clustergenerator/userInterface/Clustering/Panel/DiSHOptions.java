package clusterproject.clustergenerator.userInterface.Clustering.Panel;

import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class DiSHOptions extends JPanel {
	private static final long serialVersionUID = 1723051853199455897L;

	private final JFormattedTextField lowerMuField;
	private final JFormattedTextField stepMuField;
	private final JFormattedTextField upperMuField;

	private final JFormattedTextField lowerEpsField;
	private final JFormattedTextField stepEpsField;
	private final JFormattedTextField upperEpsField;

	private static final int INNER_PAD = 2;

	public DiSHOptions() {
		setOpaque(false);
		final SpringLayout layout = new SpringLayout();
		setLayout(layout);
		final NumberFormat integerFieldFormatter = NumberFormat.getIntegerInstance();
		integerFieldFormatter.setGroupingUsed(false);

		// Mu
		final JLabel MuLbl = new JLabel("Mu");
		layout.putConstraint(SpringLayout.NORTH, MuLbl, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST, MuLbl, 0, SpringLayout.WEST, this);
		add(MuLbl);

		// lowerBound
		lowerMuField = new JFormattedTextField(integerFieldFormatter);
		lowerMuField.setValue(1);
		lowerMuField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, lowerMuField, INNER_PAD, SpringLayout.SOUTH, MuLbl);
		layout.putConstraint(SpringLayout.EAST, lowerMuField, 0, SpringLayout.EAST, this);
		add(lowerMuField);
		final JLabel lminplbl = new JLabel("lower bound:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, lminplbl, 0, SpringLayout.VERTICAL_CENTER, lowerMuField);
		layout.putConstraint(SpringLayout.WEST, lminplbl, 0, SpringLayout.WEST, this);
		add(lminplbl);

		// step
		stepMuField = new JFormattedTextField(integerFieldFormatter);
		stepMuField.setValue(1);
		stepMuField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, stepMuField, INNER_PAD, SpringLayout.SOUTH, lowerMuField);
		layout.putConstraint(SpringLayout.EAST, stepMuField, 0, SpringLayout.EAST, this);
		add(stepMuField);
		final JLabel sminplbl = new JLabel("step size:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, sminplbl, 0, SpringLayout.VERTICAL_CENTER, stepMuField);
		layout.putConstraint(SpringLayout.WEST, sminplbl, 0, SpringLayout.WEST, this);
		add(sminplbl);

		// upperBound
		upperMuField = new JFormattedTextField(integerFieldFormatter);
		upperMuField.setValue(1);
		upperMuField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, upperMuField, INNER_PAD, SpringLayout.SOUTH, stepMuField);
		layout.putConstraint(SpringLayout.EAST, upperMuField, 0, SpringLayout.EAST, this);
		add(upperMuField);
		final JLabel uminplbl = new JLabel("upper bound:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, uminplbl, 0, SpringLayout.VERTICAL_CENTER, upperMuField);
		layout.putConstraint(SpringLayout.WEST, uminplbl, 0, SpringLayout.WEST, this);
		add(uminplbl);

		final NumberFormat doubleFieldFormatter = NumberFormat.getNumberInstance();
		// EPS
		final JLabel epsLbl = new JLabel("epsilon");
		layout.putConstraint(SpringLayout.NORTH, epsLbl, 5 * INNER_PAD, SpringLayout.SOUTH, upperMuField);
		layout.putConstraint(SpringLayout.WEST, epsLbl, 0, SpringLayout.WEST, this);
		add(epsLbl);

		// lowerBound
		lowerEpsField = new JFormattedTextField(doubleFieldFormatter);
		lowerEpsField.setValue(new Double(1.0));
		lowerEpsField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, lowerEpsField, INNER_PAD, SpringLayout.SOUTH, epsLbl);
		layout.putConstraint(SpringLayout.EAST, lowerEpsField, 0, SpringLayout.EAST, this);
		add(lowerEpsField);
		final JLabel lepslbl = new JLabel("lower bound:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, lepslbl, 0, SpringLayout.VERTICAL_CENTER, lowerEpsField);
		layout.putConstraint(SpringLayout.WEST, lepslbl, 0, SpringLayout.WEST, this);
		add(lepslbl);

		// step
		stepEpsField = new JFormattedTextField(doubleFieldFormatter);
		stepEpsField.setValue(new Double(1.0));
		stepEpsField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, stepEpsField, INNER_PAD, SpringLayout.SOUTH, lowerEpsField);
		layout.putConstraint(SpringLayout.EAST, stepEpsField, 0, SpringLayout.EAST, this);
		add(stepEpsField);
		final JLabel sepslbl = new JLabel("step size:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, sepslbl, 0, SpringLayout.VERTICAL_CENTER, stepEpsField);
		layout.putConstraint(SpringLayout.WEST, sepslbl, 0, SpringLayout.WEST, this);
		add(sepslbl);

		// upperBound
		upperEpsField = new JFormattedTextField(doubleFieldFormatter);
		upperEpsField.setValue(new Double(1.0));
		upperEpsField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, upperEpsField, INNER_PAD, SpringLayout.SOUTH, stepEpsField);
		layout.putConstraint(SpringLayout.EAST, upperEpsField, 0, SpringLayout.EAST, this);
		add(upperEpsField);
		final JLabel uepslbl = new JLabel("upper bound:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, uepslbl, 0, SpringLayout.VERTICAL_CENTER, upperEpsField);
		layout.putConstraint(SpringLayout.WEST, uepslbl, 0, SpringLayout.WEST, this);
		add(uepslbl);

	}

	public int getLBMu() {
		return Integer.parseInt(lowerMuField.getText());
	}

	public int getStepMu() {
		return Integer.parseInt(stepMuField.getText());
	}

	public int getUBMu() {
		return Integer.parseInt(upperMuField.getText());
	}

	public double getLBEps() {
		final NumberFormat format = NumberFormat.getInstance();
		Number number;
		try {
			number = format.parse(lowerEpsField.getText());
		} catch (final ParseException e1) {
			e1.printStackTrace();
			return 0;
		}
		return number.doubleValue();
	}

	public double getStepEps() {
		final NumberFormat format = NumberFormat.getInstance();
		Number number;
		try {
			number = format.parse(stepEpsField.getText());
		} catch (final ParseException e1) {
			e1.printStackTrace();
			return 0;
		}
		return number.doubleValue();
	}

	public double getUBEps() {
		final NumberFormat format = NumberFormat.getInstance();
		Number number;
		try {
			number = format.parse(upperEpsField.getText());
		} catch (final ParseException e1) {
			e1.printStackTrace();
			return 0;
		}
		return number.doubleValue();
	}

}
