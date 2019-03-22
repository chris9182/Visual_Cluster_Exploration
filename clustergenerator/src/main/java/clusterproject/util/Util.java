package clusterproject.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import clusterproject.data.ClusteringResult;
import clusterproject.data.NumberVectorClusteringResult;
import clusterproject.program.MetaClustering.ClusteringWithDistance;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.utilities.datastructures.arraylike.ArrayLikeUtil;

public class Util {
	public static final String GROUND_TRUTH = "Ground Truth";
	public static final String CLUSTER_COUNT = "# Clusters";
	public static final Color HIGHLIGHT_COLOR = Color.ORANGE;
	public static final float FILTER_ALPHA = 0.3f;
	private static final int COLOR_CACHE_SIZE = 1000;
	public static Set<String> META_PARAMS = new HashSet<String>();
	public static Map<Integer, Color> colorCache = new HashMap<Integer, Color>();
	static {
		META_PARAMS.add(GROUND_TRUTH);
		META_PARAMS.add(CLUSTER_COUNT);
	}

	public static void drawRotate(Graphics2D g2d, double x, double y, int angle, String text) {
		g2d.translate((float) x, (float) y);
		g2d.rotate(Math.toRadians(angle));
		g2d.drawString(text, 0, 0);
		g2d.rotate(-Math.toRadians(angle));
		g2d.translate(-(float) x, -(float) y);
	}

	public static double[][] transpose(double[][] data) {
		final int columns = data[0].length;
		final int rows = data.length;
		final double[][] temp = new double[columns][rows];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				temp[j][i] = data[i][j];
			}
		}
		return temp;
	}

	// https://stackoverflow.com/questions/309149/generate-distinctly-different-rgb-colors-in-graphs
	public static Color getColor(int i) {
		// return new Color(getRGB(i));
		Color color = colorCache.get(i);
		if (color == null) {
			color = new Color(getRGB(i));
			if (i < COLOR_CACHE_SIZE)
				colorCache.put(i, color);
		}
		return color;
	}

	public static int getRGB(int index) {
		final int[] p = getPattern(index);
		return getElement(p[0]) << 16 | getElement(p[1]) << 8 | getElement(p[2]);
	}

	public static int getElement(int index) {
		int value = index - 1;
		int v = 0;
		for (int i = 0; i < 8; i++) {
			v = v | (value & 1);
			v <<= 1;
			value >>= 1;
		}
		v >>= 1;
		return v & 0xFF;
	}

	public static int[] getPattern(int index) {
		final int n = (int) Math.cbrt(index);
		index -= (n * n * n);
		final int[] p = new int[3];
		Arrays.fill(p, n);
		if (index == 0) {
			return p;
		}
		index--;
		int v = index % 3;
		index = index / 3;
		if (index < n) {
			p[v] = index % n;
			return p;
		}
		index -= n;
		p[v] = index / n;
		p[++v % 3] = index % n;
		return p;
	}

	public static Object[] intersection(Object[] a, Object[] b) {
		return Stream.of(a).filter(new HashSet<Object>(Arrays.asList(b))::contains).toArray(Object[]::new);
	}

	public static List<ClusteringResult> convertClusterings(List<NumberVectorClusteringResult> clusterings,
			List<String> headers) {
		final List<ClusteringResult> sClusterings = new ArrayList<ClusteringResult>();
		final Map<NumberVector, double[]> pointMap = new HashMap<NumberVector, double[]>();
		final NumberVectorClusteringResult first = clusterings.get(0);
		double[][][] data = new double[first.getData().length][][];
		int i = 0;
		for (final NumberVector[] vecArr : first.getData()) {
			final double[][] cluster = new double[vecArr.length][];
			int j = 0;
			for (final NumberVector vec : vecArr) {
				final double[] point = ArrayLikeUtil.toPrimitiveDoubleArray(vec);
				pointMap.put(vec, point);
				cluster[j] = point;
				j++;
			}

			data[i] = cluster;
			i++;
		}
		sClusterings.add(new ClusteringResult(data, first.getDescription(), headers));

		for (int k = 1; k < clusterings.size(); ++k) {
			data = new double[clusterings.get(k).getData().length][][];
			i = 0;
			for (final NumberVector[] vecArr : clusterings.get(k).getData()) {
				final double[][] cluster = new double[vecArr.length][];
				int j = 0;
				for (final NumberVector vec : vecArr) {
					cluster[j] = pointMap.get(vec);
					j++;
				}

				data[i] = cluster;
				i++;
			}
			sClusterings.add(new ClusteringResult(data, clusterings.get(k).getDescription(), headers));
		}

		return sClusterings;
	}

	public static double[][] getSortedDistances(List<ClusteringWithDistance> list, double[][] distanceMatrix) {
		final double[][] sorted = new double[distanceMatrix.length][distanceMatrix.length];
		for (int i = 0; i < distanceMatrix.length; ++i)
			for (int j = 0; j < distanceMatrix.length; ++j) {
				sorted[i][j] = distanceMatrix[list.get(i).inIndex][list.get(j).inIndex];
			}
		return sorted;
	}

}
