package other.diclens;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.BitSet;

import javax.swing.JFileChooser;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.google.common.base.Function;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelAsShapeRenderer;
import other.diclens.DiclensMST.Cluster;
import other.diclens.DiclensMST.Edge;
import other.dm.ensemble.ExtractClusterings;

//from https://www.cs.umb.edu/~smimarog/diclens/
//modified
public class DiclensGUIController {
	static String[] setContentIdentifier;
	private final DiclensGUIView view;
	private DiclensMST algorithm;

	static {
		DiclensGUIController.setContentIdentifier = new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
				"K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
	}

	public static void main(final String[] args) {
		EventQueue.invokeLater(() -> new DiclensGUIController());
	}

	public DiclensGUIController() {
		this.view = new DiclensGUIView();
		this.view.browseBt.addActionListener(new BrowseButtonPressed());
		this.view.runAlgorithmButton.addActionListener(new RunAlgorithmPressed());
		this.view.setVisible();
		final int[][] example = { { 1, 1, 2, 2, 3, 3 }, { 3, 3, 2, 2, 1, 1 }, { 1, 1, 1, 2, 2, 2 },
				{ 1, 1, 0, 0, 0, 2 } };
		this.runAlgorithm(ExtractClusterings.fromArray(example));
	}

	private void runAlgorithm() {
		final other.dm.ensemble.ExtractClusterings.ClusterInfo clusterInfo = ExtractClusterings
				.fromFile(this.view.filenameText.getText());
		this.runAlgorithm(clusterInfo);
	}

	public static int[] runAlgorithm(int[][] clusters) {
		final DiclensMST mst = new DiclensMST(ExtractClusterings.fromArray(clusters));
		mst.run();
		mst.setNumberOfComponentsTo(mst.suggestedPoint());
		return mst.finalClusteringAsArray();

	}

	public static int[] runAlgorithm(int[][] clusters, int clusterNumber) {
		final DiclensMST mst = new DiclensMST(ExtractClusterings.fromArray(clusters));
		mst.run();
		mst.forceClusterNumber(clusterNumber);
		return mst.finalClusteringAsArray();

	}

	private void runAlgorithm(final ExtractClusterings.ClusterInfo clusterInfo) {
		(this.algorithm = new DiclensMST(clusterInfo)).run();
		this.algorithm.setNumberOfComponentsTo(this.algorithm.suggestedPoint());
		this.updateGraphs();
		this.setOutput();
	}

	private void setOutput() {
		final StringBuilder outLongText = new StringBuilder();
		for (int i = 0; i < this.algorithm.finalClusters.length; ++i) {
			outLongText.append(String.valueOf(DiclensGUIController.setContentIdentifier[i]) + ": "
					+ formatToDisplay(this.algorithm.finalClusters[i]));
			outLongText.append("\n");
		}
		final StringBuilder outShortText = new StringBuilder();
		int[] finalClusteringAsArray;
		for (int length = (finalClusteringAsArray = this.algorithm
				.finalClusteringAsArray(1)).length, j = 0; j < length; ++j) {
			final int cluster = finalClusteringAsArray[j];
			outShortText.append(cluster).append(",");
		}
		this.view.setLongOutputTo(outLongText.toString());
		this.view.setShortOutputTo(outShortText.substring(0, outShortText.length() - 1));
		this.view.draw(new VennDiagram(this.algorithm.finalClusters.length));
	}

	private void updateGraphs() {
		this.view.setSmstGraphViewer(this.generateForestViewer(this.algorithm.smst));
		this.view.setMetaClusterGraphViewer(this.generateForestViewer(this.algorithm.metaClusterForest));
	}

	private VisualizationViewer<DiclensMST.Cluster, DiclensMST.Edge> generateForestViewer(
			final Forest<DiclensMST.Cluster, DiclensMST.Edge> forest) {
		final Layout<DiclensMST.Cluster, DiclensMST.Edge> layout = new TreeLayout<Cluster, Edge>(forest);
		final VisualizationViewer<DiclensMST.Cluster, DiclensMST.Edge> vv = new VisualizationViewer<Cluster, Edge>(
				layout);
		vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
		final VertexLabelAsShapeRenderer<DiclensMST.Cluster, DiclensMST.Edge> vlasr = new VertexLabelAsShapeRenderer<Cluster, Edge>(
				vv.getRenderContext());
		vv.getRenderContext().setVertexShapeTransformer(vlasr);
		vv.setVertexToolTipTransformer(new ClusterContentDisplayer());
		vv.setEdgeToolTipTransformer(new EdgeWeightTransformer());
		vv.getRenderContext().setVertexLabelTransformer(new ClusterIndexTranformer());
		vv.setForeground(Color.white);
		return vv;
	}

	private static String formatToDisplay(final BitSet cluster) {
		final StringBuilder sb = new StringBuilder();
		for (int i = cluster.nextSetBit(0); i >= 0; i = cluster.nextSetBit(i + 1)) {
			sb.append(i + 1).append(", ");
		}
		return sb.substring(0, sb.length() - 2).toString();
	}

	private static class ClusterContentDisplayer implements Function<DiclensMST.Cluster, String> {
		@Override
		public @Nullable String apply(@Nullable Cluster input) {
			return formatToDisplay(input.contents);
		}
	}

	private static class ClusterIndexTranformer implements Function<DiclensMST.Cluster, String> {
		@Override
		public @Nullable String apply(@Nullable Cluster input) {
			return "<html>C<sub>" + (input.partitionIx + 1) + "," + (input.clusterIx + 1) + "</sub></html>";
		}
	}

	private static class EdgeWeightTransformer implements Function<DiclensMST.Edge, String> {
		@Override
		public @Nullable String apply(@Nullable Edge input) {
			return String.valueOf(input.similarity());
		}
	}

	private class BrowseButtonPressed implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final JFileChooser fc = new JFileChooser();
			final int returnVal = fc.showOpenDialog(DiclensGUIController.this.view.browseBt.getParent());
			if (returnVal == 0) {
				DiclensGUIController.this.view.filenameText.setText(fc.getSelectedFile().getAbsoluteFile().toString());
			}
		}
	}

	private final class RunAlgorithmPressed implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			DiclensGUIController.this.runAlgorithm();
		}
	}
}
