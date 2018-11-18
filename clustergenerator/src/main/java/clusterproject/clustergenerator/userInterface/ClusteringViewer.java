package clusterproject.clustergenerator.userInterface;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import clusterproject.clustergenerator.data.ClusteringResult;

public class ClusteringViewer extends JFrame {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private final List<ClusteringResult> clusterings;
	private final List<ScatterPlot> viewers;

	public ClusteringViewer(List<ClusteringResult> clusterings) {
		this.clusterings = clusterings;
		viewers = new ArrayList<ScatterPlot>();
		clusterings.forEach(clustering -> {
			final ScatterPlot plot = new ScatterPlot(null, clustering.toPointContainer(), true);
			viewers.add(plot);
		});
		add(viewers.get(0));

	}

}
