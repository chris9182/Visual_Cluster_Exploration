package other.diclens;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.google.common.base.Function;
import com.google.common.util.concurrent.AtomicDouble;

import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.shortestpath.MinimumSpanningForest2;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;
import other.dm.ensemble.ExtractClusterings;
import other.dm.evaluator.ECSEvaluator;
import other.dm.evaluator.ICSEvaluator;
import other.dm.math.Normalize;

//from https://www.cs.umb.edu/~smimarog/diclens/
//modified
public class DiclensMST {
	public static final int MaxIterations = Integer.MAX_VALUE;
	public static boolean logout;
	private final BitSet[] unsortedClusters;
	private final List<BitSet[]> partitions;
	private int numOfComponents;
	private final int objectCount;
	private final ECSEvaluator ecs;
	private final int numOfAllClusters;
	BitSet[] finalClusters;
	public Map<Integer, BitSet[]> allFinalClusters;
	double[] icsAverages;
	double[] ecsAverages;
	double[] differences;
	Graph<Cluster, Edge> graph;
	Forest<Cluster, Edge> smst;
	private List<Edge> edges;
	Forest<Cluster, Edge> metaClusterForest;
	private List<Set<Cluster>> components;
	int upperLimitForEvaluation;
	private int suggestedPoint;
	private Cluster[] allClusters;

	static {
		DiclensMST.logout = false;
	}

	public DiclensMST(final ExtractClusterings.ClusterInfo clusterInfo) {
		this.allFinalClusters = new HashMap<Integer, BitSet[]>();
		this.suggestedPoint = 0;
		this.unsortedClusters = clusterInfo.allClusters.toArray(new BitSet[0]);
		this.partitions = clusterInfo.partitions;
		this.initializeClusters();
		this.objectCount = clusterInfo.numOfObjects;
		this.numOfAllClusters = this.allClusters.length;
		this.numOfComponents = this.numOfAllClusters;
		this.upperLimitForEvaluation = this.numOfAllClusters;
		this.ecs = new ECSEvaluator(this.partitions.size(), this.objectCount, this.numOfAllClusters,
				this.unsortedClusters);
	}

	private void initializeClusters() {
		this.allClusters = new Cluster[this.unsortedClusters.length];
		int partitionIx = 0;
		int index = 0;
		for (final BitSet[] partition : this.partitions) {
			int clusterIx = 0;
			BitSet[] array;
			for (int length = (array = partition).length, i = 0; i < length; ++i) {
				final BitSet cluster = array[i];
				this.allClusters[index++] = new Cluster(partitionIx, clusterIx, cluster);
				++clusterIx;
			}
			++partitionIx;
		}
	}

