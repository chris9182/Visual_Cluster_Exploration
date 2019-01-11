package clusterproject.clustergenerator.program.Clustering.Panel;

import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class DiSHOptions extends JPanel {
	private static final long serialVersionUID = 1723051853199455897L;

	private final JFormattedTextField lowerMuField;
	private final JFormattedTextField upperMuField;

	private final JFormattedTextField lowerEpsField;
	private final JFormattedTextField upperEpsField;

	private final JFormattedTextField NField;

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

		// upperBound
		upperMuField = new JFormattedTextField(integerFieldFormatter);
		upperMuField.setValue(1);
		upperMuField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, upperMuField, INNER_PAD, SpringLayout.SOUTH, lowerMuField);
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

		// upperBound
		upperEpsField = new JFormattedTextField(doubleFieldFormatter);
		upperEpsField.setValue(new Double(1.0));
		upperEpsField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, upperEpsField, INNER_PAD, SpringLayout.SOUTH, lowerEpsField);
		layout.putConstraint(SpringLayout.EAST, upperEpsField, 0, SpringLayout.EAST, this);
		add(upperEpsField);
		final JLabel uepslbl = new JLabel("upper bound:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, uepslbl, 0, SpringLayout.VERTICAL_CENTER, upperEpsField);
		layout.putConstraint(SpringLayout.WEST, uepslbl, 0, SpringLayout.WEST, this);
		add(uepslbl);

		NField = new JFormattedTextField(integerFieldFormatter);
		NField.setValue(1);
		NField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, NField, 5 * INNER_PAD, SpringLayout.SOUTH, upperEpsField);
		layout.putConstraint(SpringLayout.EAST, NField, 0, SpringLayout.EAST, this);
		add(NField);
		final JLabel sampleslbl = new JLabel("Samples:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, sampleslbl, 0, SpringLayout.VERTICAL_CENTER, NField);
		layout.putConstraint(SpringLayout.WEST, sampleslbl, 0, SpringLayout.WEST, this);
		add(sampleslbl);

	}

	public int getLBMu() {
		return Integer.parseInt(lowerMuField.getText());
	}

	public int getNSamples() {
		return Integer.parseInt(NField.getText());
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
