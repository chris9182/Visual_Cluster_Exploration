package clusterproject.program.Clustering.Panel;

import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class SpectralOptions extends JPanel {
	private static final long serialVersionUID = -3668796468048106223L;
	private final JFormattedTextField lowerKField;
	private final JFormattedTextField upperKField;
	private final JFormattedTextField samplesEachField;
	private final JFormattedTextField lowerSigmaField;
	private final JFormattedTextField upperSigmaField;

	private static final int INNER_PAD = 2;

	public SpectralOptions() {
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

		final NumberFormat doubleFieldFormatter = NumberFormat.getNumberInstance();
		// EPS
		final JLabel sigmaLbl = new JLabel("sigma");
		layout.putConstraint(SpringLayout.NORTH, sigmaLbl, 5 * INNER_PAD, SpringLayout.SOUTH, upperKField);
		layout.putConstraint(SpringLayout.WEST, sigmaLbl, 0, SpringLayout.WEST, this);
		add(sigmaLbl);

		// lowerBound
		lowerSigmaField = new JFormattedTextField(doubleFieldFormatter);
		lowerSigmaField.setValue(new Double(1.0));
		lowerSigmaField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, lowerSigmaField, INNER_PAD, SpringLayout.SOUTH, sigmaLbl);
		layout.putConstraint(SpringLayout.EAST, lowerSigmaField, 0, SpringLayout.EAST, this);
		add(lowerSigmaField);
		final JLabel lepslbl = new JLabel("lower bound:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, lepslbl, 0, SpringLayout.VERTICAL_CENTER, lowerSigmaField);
		layout.putConstraint(SpringLayout.WEST, lepslbl, 0, SpringLayout.WEST, this);
		add(lepslbl);

		// upperBound
		upperSigmaField = new JFormattedTextField(doubleFieldFormatter);
		upperSigmaField.setValue(new Double(1.0));
		upperSigmaField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, upperSigmaField, INNER_PAD, SpringLayout.SOUTH, lowerSigmaField);
		layout.putConstraint(SpringLayout.EAST, upperSigmaField, 0, SpringLayout.EAST, this);
		add(upperSigmaField);
		final JLabel uepslbl = new JLabel("upper bound:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, uepslbl, 0, SpringLayout.VERTICAL_CENTER, upperSigmaField);
		layout.putConstraint(SpringLayout.WEST, uepslbl, 0, SpringLayout.WEST, this);
		add(uepslbl);

		samplesEachField = new JFormattedTextField(integerFieldFormatter);
		samplesEachField.setValue(1);
		samplesEachField.setColumns(5);
		layout.putConstraint(SpringLayout.NORTH, samplesEachField, INNER_PAD, SpringLayout.SOUTH, upperSigmaField);
		layout.putConstraint(SpringLayout.EAST, samplesEachField, 0, SpringLayout.EAST, this);
		add(samplesEachField);
		final JLabel samplesEachlbl = new JLabel("Samples:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, samplesEachlbl, 0, SpringLayout.VERTICAL_CENTER,
				samplesEachField);
		layout.putConstraint(SpringLayout.WEST, samplesEachlbl, 0, SpringLayout.WEST, this);
		add(samplesEachlbl);

	}

	public int getLBK() {
		return Integer.parseInt(lowerKField.getText());
	}

	public int getUBK() {
		return Integer.parseInt(upperKField.getText());
	}

	public double getLBSigma() {
		final NumberFormat format = NumberFormat.getInstance();
		Number number;
		try {
			number = format.parse(lowerSigmaField.getText());
		} catch (final ParseException e1) {
			e1.printStackTrace();
			return 0;
		}
		return number.doubleValue();
	}

	public double getUBSigma() {
		final NumberFormat format = NumberFormat.getInstance();
		Number number;
		try {
			number = format.parse(upperSigmaField.getText());
		} catch (final ParseException e1) {
			e1.printStackTrace();
			return 0;
		}
		return number.doubleValue();
	}

	public int getSamplesEach() {
		return Integer.parseInt(samplesEachField.getText());
	}

}
