package gov.ca.water.calgui.tech_service;

import java.util.List;

import org.w3c.dom.Document;

import gov.ca.water.calgui.bo.CalLiteGUIException;

/**
 * This is the interface for File Handling like reading and saving.
 */
public interface IFileSystemSvc {

	/**
	 * This will take the file name and return the lines in the file as list of strings.
	 *
	 * @param fileName
	 *            Full path and the file Name.
	 * @param isRequired
	 *            If file is required for the application to start and if the file is missing then it will throw the exception.
	 * @return return the lines in the file as list.
	 * @throws CalLiteGUIException
	 */
	public List<String> getFileData(String fileName, boolean isRequired) throws CalLiteGUIException;

	/**
	 * This will remove all the comment lines from the table file except the header line.
	 *
	 * @param fileName
	 *            Full path and the file Name.
	 * @return return the lines in the file as list after removing the comments.
	 * @throws CalLiteGUIException
	 */
	public List<String> getFileDataForTables(String fileName) throws CalLiteGUIException;

	/**
	 *
	 * @param fileName
	 * @param isRequired
	 * @return
	 * @throws FileNotFoundException
	 */
	/**
	 * This will remove all the comment lines from the file.
	 *
	 * @param fileName
	 *            Full path and the file Name.
	 * @param isRequired
	 *            If file is required for the application to start and if the file is missing then it will exit the program. If not
	 *            throw the exception.
	 * @return return the lines in the file as list after removing all comments.
	 * @throws CalLiteGUIException
	 */
	public List<String> getFileDataWithoutComment(String fileName, boolean isRequired) throws CalLiteGUIException;

	/**
	 *
	 * @param fileName
	 *            Full path and the file Name.
	 * @param isRequired
	 *            If file is required for the application to start and if the file is missing then it will exit the program. If not
	 *            throw the exception.
	 * @return return the all comment lines in the file as list.
	 * @throws CalLiteGUIException
	 */
	public List<String> getFileDataWithCommentOnly(String fileName, boolean isRequired) throws CalLiteGUIException;

	/**
	 * This will save the given data into the given file.
	 *
	 * @param fileName
	 *            Full path and the file Name.
	 * @param data
	 *            The data which is writen to the file.
	 * @throws CalLiteGUIException
	 */
	public void saveDataToFile(String fileName, String data) throws CalLiteGUIException;

	/**
	 * This will generate the Document from the XML file.
	 * 
	 * @return
	 * @throws CalLiteGUIException
	 */
	public Document getXMLDocument() throws CalLiteGUIException;
}