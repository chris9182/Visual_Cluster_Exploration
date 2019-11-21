package clusterproject.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusterInformation {
	private List<Integer> originalClusterIDs;
	private List<Integer> clusterIDs;
	Map<Integer, Integer> idMap = null;
	private final PointContainer pointContainer;
	private int noiseIndex = -1;

	public ClusterInformation(PointContainer pointContainer) {
		this.pointContainer = pointContainer;
	}

	public void setUpClusters() {
		final int size = pointContainer.getPointCount();
		clusterIDs = new ArrayList<Integer>(size);
		originalClusterIDs = new ArrayList<Integer>(size);
	}

	public void addClusterID(Integer id) {
		clusterIDs.add(id);
		originalClusterIDs.add(id);
	}

	public boolean hasClusters() {
		return clusterIDs != null && clusterIDs.size() == pointContainer.getPointCount() && originalClusterIDs != null
				&& originalClusterIDs.size() == pointContainer.getPointCount();
	}

	public List<Integer> getClusterIDs() {
		return clusterIDs;
	}

	public void setClusterIDs(List<Integer> clusterIDs) {
		this.clusterIDs = clusterIDs;

	}

	public void setAllClusterIDs(ArrayList<Integer> clusterIDs) {
		this.clusterIDs = clusterIDs;
		this.originalClusterIDs = clusterIDs;

	}

	public List<Integer> getOriginalClusterIDs() {
		return originalClusterIDs;
	}

	public void setOriginalClusterID(List<Integer> originalClusterIDs) {
		this.originalClusterIDs = originalClusterIDs;

	}

	public void setIDMap(Map<Integer, Integer> idMap) {
		this.idMap = idMap;

	}

	public Map<Integer, Integer> getIDMap() {
		return idMap;
	}

	public void copyIn(ClusterInformation clusterInformation) {
		originalClusterIDs.addAll(clusterInformation.getOriginalClusterIDs());
		clusterIDs.addAll(clusterInformation.getClusterIDs());
		if (clusterInformation.getIDMap() != null) {
			idMap = new HashMap<Integer, Integer>();
			idMap.putAll(clusterInformation.getIDMap());
		}
	}

	public int getNoiseIndex() {
		return noiseIndex;
	}

	public int getCurrentNoiseIndex() {
		if (idMap != null && idMap.containsKey(noiseIndex))
			return idMap.get(noiseIndex);
		return noiseIndex;
	}

	public void setNoiseIndex(int index) {
		this.noiseIndex = index;
	}

}
