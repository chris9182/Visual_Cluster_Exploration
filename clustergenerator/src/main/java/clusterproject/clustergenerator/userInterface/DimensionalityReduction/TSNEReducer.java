package clusterproject.clustergenerator.userInterface.DimensionalityReduction;

import java.awt.Dimension;

import javax.swing.JPanel;

import com.jujutsu.tsne.TSneConfiguration;
import com.jujutsu.tsne.barneshut.BHTSne;
import com.jujutsu.tsne.barneshut.BarnesHutTSne;
import com.jujutsu.tsne.barneshut.ParallelBHTsne;
import com.jujutsu.utils.TSneUtils;

import clusterproject.clustergenerator.data.PointContainer;
import clusterproject.clustergenerator.userInterface.MainWindow;
import clusterproject.clustergenerator.userInterface.DimensionalityReduction.Panel.TSNEOptions;

public class TSNEReducer implements IDimensionalityReduction {

	TSNEOptions tSNEOptions = new TSNEOptions();

	@Override
	public JPanel getOptionsPanel() {
		return tSNEOptions;
	}

	@Override
	public String getName() {
		return "T-SNE";
	}

	@Override
	public boolean reduce(PointContainer container) {
		if (container.getDim() < tSNEOptions.getDim() || container.getPoints().size() < 2)
			return false;
		double[][] data = new double[container.getPoints().size()][];
		data = container.getPoints().toArray(data);
		BarnesHutTSne tsne;
		final boolean parallel = tSNEOptions.getIsParallel();
		if (parallel) {
			tsne = new ParallelBHTsne();
		} else {
			tsne = new BHTSne();
		}
		final TSneConfiguration config = TSneUtils.buildConfig(data, tSNEOptions.getDim(), container.getDim(),
				tSNEOptions.getPerplexity(), tSNEOptions.getMaxIterations());
		config.setSilent(true);
		double[][] Y;
		try {
			Y = tsne.tsne(config);
		} catch (final Exception e) {
			return false;// TODO possibly error
		}
		final PointContainer newContainer = new PointContainer(tSNEOptions.getDim());
		newContainer.addPoints(Y);
		;
		final MainWindow newWindow = new MainWindow(newContainer);
		newWindow.setSize(new Dimension(1000, 800));
		newWindow.setLocationRelativeTo(null);
		newWindow.setVisible(true);
		newWindow.update();
		return true;
	}

}
