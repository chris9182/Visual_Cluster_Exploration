package clusterproject.program.MetaClustering;

import clusterproject.data.ClusteringResult;
import clusterproject.util.Util;

//from https://en.wikipedia.org/wiki/Variation_of_information
public class VariationOfInformation implements IMetaDistanceMeasure {

	@Override
	public double distanceBetween(ClusteringResult clustering, ClusteringResult clustering2) {
		final double[][][] data1orig = clustering.getData();
		final double[][][] data2orig = clustering2.getData();

		final double[][][] data1 = data1orig;
		final double[][][] data2 = data2orig;

		final int n = clustering.getPointCount();
		double sum = 0;
		for (int i = 0; i < data1.length; ++i) {
			final double pi = (data1[i].length / (double) n);
			for (int j = 0; j < data2.length; ++j) {
				final double rij = Util.intersection(data1[i], data2[j]).length / (double) n;

				final double qj = (data2[j].length / (double) n);
				if (rij > 0)
					sum += rij * (Math.log(rij / pi) + Math.log(rij / qj));
			}
		}

		return -sum;
	}

	@Override
	public String getName() {
		return "Variation of Information";
	}
}
