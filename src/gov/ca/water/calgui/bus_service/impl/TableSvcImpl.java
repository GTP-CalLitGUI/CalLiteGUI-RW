package gov.ca.water.calgui.bus_service.impl;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import gov.ca.water.calgui.bo.CalLiteGUIException;
import gov.ca.water.calgui.bo.SeedDataBO;
import gov.ca.water.calgui.bus_service.ITableSvc;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.presentation.DataTableModle;
import gov.ca.water.calgui.tech_service.IErrorHandlingSvc;
import gov.ca.water.calgui.tech_service.IFileSystemSvc;
import gov.ca.water.calgui.tech_service.impl.ErrorHandlingSvcImpl;
import gov.ca.water.calgui.tech_service.impl.FileSystemSvcImpl;

/**
 * This is the class for handling all the tables.
 */
public class TableSvcImpl implements ITableSvc {

	private static Logger log = Logger.getLogger(TableSvcImpl.class.getName());
	private static ITableSvc tableSvc;
	private IFileSystemSvc fileSystemSvc;
	private IErrorHandlingSvc errorHandlingSvc;
	private Map<String, DataTableModle> map;
	private String wsidiFileSuffix = Constant.USER_DEFINED; // We are using this to know whether the user modify the value of not.

	/**
	 * This method is for implementing the singleton.
	 *
	 * @param list
	 * @return
	 */
	public static ITableSvc getTableSvcImplInstance(List<SeedDataBO> list) {
		if (tableSvc == null) {
			tableSvc = new TableSvcImpl(list);
		}
		return tableSvc;
	}

	@Override
	public DataTableModle getWsiDiTable(String fileName) throws CalLiteGUIException {
		String errorData = "";
		fileName = Paths.get(fileName).toString();
		try {
			Object[][] data = null;
			List<String> lines = fileSystemSvc.getFileDataForTables(fileName);
			String[] columnName = new String[2];
			columnName[0] = "wsi";
			columnName[1] = "di";
			// We are trying to remove all the strings from the array.
			lines = lines.stream().filter(line -> isInteger(line.split(Constant.TAB_OR_SPACE_DELIMITER)[0]))
			        .collect(Collectors.toList());
			data = new String[lines.size()][columnName.length];
			for (int m = 0; m < data.length; m++) {
				String str = lines.get(m);
				errorData = str;
				String[] lineData = str.split(Constant.TAB_OR_SPACE_DELIMITER);
				List<String> temp = removeAllEmptyFromArray(lineData);
				data[m][0] = temp.get(0);
				data[m][1] = temp.get(1);
			}
			return new DataTableModle(fileName, columnName, data, false);
		} catch (NullPointerException ex) {
			throw new CalLiteGUIException(
			        "The data in the table is incorrect and the data is \"" + errorData + "\". The table name is = " + fileName,
			        ex);
		} catch (IndexOutOfBoundsException ex) {
			throw new CalLiteGUIException(
			        "The data in the table is incorrect and the data is \"" + errorData + "\". The table name is = " + fileName,
			        ex);
		} catch (Exception ex) {
			throw new CalLiteGUIException(
			        "There is a error in the table. File Name = " + fileName + Constant.NEW_LINE + ex.getMessage(), ex);
		}
	}

	@Override
	public DataTableModle handleTableFileWithColumnNumber(String tableName) throws CalLiteGUIException {
		String errorData = "";
		String fileName = Paths.get(Constant.Model_w2_WRESL_LOOKUP_DIR + tableName + Constant.FILE_EXT_TABLE).toString();
		try {
			String[][] data = null;
			List<String> lines = fileSystemSvc.getFileDataForTables(fileName);
			String[] columnName = getColumnName(lines);
			// We are trying to remove all the strings from the array.
			lines = lines.stream().filter(line -> isInteger(line.split(Constant.TAB_OR_SPACE_DELIMITER)[0]))
			        .collect(Collectors.toList());
			int noOfRows = getRowNumbers(lines);
			data = new String[noOfRows][columnName.length];
			for (int i = 0; i < noOfRows; i++) {
				data[i][0] = String.valueOf(i + 1);
			}
			for (String str : lines) {
				errorData = str;
				String[] arr = str.split(Constant.TAB_OR_SPACE_DELIMITER);
				data[Integer.parseInt(arr[0]) - 1][Integer.parseInt(arr[1])] = arr[2];
			}
			return new DataTableModle(tableName, columnName, data, false);
		} catch (NullPointerException ex) {
			throw new CalLiteGUIException(
			        "The data in the table is incorrect and the data is \"" + errorData + "\". The table name is = " + fileName,
			        ex);
		} catch (CalLiteGUIException ex) {
			throw new CalLiteGUIException(
			        "There is a error in the table. Table Name = " + tableName + Constant.NEW_LINE + ex.getMessage(), ex);
		}
	}

