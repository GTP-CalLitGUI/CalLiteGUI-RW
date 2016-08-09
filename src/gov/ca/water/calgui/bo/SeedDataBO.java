package gov.ca.water.calgui.bo;

/**
 * This is used to hold the information of GUI_Link2.table in memory.
 */
public class SeedDataBO {
	private String guiId;
	private String tableName;
	private String index;
	private String option;
	private String description;
	private String dashboard;
	private String dataTables;
	private String switchID;
	private String tableID;
	private String d1485d1641;
	private String d1641;
	private String noregulation;
	private String userDefined;
	private String d1485;
	private String regID;

	public SeedDataBO(String guiId, String tableName, String index, String option, String description, String dashboard,
	        String dataTables, String switchID, String tableID, String d1485d1641, String d1641, String noregulation,
	        String userDefined, String d1485, String regID) {
		this.guiId = guiId;
		this.tableName = tableName;
		this.index = index;
		this.option = option;
		this.description = description;
		this.dashboard = dashboard;
		this.dataTables = dataTables;
		this.switchID = switchID;
		this.tableID = tableID;
		this.d1485d1641 = d1485d1641;
		this.d1641 = d1641;
		this.noregulation = noregulation;
		this.userDefined = userDefined;
		this.d1485 = d1485;
		this.regID = regID;
	}

	public String getGuiId() {
		return guiId;
	}

	public String getTableName() {
		return tableName;
	}

	public String getIndex() {
		return index;
	}

	public String getOption() {
		return option;
	}

	public String getDescription() {
		return description;
	}

	public String getDashboard() {
		return dashboard;
	}

	public String getDataTables() {
		return dataTables;
	}

	public String getSwitchID() {
		return switchID;
	}

	public String getTableID() {
		return tableID;
	}

	public String getD1485D1641() {
		return d1485d1641;
	}

	public String getD1641() {
		return d1641;
	}

	public String getNoregulation() {
		return noregulation;
	}

	public String getUserDefined() {
		return userDefined;
	}

	public String getD1485() {
		return d1485;
	}

	public String getRegID() {
		return regID;
	}
}