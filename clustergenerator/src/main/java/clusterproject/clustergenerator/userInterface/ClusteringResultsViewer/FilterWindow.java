package clusterproject.clustergenerator.userInterface.ClusteringResultsViewer;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.SpringLayout;

import com.jidesoft.swing.RangeSlider;

import clusterproject.clustergenerator.data.ClusteringResult;
import clusterproject.clustergenerator.userInterface.MainWindow;

public class FilterWindow extends JFrame {

	private static final long serialVersionUID = 1052960199516074256L;
	private static final int BARWIDTH = 10;
	private static final int BARVOFFSET = 8;
	private static final int BARHOFFSET = 16;
	private static final int SPACING = 10;
	private static final int SLIDERWIDTH = 50;

	JLayeredPane mainPane = new JLayeredPane();
	SpringLayout mainLayout = new SpringLayout();

	private final List<ClusteringResult> clusteringResults;

	public FilterWindow(List<ClusteringResult> clusteringResults) {
		getContentPane().setBackground(MainWindow.BACKGROUND_COLOR);
		add(mainPane);
		mainPane.setLayout(mainLayout);
		final RangeSlider slider = new MyRangeSlider(10.3, 12.5);
		mainLayout.putConstraint(SpringLayout.NORTH, slider, SPACING, SpringLayout.NORTH, mainPane);
		mainLayout.putConstraint(SpringLayout.SOUTH, slider, -SPACING, SpringLayout.SOUTH, mainPane);
		mainLayout.putConstraint(SpringLayout.EAST, slider, SLIDERWIDTH + SPACING, SpringLayout.WEST, mainPane);
		mainLayout.putConstraint(SpringLayout.WEST, slider, SPACING, SpringLayout.WEST, mainPane);
		mainPane.add(slider, new Integer(1));
		this.clusteringResults = clusteringResults;
		final LinkedHashSet<String> clusteringNames = new LinkedHashSet<String>();
		for (final ClusteringResult result : clusteringResults) {
			clusteringNames.add(result.getParameter().getName());
		}
		final List<LinkedHashSet<String>> parameterNames = new ArrayList<LinkedHashSet<String>>();
		final List<List<LinkedHashSet<Object>>> parameters = new ArrayList<List<LinkedHashSet<Object>>>();
		for (final String clusteringName : clusteringNames) {
			final LinkedHashSet<String> clusteringParameterNames = new LinkedHashSet<String>();
			for (final ClusteringResult result : clusteringResults) {
				if (result.getParameter().getName().equals(clusteringName))
					clusteringParameterNames.addAll(result.getParameter().getParameters().keySet());
			}
			parameterNames.add(clusteringParameterNames);
			final List<LinkedHashSet<Object>> clusteringParameters = new ArrayList<LinkedHashSet<Object>>();

			for (final String clusteringParameterName : clusteringParameterNames) {
				final LinkedHashSet<Object> nameParameters = new LinkedHashSet<Object>();
				for (final ClusteringResult result : clusteringResults) {
					if (result.getParameter().getName().equals(clusteringName)) {
						nameParameters.add(result.getParameter().getParameters().get(clusteringParameterName));
					}
				}
				clusteringParameters.add(nameParameters);
			}
			parameters.add(clusteringParameters);
		}
		final Iterator<String> clusteringNamesIt = clusteringNames.iterator();
		for (int i = 0; i < clusteringNames.size(); ++i) {
			final String clusteringName = clusteringNamesIt.next();
			System.err.println(clusteringName);
			final Iterator<String> parameterNamesIt = parameterNames.get(i).iterator();
			for (int j = 0; j < parameterNames.get(i).size(); ++j) {
				final String parameterName = parameterNamesIt.next();
				System.err.println("  " + parameterName);
				final Iterator<Object> parametersIt = parameters.get(i).get(j).iterator();
				for (int k = 0; k < parameters.get(i).get(j).size(); ++k) {
					final Object parameter = parametersIt.next();
					System.err.println("    " + parameter);
				}
			}
		}

	}

	private class MyRangeSlider extends RangeSlider {
		private static final long serialVersionUID = -1145841853132161271L;
		private final List<JLabel> labels = new ArrayList<JLabel>();

		public MyRangeSlider(double minLbl, double maxLbl) {
			super(RangeSlider.VERTICAL);
			setOpaque(false);
			setMinimum(0);
			setMaximum(10000);
			setLowValue(0);
			setHighValue(10000);
			setRangeDraggable(true);
			setPaintTrack(false);
			setPaintLabels(true);
			final Dictionary<Integer, JComponent> dict = new Hashtable<Integer, JComponent>();
			for (int i = 0; i < 11; ++i) {
				final JLabel label = new JLabel("   " + (float) ((maxLbl - minLbl) * i + minLbl));
				// String.format("%.3f", (maxLbl - minLbl) * i + minLbl));
				labels.add(label);
				dict.put(i * 1000, label);
			}
			setLabelTable(dict);
		}

		@Override
		public void paint(Graphics g) {
			final Color c = g.getColor();
			g.setColor(Color.black);
			int labelwidth = 0;
			for (final JLabel label : labels)
				if (label.getWidth() > labelwidth)
					labelwidth = label.getWidth();
			g.drawRect(getWidth() / 2 - BARWIDTH + BARHOFFSET - labelwidth / 2, BARVOFFSET, BARWIDTH / 2,
					getHeight() - 2 * BARVOFFSET);
			g.setColor(c);
			super.paint(g);

		}
	};

}
