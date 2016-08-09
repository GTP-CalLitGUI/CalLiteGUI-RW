package gov.ca.water.calgui.bo;

/**
 * This is used to hold the information of GUI_Link4.table in memory.
 */
public class GuiLinks4BO {
	private String runBasisID;
	private String lodId;
	private String ccprojectId;
	private String ccmodelId;
	private String svFile;
	private String fPartSV1;
	private String initFile;
	private String fPartSV2;
	private String cvpWsiDi;
	private String swpWsiDi;
	private String lookup;

	public GuiLinks4BO(String runBasisID, String lodId, String ccprojectId, String ccmodelId, String svFile, String fPartSV1,
	        String initFile, String fPartSV2, String cvpWsiDi, String swpWsiDi, String lookup) {
		super();
		this.runBasisID = runBasisID;
		this.lodId = lodId;
		this.ccprojectId = ccprojectId;
		this.ccmodelId = ccmodelId;
		this.svFile = svFile;
		this.fPartSV1 = fPartSV1;
		this.initFile = initFile;
		this.fPartSV2 = fPartSV2;
		this.cvpWsiDi = cvpWsiDi;
		this.swpWsiDi = swpWsiDi;
		this.lookup = lookup;
	}

	public String getRunBasisID() {
		return runBasisID;
	}

	public String getLodId() {
		return lodId;
	}

	public String getCcprojectId() {
		return ccprojectId;
	}

	public String getCcmodelId() {
		return ccmodelId;
	}

	public String getSvFile() {
		return svFile;
	}

	public String getfPartSV1() {
		return fPartSV1;
	}

	public String getInitFile() {
		return initFile;
	}

	public String getfPartSV2() {
		return fPartSV2;
	}

	public String getCvpWsiDi() {
		return cvpWsiDi;
	}

	public String getSwpWsiDi() {
		return swpWsiDi;
	}

	public String getLookup() {
		return lookup;
	}
}
