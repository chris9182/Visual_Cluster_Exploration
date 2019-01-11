package clusterproject.clustergenerator.program.Clustering.Panel;

import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class SNNOptions extends JPanel {
	private static final long serialVersionUID = 559822521256476978L;

	private final JFormattedTextField lowerMinPTSField;
	private final JFormattedTextField upperMinPTSField;

	private final JFormattedTextField lowerEpsField;
	private final JFormattedTextField upperEpsField;

	private final JFormattedTextField NField;

	private final JFormattedTextField lowersnnField;

	private final JFormattedTextField uppersnnField;

	private static final int INNER_PAD = 2;

	public SNNOptions() {
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

		// upperBound
		upperMinPTSField = new JFormattedTextField(integerFieldFormatter);
		upperMinPTSField.setValue(1);
		upperMinPTSField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, upperMinPTSField, INNER_PAD, SpringLayout.SOUTH, lowerMinPTSField);
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
		lowerEpsField = new JFormattedTextField(integerFieldFormatter);
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
		upperEpsField = new JFormattedTextField(integerFieldFormatter);
		upperEpsField.setValue(new Double(1.0));
		upperEpsField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, upperEpsField, INNER_PAD, SpringLayout.SOUTH, lowerEpsField);
		layout.putConstraint(SpringLayout.EAST, upperEpsField, 0, SpringLayout.EAST, this);
		add(upperEpsField);
		final JLabel uepslbl = new JLabel("upper bound:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, uepslbl, 0, SpringLayout.VERTICAL_CENTER, upperEpsField);
		layout.putConstraint(SpringLayout.WEST, uepslbl, 0, SpringLayout.WEST, this);
		add(uepslbl);

		// snn
		final JLabel snnLbl = new JLabel("Num. Neighbors");
		layout.putConstraint(SpringLayout.NORTH, snnLbl, 5 * INNER_PAD, SpringLayout.SOUTH, upperEpsField);
		layout.putConstraint(SpringLayout.WEST, snnLbl, 0, SpringLayout.WEST, this);
		add(snnLbl);

		// lowerBound
		lowersnnField = new JFormattedTextField(integerFieldFormatter);
		lowersnnField.setValue(1);
		lowersnnField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, lowersnnField, INNER_PAD, SpringLayout.SOUTH, snnLbl);
		layout.putConstraint(SpringLayout.EAST, lowersnnField, 0, SpringLayout.EAST, this);
		add(lowersnnField);
		final JLabel lsnnlbl = new JLabel("lower bound:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, lsnnlbl, 0, SpringLayout.VERTICAL_CENTER, lowersnnField);
		layout.putConstraint(SpringLayout.WEST, lsnnlbl, 0, SpringLayout.WEST, this);
		add(lsnnlbl);

		// upperBound
		uppersnnField = new JFormattedTextField(integerFieldFormatter);
		uppersnnField.setValue(1);
		uppersnnField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, uppersnnField, INNER_PAD, SpringLayout.SOUTH, lowersnnField);
		layout.putConstraint(SpringLayout.EAST, uppersnnField, 0, SpringLayout.EAST, this);
		add(uppersnnField);
		final JLabel usnnlbl = new JLabel("upper bound:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, usnnlbl, 0, SpringLayout.VERTICAL_CENTER, uppersnnField);
		layout.putConstraint(SpringLayout.WEST, usnnlbl, 0, SpringLayout.WEST, this);
		add(usnnlbl);

		NField = new JFormattedTextField(integerFieldFormatter);
		NField.setValue(1);
		NField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, NField, 5 * INNER_PAD, SpringLayout.SOUTH, uppersnnField);
		layout.putConstraint(SpringLayout.EAST, NField, 0, SpringLayout.EAST, this);
		add(NField);
		final JLabel sampleslbl = new JLabel("Samples:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, sampleslbl, 0, SpringLayout.VERTICAL_CENTER, NField);
		layout.putConstraint(SpringLayout.WEST, sampleslbl, 0, SpringLayout.WEST, this);
		add(sampleslbl);

	}

	public int getLBMinPTS() {
		return Integer.parseInt(lowerMinPTSField.getText());
	}

	public int getNSamples() {
		return Integer.parseInt(NField.getText());
	}

	public int getUBMinPTS() {
		return Integer.parseInt(upperMinPTSField.getText());
	}

	public int getLBEps() {
		return Integer.parseInt(lowerEpsField.getText());
	}

	public int getUBEps() {
		return Integer.parseInt(upperEpsField.getText());
	}

	public int getLBsnn() {
		return Integer.parseInt(lowersnnField.getText());
	}

	public int getUBsnn() {
		return Integer.parseInt(uppersnnField.getText());
	}

}
