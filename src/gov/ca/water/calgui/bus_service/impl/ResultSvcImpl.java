package gov.ca.water.calgui.bus_service.impl;

import java.awt.Component;
import java.awt.Container;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.text.JTextComponent;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.swixml.SwingEngine;
import org.swixml.XScrollPane;

import gov.ca.water.calgui.bo.CalLiteGUIException;
import gov.ca.water.calgui.bo.SeedDataBO;
import gov.ca.water.calgui.bus_service.IResultSvc;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.presentation.DataTableModle;
import gov.ca.water.calgui.presentation.NumericTextField;
import gov.ca.water.calgui.tech_service.IErrorHandlingSvc;
import gov.ca.water.calgui.tech_service.IFileSystemSvc;
import gov.ca.water.calgui.tech_service.impl.ErrorHandlingSvcImpl;
import gov.ca.water.calgui.tech_service.impl.FileSystemSvcImpl;
import wrimsv2.evaluator.TimeOperation;

/**
 * This is the class for handling the cls file and saving the data.
 */
public class ResultSvcImpl implements IResultSvc {
	private static Logger log = Logger.getLogger(ResultSvcImpl.class.getName());
	private IErrorHandlingSvc errHandlingSvc = new ErrorHandlingSvcImpl();
	private IFileSystemSvc fileSystemSvc = new FileSystemSvcImpl();
	private static IResultSvc resultSvc;
	private int[] regulationoptions = new int[100];
	// TODO - after impl everything if there is no need of User Defined Flag remove this.
	// private Map<String, Boolean> userDefinedFlagsMap = new HashMap<String, Boolean>();
	private Map<String, DataTableModle> userDefinedTableMap = new HashMap<String, DataTableModle>();
	private boolean isCLSFlag = true;

	/**
	 * This method is for implementing the singleton.
	 *
	 * @return
	 */
	public static IResultSvc getResultSvcImplInstance() {
		if (resultSvc == null) {
			resultSvc = new ResultSvcImpl();
		}
		return resultSvc;
	}

	private ResultSvcImpl() {

	}

	@Override
	public void getCLSData(String fileName, List<String> controlStrList, List<String> dataTableModelStrList,
	        List<String> regulationoptionsStr) {
		List<String> data = null;
		boolean isDataTableModel = false;
		boolean isUserDefinedFlags = false;
		boolean isRegulationoptions = false;
		try {
			data = fileSystemSvc.getFileData(fileName, true);
		} catch (CalLiteGUIException ex) {
			errHandlingSvc.displayErrorMessageBeforeTheUI(ex);
		}
		for (String str : data) {
			switch (str) {
			case "DATATABLEMODELS":
				isDataTableModel = true;
				continue;
			case "END DATATABLEMODELS":
				isDataTableModel = false;
				continue;
			case "USERDEFINEDFLAGS":
				isUserDefinedFlags = true;
				continue;
			case "END USERDEFINEDFLAGS":
				isUserDefinedFlags = false;
				continue;
			case "REGULATIONOPTIONS":
				isRegulationoptions = true;
				continue;
			case "END REGULATIONOPTIONS":
				isRegulationoptions = false;
				continue;
			}

			if (isDataTableModel) {
				dataTableModelStrList.add(str);
			} else if (isUserDefinedFlags) { // TODO - after impl everything if there is no need of User Defined Flag remove this.
			} else if (isRegulationoptions) {
				regulationoptionsStr.add(str);
			} else {
				controlStrList.add(str);
			}
		}
	}

