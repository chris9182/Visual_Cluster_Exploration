package clusterproject.data;

import java.util.LinkedHashSet;
import java.util.Set;

public class MetaInformation {
	private LinkedHashSet<Integer> highlighted = new LinkedHashSet<Integer>();
	private Set<Integer> filteredResults;
	private int groundTruth = -1;

	public LinkedHashSet<Integer> getHighlighted() {
		return highlighted;
	}

	public void setHighlighted(LinkedHashSet<Integer> highlighted2) {
		this.highlighted = highlighted2;
	}

	public void setFilteredResults(Set<Integer> filteredResults) {
		this.filteredResults = filteredResults;

	}

	public Set<Integer> getFilteredIndexes() {
		return filteredResults;
	}

	public void setGroundTruth(int groundTruth) {
		this.groundTruth = groundTruth;

	}

	public int getGroundTruth() {
		return groundTruth;
	}

}
