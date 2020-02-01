package clusterproject.program.ClusterViewerElement;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import clusterproject.program.StartWindow;
import clusterproject.util.MinMax;
import clusterproject.util.Util;
import smile.stat.distribution.KernelDensity;

public class KernelDensityPlot extends JPanel {
	private static final long serialVersionUID = 6234671906695858813L;
	private final KernelDensity totalDensity;
	private final MinMax minMax;
	private final List<KernelDensity> densities;
	private final List<MinMax> mmList;
	private final int[] colors;
	private final double[][] data;
	private final int pointCount;

	public KernelDensityPlot(double[][] data, int[] colors) {
		setBackground(StartWindow.BACKGROUND_COLOR);
		this.data = data;
		this.colors = colors;
		final List<Double> points = new ArrayList<Double>(data.length > 0 ? data[0].length : 1);
		for (int i = 0; i < data.length; ++i)
			for (int j = 0; j < data[i].length; ++j)
				points.add(data[i][j]);
		pointCount = points.size();

		totalDensity = new KernelDensity(points.stream().mapToDouble(d -> d).toArray());
		densities = new ArrayList<KernelDensity>(data.length);
		mmList = new ArrayList<MinMax>();
		final double bandwidth = totalDensity.bandwidth();
		for (int i = 0; i < data.length; ++i) {
			if (data[i].length < 2 || bandwidth <= Double.MIN_NORMAL) {
				densities.add(null);
				continue;
			}
			densities.add(new KernelDensity(data[i], bandwidth));
			final MinMax curmm = new MinMax();
			for (int j = 0; j < data[i].length; ++j)
				curmm.add(data[i][j]);
			mmList.add(curmm);
		}
		final MinMax mm = new MinMax();

		for (int i = 0; i < points.size(); ++i)
			mm.add(points.get(i));
		minMax = mm;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		final Graphics2D g2 = (Graphics2D) g;
		g2.setComposite(AlphaComposite.SrcOver.derive(Util.FILTER_ALPHA));
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
		final int width = getWidth();
		final int height = getHeight();
		final double[] samples = new double[width + 1];
		final double min = minMax.min;
		final double range = minMax.getRange();
		final double dist = range / width;
		final MinMax mm = new MinMax();
		for (int i = 0; i < width + 1; ++i) {
			samples[i] = totalDensity.p(min + dist * i);
			mm.add(samples[i]);
		}
		final double densityMax = mm.max;

		final int[] y = new int[width + 3];
		final int[] x = new int[width + 3];
		for (int i = 0; i < width + 1; ++i) {
			x[i] = i;
			y[i] = (int) Math.round(height - (height * samples[i] / densityMax));
		}
		y[width + 1] = height;
		y[width + 2] = height;
		x[width + 1] = width;
		x[width + 2] = 0;
		g2.setColor(Util.getColor(1));
		g2.fillPolygon(x, y, width + 3);

		if (colors.length > 1)
			for (int d = 0; d < colors.length; ++d) {
				final KernelDensity density = densities.get(d);
				if (density == null)
					continue;
				for (int i = 0; i < width + 1; ++i) {
					samples[i] = density.p(min + dist * i);
				}

				final int[] y2 = new int[width + 3];
				final int[] x2 = new int[width + 3];
				for (int i = 0; i < width + 1; ++i) {
					x2[i] = i;
					y2[i] = (int) Math.round(
							height - ((height * samples[i] / densityMax) * (data[d].length / (double) pointCount)));
				}
				y2[width + 1] = height;
				y2[width + 2] = height;
				x2[width + 1] = width;
				x2[width + 2] = 0;
				g2.setColor(Util.getColor(colors[d] + 2));
				g2.fillPolygon(x2, y2, width + 3);
			}
	}

}
