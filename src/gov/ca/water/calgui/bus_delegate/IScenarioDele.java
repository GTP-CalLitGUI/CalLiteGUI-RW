package gov.ca.water.calgui.bus_delegate;

import java.util.List;

import gov.ca.water.calgui.presentation.DataTableModle;
import gov.ca.water.calgui.presentation.ScenarioFrame;

/**
 * This is used to show the Scenario state of the application and also compare to the saved files if provided.
 *
 *
 */
public interface IScenarioDele {
	/**
	 * This will add the Current_Scenario.CLS file to the file names and then build the List of {@link DataTableModle}.
	 *
	 * @param fileNames
	 *            - file names that should be displayed in the {@link ScenarioFrame}. Send null if you want to see only the current
	 *            Scenario in the frame.
	 * @return will return only one object if the file name is null that is the Current Scenario. Otherwise it will return three
	 *         objects in which the 1st is Current Scenario, 2nd is the comparison and the 3ed is differences.
	 */
	public List<DataTableModle> getScenarioTableData(List<String> fileNames);
}
