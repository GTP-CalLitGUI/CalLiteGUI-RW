package gov.ca.water.calgui.bo;

/**
 * This is holding the information for Audit.
 */
public class AuditBO {
	private String controlId;
	private String oldValue;
	private String newValue;

	public AuditBO(String controlId, String oldValue, String newValue) {
		super();
		this.controlId = controlId;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public String getControlId() {
		return controlId;
	}

	public void setControlId(String controlId) {
		this.controlId = controlId;
	}

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}
}