	@Override
	public DataTableModle getTable(String tableName) throws CalLiteGUIException {
		DataTableModle dtm = map.get(tableName);
		if (dtm == null) {
			log.info("The data table is not available. the table name is " + tableName);
		}
		return dtm;
	}

	@Override
	public String getWsidiFileSuffix() {
		return wsidiFileSuffix;
	}

	@Override
	public void setWsidiFileSuffix(String wsidiFileSuffix) {
		this.wsidiFileSuffix = wsidiFileSuffix;
	}

	/*
	 * This will the table map.
	 */
	private TableSvcImpl(List<SeedDataBO> seedDataBOList) {
		log.info("Building TableSvcImpl Object.");
		this.fileSystemSvc = new FileSystemSvcImpl();
		this.map = new HashMap<String, DataTableModle>();
		this.errorHandlingSvc = new ErrorHandlingSvcImpl();
		generateMapForTables(seedDataBOList);
		// TODO remove the following line after development is completed.
		printTheMap();
	}

	/**
	 * This will build the map of tables with the seed data.
	 *
	 * @param seedDataBOList
	 */
	private void generateMapForTables(List<SeedDataBO> seedDataBOList) {
		List<String> list1 = new ArrayList<String>();
		list1.add("wsi_di_swp");
		list1.add("wsi_di_cvp_sys");
		list1.add("IFBypassFlows_vs_SAC");
		list1.add("IsolatedFacility");
		for (SeedDataBO seedDataBOObj : seedDataBOList) {
			try {
				String dataTableName = seedDataBOObj.getDataTables();
				if (!list1.contains(dataTableName)) {
					if (dataTableName.contains(Constant.PIPELINE)) {
						String[] tableNames = dataTableName.split(Constant.PIPELINE_DELIMITER);
						map.putAll(handleX2table(tableNames[0], tableNames[1]));
					} else {
						map.putAll(getAllTypesOfTableForATableName(seedDataBOObj));
					}
				}
			} catch (CalLiteGUIException ex) {
				log.error(ex.getMessage(), ex);
				errorHandlingSvc.displayErrorMessageBeforeTheUI(ex);
			}
		}
	}

	/**
	 * This method is specially for X2 table only.
	 *
	 * @param tableName1
	 *            1st table name. Just the Table Name without the extension and path. This will by default take the extension as
	 *            ".table" and the path as lookup under the model_w2.
	 * @param tableName2
	 *            2nd table name. Just the Table Name without the extension and path. This will by default take the extension as
	 *            ".table" and the path as lookup under the model_w2.
	 * @return This will return the Map of tables with the table name as key and table data as value.
	 * @throws CalLiteGUIException
	 *             If anything wrong about the file then it will throw a exception with the information about it.
	 */
	private Map<String, DataTableModle> handleX2table(String tableName1, String tableName2) throws CalLiteGUIException {
		Map<String, DataTableModle> tempMapForDataTable = new HashMap<String, DataTableModle>();
		DataTableModle dtm1 = null;
		DataTableModle dtm2 = null;
		// Getting the two tables.
		if (tableName1.equals("gui_x2active")) {
			dtm1 = handleTableFileWithOnlyTwoColumns(tableName1);
			dtm2 = handleTableFileWithColumnNumber(tableName2);
		} else {
			dtm1 = handleTableFileWithOnlyTwoColumns(tableName1);
			dtm2 = handleTableFileWithColumnNumber(tableName2);
		}
		// Combining the tables into one table.
		String[] colName1 = dtm1.getColumnNames();
		String[] colName2 = dtm2.getColumnNames();
		String[] newColNames = { colName1[0], colName1[1], colName2[1], colName2[2], colName2[3], colName2[4], colName2[5] };
		String[][] data1 = (String[][]) dtm1.getData();
		String[][] data2 = (String[][]) dtm2.getData();
		Object[][] newData = new Object[data1.length][newColNames.length];
		for (int i = 0; i < data1.length; i++) {
			for (int j = 0; j < data1[0].length; j++) {
				if (j == 1) {
					newData[i][j] = data1[i][j].equals("1") ? Boolean.valueOf("true") : Boolean.valueOf("true");
				} else {
					newData[i][j] = data1[i][j];
				}
			}
		}
		for (int i = 0; i < newData.length; i++) {
			for (int j = 2; j < newData[0].length; j++) {
				newData[i][j] = data2[i][j - 1];
			}
		}
		DataTableModle newDtm = new DataTableModle(tableName1 + Constant.PIPELINE + tableName2, newColNames, newData, false);
		tempMapForDataTable.put(tableName1 + Constant.PIPELINE + tableName2 + Constant.DASH + Constant.D1641, newDtm);
		return tempMapForDataTable;
	}

