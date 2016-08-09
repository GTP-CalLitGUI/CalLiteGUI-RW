package gov.ca.water.calgui.bus_service.impl;

import java.awt.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JCheckBox;
import javax.swing.JTable;

import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

import gov.ca.water.calgui.bo.CalLiteGUIException;
import gov.ca.water.calgui.bus_service.IXMLParsingSvc;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.presentation.NumericTextField;
import gov.ca.water.calgui.tech_service.IErrorHandlingSvc;
import gov.ca.water.calgui.tech_service.IFileSystemSvc;
import gov.ca.water.calgui.tech_service.impl.ErrorHandlingSvcImpl;
import gov.ca.water.calgui.tech_service.impl.FileSystemSvcImpl;

public class XMLParsingSvcImpl implements IXMLParsingSvc {

	private static Logger log = Logger.getLogger(XMLParsingSvcImpl.class.getName());
	private SwingEngine swingEngine;
	private static IXMLParsingSvc xmlParsingSvc;
	private IFileSystemSvc fileSystemSvc;
	private IErrorHandlingSvc errorHandlingSvc;
	private Map<String, String> compNameIdMap;
	private List<String> newUserDefinedTables;

	/**
	 * This method is for implementing the singleton.
	 *
	 * @return
	 */
	public static IXMLParsingSvc getXMLParsingSvcImplInstance() {
		if (xmlParsingSvc == null) {
			xmlParsingSvc = new XMLParsingSvcImpl();
		}
		return xmlParsingSvc;
	}

	/*
	 * In this we 1st build the SwingEngine from the Gui.xml file. We build the newUserDefinedTables list by getting all the JTable
	 * component id and exclude the default ones.
	 */
	private XMLParsingSvcImpl() {
		log.debug("Building XMLParsingSvcImpl Object.");
		this.errorHandlingSvc = new ErrorHandlingSvcImpl();
		this.fileSystemSvc = new FileSystemSvcImpl();
		this.compNameIdMap = new HashMap<String, String>();
		this.swingEngine = new SwingEngine();
		swingEngine.getTaglib().registerTag("numtextfield", NumericTextField.class);
		try {
			swingEngine.render(fileSystemSvc.getXMLDocument());
		} catch (CalLiteGUIException ex) {
			errorHandlingSvc.displayErrorMessageBeforeTheUI(ex);
		} catch (Exception ex) {
			errorHandlingSvc.displayErrorMessageBeforeTheUI(
			        new CalLiteGUIException("This is from Swing Engine : " + Constant.NEW_LINE + ex.getMessage(), ex, true));
		}
		Set<String> compIds = this.getIdFromXML();
		compIds.stream().forEach((compId) -> {
			Component component = this.swingEngine.find(compId);
			if (component instanceof JCheckBox) {
				this.compNameIdMap.put(((JCheckBox) component).getText(), compId);
			}
		});
		this.newUserDefinedTables = compIds.stream().filter((compId) -> swingEngine.find(compId) instanceof JTable)
		        .collect(Collectors.toList());
		this.newUserDefinedTables.remove("tblRegValues");
		this.newUserDefinedTables.remove("tblOpValues");
		this.newUserDefinedTables.remove("tblBanks");
		this.newUserDefinedTables.remove("tblIF3");
		this.newUserDefinedTables.remove("tblIF2");
		this.newUserDefinedTables.remove("tblIF1");
	}

	@Override
	public String getcompIdfromName(String name) {
		return this.compNameIdMap.get(name);
	}

	@Override
	public SwingEngine getSwingEngine() {
		return this.swingEngine;
	}

	@Override
	public Set<String> getIdFromXML() {
		Map<String, Object> map = this.swingEngine.getIdMap();
		Set<String> swt = map.keySet();
		swt.stream().forEach(key -> log.debug(key + " " + map.get(key).getClass().getName()));
		return map.keySet().stream().filter((id) -> {
			Object component = map.get(id);
			if (component instanceof Component) {
				return ((Component) component).isVisible();
			}
			return false;
		}).collect(Collectors.toSet());
	}

	@Override
	public List<String> getNewUserDefinedTables() {
		return newUserDefinedTables;
	}
}
