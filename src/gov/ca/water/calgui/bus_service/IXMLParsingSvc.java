package gov.ca.water.calgui.bus_service;

import java.util.List;
import java.util.Set;

import org.swixml.SwingEngine;

/**
 * This is the interface for parsing the GUI.xml into SwingEngine.
 */
public interface IXMLParsingSvc {

	/**
	 * This method will return the object of current {@link SwingEngine}.
	 *
	 * @return
	 */
	public SwingEngine getSwingEngine();

	/**
	 * This will return the control Id which are Visible in the {@link SwingEngine} Object.
	 *
	 * @return
	 */
	public Set<String> getIdFromXML();

	/**
	 * If you pass in the name of the component it will return the id of it.
	 *
	 * @param name
	 *            Name of the component.
	 * @return
	 */
	public String getcompIdfromName(String name);

	/**
	 * This will return the table id which are defined by the user. It will exclude some default table ids.
	 *
	 * @return
	 */
	public List<String> getNewUserDefinedTables();
}