	/**
	 * This will take the {@link SeedDataBO} object and build the table map depending on the {@link SeedDataBO}.
	 *
	 * @param seedDataBOObj
	 * @return This will return the Map of tables with the table name as key and table data as value.
	 * @throws CalLiteGUIException
	 *             If anything wrong about the file then it will throw a exception with the information about it.
	 */
	private Map<String, DataTableModle> getAllTypesOfTableForATableName(SeedDataBO seedDataBOObj) throws CalLiteGUIException {
		Map<String, DataTableModle> tempMapForDataTable = new HashMap<String, DataTableModle>();
		String dataTableName = seedDataBOObj.getDataTables();
		DataTableModle dtm = null;
		if (seedDataBOObj.getD1485D1641().equals(Constant.N_A)) {
			if (dataTableName.equals("gui_EIRatio")) {
				dtm = handleTableFileWithOnlyTwoColumns(dataTableName);
			} else if (dataTableName.equals("perc_UnimparedFlow")) {
				dtm = handleTableFileLikeTable(dataTableName);
			} else if (dataTableName.equals("gui_EIsjr")) {
				dtm = handleTableFileForEIsjr(dataTableName);
			} else {
				dtm = handleTableFileWithColumnNumber(dataTableName);
			}
			if (seedDataBOObj.getD1485().equalsIgnoreCase(Constant.N_A)) {
				tempMapForDataTable.put(dataTableName + Constant.DASH + Constant.D1485, dtm);
			}
			if (seedDataBOObj.getD1641().equalsIgnoreCase(Constant.N_A)) {
				tempMapForDataTable.put(dataTableName + Constant.DASH + Constant.D1641, dtm);
			}
			if (seedDataBOObj.getUserDefined().equalsIgnoreCase(Constant.N_A)) {
				tempMapForDataTable.put(dataTableName, dtm);
			}
		} else {
			String name = dataTableName.replace("gui_", "");
			List<DataTableModle> dtmList = new ArrayList<DataTableModle>();
			if (dataTableName.equals("gui_xchanneldays") || dataTableName.equals("gui_RioVista")) {
				dtmList = handleTableFileWithTwoTableData(name);
				if (seedDataBOObj.getD1485().equalsIgnoreCase(Constant.N_A)) {
					tempMapForDataTable.put(dataTableName + Constant.DASH + Constant.D1485, dtmList.get(1));
				}
				if (seedDataBOObj.getD1641().equalsIgnoreCase(Constant.N_A)) {
					tempMapForDataTable.put(dataTableName + Constant.DASH + Constant.D1641, dtmList.get(0));
				}
			} else {
				if (seedDataBOObj.getD1485().equalsIgnoreCase(Constant.N_A)) {
					dtm = handleTableFileWithColumnNumber(name + Constant.DASH + Constant.D1485);
					tempMapForDataTable.put(dataTableName + Constant.DASH + Constant.D1485, dtm);
				}
				if (seedDataBOObj.getD1641().equalsIgnoreCase(Constant.N_A)) {
					dtm = handleTableFileWithColumnNumber(name + Constant.DASH + Constant.D1641);
					tempMapForDataTable.put(dataTableName + Constant.DASH + Constant.D1641, dtm);
				}
			}
		}
		return tempMapForDataTable;
	}

