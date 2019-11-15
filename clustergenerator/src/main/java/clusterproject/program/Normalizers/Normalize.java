package clusterproject.program.Normalizers;

import java.awt.Dimension;

import javax.swing.JPanel;

import clusterproject.data.PointContainer;
import clusterproject.program.MainWindow;
import clusterproject.program.Normalizers.Panel.NormalizeOptions;

public class Normalize implements INormalizer {

	NormalizeOptions options = new NormalizeOptions();

	@Override
	public JPanel getOptionsPanel() {
		return options;
	}

	@Override
	public String getName() {
		return "Normalize";
	}

	@Override
	public boolean normalize(PointContainer container) {
		double[][] data = new double[container.getPoints().size()][];
		data = container.getPoints().toArray(data);

		final double[] min = new double[data[0].length];
		final double[] max = new double[data[0].length];

		for (int i = 0; i < data[0].length; ++i) {
			min[i] = Double.MAX_VALUE;
			max[i] = -Double.MAX_VALUE;
		}

		for (int i = 0; i < data.length; ++i) {
			for (int j = 0; j < data[i].length; ++j) {
				if (data[i][j] < min[j])
					min[j] = data[i][j];
				if (data[i][j] > max[j])
					max[j] = data[i][j];
			}
		}

		final double[][] newdata = new double[container.getPoints().size()][];

		for (int i = 0; i < data.length; i++) {
			newdata[i] = new double[data[i].length];
			for (int j = 0; j < data[i].length; j++) {
				if (max[j] - min[j] == 0)
					newdata[i][j] = 0;
				else
					newdata[i][j] = (data[i][j] - min[j]) / (max[j] - min[j]);
			}
		}

		final PointContainer newContainer = new PointContainer(container.getDim());
		newContainer.addPoints(newdata);
		newContainer.copyInfoFrom(container);
		newContainer.setHeaders(container.getHeaders());

		final MainWindow newWindow = new MainWindow(newContainer);
		newWindow.setSize(new Dimension(1000, 800));
		newWindow.setLocationRelativeTo(null);
		newWindow.setVisible(true);
		newWindow.update();
		return false;
	}

}