	public void run() {
		this.icsAverages = new double[this.upperLimitForEvaluation];
		this.ecsAverages = new double[this.upperLimitForEvaluation];
		this.differences = new double[this.upperLimitForEvaluation];
		this.graph = new UndirectedSparseGraph<Cluster, Edge>();
		Cluster[] allClusters;
		for (int length = (allClusters = this.allClusters).length, n = 0; n < length; ++n) {
			final Cluster cluster = allClusters[n];
			this.graph.addVertex(cluster);
		}
		for (int i = 0; i < this.allClusters.length; ++i) {
			for (int j = i + 1; j < this.allClusters.length; ++j) {
				this.graph.addEdge(
						new Edge(this.ecs.evaluate(this.allClusters[i].contents, this.allClusters[j].contents)),
						this.allClusters[i], this.allClusters[j]);
			}
		}
		final MinimumSpanningForest2<Cluster, Edge> prim = new MinimumSpanningForest2<Cluster, Edge>(this.graph,
				new DelegateForest<Cluster, Edge>(), DelegateTree.getFactory(),
				new SimilarityToDissimilarity(this.graph.getEdges()));
		this.smst = prim.getForest();
		Collections.sort(this.edges = new ArrayList<Edge>(this.smst.getEdges()), new SimilarityReverseComparator());
		this.metaClusterForest = this.newEdgelessForest();
		for (final Edge edge : this.edges) {
			this.components = this.componentsOf(this.metaClusterForest);
			this.numOfComponents = this.components.size();
			if (this.numOfComponents <= this.upperLimitForEvaluation) {
//				this.voteForMajority();
//				double icsAverage = 0.0;
//				for (int k = 0; k < this.finalClusters.length; ++k) {
//					icsAverage += ICSEvaluator.evaluate(this.unsortedClusters, this.finalClusters[k]);
//				}
//				icsAverage /= this.finalClusters.length;
//				double ecsAverage = 0.0;
//				for (int l = 0; l < this.finalClusters.length; ++l) {
//					for (int m = l + 1; m < this.finalClusters.length; ++m) {
//						ecsAverage += this.ecs.evaluate(this.finalClusters[l], this.finalClusters[m]);
//					}
//				}
//				ecsAverage /= this.finalClusters.length * (this.finalClusters.length - 1) / 2.0;
//				this.icsAverages[this.numOfComponents - 1] = icsAverage;
//				this.ecsAverages[this.numOfComponents - 1] = ecsAverage;
//				final Pair<Cluster> endpoints = this.smst.getEndpoints(edge);
//				this.metaClusterForest.addEdge(edge, endpoints);

				this.voteForMajority();
				final AtomicDouble icsSum = new AtomicDouble(0);
				final AtomicDouble ecsSum = new AtomicDouble(0);
				IntStream.range(0, this.finalClusters.length).parallel().forEach(k -> {
					final double ics = ICSEvaluator.evaluate(this.unsortedClusters, this.finalClusters[k]);
					icsSum.addAndGet(ics);
					for (int m = k + 1; m < this.finalClusters.length; ++m) {
						final double ecs = this.ecs.evaluate(this.finalClusters[k], this.finalClusters[m]);
						ecsSum.addAndGet(ecs);
					}
				});
				final double icsAverageD = icsSum.get() / this.finalClusters.length;
				final double ecsAverageD = ecsSum.get()
						/ (this.finalClusters.length * (this.finalClusters.length - 1) / 2.0);
				this.icsAverages[this.numOfComponents - 1] = icsAverageD;
				this.ecsAverages[this.numOfComponents - 1] = ecsAverageD;
				final Pair<Cluster> endpoints = this.smst.getEndpoints(edge);
				this.metaClusterForest.addEdge(edge, endpoints);
			}
		}

		printFormatted(this.icsAverages, "orig ics");
		printFormatted(this.ecsAverages, "orig ecs");
		this.normalize(this.ecsAverages);
		this.normalize(this.icsAverages);
		for (int i2 = 0; i2 < this.upperLimitForEvaluation; ++i2) {
			this.differences[i2] = this.icsAverages[i2] - this.ecsAverages[i2];
		}
		this.printForest(this.metaClusterForest);
		printFormatted(this.icsAverages, "icsAverages");
		printFormatted(this.ecsAverages, "ecsAverages");
	}

	int suggestedPoint() {
		if (this.suggestedPoint == 0) {
			double maxDiff = -2.147483648E9;
			println();
			for (int i = 1; i < this.upperLimitForEvaluation; ++i) {
				if (this.differences[i] > maxDiff) {
					this.suggestedPoint = i;
					maxDiff = this.differences[i];
				}
				print(String.valueOf(this.differences[i]) + ", ");
			}
			println();
			if (this.suggestedPoint == 0) {
				throw new AssertionError("Could not suggest number of clusters!!");
			}
		}
		return this.suggestedPoint + 1;
	}

	void setNumberOfComponentsTo(final int numberOfComponents) {
		this.metaClusterForest = this.generateComponents(numberOfComponents);
		this.components = this.componentsOf(this.metaClusterForest);
		this.numOfComponents = this.components.size();
		this.voteForMajority();
	}