	/**
	 * This method will handle the table format with only two columns.
	 *
	 * @param tableName
	 *            Just the Table Name without the extension and path. This will by default take the extension as ".table" and the
	 *            path as lookup under the model_w2.
	 * @return This will return the {@link DataTableModle} object of the table.
	 * @throws CalLiteGUIException
	 *             If anything wrong about the file then it will throw a exception with the information about it.
	 */
	private DataTableModle handleTableFileWithOnlyTwoColumns(String tableName) throws CalLiteGUIException {
		String errorData = "";
		String fileName = Paths.get(Constant.Model_w2_WRESL_LOOKUP_DIR + tableName + Constant.FILE_EXT_TABLE).toString();
		try {
			Object[][] data = null;
			List<String> lines = fileSystemSvc.getFileDataForTables(fileName);
			String[] columnName = getColumnName(lines);
			// We are trying to remove all the strings from the array.
			lines = lines.stream().filter(line -> isInteger(line.split(Constant.TAB_OR_SPACE_DELIMITER)[0]))
			        .collect(Collectors.toList());
			data = new String[lines.size()][columnName.length];
			for (String str : lines) {
				errorData = str;
				String[] arr = str.split(Constant.TAB_OR_SPACE_DELIMITER);
				data[Integer.parseInt(arr[0]) - 1][0] = arr[0];
				data[Integer.parseInt(arr[0]) - 1][1] = arr[1];
			}
			return new DataTableModle(tableName, columnName, data, false);
		} catch (NullPointerException ex) {
			throw new CalLiteGUIException(
			        "The data in the table is incorrect and the data is \"" + errorData + "\". The table name is = " + fileName,
			        ex);
		} catch (CalLiteGUIException ex) {
			throw new CalLiteGUIException(
			        "There is a error in the table. Table Name = " + tableName + Constant.NEW_LINE + ex.getMessage(), ex);
		}
	}

	/**
	 * This method is specially for "gui_eisjr" table format only.
	 *
	 * @param tableName
	 *            Just the Table Name without the extension and path. This will by default take the extension as ".table" and the
	 *            path as lookup under the model_w2.
	 * @return This will return the {@link DataTableModle} object of the table.
	 * @throws CalLiteGUIException
	 *             If anything wrong about the file then it will throw a exception with the information about it.
	 */
	private DataTableModle handleTableFileForEIsjr(String tableName) throws CalLiteGUIException {
		String errorData = "";
		String fileName = Paths.get(Constant.Model_w2_WRESL_LOOKUP_DIR + tableName + Constant.FILE_EXT_TABLE).toString();
		try {
			Object[][] data = null;
			List<String> lines = fileSystemSvc.getFileDataForTables(fileName);
			String[] columnName = getColumnName(lines);
			// We are trying to remove all the strings from the array.
			lines = lines.stream().filter(line -> isInteger(line.split(Constant.TAB_OR_SPACE_DELIMITER)[0]))
			        .collect(Collectors.toList());
			int noOfRows = getRowNumbers(lines);
			data = new String[noOfRows][columnName.length];
			for (int i = 0; i < noOfRows; i++) {
				data[i][0] = String.valueOf(i + 1);
			}
			String colNo = "1";
			int offsetNo = 1;
			int multiplierNo = 2;
			for (String str : lines) {
				errorData = str;
				String[] arr = str.split(Constant.TAB_OR_SPACE_DELIMITER);
				if (arr[1].equals(colNo)) {
					data[Integer.parseInt(arr[0]) - 1][offsetNo] = arr[2];
					data[Integer.parseInt(arr[0]) - 1][multiplierNo] = arr[3];
				} else {
					colNo = arr[1];
					offsetNo += 2;
					multiplierNo += 2;
					data[Integer.parseInt(arr[0]) - 1][offsetNo] = arr[2];
					data[Integer.parseInt(arr[0]) - 1][multiplierNo] = arr[3];
				}
			}
			return new DataTableModle(tableName, columnName, data, false);
		} catch (NullPointerException ex) {
			throw new CalLiteGUIException(
			        "The data in the table is incorrect and the data is \"" + errorData + "\". The table name is = " + fileName,
			        ex);
		} catch (IndexOutOfBoundsException ex) {
			throw new CalLiteGUIException(
			        "The data in the table is incorrect and the data is \"" + errorData + "\". The table name is = " + fileName,
			        ex);
		} catch (CalLiteGUIException ex) {
			throw new CalLiteGUIException(
			        "There is a error in the table. Table Name = " + tableName + Constant.NEW_LINE + ex.getMessage(), ex);
		}
	}

