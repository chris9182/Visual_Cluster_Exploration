package clusterproject.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClusterInformation {
	private List<Integer> originalClusterIDs;
	private List<Integer> clusterIDs;
	Map<Integer, Integer> idMap = null;
	private PointContainer pointContainer;

	public ClusterInformation(PointContainer pointContainer) {
		this.pointContainer = pointContainer;
	}

	public void setUpClusters() {
		clusterIDs = new ArrayList<Integer>();
		originalClusterIDs = new ArrayList<Integer>();
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

}
