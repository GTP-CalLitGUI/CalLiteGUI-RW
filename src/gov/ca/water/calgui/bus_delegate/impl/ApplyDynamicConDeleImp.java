package gov.ca.water.calgui.bus_delegate.impl;

import java.awt.Component;
import java.awt.Container;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

import gov.ca.water.calgui.bo.CalLiteGUIException;
import gov.ca.water.calgui.bo.GuiLinks4BO;
import gov.ca.water.calgui.bo.SeedDataBO;
import gov.ca.water.calgui.bus_delegate.IApplyDynamicConDele;
import gov.ca.water.calgui.bus_service.IDynamicControlSvc;
import gov.ca.water.calgui.bus_service.IResultSvc;
import gov.ca.water.calgui.bus_service.ISeedDataSvc;
import gov.ca.water.calgui.bus_service.ITableSvc;
import gov.ca.water.calgui.bus_service.IXMLParsingSvc;
import gov.ca.water.calgui.bus_service.impl.DynamicControlSvcImpl;
import gov.ca.water.calgui.bus_service.impl.ResultSvcImpl;
import gov.ca.water.calgui.bus_service.impl.SeedDataSvcImpl;
import gov.ca.water.calgui.bus_service.impl.TableSvcImpl;
import gov.ca.water.calgui.bus_service.impl.XMLParsingSvcImpl;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.presentation.DataTableModle;
import gov.ca.water.calgui.tech_service.IErrorHandlingSvc;
import gov.ca.water.calgui.tech_service.impl.ErrorHandlingSvcImpl;

public class ApplyDynamicConDeleImp implements IApplyDynamicConDele {
	private static Logger log = Logger.getLogger(ApplyDynamicConDeleImp.class.getName());
	private ISeedDataSvc seedDataSvc = SeedDataSvcImpl.getSeedDataSvcImplInstance();
	private IDynamicControlSvc dynamicControlSvc = DynamicControlSvcImpl.getDynamicControlSvcImplInstance();
	private ITableSvc tableSvc = TableSvcImpl.getTableSvcImplInstance(null);
	private IErrorHandlingSvc errorHandlingSvc = new ErrorHandlingSvcImpl();
	private IXMLParsingSvc xmlParsingSvc = XMLParsingSvcImpl.getXMLParsingSvcImplInstance();
	private SwingEngine swingEngine = xmlParsingSvc.getSwingEngine();
	private IResultSvc resultSvc = ResultSvcImpl.getResultSvcImplInstance();

	@Override
	public void applyDynamicControl(String itemName, boolean isSelected, boolean isEnabled) {
		if (itemName.equalsIgnoreCase("run_rdbD1485") || itemName.equalsIgnoreCase("run_rdbD1641")
		        || itemName.equalsIgnoreCase("run_rdbBO")) {
			if (isSelected)
				hydroclimate();
		}
		if (seedDataSvc.hasSeedDataObject(itemName) && isSelected) {
			if (seedDataSvc.getObjByGuiId(itemName).getDashboard().equals(Constant.HYDROCLIMATE_TABNAME)) {
				if (itemName.equalsIgnoreCase("hyd_rdb2005") || itemName.equalsIgnoreCase("hyd_rdb2030")
				        || itemName.equalsIgnoreCase("hyd_rdbCCEL") || itemName.equalsIgnoreCase("hyd_rdbCCLL")) {
					JRadioButton regrdb = (JRadioButton) swingEngine.find("rdbRegQS_UD");
					if (!regrdb.isSelected()) {
						if (itemName.equals("hyd_rdb2005")) {
							regrdb = (JRadioButton) swingEngine.find("SJR_interim");
							regrdb.setSelected(true);
						} else {
							regrdb = (JRadioButton) swingEngine.find("SJR_full");
							regrdb.setSelected(true);
						}
						dynamicControlSvc.toggleEnComponentAndChildren(swingEngine.find("regpan2c"), false);
					} else {
						dynamicControlSvc.toggleEnComponentAndChildren(swingEngine.find("regpan2c"), true);
					}
				}
				hydroclimate();
			}
		}
		try {
			regulations(itemName, isSelected);
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CalLiteGUIException ex) {
			errorHandlingSvc.businessErrorHandler((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME), ex);
		}
		dynamicControlSvc.doDynamicControl(itemName, isSelected, isEnabled, swingEngine);
	}

