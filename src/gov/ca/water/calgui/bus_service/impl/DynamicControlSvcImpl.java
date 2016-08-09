package gov.ca.water.calgui.bus_service.impl;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JCheckBox;
import javax.swing.JRadioButton;

import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

import gov.ca.water.calgui.bo.CalLiteGUIException;
import gov.ca.water.calgui.bo.TriggerBO;
import gov.ca.water.calgui.bus_service.IDynamicControlSvc;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.tech_service.IErrorHandlingSvc;
import gov.ca.water.calgui.tech_service.IFileSystemSvc;
import gov.ca.water.calgui.tech_service.impl.ErrorHandlingSvcImpl;
import gov.ca.water.calgui.tech_service.impl.FileSystemSvcImpl;

/**
 * This is the class for applying the dynamic controls.
 */
public class DynamicControlSvcImpl implements IDynamicControlSvc {
	private static Logger log = Logger.getLogger(DynamicControlSvcImpl.class.getName());
	private IFileSystemSvc fileSystemSvc;
	private List<TriggerBO> triggerListForEnableDisable;
	private Map<String, List<TriggerBO>> triggerMapForEnableDisable;
	private List<TriggerBO> triggerListForCheckUncheck;
	private Map<String, List<TriggerBO>> triggerMapForCheckUncheck;
	private static IDynamicControlSvc dynamicControlSvc;
	private IErrorHandlingSvc errorHandlingSvc;

	/**
	 * This method is for implementing the singleton.
	 *
	 * @return
	 */
	public static IDynamicControlSvc getDynamicControlSvcImplInstance() {
		if (dynamicControlSvc == null)
			dynamicControlSvc = new DynamicControlSvcImpl();
		return dynamicControlSvc;
	}

	/*
	 * This will load TriggerForDymanicSelection.table and TriggerForDynamicDisplay.table files into memory.
	 */
	private DynamicControlSvcImpl() {
		log.info("Building DynamicControlSvcImpl Object.");
		this.fileSystemSvc = new FileSystemSvcImpl();
		this.triggerListForEnableDisable = new ArrayList<TriggerBO>();
		this.triggerListForCheckUncheck = new ArrayList<TriggerBO>();
		this.triggerMapForEnableDisable = new HashMap<String, List<TriggerBO>>();
		this.triggerMapForCheckUncheck = new HashMap<String, List<TriggerBO>>();
		this.errorHandlingSvc = new ErrorHandlingSvcImpl();
		loadTriggerTableForEnableDisable();
		loadTriggerTableForCheckUncheck();
	}

	/**
	 * This method will load the TriggerForDynamicDisplay.table file into the memory. The data really is if one component is
	 * selected then this will do two process.
	 *
	 * <pre>
	 *  1. on and off which will enable and disable based on the data.
	 *  2. show and hide which will make the component visible and not visible.
	 * </pre>
	 */
	private void loadTriggerTableForEnableDisable() {
		List<String> triggerStrListForED;
		Set<String> keys = new HashSet<String>();
		String errorStr = "";
		try {
			triggerStrListForED = fileSystemSvc.getFileDataWithoutComment(Constant.TRIGGER_ENABLE_DISABLE_FILENAME, true);
			for (String triggerStrForED : triggerStrListForED) {
				errorStr = triggerStrForED;
				String[] list = triggerStrForED.split(Constant.DELIMITER);
				keys.add(list[0]);
				TriggerBO triggerBO = new TriggerBO(list[0], list[1], list[2], list[3]);
				triggerListForEnableDisable.add(triggerBO);
			}
		} catch (ArrayIndexOutOfBoundsException ex) {
			String errorMessage = "In file \"" + Constant.TRIGGER_ENABLE_DISABLE_FILENAME + "\" has a corrupted data at line \""
			        + errorStr + "\"" + Constant.NEW_LINE + "The column number which the data is corrupted is " + ex.getMessage();
			log.error(errorMessage, ex);
			errorHandlingSvc.displayErrorMessageBeforeTheUI(new CalLiteGUIException(errorMessage, ex, true));
		} catch (CalLiteGUIException ex) {
			log.error(ex.getMessage(), ex);
			errorHandlingSvc.displayErrorMessageBeforeTheUI(ex);
		}
		keys.stream().forEach((key) -> {
			List<TriggerBO> tempList = getEnableDisableList(key);
			for (TriggerBO triggerBO2 : tempList) {
				String mapKey = triggerBO2.getTriggerGuiId() + Constant.DASH + triggerBO2.getTriggerAction();
				if (this.triggerMapForEnableDisable.containsKey(mapKey)) {
					this.triggerMapForEnableDisable.get(mapKey).add(triggerBO2);
				} else {
					List<TriggerBO> temp = new ArrayList<TriggerBO>();
					temp.add(triggerBO2);
					this.triggerMapForEnableDisable.put(mapKey, temp);
				}
			}
		});
	}

