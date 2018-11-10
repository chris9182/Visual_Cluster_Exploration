package clusterproject.clustergenerator.userInterface.DimensionalityReduction;

import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

public class TSNEOptions extends JPanel {

	private static final long serialVersionUID = 1L;

	private final JFormattedTextField dimField;
	private final JFormattedTextField perplexityField;
	private final JFormattedTextField maxIterField;
	private final JCheckBox parallelCheck;

	private static final int DEFAULT_DIM = 2;
	private static final double DEFAULT_PERPLEXITY = 20;
	private static final int DEFAULT_MAX_ITER = 1000;
	private static final boolean DEFAULT_PARALLEL = true;

	private static final int INNER_PAD = 2;

	public TSNEOptions() {
		setOpaque(false);
		final SpringLayout layout = new SpringLayout();
		setLayout(layout);
		final NumberFormat integerFieldFormatter = NumberFormat.getIntegerInstance();
		integerFieldFormatter.setGroupingUsed(false);
		dimField = new JFormattedTextField(integerFieldFormatter);
		dimField.setValue(DEFAULT_DIM);
		dimField.setColumns(5);
		dimField.setHorizontalAlignment(SwingConstants.RIGHT);
		layout.putConstraint(SpringLayout.NORTH, dimField, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, dimField, 0, SpringLayout.EAST, this);
		add(dimField);
		final JLabel dimLabel = new JLabel("Dimensions:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, dimLabel, 0, SpringLayout.VERTICAL_CENTER, dimField);
		layout.putConstraint(SpringLayout.WEST, dimLabel, 0, SpringLayout.WEST, this);
		add(dimLabel);
		perplexityField = new JFormattedTextField(NumberFormat.getNumberInstance());
		perplexityField.setValue(DEFAULT_PERPLEXITY);
		perplexityField.setColumns(5);
		perplexityField.setHorizontalAlignment(SwingConstants.RIGHT);
		layout.putConstraint(SpringLayout.NORTH, perplexityField, INNER_PAD, SpringLayout.SOUTH, dimField);
		layout.putConstraint(SpringLayout.EAST, perplexityField, 0, SpringLayout.EAST, this);
		add(perplexityField);
		final JLabel perplexityLabel = new JLabel("Perplexity:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, perplexityLabel, 0, SpringLayout.VERTICAL_CENTER,
				perplexityField);
		layout.putConstraint(SpringLayout.WEST, perplexityLabel, 0, SpringLayout.WEST, this);
		add(perplexityLabel);
		maxIterField = new JFormattedTextField(integerFieldFormatter);
		maxIterField.setValue(DEFAULT_MAX_ITER);
		maxIterField.setColumns(5);
		maxIterField.setHorizontalAlignment(SwingConstants.RIGHT);
		layout.putConstraint(SpringLayout.NORTH, maxIterField, INNER_PAD, SpringLayout.SOUTH, perplexityField);
		layout.putConstraint(SpringLayout.EAST, maxIterField, 0, SpringLayout.EAST, this);
		add(maxIterField);
		final JLabel maxIterLabel = new JLabel("Max. Iterations:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, maxIterLabel, 0, SpringLayout.VERTICAL_CENTER, maxIterField);
		layout.putConstraint(SpringLayout.WEST, maxIterLabel, 0, SpringLayout.WEST, this);
		add(maxIterLabel);
		parallelCheck = new JCheckBox();
		parallelCheck.setSelected(DEFAULT_PARALLEL);
		parallelCheck.setOpaque(false);
		layout.putConstraint(SpringLayout.NORTH, parallelCheck, INNER_PAD, SpringLayout.SOUTH, maxIterField);
		layout.putConstraint(SpringLayout.EAST, parallelCheck, 0, SpringLayout.EAST, this);
		add(parallelCheck);
		final JLabel parallelLabel = new JLabel("Run Parallel:");
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, parallelLabel, 0, SpringLayout.VERTICAL_CENTER,
				parallelCheck);
		layout.putConstraint(SpringLayout.WEST, parallelLabel, 0, SpringLayout.WEST, this);
		add(parallelLabel);
	}

	public int getDim() {
		return Integer.parseInt(dimField.getText());
	}

	public double getPerplexity() {
		final NumberFormat format = NumberFormat.getInstance();
		Number number;
		try {
			number = format.parse(perplexityField.getText());
		} catch (final ParseException e1) {
			e1.printStackTrace();
			return DEFAULT_PERPLEXITY;
		}
		return number.doubleValue();
	}

	public int getMaxIterations() {
		return Integer.parseInt(maxIterField.getText());
	}

	public boolean getIsParallel() {
		return parallelCheck.isSelected();
	}
}