	// TODO: we should implement the pop up box.
	private void hydroclimate() {
		String lookup = "error";
		String label = "";
		if (((JRadioButton) swingEngine.find("run_rdbD1641")).isSelected()) {
			lookup = "1";
			label = "D1641";
		} else if (((JRadioButton) swingEngine.find("run_rdbBO")).isSelected()) {
			lookup = "2";
			label = "BO";
		} else if (((JRadioButton) swingEngine.find("run_rdbD1485")).isSelected()) {
			lookup = "3";
			label = "D1485";
		}
		if (((JRadioButton) swingEngine.find("hyd_rdb2005")).isSelected()) {
			lookup = lookup + "110";
			label = label + "-Current LOD";
		} else if (((JRadioButton) swingEngine.find("hyd_rdb2030")).isSelected()) {
			lookup = lookup + "210";
			label = label + "-Future LOD";
		} else {
			if (((JRadioButton) swingEngine.find("hyd_rdbCCEL")).isSelected()) {
				lookup = lookup + "31";
				label = label + "-Early CC";
			} else if (((JRadioButton) swingEngine.find("hyd_rdbCCLL")).isSelected()) {
				lookup = lookup + "41";
				label = label + "-Late CC";
			}
			if (((JRadioButton) swingEngine.find("hyd_ckb1")).isSelected()) {
				lookup = lookup + "1";
				label = label + "1";
			} else if (((JRadioButton) swingEngine.find("hyd_ckb2")).isSelected()) {
				lookup = lookup + "2";
				label = label + "2";
			} else if (((JRadioButton) swingEngine.find("hyd_ckb3")).isSelected()) {
				lookup = lookup + "3";
				label = label + "3";
			} else if (((JRadioButton) swingEngine.find("hyd_ckb4")).isSelected()) {
				lookup = lookup + "4";
				label = label + "4";
			} else if (((JRadioButton) swingEngine.find("hyd_ckb5")).isSelected()) {
				lookup = lookup + "5";
				label = label + "5";
			} else {
				lookup = lookup + "0";
				label = label + "0";
			}
		}
		GuiLinks4BO guiLinks4BO = seedDataSvc.getObjByRunBasisLodCcprojCcmodelIds(lookup);
		((JTextField) swingEngine.find("hyd_DSS_SV")).setText(guiLinks4BO.getSvFile());
		((JTextField) swingEngine.find("hyd_DSS_SV_F")).setText(guiLinks4BO.getfPartSV1());
		((JTextField) swingEngine.find("hyd_DSS_Init")).setText(guiLinks4BO.getInitFile());
		((JTextField) swingEngine.find("hyd_DSS_Init_F")).setText(guiLinks4BO.getfPartSV2());
		tableSvc.setWsidiFileSuffix(guiLinks4BO.getLookup());
		// Change WSI/DI Status Label
		JLabel lab = (JLabel) swingEngine.find("op_WSIDI_Status");
		String selHyd = label;
		lab.setText(selHyd + " (Unedited)");
	}

