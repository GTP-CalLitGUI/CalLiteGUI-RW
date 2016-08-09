package gov.ca.water.calgui.bus_delegate;

import javax.swing.JComponent;
import javax.swing.JTable;

public interface IAllButtonsDele {

	public void loadScenarioButton();

	public void saveAsButton();

	public boolean saveCurrentStateToFile();

	public boolean saveCurrentStateToFile(String clsFileName, boolean isSaveAs);

	public boolean saveForViewScen();

	public void runSingleBatch();

	public void runSingleBatchForWsiDi();

	public void runMultipleBatch();

	public void editButtonOnOperations(JComponent component);

	public void helpButton();

	public void aboutButton();

	public void windowClosing();

	public void copyTableValues(JTable table);

	public void pasteTableValues(JTable table);
}
