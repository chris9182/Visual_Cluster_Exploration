package clusterproject.clustergenerator.program.Clustering.Parameters;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Parameter implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -2644835073796978627L;

	private final String name;

	private final Map<String, Object> parameters = new HashMap<String, Object>();

	public Parameter(String name) {
		this.name = name;
	}

	public void addParameter(String name, Object parameter) {
		parameters.put(name, parameter);
	}

	public String getName() {
		return name;
	}

	public String getInfoString() {
		String returnval = getName() + ": ";
		for (final String param : parameters.keySet()) {
			returnval += param + ": " + parameters.get(param) + " ";

		}
		return returnval.trim();
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

}