	/**
	 * This method will handle the table format which are excality like the table which is displayed on the UI.
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
	 *            Just the Table Name without the extension and path. This will by default take the extension as ".table" and the
	 *            path as lookup under the model_w2.
	 * @return This will return the {@link DataTableModle} object of the table.
	 * @throws CalLiteGUIException
	 *             If anything wrong about the file then it will throw a exception with the information about it.
	 */
	private DataTableModle handleTableFileLikeTable(String tableName) throws CalLiteGUIException {
		String errorData = "";
		String fileName = Paths.get(Constant.Model_w2_WRESL_LOOKUP_DIR + tableName + Constant.FILE_EXT_TABLE).toString();
		try {
			String[][] data = null;
			List<String> lines = fileSystemSvc.getFileDataForTables(fileName);
			String[] columnName = getColumnName(lines);
			// We are trying to remove all the strings from the array.
			lines = lines.stream().filter(line -> isInteger(line.split(Constant.TAB_OR_SPACE_DELIMITER)[0]))
			        .collect(Collectors.toList());
			data = new String[lines.size()][columnName.length];
			for (int i = 0; i < data.length; i++) {
				String[] lineData = lines.get(i).split(Constant.TAB_OR_SPACE_DELIMITER);
				List<String> temp = removeAllEmptyFromArray(lineData);
				for (int j = 0; j < data[0].length; j++) {
					data[i][j] = temp.get(j);
				}
			}
			return new DataTableModle(tableName, columnName, data, false);
		} catch (NullPointerException ex) {
			throw new CalLiteGUIException(
			        "The data in the table is incorrect and the data is \"" + errorData + "\". The table name is = " + fileName,
			        ex);
		} catch (IndexOutOfBoundsException ex) {
			throw new CalLiteGUIException(
			        "The data in the table is incorrect and the data is \"" + errorData + "\". The table name is = " + fileName,
			        ex);
		} catch (CalLiteGUIException ex) {
			throw new CalLiteGUIException(
			        "There is a error in the table. Table Name = " + tableName + Constant.NEW_LINE + ex.getMessage(), ex);
		}
	}

