package gov.ca.water.calgui.bus_service;

import gov.ca.water.calgui.bo.CalLiteGUIException;
import gov.ca.water.calgui.presentation.DataTableModle;

/**
 * This is the interface for all the tables.
 */
public interface ITableSvc {
	/**
	 * This method will take the Table Name and return the DataTableModle object of that table.
	 *
	 * @param tableName
	 *            Name of the table with the option like D1641 or D1485.
	 * @return It will return the table as {@link DataTableModle}.
	 * @throws CalLiteGUIException
	 *             When loading the tables if it gets an error it will throw it.
	 */
	public DataTableModle getTable(String tableName) throws CalLiteGUIException;

	/**
	 * This will hold the suffix of the WSIDI file value.
	 *
	 * @return This will return the WSIDI Suffix value.
	 */
	public String getWsidiFileSuffix();

	/**
	 * This will set the suffix value of WSIDI.
	 *
	 * @param wsidiFileSuffix
	 */
	public void setWsidiFileSuffix(String wsidiFileSuffix);

	/**
	 *
	 * This method will handle the table format which are excality like the table which is displayed on the UI.
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
	 *            Just the Table Name without the extension and path. This will by default take the extension as .table and the path
	 *            as lookup under the model_w2.
	 * @return Return the Object of {@link DataTableModle} with the table data in it.
	 * @throws CalLiteGUIException
	 *             If anything wrong about the file then it will throw a exception with the information about it.
	 */
	public DataTableModle handleTableFileWithColumnNumber(String tableName) throws CalLiteGUIException;

	/**
	 * This method is used to get the WSIDI tables.
	 *
	 * @param fileName
	 *            The whole path of the file with the table name and the extension.
	 * @return Return the Object of {@link DataTableModle} with the table data in it.
	 * @throws CalLiteGUIException
	 *             If anything wrong about the file then it will throw a exception with the information about it.
	 */
	public DataTableModle getWsiDiTable(String fileName) throws CalLiteGUIException;
}
