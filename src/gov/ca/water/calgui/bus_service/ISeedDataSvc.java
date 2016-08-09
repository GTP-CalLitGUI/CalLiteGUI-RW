package gov.ca.water.calgui.bus_service;

import java.util.List;
import java.util.Map;

import gov.ca.water.calgui.bo.GuiLinks4BO;
import gov.ca.water.calgui.bo.SeedDataBO;

/**
 * This is the interface for loading the GUI_Link2.table and GUI_Link4.table.
 */
public interface ISeedDataSvc {

	/**
	 * This will take the {@code guiId} and return the Object of that {@code guiId}. If the guiId is not there then it will return
	 * null.
	 *
	 * @param guiId
	 * @return
	 */
	public SeedDataBO getObjByGuiId(String guiId);

	/**
	 * This will concatenate "RunBasis_ID LOD_ID CCProject_ID CCModel_ID" id's from the Gui_Link4.table and match with the
	 * {@code id} and return the object which is matched. If the {@code id} is not there then it will return null.
	 *
	 * @param id
	 * @return
	 */
	public GuiLinks4BO getObjByRunBasisLodCcprojCcmodelIds(String id);

	/**
	 * This will return the list of {@link SeedDataBO} objects which has data Table value in it.
	 *
	 * @return
	 */
	public List<SeedDataBO> getUserTables();

	/**
	 * This will return the Gui_Link2.table data as list of {@link SeedDataBO} Objects.
	 *
	 * @return
	 */
	public List<SeedDataBO> getSeedDataBOList();

	/**
	 * This will return the map with key as the Table id and value as {@link SeedDataBO}.
	 *
	 * @return
	 */
	public Map<String, SeedDataBO> getTableIdMap();

	/**
	 * This will tell whether the guiId have {@link SeedDataBO} or not.
	 *
	 * @param itemName
	 * @return
	 */
	public boolean hasSeedDataObject(String guiId);

	/**
	 * This will return the list of {@link SeedDataBO} which belong to the Regulation Tab.
	 *
	 * @return
	 */
	public List<SeedDataBO> getRegulationsTabData();
}
