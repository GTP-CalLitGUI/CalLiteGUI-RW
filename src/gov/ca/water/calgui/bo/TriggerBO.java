package gov.ca.water.calgui.bo;

/**
 * This is used to hold the information of Trigger*.table in memory.
 */
public class TriggerBO {
	private String triggerGuiId;
	private String triggerAction;
	private String affectdeGuiId;
	private String affectdeAction;

	public TriggerBO(String triggerGuiId, String triggerAction, String affectdeGuiId, String affectdeAction) {
		this.triggerGuiId = triggerGuiId;
		this.triggerAction = triggerAction;
		this.affectdeGuiId = affectdeGuiId;
		this.affectdeAction = affectdeAction;
	}

	public String getTriggerGuiId() {
		return triggerGuiId;
	}

	public void setTriggerGuiId(String triggerGuiId) {
		this.triggerGuiId = triggerGuiId;
	}

	public String getTriggerAction() {
		return triggerAction;
	}

	public void setTriggerAction(String triggerAction) {
		this.triggerAction = triggerAction;
	}

	public String getAffectdeGuiId() {
		return affectdeGuiId;
	}

	public void setAffectdeGuiId(String affectdeGuiId) {
		this.affectdeGuiId = affectdeGuiId;
	}

	public String getAffectdeAction() {
		return affectdeAction;
	}

	public void setAffectdeAction(String affectdeAction) {
		this.affectdeAction = affectdeAction;
	}
}
