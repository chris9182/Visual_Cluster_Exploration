package clusterproject.util;

import net.sf.javaml.core.DenseInstance;

public class ReferencedDenseInstance extends DenseInstance {

	private static final long serialVersionUID = 5092429139540910807L;
	final double[] attributeRef;

	public ReferencedDenseInstance(double[] att) {
		super(att);
		attributeRef = att;
	}

	public double[] getPoint() {
		return attributeRef;
	}

}
