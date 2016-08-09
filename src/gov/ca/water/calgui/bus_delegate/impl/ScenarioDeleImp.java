package gov.ca.water.calgui.bus_delegate.impl;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.swixml.SwingEngine;

import gov.ca.water.calgui.bo.ScenarioDisplayBO;
import gov.ca.water.calgui.bo.SeedDataBO;
import gov.ca.water.calgui.bus_delegate.IScenarioDele;
import gov.ca.water.calgui.bus_service.IResultSvc;
import gov.ca.water.calgui.bus_service.ISeedDataSvc;
import gov.ca.water.calgui.bus_service.IXMLParsingSvc;
import gov.ca.water.calgui.bus_service.impl.ResultSvcImpl;
import gov.ca.water.calgui.bus_service.impl.SeedDataSvcImpl;
import gov.ca.water.calgui.bus_service.impl.XMLParsingSvcImpl;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.presentation.DataTableModle;
import gov.ca.water.calgui.presentation.NumericTextField;

public class ScenarioDeleImp implements IScenarioDele {
	private IXMLParsingSvc xmlParsingSvc = XMLParsingSvcImpl.getXMLParsingSvcImplInstance();
	private SwingEngine swingEngine = xmlParsingSvc.getSwingEngine();
	private IResultSvc resultSvc = ResultSvcImpl.getResultSvcImplInstance();
	private ISeedDataSvc seedDataSvc = SeedDataSvcImpl.getSeedDataSvcImplInstance();

	@Override
	public List<DataTableModle> getScenarioTableData(List<String> fileNames) {
		if (fileNames == null) {
			fileNames = new ArrayList<String>();
		}
		String currentScenario = Constant.CURRENT_SCENARIO + Constant.CLS_EXT;
		fileNames.add(0, currentScenario);
		List<DataTableModle> dtmList = new ArrayList<DataTableModle>();
		if (fileNames.size() > 1) {
			DataTableModle comparisonDTM = buildScenarioTables(fileNames);
			DataTableModle baseDTM = builsBaseScenarioTables(comparisonDTM);
			DataTableModle differenceDTM = differenceScenarioTables(comparisonDTM);
			dtmList.add(baseDTM);
			dtmList.add(comparisonDTM);
			dtmList.add(differenceDTM);
		} else {
			dtmList.add(buildScenarioTables(fileNames));
		}
		return dtmList;
	}

	/**
	 * This method will build the Map's for all the files and then convert them into the {@link DataTableModle}.
	 *
	 * @param fileNames
	 *            - names of the cls file.
	 * @return The Object of {@link DataTableModle} which has the comparison for all the files which are passed in from the @param
	 *         fileName. If there is only one file name then it will give the base one.
	 */
	private DataTableModle buildScenarioTables(List<String> fileNames) {
		List<Map<String, String>> clsMapList = new ArrayList<Map<String, String>>();
		String[] columnNames = new String[fileNames.size() + 1];
		columnNames[0] = "Dashboard Options";
		int index = 1;
		// This will take the file names and get the Map of each cls file.
		for (String fileName : fileNames) {
			clsMapList.add(buildMapOfCLSFile(fileName, swingEngine, seedDataSvc.getTableIdMap()));
			columnNames[index] = fileName;
			index++;
		}
		/*
		 * This will take all map's of the cls file and make a linear structure of the file. we are using List because we don't know
		 * the no of rows. It may vary based on file.
		 */
		List<Object[]> data = new ArrayList<Object[]>();
		Map<String, String> firstMap = clsMapList.get(0);
		for (String key : firstMap.keySet()) {
			Object[] temp = new Object[columnNames.length];
			if (key.startsWith(Constant.EMPTY)) {
				temp[0] = key.replace(Constant.EMPTY + Constant.DASH, " ");
			} else {
				temp[0] = key;
			}
			index = 1;
			for (Map<String, String> map : clsMapList) {
				temp[index] = map.get(key);
				index++;
			}
			data.add(temp);
		}
		// This will change the data into the structure which the DataTableModle accepts.
		Object[][] tableData = new Object[data.size()][];
		index = 0;
		for (Object[] objects : data) {
			tableData[index] = objects;
			index++;
		}
		return new DataTableModle("", columnNames, tableData, false);
	}