	private void regulations(String itemName, boolean isSelected) throws CalLiteGUIException, CloneNotSupportedException {
		List<SeedDataBO> seedDataList = seedDataSvc.getRegulationsTabData();
		int[] regFlags = resultSvc.getRegulationoptions();
		String tableName = "";
		String optionName = "";
		try {
			if (isSelected) {
				if (itemName.equals(Constant.QUICK_SELECT_RB_D1485)) {
					for (SeedDataBO seedDataBO : seedDataList) {
						if (seedDataBO.getD1485().equals(Constant.N_A)) {
							regFlags[Integer.parseInt(seedDataBO.getRegID())] = 3;
						} else if (seedDataBO.getD1641().equals(Constant.N_A)) {
							regFlags[Integer.parseInt(seedDataBO.getRegID())] = 1;
						} else if (seedDataBO.getUserDefined().equals(Constant.N_A)) {
							regFlags[Integer.parseInt(seedDataBO.getRegID())] = 4;
						}
						if (!seedDataBO.getDataTables().equals(Constant.N_A)) {
							resultSvc.removeUserDefinedTable(seedDataBO.getDataTables());
						}
					}
				} else if (itemName.equals(Constant.QUICK_SELECT_RB_D1641) || itemName.equals(Constant.QUICK_SELECT_RB_D1641_BO)) {
					for (SeedDataBO seedDataBO : seedDataList) {
						if (seedDataBO.getD1641().equals(Constant.N_A)) {
							regFlags[Integer.parseInt(seedDataBO.getRegID())] = 1;
						} else if (seedDataBO.getD1485().equals(Constant.N_A)) {
							regFlags[Integer.parseInt(seedDataBO.getRegID())] = 3;
						} else if (seedDataBO.getUserDefined().equals(Constant.N_A)) {
							regFlags[Integer.parseInt(seedDataBO.getRegID())] = 4;
						}
						if (!seedDataBO.getDataTables().equals(Constant.N_A)) {
							resultSvc.removeUserDefinedTable(seedDataBO.getDataTables());
						}
					}
				} else if (itemName.startsWith("ckbReg")) {
					SeedDataBO seedDataBO = seedDataSvc.getObjByGuiId(itemName);
					makeRBVisible(seedDataBO);
					String panelId = dynamicControlSvc.getTriggerBOById(itemName).getAffectdeGuiId();
					String guiTableName = getTableNameFromTheConponent(swingEngine.find(panelId));
					((TitledBorder) ((JPanel) this.swingEngine.find(panelId)).getBorder())
					        .setTitle(((JCheckBox) this.swingEngine.find(itemName)).getText());
					((JPanel) this.swingEngine.find(panelId)).repaint();
					Component scrRegValues = (this.swingEngine.find("scrRegValues"));
					int regId = Integer.parseInt(seedDataBO.getRegID());
					if (regFlags[regId] == 1) {
						((JRadioButton) swingEngine.find(Constant.PANEL_RB_D1641)).setSelected(true);
						optionName = Constant.D1641;
					} else if (regFlags[regId] == 3) {
						((JRadioButton) swingEngine.find(Constant.PANEL_RB_D1485)).setSelected(true);
						optionName = Constant.D1485;
					} else if (regFlags[regId] == 2) {
						((JRadioButton) swingEngine.find(Constant.PANEL_RB_USER_DEFIND)).setSelected(true);
						optionName = Constant.USER_DEFINED;
					} else if (regFlags[regId] == 4) {
						optionName = Constant.USER_DEFINED;
					}
					if (!seedDataBO.getDataTables().equals(Constant.N_A)) {
						tableName = seedDataBO.getDataTables();
						scrRegValues.setVisible(true);
						JTable table = (JTable) this.swingEngine.find(guiTableName);
						table.setModel(getTable(tableName, regFlags[regId], seedDataBO, optionName));
						table.setCellSelectionEnabled(true);
					} else {
						String valueToDisplay = "Access regulation table by selecting or right-clicking on item at left";
						if (itemName.equals("ckbReg_VAMP")) {
							valueToDisplay = "If D1485 is selected, take VAMP D1641 hydrology with a D1485 run.";
						}
						scrRegValues.setVisible(false);
						JLabel lab = (JLabel) swingEngine.find("labReg");
						lab.setText(valueToDisplay);
					}
				} else if (itemName.startsWith("btnReg")) {
					JRadioButton radioButton = ((JRadioButton) this.swingEngine.find(itemName));
					TitledBorder titledBorder = (TitledBorder) ((JPanel) radioButton.getParent()).getBorder();
					SeedDataBO seedData = seedDataSvc.getObjByGuiId(xmlParsingSvc.getcompIdfromName(titledBorder.getTitle()));
					String guiTableName = getTableNameFromTheConponent(radioButton.getParent());
					tableName = seedData.getDataTables();
					if (itemName.endsWith(Constant.D1641)) {
						optionName = Constant.D1641;
					} else if (itemName.endsWith(Constant.D1485)) {
						optionName = Constant.D1485;
					} else if (itemName.endsWith(Constant.USER_DEFINED)) {
						optionName = Constant.USER_DEFINED;
					}
					int regId = Integer.parseInt(seedData.getRegID());
					((JTable) this.swingEngine.find(guiTableName))
					        .setModel(getTable(tableName, regFlags[regId], seedData, optionName));
					// setting the regFlag cann't be done in the above if else statement. Please see the getTable method in this
					// class.
					if (itemName.endsWith(Constant.D1641)) {
						regFlags[regId] = 1;
					} else if (itemName.endsWith(Constant.D1485)) {
						regFlags[regId] = 3;
					} else if (itemName.endsWith(Constant.USER_DEFINED)) {
						regFlags[regId] = 2;
					}
				}
			}
		} catch (NoSuchElementException ex) {
			// TODO this is thrown because of the data is not there in the enable table for parent id.
			// ex.printStackTrace();
			errorHandlingSvc.businessErrorHandler((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME), new CalLiteGUIException(
			        "The control id " + itemName + " don't have the proper data in the TriggerForDynamicDisplay File", ex));
		}
	}

