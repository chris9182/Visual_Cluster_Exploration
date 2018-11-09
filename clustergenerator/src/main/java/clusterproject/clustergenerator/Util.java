package clusterproject.clustergenerator;

import java.awt.Graphics2D;

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

}