	/**
	 * This method will give the Object of the current Scenario.
	 *
	 * @param dataTableModle
	 *            - The whole comparison data Object.
	 * @return will return the Object of the current Scenario.
	 */
	private DataTableModle builsBaseScenarioTables(DataTableModle dataTableModle) {
		Object[][] oldData = dataTableModle.getData();
		Object[][] newData = new Object[oldData.length][2];
		String[] oldColName = dataTableModle.getColumnNames();
		String[] colName = new String[2];
		colName[0] = oldColName[0];
		colName[1] = oldColName[1];
		for (int i = 0; i < newData.length; i++) {
			for (int j = 0; j < newData[0].length; j++) {
				newData[i][j] = oldData[i][j];
			}
		}
		return new DataTableModle("", colName, newData, false);
	}

	/**
	 * This will loop on the data and find the difference between the rows.
	 *
	 * @param dataTableModle
	 *            - The whole comparison data Object.
	 * @return The Object has the rows which holds different values.
	 */
	private DataTableModle differenceScenarioTables(DataTableModle dataTableModle) {
		Object[][] data = dataTableModle.getData();
		Object[][] compData = new Object[data.length][data[0].length];
		int index = 0;
		for (int i = 0; i < data.length; i++) {
			boolean isDifferent = false;
			Object[] temp = data[i];
			Object value = temp[1];
			for (int j = 2; j < temp.length; j++) {
				isDifferent = value.equals(temp[j]) ? false : true;
				if (isDifferent)
					break;
			}
			if (isDifferent) {
				compData[index] = data[i];
				index++;
			}
		}
		Object[][] newData = new Object[index][data[0].length];
		System.arraycopy(compData, 0, newData, 0, index);
		return new DataTableModle("", dataTableModle.getColumnNames(), newData, false);
	}

	/**
	 * This will read in the cls file and from that it will convert the cls file data into the Map.
	 *
	 * @param fileName
	 *            - names of the cls file.
	 * @param swingEngine
	 *            - The instance of Swing Engine.
	 * @param tableMap
	 *            - Map of the table data and the key is the table Id.
	 * @return will return the map of whole cls file.
	 */
	private Map<String, String> buildMapOfCLSFile(String fileName, SwingEngine swingEngine, Map<String, SeedDataBO> tableMap) {
		List<String> controlStrList = new ArrayList<String>();
		List<String> dataTableModelStrList = new ArrayList<String>();
		List<String> regulationoptionsStr = new ArrayList<String>();
		// Read in the cls file data.
		resultSvc.getCLSData(Constant.SCENARIOS_DIR + fileName, controlStrList, dataTableModelStrList, regulationoptionsStr);
		// This will build all the component data into list of ScenarioDisplayBO.
		List<ScenarioDisplayBO> componentData = buildCompDataIntoScenarioDisplayBOs(controlStrList, swingEngine);
		// This will convert the table Data in the cls file to the map.
		Map<String, String> tableData = buildTableDataInCLSIntoMap(dataTableModelStrList, swingEngine, tableMap);
		return buildDisplayBOsAndTableDataIntoMap(componentData, tableData);
	}

