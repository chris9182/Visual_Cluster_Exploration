package clusterproject.clustergenerator.userInterface.Clustering.Panel;

import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class DBScanOptions extends JPanel {
	private static final long serialVersionUID = 559822521256476978L;

	private final JFormattedTextField lowerMinPTSField;
	private final JFormattedTextField stepMinPTSField;
	private final JFormattedTextField upperMinPTSField;

	private final JFormattedTextField lowerEpsField;
	private final JFormattedTextField stepEpsField;
	private final JFormattedTextField upperEpsField;

	private static final int INNER_PAD = 2;

	public DBScanOptions() {
		setOpaque(false);
		final SpringLayout layout = new SpringLayout();
		setLayout(layout);
		final NumberFormat integerFieldFormatter = NumberFormat.getIntegerInstance();
		integerFieldFormatter.setGroupingUsed(false);

		// MINPTS
		final JLabel minPtsLbl = new JLabel("minPTS");
		layout.putConstraint(SpringLayout.NORTH, minPtsLbl, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST, minPtsLbl, 0, SpringLayout.WEST, this);
		add(minPtsLbl);

		// lowerBound
		lowerMinPTSField = new JFormattedTextField(integerFieldFormatter);
		lowerMinPTSField.setValue(1);
		lowerMinPTSField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, lowerMinPTSField, INNER_PAD, SpringLayout.SOUTH, minPtsLbl);
		layout.putConstraint(SpringLayout.EAST, lowerMinPTSField, 0, SpringLayout.EAST, this);
		add(lowerMinPTSField);
		final JLabel lminplbl = new JLabel("lower bound:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, lminplbl, 0, SpringLayout.VERTICAL_CENTER, lowerMinPTSField);
		layout.putConstraint(SpringLayout.WEST, lminplbl, 0, SpringLayout.WEST, this);
		add(lminplbl);

		// step
		stepMinPTSField = new JFormattedTextField(integerFieldFormatter);
		stepMinPTSField.setValue(1);
		stepMinPTSField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, stepMinPTSField, INNER_PAD, SpringLayout.SOUTH, lowerMinPTSField);
		layout.putConstraint(SpringLayout.EAST, stepMinPTSField, 0, SpringLayout.EAST, this);
		add(stepMinPTSField);
		final JLabel sminplbl = new JLabel("step size:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, sminplbl, 0, SpringLayout.VERTICAL_CENTER, stepMinPTSField);
		layout.putConstraint(SpringLayout.WEST, sminplbl, 0, SpringLayout.WEST, this);
		add(sminplbl);

		// upperBound
		upperMinPTSField = new JFormattedTextField(integerFieldFormatter);
		upperMinPTSField.setValue(1);
		upperMinPTSField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, upperMinPTSField, INNER_PAD, SpringLayout.SOUTH, stepMinPTSField);
		layout.putConstraint(SpringLayout.EAST, upperMinPTSField, 0, SpringLayout.EAST, this);
		add(upperMinPTSField);
		final JLabel uminplbl = new JLabel("upper bound:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, uminplbl, 0, SpringLayout.VERTICAL_CENTER, upperMinPTSField);
		layout.putConstraint(SpringLayout.WEST, uminplbl, 0, SpringLayout.WEST, this);
		add(uminplbl);

		final NumberFormat doubleFieldFormatter = NumberFormat.getNumberInstance();
		// EPS
		final JLabel epsLbl = new JLabel("epsilon");
		layout.putConstraint(SpringLayout.NORTH, epsLbl, 5 * INNER_PAD, SpringLayout.SOUTH, upperMinPTSField);
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

	public int getLBMinPTS() {
		return Integer.parseInt(lowerMinPTSField.getText());
	}

	public int getStepMinPTS() {
		return Integer.parseInt(stepMinPTSField.getText());
	}

	public int getUBMinPTS() {
		return Integer.parseInt(upperMinPTSField.getText());
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
