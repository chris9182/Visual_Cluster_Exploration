package clusterproject.clustergenerator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;

import de.lmu.ifi.dbs.elki.data.NumberVector;

public class Util {
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
		return new Color(getRGB(i));
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

	public static Object[] intersection(NumberVector[] a, NumberVector[] b) {
		return Arrays.stream(a).distinct().filter(x -> Arrays.stream(b).anyMatch(y -> y == x)).toArray();
	}

}