	/**
	 * This will convert the Table Data in the cls file into the map. the table rows have the key as the following format
	 * "Empty-dataTable name-RowNo".
	 *
	 * @param dataTableModelStrList
	 *            - The table Data list from the cls file.
	 * @param swingEngine
	 *            - The instance of Swing Engine.
	 * @param tableMap
	 *            - Map of the table data and the key is the table Id.
	 * @return This contain all the table Data as a map.
	 */
	private Map<String, String> buildTableDataInCLSIntoMap(List<String> dataTableModelStrList, SwingEngine swingEngine,
	        Map<String, SeedDataBO> tableMap) {
		Map<String, String> allTableData = new LinkedHashMap<String, String>();
		String tableName = "";
		for (String dataTableStr : dataTableModelStrList) {
			String[] arr = dataTableStr.split(Constant.PIPELINE_DELIMITER);
			if (isInteger(arr[0])) {
				SeedDataBO seedDataBO = tableMap.get(arr[0]);
				tableName = seedDataBO.getDataTables();
			} else {
				tableName = arr[0];
			}
			allTableData.put(tableName.toUpperCase(), Constant.SPACE);
			String[] tableData = arr[1].split(Constant.SEMICOLON);
			int index = 1;
			for (int i = 0; i < tableData.length; i++) {
				allTableData.put(Constant.EMPTY + Constant.DASH + tableName.toUpperCase() + Constant.DASH + index, tableData[i]);
				index++;
			}
		}
		return allTableData;
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
	 * This method will convert the component Data into the map and add the table Data map into the generated map.
	 *
	 * @param componentData
	 *            - The list of {@link ScenarioDisplayBO} Objects.
	 * @param tableData
	 *            - Map of the Table Data.
	 * @return This will return the map of all the values in the cls file.
	 */
	private Map<String, String> buildDisplayBOsAndTableDataIntoMap(List<ScenarioDisplayBO> componentData,
	        Map<String, String> tableData) {
		String parent1 = "";
		String parent2 = "";
		String currentParent = "";
		String currentParent2 = "";
		String space = Constant.SPACE + Constant.SPACE;
		Map<String, String> data = new LinkedHashMap<String, String>();
		// This will convert the component Data into the map.
		for (ScenarioDisplayBO scenarioDisplayBO : componentData) {
			if (scenarioDisplayBO.getComponentParents().contains(Constant.PIPELINE)) {
				String[] parentArr = scenarioDisplayBO.getComponentParents().split(Constant.PIPELINE_DELIMITER);
				if (parent1.equals("")) {
					parent1 = parentArr[0];
					parent2 = parentArr[1];
					data.put(parent1, Constant.SPACE);
					data.put(space + parent2, Constant.SPACE);
				}
				currentParent = parentArr[0];
				currentParent2 = parentArr[1];
				if (currentParent.equals(parent1)) {
					if (!currentParent2.equals(parent2)) {
						parent2 = parentArr[1];
						data.put(space + parentArr[1], Constant.SPACE);
					}
				} else {
					parent1 = currentParent;
					parent2 = currentParent2;
					data.put(currentParent, Constant.SPACE);
					data.put(space + currentParent2, Constant.SPACE);
				}
				data.put(space + space + scenarioDisplayBO.getComponentText(), scenarioDisplayBO.getComponentValue());
			} else {
				if (parent1.equals("")) {
					parent1 = scenarioDisplayBO.getComponentParents();
					data.put(parent1, Constant.SPACE);
				}
				currentParent = scenarioDisplayBO.getComponentParents();
				if (!currentParent.equals(parent1)) {
					parent1 = currentParent;
					data.put(parent1, Constant.SPACE);
				}
				data.put(space + scenarioDisplayBO.getComponentText(), scenarioDisplayBO.getComponentValue());
			}
		}
		data.putAll(tableData);
		return data;
	}

	/**
	 * This method will take the control strings from the cls file and convert them into the {@link ScenarioDisplayBO} Object. This
	 * method will also remove all the unwanted control from the cls file for the scenario display.
	 *
	 * @param controlStrList
	 *            - The control Data list from the cls file.
	 * @param swingEngine
	 *            - The instance of Swing Engine.
	 * @return This will return the list of {@link ScenarioDisplayBO}.
	 */
	private List<ScenarioDisplayBO> buildCompDataIntoScenarioDisplayBOs(List<String> controlStrList, SwingEngine swingEngine) {
		List<ScenarioDisplayBO> componentData = new ArrayList<ScenarioDisplayBO>();
		for (String controlStr : controlStrList) {
			String[] arr = controlStr.split(Constant.PIPELINE_DELIMITER);
			String componentName = arr[0];
			String value = arr[1];
			String componentText = "";
			JComponent component = (JComponent) swingEngine.find(componentName);
			String sbparents = getControlParents(component);
			// This check is to remove the unwanted data for the display.
			if (sbparents.contains("facilities"))
				continue;
			if (component instanceof JCheckBox) {
				JCheckBox jcb = (JCheckBox) component;
				if (jcb.getText().equals("")) {
					componentText = componentName;
				} else {
					componentText = jcb.getText();
				}
				if (value.equals("false"))
					value = "Turned off";
				else if (sbparents.contains("D-1641") && jcb.getText().contains("Default"))
					value = "Per D-1641";
				else
					value = "Turned on";
			} else if (component instanceof JRadioButton) {
				JRadioButton jrb = (JRadioButton) component;
				componentText = componentName;
				if (value.equals("true")) {
					Container cont = jrb;
					while (cont.getParent() != null) {
						cont = cont.getParent();
						if (cont instanceof JPanel) {
							TitledBorder tb = (TitledBorder) ((JPanel) cont).getBorder();
							if (tb != null) {
								componentText = tb.getTitle();
								break;
							}
						}
					}
					value = jrb.getText();
				} else {
					continue;
				}
			} else {
				if (component instanceof JTextField || component instanceof NumericTextField) {
					JLabel label = (JLabel) swingEngine.find(componentName + "_t");
					if (label != null)
						componentText = label.getText();
					else
						componentText = componentName;
				} else {
					componentText = componentName;
				}
			}
			switch (componentText) {
			case "run_txaScenDesc":
				componentText = "Scenario Description";
				componentData.add(new ScenarioDisplayBO(componentText, sbparents, value));
				break;
			case "spnRunStartMonth":
				componentText = "Run Start";
				componentData.add(new ScenarioDisplayBO(componentText, sbparents, value));
				break;
			case "spnRunStartYear":
				for (ScenarioDisplayBO sdBO : componentData) {
					if (sdBO.getComponentText().equals("Run Start")) {
						sdBO.setComponentValue(sdBO.getComponentValue() + Constant.SPACE + value);
					}
				}
				break;
			case "spnRunEndMonth":
				componentText = "Run End";
				componentData.add(new ScenarioDisplayBO(componentText, sbparents, value));
				break;
			case "spnRunEndYear":
				for (ScenarioDisplayBO sdBO : componentData) {
					if (sdBO.getComponentText().equals("Run End")) {
						sdBO.setComponentValue(sdBO.getComponentValue() + Constant.SPACE + value);
					}
				}
				break;
			default:
				componentData.add(new ScenarioDisplayBO(componentText, sbparents, value));
				break;
			}
		}
		return componentData;
	}

	/**
	 * This method will go to the root parent and list them as a string with separated as "|".
	 *
	 * @param component
	 *            - The component in the SwingEngine
	 * @return The string of the parents which is separated with "|".
	 */
	private String getControlParents(Component component) {
		StringBuffer sb = new StringBuffer();
		Container cont = (Container) component;
		while (cont.getParent() != null) {
			cont = cont.getParent();
			if (cont instanceof JFrame)
				break;
			if (cont instanceof JPanel) {
				JPanel jp = (JPanel) cont;
				String name = jp.getName();
				if (name != null && !name.equals("")) {
					// if (!name.equals("runsettings")) {
					if (!sb.toString().contains(name.toLowerCase())) {
						sb.append(name + Constant.PIPELINE);
					}
					// }
				}
			}
		}
		sb = sb.deleteCharAt(sb.length() - 1);
		// Reverse the string
		String[] strArray = sb.toString().split(Constant.PIPELINE_DELIMITER);
		List<String> result = new LinkedList<String>();
		for (int i = 0; i < strArray.length; i++) {
			if (!strArray[i].equals("null.contentPane")) {
				result.add(strArray[i]);
			}
		}
		Collections.reverse(result);
		StringBuffer sb1 = new StringBuffer();
		for (int i = 0; i < result.size(); i++) {
			sb1.append(result.toArray()[i].toString() + Constant.PIPELINE);
		}
		sb = sb1.deleteCharAt(sb1.length() - 1);
		return sb.toString();
	}
}