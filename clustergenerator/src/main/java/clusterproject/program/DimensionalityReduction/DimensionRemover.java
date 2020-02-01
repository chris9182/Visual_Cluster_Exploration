package clusterproject.program.DimensionalityReduction;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JPanel;

import clusterproject.data.PointContainer;
import clusterproject.program.MainWindow;
import clusterproject.program.Normalizers.Panel.DimensionRemoverOptions;

public class DimensionRemover implements IReducer {

	DimensionRemoverOptions options = new DimensionRemoverOptions();

	@Override
	public JPanel getOptionsPanel() {
		return options;
	}

	@Override
	public String getName() {
		return "Dim. Remover";
	}

	@Override
	public boolean reduce(PointContainer container) {
		final double[][] newdata = new double[container.getPoints().size()][];
		final int removeDim = options.getDim();
		final int newDim = container.getDim() - 1;
		final List<double[]> points = container.getPoints();
		for (int i = 0; i < points.size(); ++i) {
			final double[] point = points.get(i);
			final double[] newPoint = new double[newDim];
			for (int j = 0; j < newDim; ++j)
				if (j >= removeDim)
					newPoint[j] = point[j + 1];
				else
					newPoint[j] = point[j];
			newdata[i] = newPoint;
		}

		final PointContainer newContainer = new PointContainer(newDim);
		newContainer.addPoints(newdata);
		newContainer.copyInfoFrom(container);
		final List<String> headers = container.getHeaders();
		if (headers.size() > removeDim)
			headers.remove(removeDim);
		newContainer.setHeaders(headers);

		final MainWindow newWindow = new MainWindow(newContainer);
		newWindow.setSize(new Dimension(1000, 800));
		newWindow.setLocationRelativeTo(null);
		newWindow.setVisible(true);
		newWindow.update();
		return false;
	}

}
