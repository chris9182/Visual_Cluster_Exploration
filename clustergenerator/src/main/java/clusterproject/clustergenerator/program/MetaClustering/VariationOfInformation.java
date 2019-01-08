package clusterproject.clustergenerator.program.MetaClustering;

import clusterproject.clustergenerator.Util;
import clusterproject.clustergenerator.data.ClusteringResult;

//from https://en.wikipedia.org/wiki/Variation_of_information
public class VariationOfInformation implements IDistanceMeasure {

	@Override
	public double distanceBetween(ClusteringResult clustering, ClusteringResult clustering2) {
		final int n = clustering.getPointCount();
		double sum = 0;
		for (int i = 0; i < clustering.getData().length; ++i)
			for (int j = 0; j < clustering2.getData().length; ++j) {
				final double rij = Util.intersection(clustering.getData()[i], clustering2.getData()[j]).length
						/ (double) n;

				final double pi = (clustering.getData()[i].length / (double) n);
				final double qj = (clustering2.getData()[j].length / (double) n);
				if (rij > 0)
					sum += rij * (Math.log(rij / pi) + Math.log(rij / qj));
			}

		return -sum;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Variation of Information";
	}
}
