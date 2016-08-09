package gov.ca.water.calgui.bus_service;

import java.util.List;
import java.util.Map;

import org.swixml.SwingEngine;

import gov.ca.water.calgui.bo.CalLiteGUIException;
import gov.ca.water.calgui.bo.SeedDataBO;
import gov.ca.water.calgui.presentation.DataTableModle;

/**
 * This is the interface for handling the cls file and saving the data.
 */
public interface IResultSvc {

	/**
	 * This will open the cls file and build the following list of strings.
	 *
	 * <pre>
	 * controlStrList
	 * dataTableModelStrList
	 * regulationoptionsStr
	 * </pre>
	 *
	 * @param fileName
	 *            The cls file name with complete path.
	 * @param controlStrList
	 *            It will take the empty list. When the method is completed this is filled with the control id string from the cls
	 *            file.
	 * @param dataTableModelStrList
	 *            It will take the empty list. When the method is completed this is filled with the data table strings from the cls
	 *            file.
	 * @param regulationoptionsStr
	 *            It will take the empty list. When the method is completed this is filled with the regulation options string from
	 *            the cls file.
	 */
	public void getCLSData(String fileName, List<String> controlStrList, List<String> dataTableModelStrList,
	        List<String> regulationoptionsStr);

	/**
	 * This will open the cls file read in the data and apply it for the current ui.
	 *
	 * @param fileName
	 *            The cls file name with complete path.
	 * @param swingEngine
	 * @param tableMap
	 *            The map with key as the table id and value as table object.
	 */
	public void applyClsFile(String fileName, SwingEngine swingEngine, Map<String, SeedDataBO> tableMap);

	/**
	 * This will save the current state of the ui into the cls file and the Scenario directory.
	 *
	 * @param fileName
	 *            The cls file name with out the path and the extension.
	 * @param swingEngine
	 * @param seedDataBOList
	 *            The data list from gui_link2.table.
	 */
	public boolean save(String fileName, SwingEngine swingEngine, List<SeedDataBO> seedDataBOList);

	/**
	 * This will return the Regulation options data.
	 *
	 * @return
	 */
	public int[] getRegulationoptions();

	/**
	 * This will add the {@code tableName} as key and the {@code dataTableModle} as value to the user defined table map.
	 *
	 * @param tableName
	 * @param dataTableModle
	 */
	public void addUserDefinedTable(String tableName, DataTableModle dataTableModle);

	/**
	 * Will return the user defined table for the given {@code tableName}. if the table is not there it will return null.
	 *
	 * @param tableName
	 *            Just table name as per the gui_link2.table.
	 * @return
	 */
	public DataTableModle getUserDefinedTable(String tableName);

	/**
	 * This will tell whether the table name has the user defined table or not.
	 *
	 * @param tableName
	 *            Just table name as per the gui_link2.table.
	 * @return
	 */
	public boolean hasUserDefinedTable(String tableName);

	/**
	 * It will delete the table if it is in the user defined table map.
	 *
	 * @param tableName
	 *            Just table name as per the gui_link2.table.
	 */
	public void removeUserDefinedTable(String tableName);

	/**
	 * It will return true when the cls file is loading.
	 *
	 * @return
	 */
	public boolean isCLSFileLoading();

	/**
	 * This will save the current ui state to the cls file.
	 *
	 * @param fileName
	 *            Just the file name with out the path and extension.
	 * @param swingEngine
	 * @param seedDataBOList
	 * @throws CalLiteGUIException
	 */
	public void saveToCLSFile(String fileName, SwingEngine swingEngine, List<SeedDataBO> seedDataBOList) throws CalLiteGUIException;
}
