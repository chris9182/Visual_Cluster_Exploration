package clusterproject.program.DimensionalityReduction;

import java.awt.Dimension;
import java.util.LinkedList;

import javax.swing.JPanel;

import clusterproject.data.PointContainer;
import clusterproject.program.MainWindow;
import clusterproject.program.DimensionalityReduction.Panel.PCAOptions;
import smile.projection.PCA;

public class PCAReducer implements IDimensionalityReduction {

	PCAOptions pCAOptions;

	public PCAReducer() {
		pCAOptions = new PCAOptions();
	}

	@Override
	public JPanel getOptionsPanel() {
		return pCAOptions;
	}

	@Override
	public String getName() {
		return "PCA";
	}

	@Override
	public boolean reduce(PointContainer container) {
		if (container.getDim() < pCAOptions.getDim() || container.getPoints().size() < 2)
			return false;
		double[][] data = new double[container.getPoints().size()][];
		data = container.getPoints().toArray(data);

		PCA pca = new PCA(data);
		pca.setProjection(pCAOptions.getDim());
		double[][] result = pca.project(data);

		final PointContainer newContainer = new PointContainer(pCAOptions.getDim());
		newContainer.addPoints(result);
		newContainer.copyInfo(container);

		final MainWindow newWindow = new MainWindow(newContainer);
		newWindow.setSize(new Dimension(1000, 800));
		newWindow.setLocationRelativeTo(null);
		newWindow.setVisible(true);
		newWindow.update();

		return true;
	}

}
