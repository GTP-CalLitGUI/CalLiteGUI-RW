package gov.ca.water.calgui.presentation;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComponent;

import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

import gov.ca.water.calgui.bus_delegate.IApplyDynamicConDele;
import gov.ca.water.calgui.bus_delegate.impl.ApplyDynamicConDeleImp;
import gov.ca.water.calgui.bus_service.IResultSvc;
import gov.ca.water.calgui.bus_service.impl.ResultSvcImpl;
import gov.ca.water.calgui.bus_service.impl.XMLParsingSvcImpl;
import gov.ca.water.calgui.tech_service.IAuditSvc;
import gov.ca.water.calgui.tech_service.impl.AuditSvcImpl;

public class GlobalItemListener implements ItemListener {

	private static Logger log = Logger.getLogger(GlobalItemListener.class.getName());
	private IApplyDynamicConDele applyDynamicConDele;
	private SwingEngine swingEngine = XMLParsingSvcImpl.getXMLParsingSvcImplInstance().getSwingEngine();
	private IAuditSvc auditSvc = AuditSvcImpl.getAuditSvcImplInstance();
	private IResultSvc resultSvc = ResultSvcImpl.getResultSvcImplInstance();

	public GlobalItemListener() {
		this.applyDynamicConDele = new ApplyDynamicConDeleImp();
	}

	@Override
	public void itemStateChanged(ItemEvent ie) {
		if (resultSvc.isCLSFileLoading()) {
			return;
		}
		String itemName = ((JComponent) ie.getItem()).getName();
		log.debug(itemName);
		boolean isSelected = ie.getStateChange() == ItemEvent.SELECTED;
		if (swingEngine.find(itemName).isEnabled()) {
			applyDynamicConDele.applyDynamicControl(itemName, isSelected, ((JComponent) ie.getItem()).isEnabled());
		}
		auditSvc.addAudit(itemName, String.valueOf(!isSelected), String.valueOf(isSelected));

	}

}