	public int[] finalClusteringAsArray() {
		return this.finalClusteringAsArray(0);
	}

	public int[] finalClusteringAsArray(final int offset) {
		final int[] finalArray = new int[this.objectCount];
		for (int i = 0; i < this.finalClusters.length; ++i) {
			final BitSet cluster = this.finalClusters[i];
			final int clusterNo = i + offset;
			for (int j = cluster.nextSetBit(0); j >= 0; j = cluster.nextSetBit(j + 1)) {
				finalArray[j] = clusterNo;
			}
		}
		return finalArray;
	}

	private void printComponents() {
		System.out.println("==== components (" + this.components.size() + ") =====");
		for (final Set<Cluster> component : this.components) {
			for (final Cluster vertex : component) {
				print(String.valueOf(vertex.partitionIx) + "," + vertex.clusterIx + " + ");
			}
			println();
		}
		println();
	}

	private List<Set<Cluster>> componentsOf(final Forest<Cluster, Edge> forest) {
		final WeakComponentClusterer<Cluster, Edge> wcc = new WeakComponentClusterer<Cluster, Edge>();
		return new ArrayList<Set<Cluster>>(wcc.apply(forest));
	}

	private Forest<Cluster, Edge> generateComponents(final int numberOfComponents) {
		final Forest<Cluster, Edge> forest = this.newEdgelessForest();
		for (int numberOfEdges = this.edges.size() - numberOfComponents, i = 0; i <= numberOfEdges; ++i) {
			forest.addEdge(this.edges.get(i), this.smst.getEndpoints(this.edges.get(i)));
		}
		return forest;
	}

	private Forest<Cluster, Edge> newEdgelessForest() {
		final Forest<Cluster, Edge> finalGraph = new DelegateForest<Cluster, Edge>();
		for (final Cluster vertex : this.smst.getVertices()) {
			finalGraph.addVertex(vertex);
		}
		return finalGraph;
	}

	private void voteForMajority() {
		final double[][] votes = new double[this.numOfComponents][this.objectCount];
		int componentIx = 0;
		for (final Set<Cluster> component : this.components) {
			for (final Cluster cluster : component) {
				for (int objIx = cluster.contents.nextSetBit(0); objIx > -1; objIx = cluster.contents
						.nextSetBit(objIx + 1)) {
					final double[] array = votes[componentIx];
					final int n = objIx;
					++array[n];
				}
			}
			++componentIx;
		}
		final BitSet[] finalClusters = new BitSet[this.numOfComponents];
		for (int i = 0; i < this.numOfComponents; ++i) {
			finalClusters[i] = new BitSet();
		}
		for (int columnIx = 0; columnIx < this.objectCount; ++columnIx) {
			double maxVote = -1.0;
			int maxVotedCluster = 0;
			for (int rowIx = 0; rowIx < this.numOfComponents; ++rowIx) {
				if (votes[rowIx][columnIx] > maxVote) {
					maxVote = votes[rowIx][columnIx];
					maxVotedCluster = rowIx;
				}
			}
			finalClusters[maxVotedCluster].set(columnIx);
		}
		final boolean[] clusterIsNotEmpty = new boolean[finalClusters.length];
		int notEmptyClustersCount = 0;
		for (int j = 0; j < finalClusters.length; ++j) {
			if (finalClusters[j].cardinality() != 0) {
				clusterIsNotEmpty[j] = true;
				++notEmptyClustersCount;
			}
		}
		final BitSet[] trimmedClusters = new BitSet[notEmptyClustersCount];
		int count = 0;
		for (int k = 0; k < finalClusters.length; ++k) {
			if (clusterIsNotEmpty[k]) {
				trimmedClusters[count] = finalClusters[k];
				++count;
			}
		}
		this.finalClusters = trimmedClusters;
	}

	void printICSValues() {
		printFormatted(this.icsAverages, "ics");
	}

