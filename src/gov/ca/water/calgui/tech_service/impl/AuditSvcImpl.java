package gov.ca.water.calgui.tech_service.impl;

import java.util.ArrayList;
import java.util.List;

import gov.ca.water.calgui.bo.AuditBO;
import gov.ca.water.calgui.tech_service.IAuditSvc;

/**
 * This is the class for Audit records.
 */
public class AuditSvcImpl implements IAuditSvc {
	private List<AuditBO> auditRecord;
	private static IAuditSvc auditSvc;

	/**
	 * This method is for implementing the singleton.
	 *
	 * @return
	 */
	public static IAuditSvc getAuditSvcImplInstance() {
		if (auditSvc == null)
			auditSvc = new AuditSvcImpl();
		return auditSvc;
	}

	private AuditSvcImpl() {
		auditRecord = new ArrayList<AuditBO>();
	}

	@Override
	public void addAudit(String controlId, String oldValue, String newValue) {
		this.auditRecord.add(new AuditBO(controlId, oldValue, newValue));
	}

	@Override
	public void clearAudit() {
		this.auditRecord.removeAll(this.auditRecord);
	}

	@Override
	public boolean hasValues() {
		return !this.auditRecord.isEmpty();
	}
}