	/**
	 * This method will handle the table format shown bellow.
	 *
	 * <pre>
	 * month   days_open_D1641   days_open_D1485
	 * 	  1 		31 				31
	 * 	  2 		20 				30
	 *    3 		16 				31
	 *    4 		11 				0
	 * </pre>
	 *
	 * @param tableName
	 *            Just the Table Name without the extension and path. This will by default take the extension as ".table" and the
	 *            path as lookup under the model_w2.
	 * @return This will return the {@link DataTableModle} object of the table.
	 * @throws CalLiteGUIException
	 *             If anything wrong about the file then it will throw a exception with the information about it.
	 */
	private List<DataTableModle> handleTableFileWithTwoTableData(String tableName) throws CalLiteGUIException {
		String errorData = "";
		String fileName = Paths.get(Constant.Model_w2_WRESL_LOOKUP_DIR + tableName + Constant.FILE_EXT_TABLE).toString();
		try {
			List<DataTableModle> tempDataTableList = new ArrayList<DataTableModle>();
			Object[][] dataForD1641 = null;
			Object[][] dataForD1485 = null;
			List<String> lines = fileSystemSvc.getFileDataForTables(fileName);
			String[] columnName = getColumnName(lines);
			// We are trying to remove all the strings from the array.
			lines = lines.stream().filter(line -> isInteger(line.split(Constant.TAB_OR_SPACE_DELIMITER)[0]))
			        .collect(Collectors.toList());
			if (removeAllEmptyFromArray(lines.get(0).split(Constant.TAB_OR_SPACE_DELIMITER)).size() == 4) {
				int noOfRows = 12;
				dataForD1641 = new String[noOfRows][columnName.length];
				dataForD1485 = new String[noOfRows][columnName.length];
				for (int i = 0; i < noOfRows; i++) {
					dataForD1641[i][0] = String.valueOf(i + 1);
					dataForD1485[i][0] = String.valueOf(i + 1);
				}
				for (String line : lines) {
					errorData = line;
					String[] lineData = line.split(Constant.TAB_OR_SPACE_DELIMITER);
					List<String> temp = removeAllEmptyFromArray(lineData);
					int index = Integer.parseInt(temp.get(0));
					int col = Integer.parseInt(temp.get(1));
					dataForD1641[index - 1][col] = temp.get(2);
					dataForD1485[index - 1][col] = temp.get(3);
				}
			} else {
				dataForD1641 = new String[lines.size()][columnName.length];
				dataForD1485 = new String[lines.size()][columnName.length];
				for (String line : lines) {
					errorData = line;
					String[] lineData = line.split(Constant.TAB_OR_SPACE_DELIMITER);
					List<String> temp = removeAllEmptyFromArray(lineData);
					int index = Integer.parseInt(temp.get(0));
					dataForD1641[index - 1][0] = temp.get(0);
					dataForD1641[index - 1][1] = temp.get(1);
					dataForD1485[index - 1][0] = temp.get(0);
					dataForD1485[index - 1][1] = temp.get(2);
				}
			}
			tempDataTableList.add(new DataTableModle(tableName + Constant.DASH + Constant.D1641, columnName, dataForD1641, false));
			tempDataTableList.add(new DataTableModle(tableName + Constant.DASH + Constant.D1485, columnName, dataForD1485, false));
			return tempDataTableList;
		} catch (NullPointerException ex) {
			throw new CalLiteGUIException(
			        "The data in the table is incorrect and the data is \"" + errorData + "\". The table name is = " + fileName,
			        ex);
		} catch (IndexOutOfBoundsException ex) {
			throw new CalLiteGUIException(
			        "The data in the table is incorrect and the data is \"" + errorData + "\". The table name is = " + fileName,
			        ex);
		} catch (CalLiteGUIException ex) {
			throw new CalLiteGUIException(
			        "There is a error in the table. Table Name = " + tableName + Constant.NEW_LINE + ex.getMessage(), ex);
		}
	}

	/**
	 * This method is used to get the real values from the array passed in.
	 *
	 * @param lineData
	 * @return
	 */
	private List<String> removeAllEmptyFromArray(String[] lineData) {
		List<String> temp = new ArrayList<String>();
		for (int m = 0; m < lineData.length; m++) {
			if (!(lineData[m].equals(Constant.SPACE) || lineData[m].equals(null) || lineData[m].equals(""))) {
				temp.add(lineData[m]);
			}
		}
		return temp;
	}

	/**
	 * This will return the Columns names in a {@link String} array.
	 *
	 * @param data
	 *            The table data.
	 * @return columns names as {@link String} Array.
	 * @throws CalLiteGUIException
	 */
	private String[] getColumnName(List<String> data) throws CalLiteGUIException {
		try {
			String header = null;
			header = data.stream().filter(obj -> obj.contains(Constant.HEADERS)).findFirst().get();
			String[] da = header.split(Constant.OLD_DELIMITER);
			String[] headers = new String[da.length - 1]; // We are excluding the header word.
			for (int i = 0; i < headers.length; i++) {
				headers[i] = da[i + 1];
			}
			return headers;
		} catch (NoSuchElementException ex) {
			throw new CalLiteGUIException(
			        "The Header is missing or not been formarted correctly in the table." + Constant.NEW_LINE + ex.getMessage(),
			        ex);
		}
	}

	/**
	 * This will take the table data and return how many rows in that table.
	 *
	 * @param data
	 * @return Number of rows.
	 */
	private int getRowNumbers(List<String> data) {
		int noOfRows = 0;
		for (String string : data) {
			if (Integer.parseInt(string.split(Constant.TAB_OR_SPACE_DELIMITER)[1]) == 1) {
				noOfRows++;
			}
		}
		return noOfRows;
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

	// TODO remove the following method after development is completed.
	private void printTheMap() {
		log.debug(
		        "######################################## Print Map Start ############################################################");
		Set<String> key = this.map.keySet();
		key.forEach(keyString -> {
			log.debug(keyString);
			DataTableModle ddd = this.map.get(keyString);
			ddd.print();
		});
		log.debug(
		        "######################################## Print Map Stop ############################################################");
	}
}