	static void print(final BitSet[] aClustering) {
		for (int i = 0; i < aClustering.length; ++i) {
			println(aClustering[i]);
		}
	}

	private void printForest(final Forest<Cluster, Edge> forest) {
		final boolean humanReadable = true;
		int hR_Offset = 0;
		if (humanReadable) {
			hR_Offset = 1;
		}
		final StringBuffer sb = new StringBuffer("Edges:");
		for (final Edge e : forest.getEdges()) {
			final Pair<Cluster> ep = forest.getEndpoints(e);
			sb.append("[" + (ep.getFirst().partitionIx + hR_Offset));
			sb.append("_" + (ep.getFirst().clusterIx + hR_Offset));
			sb.append("-");
			sb.append(ep.getSecond().partitionIx + hR_Offset);
			sb.append("_" + (ep.getSecond().clusterIx + hR_Offset));
			sb.append("," + String.format("%2.2f", e.similarity) + "] ");
		}
		println(sb.toString());
	}

	private static void printFormatted(final double[] values, final String variableName) {
		print(String.valueOf(variableName) + "=[");
		for (int i = 1; i < values.length; ++i) {
			print(String.valueOf(values[i]) + " ");
		}
		println("]");
		println();
	}

	private static void println() {
		if (DiclensMST.logout) {
			System.out.println();
		}
	}

	private static void print(final Object output) {
		if (DiclensMST.logout) {
			System.out.print(output);
		}
	}

	private static void println(final Object output) {
		if (DiclensMST.logout) {
			System.out.println(output);
		}
	}

	private void normalize(final double[] values) {
		Normalize.usingMinMaxMethod(values);
	}

	private void internalNormalize(final double[] values) {
		final double divider = this.partitions.size();
		for (int i = 0; i < values.length; ++i) {
			values[i] /= divider;
		}
	}

	private static class SimilarityReverseComparator implements Comparator<Edge>, Serializable {
		private static final long serialVersionUID = -5255261862892605705L;

		@Override
		public int compare(final Edge o1, final Edge o2) {
			if (o1.similarity > o2.similarity) {
				return -1;
			}
			if (o1.similarity < o2.similarity) {
				return 1;
			}
			return 0;
		}
	}

	public static class SimilarityToDissimilarity implements Function<Edge, Double> {
		private final double max_similarity;

		public SimilarityToDissimilarity(final Collection<Edge> edges) {
			double max = Double.MIN_VALUE;
			for (final Edge edge : edges) {
				if (max < edge.similarity) {
					max = edge.similarity;
				}
			}
			this.max_similarity = max;
		}

		@Override
		public @Nullable Double apply(@Nullable Edge input) {
			if (input.similarity > this.max_similarity) {
				throw new IllegalStateException(
						"similarity is greater than max_similarity (" + this.max_similarity + ")");
			}
			return this.max_similarity - input.similarity + 1.0;
		}
	}

	static class Edge {
		private static int auto_ID;
		private final double similarity;
		private final int id;

		static {
			Edge.auto_ID = 1;
		}

		Edge(final double similarity) {
			this.id = Edge.auto_ID++;
			this.similarity = similarity;
		}

		public double similarity() {
			return this.similarity;
		}

		@Override
		public int hashCode() {
			return this.id;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (this.getClass() != obj.getClass()) {
				return false;
			}
			final Edge other = (Edge) obj;
			return this.id == other.id;
		}

		@Override
		public String toString() {
			return "Edge [id=" + this.id + ", similarity=" + this.similarity + "]";
		}
	}

	static class Cluster {
		int partitionIx;
		int clusterIx;
		BitSet contents;

		public Cluster(final int partitionIx, final int clusterIx, final BitSet cluster) {
			this.partitionIx = partitionIx;
			this.clusterIx = clusterIx;
			this.contents = cluster;
		}

		@Override
		public String toString() {
			return "Cluster[" + this.partitionIx + "," + this.clusterIx + "] -> " + this.contents;
		}
	}
}
