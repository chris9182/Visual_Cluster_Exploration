package clusterproject.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NMI {

	public static double calc(List<Integer> targetLabels, List<Integer> clusterLabels) {
		final int length1 = targetLabels.size();
		final int length2 = clusterLabels.size();
		if (length1 != length2)
			throw new IllegalArgumentException("The label lists need to have the same length to compute NMI!");
		final int length = length1;
		final Map<Integer, Integer> countPerLabel1 = new HashMap<Integer, Integer>();
		for (int i = 0; i < length; ++i) {
			final int currentValue = targetLabels.get(i);
			Integer count = countPerLabel1.get(currentValue);
			if (count == null)
				count = 0;
			countPerLabel1.put(currentValue, count + 1);
		}
		final Map<Integer, Integer> countPerLabel2 = new HashMap<Integer, Integer>();
		for (int i = 0; i < length; ++i) {
			final int currentValue = clusterLabels.get(i);
			Integer count = countPerLabel2.get(currentValue);
			if (count == null)
				count = 0;
			countPerLabel2.put(currentValue, count + 1);
		}

		final double logBase = 2;// Math.E;
		double hl = 0;
		double hc = 0;

		for (final Integer value : countPerLabel1.values()) {
			hl -= value / (double) length * logn(value / (double) length, logBase);
		}
		for (final Integer value : countPerLabel2.values()) {
			hc -= value / (double) length * logn(value / (double) length, logBase);
		}

		double hlc = 0;
		for (final Integer key2 : countPerLabel2.keySet()) {
			double sumv = 0;
			final double val = countPerLabel2.get(key2);
			for (final Integer key1 : countPerLabel1.keySet()) {
				int count = 0;
				for (int i = 0; i < length; ++i) {
					if (targetLabels.get(i) == key1 && clusterLabels.get(i) == key2)
						++count;
				}
				final double pr = count / val;
				if (Math.abs(pr) > Double.MIN_NORMAL)
					sumv += pr * logn(pr, logBase);
			}
			sumv *= -val / length;
			hlc += sumv;
		}
		final double bigI = hl - hlc;
		return (2 * bigI / (hl + hc));
		// XXX: remove
		// def nmi(target_labels, cluster_labels):
		// if len(target_labels)!=len(cluster_labels):
		// return 'error'
		// length=len(target_labels)
		// logbase=math.e
		// lables_unique, lables_counts = np.unique(target_labels, return_counts=True)
		// clusters_unique, clusters_counts = np.unique(cluster_labels,
		// return_counts=True)
		// ldict=(dict(zip(lables_unique, lables_counts)))
		// cdict=(dict(zip(clusters_unique, clusters_counts)))
		// hl=0
		// hc=0
		// for key, value in ldict.items():
		// hl-=value/length*math.log(value/length,logbase)
		// for key, value in cdict.items():
		// hc-=value/length*math.log(value/length,logbase)
		// hlc=0
		// for keyc, valuec in cdict.items():
		// sumv=0
		// for keyl, valuel in ldict.items():
		// count=0
		// for i in range(length):
		// if cluster_labels[i]==keyc and target_labels[i]==keyl:
		// count+=1
		// pr=count/valuec
		// if pr!=0:
		// sumv+=pr*math.log(pr,logbase)
		// sumv*=-valuec/length
		// hlc+=sumv
		// I=hl-hlc
		// #(I)/math.sqrt(hl*hc) <- this is how other libs do it
		// #just like sklearn.metrics.cluster.normalized_mutual_info_score
		// #this is just a different normalization, see:
		// #https://www.researchgate.net/publication/321236674_On_Normalized_Mutual_Information_Measure_Derivations_and_Properties
		// #just bellow the collection of formulas No (27)
		// return (2*I)/(hl+hc)
	}

	private static double logn(double x, double b) {
		return Math.log(x) / Math.log(b);
	}
}
