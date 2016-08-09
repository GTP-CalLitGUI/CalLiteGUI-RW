package gov.ca.water.calgui.bo;

/**
 * This will hold the intermediate information used for building the Scenario Display.
 */
public class ScenarioDisplayBO {
	String componentText;
	String componentParents;
	String componentValue;

	public ScenarioDisplayBO(String componentText, String componentParents, String componentValue) {
		this.componentText = componentText;
		this.componentParents = componentParents;
		this.componentValue = componentValue;
	}

	public String getComponentText() {
		return componentText;
	}

	public void setComponentText(String componentText) {
		this.componentText = componentText;
	}

	public String getComponentParents() {
		return componentParents;
	}

	public void setComponentParents(String componentParents) {
		this.componentParents = componentParents;
	}

	public String getComponentValue() {
		return componentValue;
	}

	public void setComponentValue(String componentValue) {
		this.componentValue = componentValue;
	}
}