	public DataTableModle getTable(String tableName, int regValue, SeedDataBO seedDataBO, String optionName)
	        throws CalLiteGUIException, CloneNotSupportedException {
		DataTableModle dataTableModle = null;
		switch (optionName) {
		case Constant.D1641:
			dataTableModle = desideTableName(tableName, seedDataBO, Constant.D1641);
			if (resultSvc.hasUserDefinedTable(tableName))
				resultSvc.removeUserDefinedTable(tableName);
			break;
		case Constant.D1485:
			dataTableModle = desideTableName(tableName, seedDataBO, Constant.D1485);
			if (resultSvc.hasUserDefinedTable(tableName))
				resultSvc.removeUserDefinedTable(tableName);
			break;
		case Constant.USER_DEFINED:
			if (resultSvc.hasUserDefinedTable(tableName)) {
				dataTableModle = resultSvc.getUserDefinedTable(tableName);
			} else {
				if (regValue == 1) {
					dataTableModle = (DataTableModle) desideTableName(tableName, seedDataBO, Constant.D1641).clone();
				} else if (regValue == 3) {
					dataTableModle = (DataTableModle) desideTableName(tableName, seedDataBO, Constant.D1485).clone();
				} else if (regValue == 4) {
					dataTableModle = (DataTableModle) desideTableName(tableName, seedDataBO, Constant.USER_DEFINED).clone();
				}
				dataTableModle.setCellEditable(true);
				resultSvc.addUserDefinedTable(tableName, dataTableModle);
			}
			break;
		}
		return dataTableModle;
	}

	private DataTableModle desideTableName(String tableName, SeedDataBO seedDataBO, String type) throws CalLiteGUIException {
		DataTableModle dtm = null;
		switch (type) {
		case Constant.D1485:
			if (seedDataBO.getD1485().equalsIgnoreCase(Constant.N_A)) {
				dtm = tableSvc.getTable(tableName + Constant.DASH + Constant.D1485);
			} else if (seedDataBO.getD1641().equalsIgnoreCase(Constant.N_A)) {
				dtm = tableSvc.getTable(tableName + Constant.DASH + Constant.D1641);
			} else {
				dtm = tableSvc.getTable(tableName);
			}
			break;
		case Constant.D1641:
			if (seedDataBO.getD1641().equalsIgnoreCase(Constant.N_A)) {
				dtm = tableSvc.getTable(tableName + Constant.DASH + Constant.D1641);
			} else if (seedDataBO.getD1485().equalsIgnoreCase(Constant.N_A)) {
				dtm = tableSvc.getTable(tableName + Constant.DASH + Constant.D1485);
			} else {
				dtm = tableSvc.getTable(tableName);
			}
			break;
		case Constant.USER_DEFINED:
			dtm = tableSvc.getTable(tableName);
			break;
		}
		if (dtm == null) {
			throw new CalLiteGUIException(
			        "The table is not available as showed in the start of the program. The table name is " + tableName);
		}
		return dtm;
	}

	public String getTableNameFromTheConponent(Component component) {
		if (component instanceof JTable) {
			return component.getName();
		}
		for (Component child : ((Container) component).getComponents()) {
			String value = getTableNameFromTheConponent(child);
			if (!value.equals(""))
				return value;
		}
		return "";
	}

	public void makeRBVisible(SeedDataBO seedDataBO) {
		swingEngine.find(Constant.PANEL_RB_D1485).setVisible(false);
		swingEngine.find(Constant.PANEL_RB_D1641).setVisible(false);
		swingEngine.find(Constant.PANEL_RB_USER_DEFIND).setVisible(false);
		if (seedDataBO.getD1485().equals(Constant.N_A)) {
			swingEngine.find(Constant.PANEL_RB_D1485).setVisible(true);
		}
		if (seedDataBO.getD1641().equals(Constant.N_A)) {
			swingEngine.find(Constant.PANEL_RB_D1641).setVisible(true);
		}
		if (seedDataBO.getUserDefined().equals(Constant.N_A)) {
			swingEngine.find(Constant.PANEL_RB_USER_DEFIND).setVisible(true);
		}
	}
}
