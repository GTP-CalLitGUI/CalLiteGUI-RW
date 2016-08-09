package gov.ca.water.calgui.tech_service;

/**
 * This is the interface for Audit records.
 */
public interface IAuditSvc {
	/**
	 * This will add add the value to the Audit record.
	 *
	 * @param controlId
	 *            The id of the control.
	 * @param oldValue
	 *            The old value of the control.
	 * @param newValue
	 *            The new value of the control.
	 */
	public void addAudit(String controlId, String oldValue, String newValue);

	/**
	 * This will remove all the Audit records.
	 */
	public void clearAudit();

	/**
	 * This will tell whether the Audit is empty or not.
	 *
	 * @return
	 */
	public boolean hasValues();
}