	@Override
	public void applyClsFile(String fileName, SwingEngine swingEngine, Map<String, SeedDataBO> tableMap) {
		this.isCLSFlag = true;
		List<String> controlStrList = new ArrayList<String>();
		List<String> dataTableModelStrList = new ArrayList<String>();
		// List<String> userDefinedFlagsStrList = new ArrayList<String>();
		List<String> regulationoptionsStr = new ArrayList<String>();
		this.getCLSData(fileName, controlStrList, dataTableModelStrList, regulationoptionsStr);

		if (!regulationoptionsStr.isEmpty()) {
			List<String> regData = Arrays.asList(regulationoptionsStr.get(0).split(Constant.PIPELINE_DELIMITER));
			for (int i = 0; i < regData.size(); i++) {
				this.regulationoptions[i] = Integer.parseInt(regData.get(i));
			}
		}
		if (!dataTableModelStrList.isEmpty()) {
			try {
				populateClsTableMap(dataTableModelStrList, tableMap);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		applyControls(controlStrList, swingEngine);
		// if (!userDefinedFlagsStrList.isEmpty()) {
		// TODO - after impl everything if there is no need of User Defined Flag remove this.
		// populateUserDefinedFlagsMap(userDefinedFlagsStrList);
		// }
		if (((JRadioButton) swingEngine.find("rdbRegQS_UD")) != null
		        && ((JRadioButton) swingEngine.find("rdbRegQS_UD")).isSelected()) {
			toggleEnComponentAndChildren(swingEngine.find("regpan1"), true);
			toggleEnComponentAndChildren(swingEngine.find("regpan2"), true);
			toggleEnComponentAndChildren(swingEngine.find("regpan2b"), true);
			toggleEnComponentAndChildren(swingEngine.find("regpan3"), true);
		} else {
			toggleEnComponentAndChildren(swingEngine.find("regpan1"), false);
			toggleEnComponentAndChildren(swingEngine.find("regpan2"), false);
			toggleEnComponentAndChildren(swingEngine.find("regpan2b"), false);
			toggleEnComponentAndChildren(swingEngine.find("regpan3"), false);
		}

		this.isCLSFlag = false;
	}

	@Override
	public boolean save(String fileName, SwingEngine swingEngine, List<SeedDataBO> seedDataBOList) {
		try {
			saveToCLSFile(Constant.SCENARIOS_DIR + fileName + Constant.CLS_EXT, swingEngine, seedDataBOList);
			saveFiles(fileName, swingEngine, seedDataBOList);
			return true;
		} catch (CalLiteGUIException ex) {
			errHandlingSvc.businessErrorHandler((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME),
			        new CalLiteGUIException("We are unable to save the file.", ex));
			return false;
		}
	}

	@Override
	public boolean hasUserDefinedTable(String tableName) {
		return this.userDefinedTableMap.get(tableName) != null ? true : false;
	}

	@Override
	public void removeUserDefinedTable(String tableName) {
		if (this.hasUserDefinedTable(tableName))
			this.userDefinedTableMap.remove(tableName);
	}

	@Override
	public DataTableModle getUserDefinedTable(String tableName) {
		return this.userDefinedTableMap.get(tableName);
	}

	@Override
	public void addUserDefinedTable(String tableName, DataTableModle dataTableModle) {
		this.userDefinedTableMap.put(tableName, dataTableModle);
	}

	@Override
	public int[] getRegulationoptions() {
		return regulationoptions;
	}

	@Override
	public boolean isCLSFileLoading() {
		return this.isCLSFlag;
	}

	/**
	 * This method is used to convert the table data in the cls file to the user defined table map.
	 *
	 * @param dataTableModelStrList
	 *            This data table strings from the cls file.
	 * @param tableMap
	 *            The map with key as the table id and value as table object.
	 * @throws CalLiteGUIException
	 */
	private void populateClsTableMap(List<String> dataTableModelStrList, Map<String, SeedDataBO> tableMap)
	        throws CalLiteGUIException {
		String tableName = "";
		for (String dataTableModelStr : dataTableModelStrList) {
			String[] strArr = dataTableModelStr.split(Constant.PIPELINE_DELIMITER);
			String tableId = strArr[0];
			if (!(tableId.equals("9") || tableId.equals("10") || tableId.equals("5"))) {
				if (tableMap.get(strArr[0]) == null) {
					tableName = strArr[0];
				} else {
					tableName = tableMap.get(tableId).getDataTables();
				}
				String[] columnNames = getColumnNamesFromTableId(tableName);
				userDefinedTableMap.put(tableName,
				        new DataTableModle(tableName, columnNames, getTableDataFromCLSFile(strArr[1]), true));
			} else if (tableId.equals("5")) {
				tableName = tableMap.get(tableId).getDataTables();
				String[] tableNames = tableName.split(Constant.PIPELINE_DELIMITER);
				String[] columnNames1;
				String[] columnNames2;
				if (tableNames[0].equals("gui_x2active")) {
					columnNames1 = getColumnNamesFromTableId(tableNames[0]);
					columnNames2 = getColumnNamesFromTableId(tableNames[1]);
				} else {
					columnNames1 = getColumnNamesFromTableId(tableNames[1]);
					columnNames2 = getColumnNamesFromTableId(tableNames[0]);
				}
				String[] newColumnNames = { columnNames1[0], columnNames1[1], columnNames2[1], columnNames2[2], columnNames2[3],
				        columnNames2[4], columnNames2[5] };
				userDefinedTableMap.put(tableName,
				        new DataTableModle(tableName, newColumnNames, getTableDataFromCLSFile(strArr[1]), true));
			} else {
				tableName = tableMap.get(strArr[0]).getDataTables();
				String[] columnNames = new String[2];
				columnNames[0] = "wsi";
				columnNames[1] = "di";
				userDefinedTableMap.put(tableName,
				        new DataTableModle(tableName, columnNames, getTableDataFromCLSFile(strArr[1]), true));
			}
		}
	}

	/**
	 * This will convert the table string from the cls file into the array.
	 *
	 * @param data
	 *            Table data string.
	 * @return
	 */
	private Object[][] getTableDataFromCLSFile(String data) {
		String[] tableDataArr = data.split(Constant.SEMICOLON);
		int noOfRows = tableDataArr.length;
		int noOfCol = tableDataArr[0].split(Constant.DELIMITER).length;
		Object[][] tableData = new Object[noOfRows][noOfCol];
		for (int i = 0; i < tableDataArr.length; i++) {
			String[] colData = tableDataArr[i].split(Constant.DELIMITER);
			for (int j = 0; j < colData.length; j++) {
				tableData[i][j] = colData[j];
			}
		}
		return tableData;
	}

	/**
	 * This method is used to get the column name for the table from the table name.
	 *
	 * @param tableName
	 *            Just the table name as gui_link2.table
	 * @return
	 * @throws CalLiteGUIException
	 */
	private String[] getColumnNamesFromTableId(String tableName) throws CalLiteGUIException {
		List<String> tableStrList = fileSystemSvc
		        .getFileDataForTables(Constant.Model_w2_WRESL_LOOKUP_DIR + tableName + Constant.FILE_EXT_TABLE);
		String header = null;
		header = tableStrList.stream().filter(obj -> obj.contains(Constant.HEADERS)).findFirst().get();
		String[] da = header.split(Constant.OLD_DELIMITER);
		String[] headers = new String[da.length - 1];
		for (int i = 0; i < headers.length; i++) {
			headers[i] = da[i + 1];
		}
		return headers;
	}

	// TODO - after impl everything if there is no need of User Defined Flag remove this.
	// public void populateUserDefinedFlagsMap(List<String> userDefinedFlagsStrList) {
	// for (String userDefinedFlagsStr : userDefinedFlagsStrList) {
	// String[] values = userDefinedFlagsStr.split(Constant.PIPELINE_DELIMITER);
	// this.userDefinedFlagsMap.put(values[0], Boolean.valueOf(values[1]));
	// }
	// }

	/**
	 * This will take the control strings from the cls file and apply it to the current ui.
	 *
	 * @param controlStrList
	 * @param swingEngine
	 */
	private void applyControls(List<String> controlStrList, SwingEngine swingEngine) {
		for (String controlStr : controlStrList) {
			String[] comArr = controlStr.split(Constant.PIPELINE_DELIMITER);
			if (comArr.length < 1) {
				log.info("the line in the cls file is in wrong formate. the line is \"" + controlStr + "\"");
				continue;
			}
			String compName = comArr[0];
			String value = comArr[1];
			JComponent component = (JComponent) swingEngine.find(compName);

			if (component == null) {
				log.error("Not found: " + compName);
			} else {
				if (component instanceof JCheckBox || component instanceof JRadioButton) {
					((AbstractButton) component).setSelected(Boolean.parseBoolean(value));
				} else if (component instanceof JSpinner) {
					JSpinner spn = (JSpinner) component;
					if (value.matches("((-|\\+)?[0-9])+")) {
						int val1 = Integer.parseInt(value);
						spn.setValue(val1);
					} else {
						spn.setValue(value);
					}
				} else {
					if (component != null) {
						((JTextComponent) component).setText(value.replace("~~", "\n"));
					}
				}
			}

		}
	}

	/**
	 * This will save the current ui state to the tables and scenario directory.
	 *
	 * @param fileName
	 *            Just the file name with out the path and extension.
	 * @param swingEngine
	 * @param seedDataBOList
	 * @throws CalLiteGUIException
	 */
	private void saveFiles(String fileName, SwingEngine swingEngine, List<SeedDataBO> seedDataBOList) throws CalLiteGUIException {
		String runDir_absPath = Paths.get(Constant.RUN_DETAILS_DIR + fileName + Constant.RUN_DIR).toString();
		String generatedDir_absPath = Paths.get(Constant.RUN_DETAILS_DIR + fileName + Constant.GENERATED_DIR).toString();
		updateSaveStatusFile(runDir_absPath + Constant.SAVE_FILE + Constant.TXT_EXT,
		        "Createing the directory and copying the files.");
		createDirAndCopyFiles(Constant.MODEL_W2_WRESL_DIR, runDir_absPath);
		updateSaveStatusFile(runDir_absPath + Constant.SAVE_FILE + Constant.TXT_EXT, "Deleteing the previous data.");
		File generatedDir = new File(generatedDir_absPath);
		// deleting all directory and files under Generate directory.
		if (generatedDir.listFiles() != null) {
			for (File file : generatedDir.listFiles()) {
				try {
					FileDeleteStrategy.FORCE.delete(file);
				} catch (IOException ex) {
					throw new CalLiteGUIException(
					        "We had a problem when deleteing the directory. The directory is " + generatedDir_absPath, ex);
				}
			}
		}

		// create DSS, Lookup, and external folders

		generatedDir = new File(generatedDir_absPath, "DSS");
		generatedDir.mkdirs();
		generatedDir = new File(generatedDir_absPath, "Lookup");
		generatedDir.mkdirs();
		generatedDir = new File(generatedDir_absPath, "External");
		generatedDir.mkdirs();

		updateSaveStatusFile(runDir_absPath + Constant.SAVE_FILE + Constant.TXT_EXT, "Copying DSS Files.");
		// Copy DSS files to "Generated" folder
		copyDSSFileToScenarioDirectory(generatedDir_absPath, ((JTextField) swingEngine.find("hyd_DSS_SV")).getText());
		copyDSSFileToScenarioDirectory(generatedDir_absPath, ((JTextField) swingEngine.find("hyd_DSS_Init")).getText());

		// Copy DSS files to "Run" folder
		copyDSSFileToScenarioDirectory(runDir_absPath, ((JTextField) swingEngine.find("hyd_DSS_SV")).getText());
		copyDSSFileToScenarioDirectory(runDir_absPath, ((JTextField) swingEngine.find("hyd_DSS_Init")).getText());

		updateSaveStatusFile(runDir_absPath + Constant.SAVE_FILE + Constant.TXT_EXT, "Saveing the table files.");
		writeToFileIndexAndOption(swingEngine, seedDataBOList, runDir_absPath + "//Lookup//", generatedDir_absPath + "//Lookup//");
		writeUserDefinedTables(seedDataBOList, runDir_absPath + "//Lookup//", generatedDir_absPath + "//Lookup//");
		// Copying demand tables.
		String demandDirPath = "";
		if (((JRadioButton) swingEngine.find("dem_rdbCurSWP")).isSelected()) {
			demandDirPath = Constant.Model_w2_WRESL_LOOKUP_DIR + "//VariableDemand//";
		} else {
			demandDirPath = Constant.Model_w2_WRESL_LOOKUP_DIR + "//FutureDemand//";
		}
		String lookupFilePath = "";
		try {
			// copy either variableDemand or futureDemand lookup tables to "Generated" folder
			lookupFilePath = generatedDir_absPath + "//Lookup//";
			FileUtils.copyDirectory(new File(demandDirPath), new File(lookupFilePath));
			// copy either variableDemand or futureDemand lookup tables to "Run" folder
			lookupFilePath = runDir_absPath + "//Lookup//";
			FileUtils.copyDirectory(new File(demandDirPath), new File(lookupFilePath));
		} catch (IOException ex) {
			throw new CalLiteGUIException(
			        "There is a error when copying the directory from " + demandDirPath + " to " + lookupFilePath, ex);
		}

		// Copying WRIMSv2 DLL.
		updateSaveStatusFile(runDir_absPath + Constant.SAVE_FILE + Constant.TXT_EXT, "Copying WRIMSv2 DLL.");
		// wrims2 ANN file name is different from wrims1
		String wrims2AnnSource;
		String wrims2AnnRun;
		String wrims2AnnGenerated;

		JRadioButton rdbSLR45 = (JRadioButton) swingEngine.find("hyd_rdb1");
		JRadioButton rdbSLR15 = (JRadioButton) swingEngine.find("hyd_rdb2");

		if (rdbSLR45.isSelected()) {
			wrims2AnnSource = Constant.MODEL_W2_WRESL_DIR + "//External//Ann7inp_BDCP_LLT_45cm.dll";
			wrims2AnnRun = runDir_absPath + "//External//Ann7inp_CA.dll";
			wrims2AnnGenerated = generatedDir_absPath + "//External//Ann7inp_BDCP_LLT_45cm.dll";
		} else if (rdbSLR15.isSelected()) {
			wrims2AnnSource = Constant.MODEL_W2_WRESL_DIR + "//External//Ann7inp_BDCP_ELT_15cm.dll";
			wrims2AnnRun = runDir_absPath + "//External//Ann7inp_CA.dll";
			wrims2AnnGenerated = generatedDir_absPath + "//External//Ann7inp_BDCP_ELT_15cm.dll";
		} else {
			wrims2AnnSource = Constant.MODEL_W2_WRESL_DIR + "//External//Ann7inp_BST_noSLR_111709.dll";
			wrims2AnnRun = runDir_absPath + "//External//Ann7inp_CA.dll";
			wrims2AnnGenerated = generatedDir_absPath + "//External//Ann7inp_BST_noSLR_111709.dll";
		}
		try {
			// copy dll to "Run" folder
			FileUtils.copyFile(Paths.get(wrims2AnnSource).toFile(), Paths.get(wrims2AnnRun).toFile());
		} catch (IOException ex) {
			throw new CalLiteGUIException("There is a error when copying the file from " + Paths.get(wrims2AnnSource).toString()
			        + " to " + Paths.get(wrims2AnnRun).toString(), ex);
		}
		try {
			// copy dll to "Generated" folder
			FileUtils.copyFile(Paths.get(wrims2AnnSource).toFile(), Paths.get(wrims2AnnGenerated).toFile());
		} catch (IOException ex) {
			throw new CalLiteGUIException("There is a error when copying the file from " + Paths.get(wrims2AnnSource).toString()
			        + " to " + Paths.get(wrims2AnnGenerated).toString(), ex);
		}

		// Creating study.sty.
		updateSaveStatusFile(runDir_absPath + Constant.SAVE_FILE + Constant.TXT_EXT, "Creating study.sty");
		Calendar cal = Calendar.getInstance();

		String startMon = ((String) ((JSpinner) swingEngine.find("spnRunStartMonth")).getValue()).trim().toUpperCase();
		String endMon = ((String) ((JSpinner) swingEngine.find("spnRunEndMonth")).getValue()).trim().toUpperCase();
		Integer startYr = (Integer) ((JSpinner) swingEngine.find("spnRunStartYear")).getValue();
		Integer endYr = (Integer) ((JSpinner) swingEngine.find("spnRunEndYear")).getValue();

		// Determine Month/Count
		Integer dayct = getDaysinMonth(startMon);
		Integer iSMon = monthToInt(startMon);
		Integer iEMon = monthToInt(endMon);
		Integer numMon = (endYr - startYr) * 12 + (iEMon - iSMon) + 1;

		String oDSS = ((JTextField) swingEngine.find("run_txfoDSS")).getText().trim();

		String[] newtext = new String[20];
		Integer[] LineNum = new Integer[20];

		newtext[0] = fileName + Constant.CLS_EXT;
		LineNum[0] = 2;
		newtext[1] = cal.getTime().toString();
		LineNum[1] = 4;
		newtext[2] = runDir_absPath;
		LineNum[2] = 7;
		newtext[3] = Paths.get(runDir_absPath + "//CALLITE_BO_FUTURE.STY").toString();
		LineNum[3] = 8;
		newtext[4] = Paths.get(runDir_absPath + "//MAIN.WRESL").toString();
		LineNum[4] = 9;
		if (oDSS.toUpperCase().endsWith(".DSS")) {
			newtext[6] = Paths.get(Constant.SCENARIOS_DIR + oDSS).toString();
			LineNum[6] = 11;
		} else {
			newtext[6] = Paths.get(Constant.SCENARIOS_DIR + oDSS + ".DSS").toString();
			LineNum[6] = 11;
		}

		LineNum[5] = 10;
		newtext[5] = Paths.get(runDir_absPath + "\\DSS\\" + ((JTextField) swingEngine.find("hyd_DSS_SV")).getText()).toString();
		LineNum[7] = 12;
		newtext[7] = Paths.get(runDir_absPath + "\\DSS\\" + ((JTextField) swingEngine.find("hyd_DSS_Init")).getText()).toString();

		newtext[8] = numMon.toString();
		LineNum[8] = 14;
		newtext[9] = dayct.toString();
		LineNum[9] = 15;
		newtext[10] = startMon;
		LineNum[10] = 16;
		newtext[11] = startYr.toString();
		LineNum[11] = 17;

		LineNum[12] = 33;
		newtext[12] = ((JTextField) swingEngine.find("hyd_DSS_SV_F")).getText();
		LineNum[13] = 34;
		newtext[13] = ((JTextField) swingEngine.find("hyd_DSS_Init_F")).getText();

		replaceLinesInFile(runDir_absPath + "\\study.sty", LineNum, newtext);

		updateSaveStatusFile(runDir_absPath + Constant.SAVE_FILE + Constant.TXT_EXT, "Writing WRIMSv2 Batchfile.");
		// configuration file for wrims v2
		Integer iStartMonth = TimeOperation.monthValue(startMon.toLowerCase());
		Integer iEndMonth = TimeOperation.monthValue(endMon.toLowerCase());
		Integer iStartDay = TimeOperation.numberOfDays(iStartMonth, startYr);
		Integer iEndDay = TimeOperation.numberOfDays(iEndMonth, endYr);

		Map<String, String> configMap = new HashMap<String, String>();
		configMap.put("MainFile", runDir_absPath + "\\main.wresl");
		configMap.put("DvarFile", FilenameUtils.removeExtension(newtext[6]) + ".dss");
		configMap.put("SvarFile", newtext[5]);
		configMap.put("SvarFPart", newtext[12]);
		configMap.put("InitFile", newtext[7]);
		configMap.put("InitFPart", newtext[13]);
		configMap.put("StartYear", startYr.toString());
		configMap.put("StartMonth", iStartMonth.toString());
		configMap.put("StartDay", iStartDay.toString());
		configMap.put("EndYear", endYr.toString());
		configMap.put("EndMonth", iEndMonth.toString());
		configMap.put("EndDay", iEndDay.toString());
		configMap.put("UserPath", System.getProperty("user.dir"));
		configMap.put("ScenarioName", fileName);
		configMap.put("ScenarioPath", new File(runDir_absPath).getParentFile().getAbsolutePath());
		configMap.put("RunPath", runDir_absPath);
		configMap.put("ConfigFilePath",
		        new File(configMap.get("ScenarioPath"), configMap.get("ScenarioName") + ".config").getAbsolutePath());
		configMap.put("ConfigFilePath_wsidi",
		        new File(configMap.get("ScenarioPath"), configMap.get("ScenarioName") + "_wsidi.config").getAbsolutePath());
		updateSaveStatusFile(runDir_absPath + Constant.SAVE_FILE + Constant.TXT_EXT, "Writing Scenario Config.");
		// replace vars in config template file

		String configText = wrimsv2.wreslparser.elements.Tools.readFileAsString(Constant.MODEL_W2_DIR + "//config.template");

		configText = configText.replace("{SvarFile}", configMap.get("SvarFile"));
		configText = configText.replace("{SvarFPart}", configMap.get("SvarFPart"));
		configText = configText.replace("{InitFile}", configMap.get("InitFile"));
		configText = configText.replace("{InitFPart}", configMap.get("InitFPart"));
		// configText = configText.replace("{DvarFile}", configMap.get("DvarFile"));
		configText = configText.replace("{StartYear}", configMap.get("StartYear"));
		configText = configText.replace("{StartMonth}", configMap.get("StartMonth"));
		configText = configText.replace("{EndYear}", configMap.get("EndYear"));
		configText = configText.replace("{EndMonth}", configMap.get("EndMonth"));
		configText = configText.replace("{StartDay}", configMap.get("StartDay"));
		configText = configText.replace("{EndDay}", configMap.get("EndDay"));

		// wsidi run config file
		String configText_wsidi = configText.replace("{MainFile}", "run\\main_wsidi.wresl");
		configText_wsidi = configText_wsidi.replace("{DvarFile}",
		        FilenameUtils.getBaseName(configMap.get("DvarFile")) + "_wsidi.dss");

		try {
			File configFile_wsidi = new File(configMap.get("ConfigFilePath_wsidi"));
			PrintWriter configFilePW_wsidi = new PrintWriter(new BufferedWriter(new FileWriter(configFile_wsidi)));
			configFilePW_wsidi.print(configText_wsidi);
			configFilePW_wsidi.flush();
			configFilePW_wsidi.close();
			// normal run config file
			String configText_simple = configText.replace("{MainFile}", "run\\main.wresl");
			configText_simple = configText_simple.replace("{DvarFile}", configMap.get("DvarFile"));
			File configFile = new File(configMap.get("ConfigFilePath"));
			PrintWriter configFilePW = new PrintWriter(new BufferedWriter(new FileWriter(configFile)));
			configFilePW.print(configText_simple);
			configFilePW.flush();
			configFilePW.close();
		} catch (IOException ex) {
			throw new CalLiteGUIException("There is a error when building the congig file for wsidi", ex);
		}
		updateSaveStatusFile(runDir_absPath + Constant.SAVE_FILE + Constant.TXT_EXT, "Save is completed.");
	}

	/**
	 * In this method we open the study.sty file and write the {@code newText} to the study.sty file.
	 *
	 * @param fileName
	 * @param LineNum
	 * @param newText
	 * @throws CalLiteGUIException
	 */
	private void replaceLinesInFile(String fileName, Integer[] LineNum, String[] newText) throws CalLiteGUIException {
		Integer LineCt = 0;
		Integer n = 0;
		StringBuffer sb = new StringBuffer();
		List<String> data = fileSystemSvc.getFileData(fileName, false);
		for (String textinLine : data) {
			LineCt = LineCt + 1;
			if (LineCt == LineNum[n]) {
				sb.append(newText[n] + Constant.NEW_LINE);
				n = n + 1;
			} else {
				sb.append(textinLine + Constant.NEW_LINE);
			}
		}
		fileSystemSvc.saveDataToFile(fileName, sb.toString());
	}

	/**
	 * Convert the month name to the int value of it.
	 *
	 * @param month
	 *            - The month name which you want to convert.
	 * @return The int value of the month.
	 */
	public static int monthToInt(String month) {
		HashMap<String, Integer> monthMap = new HashMap<String, Integer>();
		monthMap.put("jan", 1);
		monthMap.put("feb", 2);
		monthMap.put("mar", 3);
		monthMap.put("apr", 4);
		monthMap.put("may", 5);
		monthMap.put("jun", 6);
		monthMap.put("jul", 7);
		monthMap.put("aug", 8);
		monthMap.put("sep", 9);
		monthMap.put("oct", 10);
		monthMap.put("nov", 11);
		monthMap.put("dec", 12);
		month = month.toLowerCase();
		Integer monthCode = null;
		monthCode = monthMap.get(month);
		return monthCode == null ? -1 : monthCode.intValue();
	}

	/**
	 * This will convert the string month into the int value.
	 *
	 * @param mon
	 * @return
	 */
	private int getDaysinMonth(String mon) {
		int dayct = 0;
		mon = mon.toLowerCase();
		switch (mon) {
		case "jan":
		case "mar":
		case "may":
		case "jul":
		case "aug":
		case "oct":
		case "dec":
			dayct = 31;
			break;
		case "apr":
		case "jun":
		case "sep":
		case "nov":
			dayct = 30;
			break;
		case "feb":
			dayct = 28;
			break;
		}
		return dayct;
	}

	/**
	 * This will write the user define table data to the table files as given in the gui_link2.table
	 *
	 * @param seedDataBOList
	 * @param runDir
	 * @param generatedDir
	 * @throws CalLiteGUIException
	 */
	private void writeUserDefinedTables(List<SeedDataBO> seedDataBOList, String runDir, String generatedDir)
	        throws CalLiteGUIException {
		for (String tableName : this.userDefinedTableMap.keySet()) {
			Map<String, StringBuffer> fileDataMap = new HashMap<String, StringBuffer>();
			DataTableModle table = this.userDefinedTableMap.get(tableName);
			if (tableName.equals("gui_xchanneldays") || tableName.equals("gui_EIRatio") || tableName.equals("perc_UnimparedFlow")
			        || tableName.equals("wsi_di_swp") || tableName.equals("wsi_di_cvp_sys")) {
				fileDataMap.put(tableName, saveTableLikeTable(tableName, table));
			} else if (tableName.equals("gui_EIsjr")) {
				fileDataMap.put(tableName, saveEisjrTable(tableName, table));
			} else if (tableName.equals("gui_x2active|gui_x2km")) {
				fileDataMap.putAll(saveX2Table(table));
			} else {
				fileDataMap.put(tableName, saveTableWithColumnNumber(tableName, table));
			}
			for (String fileName : fileDataMap.keySet()) {
				fileSystemSvc.saveDataToFile(runDir + fileName + Constant.TABLE_EXT, fileDataMap.get(fileName).toString());
				fileSystemSvc.saveDataToFile(generatedDir + fileName + Constant.TABLE_EXT, fileDataMap.get(fileName).toString());
			}
		}
	}

	/**
	 * This method is used for the X2 table.
	 *
	 * @param table
	 * @return
	 * @throws CalLiteGUIException
	 */
	private Map<String, StringBuffer> saveX2Table(DataTableModle table) throws CalLiteGUIException {
		Map<String, StringBuffer> map = new HashMap<String, StringBuffer>();
		Object[][] data = table.getData();
		Object[][] activeData = new Object[12][2];
		Object[][] kmData = new Object[12][6];
		for (int m = 0; m < data.length; m++) {
			for (int n = 0; n < 2; n++) {
				activeData[m][n] = data[m][n];
			}
		}
		for (int m = 0; m < kmData.length; m++) {
			kmData[m][0] = m + 1;
		}
		for (int m = 0; m < data.length; m++) {
			for (int n = 2; n < 7; n++) {
				kmData[m][n - 1] = data[m][n];
			}
		}
		map.put("gui_x2active", saveTableLikeTable("gui_x2active", new DataTableModle("", null, activeData, false)));
		map.put("gui_x2km", saveTableWithColumnNumber("gui_x2km", new DataTableModle("", null, kmData, false)));
		return map;
	}

	/**
	 * This is used for the Eisjr table only.
	 *
	 * @param tableName
	 * @param table
	 * @return
	 * @throws CalLiteGUIException
	 */
	private StringBuffer saveEisjrTable(String tableName, DataTableModle table) throws CalLiteGUIException {
		StringBuffer fileDataStrBuff = getTheCommentFromFile(tableName);
		Object[][] tableData = table.getData();
		int offset = 1;
		int mul = 2;
		for (int colNum = 1; colNum < 6; colNum++) {
			for (int i = 0; i < tableData.length; i++) {
				fileDataStrBuff.append((i + 1) + Constant.TAB_SPACE + colNum + Constant.TAB_SPACE + tableData[i][offset]
				        + Constant.TAB_SPACE + tableData[i][mul] + Constant.NEW_LINE);
			}
			offset += 2;
			mul += 2;
		}
		return fileDataStrBuff;
	}

	/**
	 * This method will save the table like the bellow format.
	 *
	 * <pre>
	 * month	NDO	     SAC	 SJR
	 *	 1	     0	      0	       0
	 *	 2	     0	      0	       0
	 *   3       0  	  0        0
	 *   4	     0.75	  0	       0
	 *   5	     0.75	  0	       0.75
	 *   6	     0.75	  0	       0.75
	 *   7	     0.75	  0.75     0.75
	 *   8	     0.75	  0.75     0.75
	 *   9	     0.75	  0.75     0.75
	 *   10	     0	      0	       0
	 *   11      0	      0	       0
	 *   12	     0	      0        0
	 *
	 * </pre>
	 *
	 * @param tableName
	 * @param table
	 * @return
	 * @throws CalLiteGUIException
	 */
	private StringBuffer saveTableLikeTable(String tableName, DataTableModle table) throws CalLiteGUIException {
		StringBuffer fileDataStrBuff = getTheCommentFromFile(tableName);
		Object[][] tableData = table.getData();
		for (int i = 0; i < tableData.length; i++) {
			for (int j = 0; j < tableData[i].length; j++) {
				fileDataStrBuff.append(tableData[i][j] + Constant.TAB_SPACE);
			}
			fileDataStrBuff.append(Constant.NEW_LINE);
		}
		return fileDataStrBuff;
	}

	/**
	 * This method will save the table like the bellow format.
	 *
	 * <pre>
	 *
	 * month	Column Number	   value
	 *   1			1				10
	 *   2			1				54
	 *   3			1				98
	 *   4			1				45
	 *
	 * </pre>
	 *
	 * @param tableName
	 * @param table
	 * @return
	 * @throws CalLiteGUIException
	 */
	private StringBuffer saveTableWithColumnNumber(String tableName, DataTableModle table) throws CalLiteGUIException {
		StringBuffer fileDataStrBuff = getTheCommentFromFile(tableName);
		Object[][] tableData = table.getData();
		for (int colNum = 1; colNum < tableData[0].length; colNum++) {
			for (int i = 0; i < tableData.length; i++) {
				fileDataStrBuff.append(
				        (i + 1) + Constant.TAB_SPACE + colNum + Constant.TAB_SPACE + tableData[i][colNum] + Constant.NEW_LINE);
			}
		}
		return fileDataStrBuff;
	}

	/**
	 * This will get the comment lines in the old table file.
	 *
	 * @param tableName
	 *            Table name with out existence.
	 * @return
	 * @throws CalLiteGUIException
	 */
	private StringBuffer getTheCommentFromFile(String tableName) throws CalLiteGUIException {
		StringBuffer fileDataStrBuff = new StringBuffer();
		String fileName = Paths.get(Constant.Model_w2_WRESL_LOOKUP_DIR + tableName + Constant.TABLE_EXT).toString();
		try {
			List<String> oldFileData = fileSystemSvc.getFileData(fileName, false);
			// Adding the headings of the default file.
			for (String line : oldFileData) {
				if (isInteger(line.split(Constant.TAB_OR_SPACE_DELIMITER)[0]))
					break;
				fileDataStrBuff.append(line + Constant.NEW_LINE);
			}
			return fileDataStrBuff;
		} catch (CalLiteGUIException ex) {
			throw new CalLiteGUIException(
			        "There is a error when we are geting the comments from the file. The file path is " + fileName, ex);
		}
	}

	/**
	 * This method will take the string and tell whether it's int or not.
	 *
	 * @param str
	 * @return
	 */
	private boolean isInteger(String str) {
		if (str == null) {
			return false;
		}
		int length = str.length();
		if (length == 0) {
			return false;
		}
		for (int i = 0; i < length; i++) {
			char c = str.charAt(i);
			if (c < '0' || c > '9') {
				if (c != '.') {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * This method will write the index and option of the current state in the ui to there files given in the gui_link2.table.
	 *
	 * @param swingEngine
	 * @param seedDataBOList
	 * @param runDir
	 * @param generatedDir
	 * @throws CalLiteGUIException
	 */
	private void writeToFileIndexAndOption(SwingEngine swingEngine, List<SeedDataBO> seedDataBOList, String runDir,
	        String generatedDir) throws CalLiteGUIException {
		Map<String, List<SeedDataBO>> tableNameMap = new HashMap<String, List<SeedDataBO>>();
		for (SeedDataBO seedDataBO : seedDataBOList) {
			String tableName = seedDataBO.getTableName();
			if (tableName.equals(Constant.N_A))
				continue;
			if (tableNameMap.get(tableName) == null) {
				List<SeedDataBO> data = new ArrayList<SeedDataBO>();
				data.add(seedDataBO);
				tableNameMap.put(tableName, data);
			} else {
				tableNameMap.get(tableName).add(seedDataBO);
			}
		}
		for (String tableName : tableNameMap.keySet()) {
			List<SeedDataBO> data = tableNameMap.get(tableName);
			List<String> headerList = null;
			StringBuffer fileDataStrBuf = new StringBuffer();
			headerList = fileSystemSvc.getFileDataWithCommentOnly(Constant.Model_w2_WRESL_LOOKUP_DIR + tableName, false);
			headerList.stream().forEach(header -> fileDataStrBuf.append(header + Constant.NEW_LINE));
			fileDataStrBuf.append(FilenameUtils.removeExtension(tableName) + Constant.NEW_LINE);
			fileDataStrBuf.append("Index" + Constant.OLD_DELIMITER + "Option" + Constant.NEW_LINE);
			for (SeedDataBO seedDataBO : data) {
				String index = seedDataBO.getIndex();
				String option = seedDataBO.getOption();
				String description = Constant.EXCLAMATION + seedDataBO.getDescription();
				Component c = swingEngine.find(seedDataBO.getGuiId());
				if (c instanceof JTextField || c instanceof NumericTextField || c instanceof JTextArea) {
					option = ((JTextComponent) c).getText();
					fileDataStrBuf.append(
					        index + Constant.OLD_DELIMITER + option + Constant.OLD_DELIMITER + description + Constant.NEW_LINE);
				} else if (c instanceof JRadioButton) {
					if (seedDataBO.getGuiId().startsWith("hyd_ckb")) {
						boolean isSelected = ((AbstractButton) swingEngine.find("hyd_rdb2005")).isSelected()
						        || ((AbstractButton) swingEngine.find("hyd_rdb2030")).isSelected();
						if (isSelected)
							option = "0";
					}
					if (((AbstractButton) c).isSelected()) {
						fileDataStrBuf.append(
						        index + Constant.OLD_DELIMITER + option + Constant.OLD_DELIMITER + description + Constant.NEW_LINE);
					}
				} else if (c instanceof JCheckBox) {
					if (((AbstractButton) c).isSelected()) {
						if (seedDataBO.getGuiId().startsWith("ckbReg")) {
							if (!seedDataBO.getRegID().equals("n/a")) {
								int rID = Integer.parseInt(seedDataBO.getRegID());
								option = String.valueOf(this.regulationoptions[rID]);
							}
						} else {
							option = "1";
						}
					} else {
						String NAFlag = seedDataBO.getNoregulation();
						if (NAFlag == "1") {
							option = "NA";
						} else {
							option = "0";
						}
					}
					fileDataStrBuf.append(
					        index + Constant.OLD_DELIMITER + option + Constant.OLD_DELIMITER + description + Constant.NEW_LINE);
				} else if (c == null) { // control not found we have this scenario with "GUI_SJR.table" index 2.
					option = "0";
					fileDataStrBuf.append(
					        index + Constant.OLD_DELIMITER + option + Constant.OLD_DELIMITER + description + Constant.NEW_LINE);
				}
			}
			fileSystemSvc.saveDataToFile(generatedDir + tableName, fileDataStrBuf.toString());
			fileSystemSvc.saveDataToFile(runDir + tableName, fileDataStrBuf.toString());
		}
	}

	/**
	 * This method will copy the DSS files to {@code dssFileName} directory.
	 *
	 * @param directory
	 * @param dssFileName
	 * @throws CalLiteGUIException
	 */
	private void copyDSSFileToScenarioDirectory(String directory, String dssFileName) throws CalLiteGUIException {
		Path fDestination = Paths.get(directory, "//DSS//" + dssFileName);
		try {
			FileUtils.copyFile(Paths.get(Constant.MODEL_W2_DSS_DIR + dssFileName).toFile(), fDestination.toFile());
		} catch (IOException ex) {
			throw new CalLiteGUIException("There is a problem copying the DSS Files. The file name is "
			        + Paths.get(Constant.MODEL_W2_DSS_DIR + dssFileName).toString() + " and the destination is "
			        + Paths.get(directory, "//DSS//" + dssFileName).toString(), ex);
		}
	}

	/**
	 * This will copy the {@code sourceDirectory} Directory to {@code destinationDirectory} Directory.
	 *
	 * @param sourceDirectory
	 * @param destinationDirectory
	 * @throws CalLiteGUIException
	 */
	private void createDirAndCopyFiles(String sourceDirectory, String destinationDirectory) throws CalLiteGUIException {
		try {
			FileUtils.copyDirectory(new File(sourceDirectory), new File(destinationDirectory));
		} catch (IOException ex) {
			throw new CalLiteGUIException("There is a problem copying the Directorys. The source directory is " + sourceDirectory
			        + " and the destination directory is " + destinationDirectory, ex);
		}
	}

	@Override
	public void saveToCLSFile(String fileName, SwingEngine swingEngine, List<SeedDataBO> seedDataBOList)
	        throws CalLiteGUIException {
		List<String> panelNames = new ArrayList<String>();
		JTabbedPane main = (JTabbedPane) swingEngine.find(Constant.MAIN_PANEL_NAME);
		main.getComponents();
		for (Component child : main.getComponents()) {
			if (child instanceof XScrollPane) {
				for (Component child1 : ((Container) child).getComponents()) {
					if (child1 instanceof JViewport) {
						for (Component child2 : ((Container) child1).getComponents())
							if (child2 instanceof JPanel) {
								panelNames.add(child2.getName());
							}
					}
				}
			}
		}
		StringBuffer sb = new StringBuffer();
		panelNames.forEach((panelName) -> {
			setControlValues(swingEngine.find(panelName), sb);
		});

		sb.append("DATATABLEMODELS" + Constant.NEW_LINE);
		Set<String> keys = this.userDefinedTableMap.keySet();
		if (!keys.isEmpty()) {
			for (String key : keys) {
				try {
					SeedDataBO seedDataBO = seedDataBOList.stream().filter(seedData -> seedData.getDataTables().equals(key))
					        .findFirst().get();
					sb.append(convertTableToString(seedDataBO.getTableID(), this.userDefinedTableMap.get(key)) + Constant.NEW_LINE);
				} catch (NoSuchElementException ex) {
					sb.append(convertTableToString(key, this.userDefinedTableMap.get(key)) + Constant.NEW_LINE);
				}
			}
		}
		sb.append("END DATATABLEMODELS" + Constant.NEW_LINE);

		sb.append("REGULATIONOPTIONS" + Constant.NEW_LINE);
		String sRegFlags = String.valueOf(this.regulationoptions[0]);
		for (int i = 1; i < this.regulationoptions.length; i++) {
			sRegFlags += Constant.PIPELINE + String.valueOf(this.regulationoptions[i]);
		}
		sb.append(sRegFlags + Constant.NEW_LINE);
		sb.append("END REGULATIONOPTIONS" + Constant.NEW_LINE);
		fileSystemSvc.saveDataToFile(fileName, sb.toString());
	}

	/**
	 * This method will convert the {@link DataTableModle} object into the table string which is stored in the cls file.
	 *
	 * @param tableId
	 * @param dataTableModle
	 * @return
	 */
	private String convertTableToString(String tableId, DataTableModle dataTableModle) {
		String tableStr = tableId + Constant.PIPELINE;
		Object[][] data = dataTableModle.getData();
		for (int i = 0; i < data.length; i++) {
			tableStr += String.valueOf(data[i][0]);
			for (int j = 1; j < data[0].length; j++) {
				tableStr += Constant.COMMA + String.valueOf(data[i][j]);
			}
			tableStr += Constant.SEMICOLON;
		}
		return tableStr;
	}

	/**
	 * This will convert the ui controls into the string buffer which con be return to the cls file.
	 *
	 * @param component
	 * @param stringBuffer
	 */
	private void setControlValues(Component component, StringBuffer stringBuffer) {
		String compName = "";
		String value = "";
		Boolean val;
		compName = component.getName();
		if (compName != null) {
			if (component instanceof JTextField || component instanceof NumericTextField || component instanceof JTextArea) {
				value = ((JTextComponent) component).getText();
				stringBuffer.append(compName + Constant.PIPELINE + value + Constant.NEW_LINE);
			} else if (component instanceof JSpinner) {
				value = ((JSpinner) component).getValue().toString();
				stringBuffer.append(compName + Constant.PIPELINE + value + Constant.NEW_LINE);
			} else if (component instanceof JCheckBox || component instanceof JRadioButton) {
				val = ((AbstractButton) component).isSelected();
				value = val.toString();
				stringBuffer.append(compName + Constant.PIPELINE + value + Constant.NEW_LINE);
			}
		}
		for (Component child : ((Container) component).getComponents()) {
			if (component instanceof JSpinner) {
				break;
			}
			setControlValues(child, stringBuffer);
		}
	}

	/**
	 * This method is used for the writing the state of the process to the file.
	 *
	 * @param statusFilename
	 *            full path of the file name.
	 * @param text
	 *            text to write in the file.
	 */
	private void updateSaveStatusFile(String statusFilename, String text) {
		text += Constant.NEW_LINE;
		try {
			Files.write(Paths.get(statusFilename), text.getBytes(), StandardOpenOption.APPEND);
		} catch (NoSuchFileException ex) {
			try {
				Files.write(Paths.get(statusFilename), text.getBytes(), StandardOpenOption.CREATE);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		} catch (IOException ex) {
			log.debug("IOException: " + ex.getMessage(), ex);
		}
		// try {
		// FileWriter fw = new FileWriter(statusFilename, (new File(statusFilename)).exists()); // the true will append the new
		// // data
		// fw.write(text);
		// fw.close();
		// } catch (IOException ioe) {
		// log.debug("IOException: " + ioe.getMessage());
		// }
	}

	public void toggleEnComponentAndChildren(Component component, Boolean b) {
		component.setEnabled(b);
		for (Component child : ((Container) component).getComponents()) {
			toggleEnComponentAndChildren(child, b);
		}
	}
}