	/**
	 * This method will load the TriggerForDymanicSelection.table file into the memory. The data really is if one JCheckBox and
	 * JRadioButton is selected then all the other AFFECTED once will be selected or de-selected based on the data.
	 */
	private void loadTriggerTableForCheckUncheck() {
		List<String> triggerStrListForCU;
		Set<String> keys = new HashSet<String>();
		String errorStr = "";
		try {
			triggerStrListForCU = fileSystemSvc.getFileDataWithoutComment(Constant.TRIGGER_CHECK_UNCHECK_FILENAME, true);
			for (String triggerStrForCU : triggerStrListForCU) {
				errorStr = triggerStrForCU;
				String[] list = triggerStrForCU.split(Constant.DELIMITER);
				keys.add(list[0]);
				TriggerBO triggerBO = new TriggerBO(list[0], list[1], list[2], list[3]);
				triggerListForCheckUncheck.add(triggerBO);
			}
		} catch (ArrayIndexOutOfBoundsException ex) {
			String errorMessage = "In file \"" + Constant.TRIGGER_CHECK_UNCHECK_FILENAME + "\" has a corrupted data at line \""
			        + errorStr + "\"" + Constant.NEW_LINE + "The column number which the data is corrupted is " + ex.getMessage();
			log.error(errorMessage, ex);
			errorHandlingSvc.displayErrorMessageBeforeTheUI(new CalLiteGUIException(errorMessage, ex, true));
		} catch (CalLiteGUIException ex) {
			log.error(ex.getMessage(), ex);
			errorHandlingSvc.displayErrorMessageBeforeTheUI(ex);
		}
		keys.stream().forEach((key) -> {
			List<TriggerBO> tempList = getCheckUncheckList(key);
			for (TriggerBO triggerBO2 : tempList) {
				String mapKey = triggerBO2.getTriggerGuiId() + Constant.DASH + triggerBO2.getTriggerAction();
				if (this.triggerMapForCheckUncheck.containsKey(mapKey)) {
					this.triggerMapForCheckUncheck.get(mapKey).add(triggerBO2);
				} else {
					List<TriggerBO> temp = new ArrayList<TriggerBO>();
					temp.add(triggerBO2);
					this.triggerMapForCheckUncheck.put(mapKey, temp);
				}
			}
		});
	}

	/**
	 * This will return the list of {@link TriggerBO} objects which match with the {@code triggerId} and for enable and disable
	 * purpose.
	 *
	 * @param triggerId
	 * @return
	 */
	private List<TriggerBO> getEnableDisableList(String triggerId) {
		return triggerListForEnableDisable.stream().filter(t -> t.getTriggerGuiId().equals(triggerId)
		        && (t.getTriggerAction().equals("on") || t.getTriggerAction().equals("off"))).collect(Collectors.toList());
	}

	/**
	 * This will return the list of {@link TriggerBO} objects which match with the {@code triggerId}.
	 *
	 * @param triggerId
	 * @return
	 */
	private List<TriggerBO> getCheckUncheckList(String triggerId) {
		return triggerListForCheckUncheck.stream().filter(t -> t.getTriggerGuiId().equals(triggerId)).collect(Collectors.toList());
	}

	@Override
	public void doDynamicControl(String itemName, boolean isSelected, boolean isEnabled, SwingEngine swingEngine) {
		List<TriggerBO> listForEnableDisable = this.triggerMapForEnableDisable
		        .get(itemName + Constant.DASH + booleanToStringOnOff(isSelected));
		if (listForEnableDisable != null) {
			listForEnableDisable.forEach((triggerBO) -> {
				if (triggerBO.getAffectdeAction().equalsIgnoreCase("show")
			            || triggerBO.getAffectdeAction().equalsIgnoreCase("hide")) {
					toggleVisComponentAndChildren(swingEngine.find(triggerBO.getAffectdeGuiId()),
			                stringShowHideToBoolean(triggerBO.getAffectdeAction()));
				} else {
					if (isEnabled) {
						toggleEnComponentAndChildren(swingEngine.find(triggerBO.getAffectdeGuiId()),
			                    stringOnOffToBoolean(triggerBO.getAffectdeAction()));
					}
				}
			});
		}
		List<TriggerBO> listForCheckUncheck = this.triggerMapForCheckUncheck
		        .get(itemName + Constant.DASH + Boolean.toString(isSelected).toUpperCase());
		if (listForCheckUncheck != null) {
			listForCheckUncheck.forEach((triggerBO) -> {
				setComponentSelected(swingEngine.find(triggerBO.getAffectdeGuiId()),
			            Boolean.valueOf(triggerBO.getAffectdeAction()));
			});
		}
	}

	/**
	 * This method will only select and deselect the JCheckBox and JRadioButton.
	 *
	 * @param component
	 * @param isSelect
	 */
	private void setComponentSelected(Component component, boolean isSelect) {
		if (component instanceof JCheckBox) {
			((JCheckBox) component).setSelected(isSelect);
		} else if (component instanceof JRadioButton) {
			((JRadioButton) component).setSelected(isSelect);
		}
	}

	@Override
	public void toggleVisComponentAndChildren(Component component, Boolean isVisible) {
		component.setVisible(isVisible);
		for (Component child : ((Container) component).getComponents()) {
			toggleVisComponentAndChildren(child, isVisible);
		}
	}

	@Override
	public void toggleEnComponentAndChildren(Component component, boolean isEnable) {
		component.setEnabled(isEnable);
		for (Component child : ((Container) component).getComponents()) {
			toggleEnComponentAndChildren(child, isEnable);
		}
	}

	/**
	 * This will convert boolean to on and off string and return it. true -> on
	 *
	 * @param value
	 * @return
	 */
	private String booleanToStringOnOff(boolean value) {
		return value ? "on" : "off";
	}

	/**
	 * This will convert the string show and hide to boolean value and return it. show -> true
	 *
	 * @param value
	 * @return
	 */
	private boolean stringShowHideToBoolean(String value) {
		return value.equals("show");
	}

	/**
	 * This will convert the on and off string to boolean value and return it. on -> true
	 *
	 * @param value
	 * @return
	 */
	private boolean stringOnOffToBoolean(String value) {
		return value.equals("on");
	}

	@Override
	public TriggerBO getTriggerBOById(String id) {
		return this.triggerListForEnableDisable.stream().filter((triggerBo) -> {
			return triggerBo.getTriggerGuiId().equals(id) && triggerBo.getTriggerAction().equals("on");
		}).findAny().get();
	}
}
