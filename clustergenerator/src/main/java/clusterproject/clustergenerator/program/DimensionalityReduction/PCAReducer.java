package clusterproject.clustergenerator.program.DimensionalityReduction;

import java.awt.Dimension;
import java.util.LinkedList;

import javax.swing.JPanel;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.linalg.Matrix;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.linalg.distributed.RowMatrix;

import clusterproject.clustergenerator.data.PointContainer;
import clusterproject.clustergenerator.program.MainWindow;
import clusterproject.clustergenerator.program.DimensionalityReduction.Panel.PCAOptions;

public class PCAReducer implements IDimensionalityReduction {

	PCAOptions pCAOptions = new PCAOptions();

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

		final LinkedList<Vector> rowsList = new LinkedList<>();
		for (int i = 0; i < data.length; i++) {
			final Vector currentRow = Vectors.dense(data[i]);
			rowsList.add(currentRow);
		}

		// XXX this may be changed for release
		final SparkConf conf = new SparkConf().setAppName("JavaPCA").setMaster("local[*]");
		final JavaSparkContext jsc = new JavaSparkContext(conf);

		final JavaRDD<Vector> rows = jsc.parallelize(rowsList);
		final RowMatrix mat = new RowMatrix(rows.rdd());

		final Matrix pc = mat.computePrincipalComponents(pCAOptions.getDim());

		final RowMatrix projected = mat.multiply(pc);

		final Vector[] collectPartitions = (Vector[]) projected.rows().collect();

		final PointContainer newContainer = new PointContainer(pCAOptions.getDim());
		newContainer.addPoints(collectPartitions);
		newContainer.copyClusterInfo(container);

		jsc.close();
		final MainWindow newWindow = new MainWindow(newContainer);
		newWindow.setSize(new Dimension(1000, 800));
		newWindow.setLocationRelativeTo(null);
		newWindow.setVisible(true);
		newWindow.update();
		// for (int i = 0; i < output.length; i++)
		// System.out.println(output[0][i] + " " + output[1][i]);
		return true;
	}

}
