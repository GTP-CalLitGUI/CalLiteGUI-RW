package gov.ca.water.calgui.constant;

public class Constant {
	// Error Handling email address.
	public static String TO_ADDRESS = "xxxx@gmail.com";
	public static String FROM_ADDRESS = "yyyy@gmail.com";
	public static String SUBJECT = "Test";
	public static String USER_NAME = "xxxx";
	public static String PASSWORD = "password";

	public static String CLS_EXT = ".CLS";
	public static String TABLE_EXT = ".table";
	public static String CSV_EXT = ".csv";
	public static String DSS_EXT = ".dss";
	public static String TXT_EXT = ".txt";
	public static String DV_NAME = "_DV";
	public static String SCENARIOS_DIR = System.getProperty("user.dir") + "//Scenarios//";
	public static String RUN_DETAILS_DIR = SCENARIOS_DIR + "Run_Details//";
	public static String MODEL_W2_DIR = System.getProperty("user.dir") + "//Model_w2//";
	public static String MODEL_W2_DSS_DIR = MODEL_W2_DIR + "//DSS_Files//";
	public static String MODEL_W2_WRESL_DIR = MODEL_W2_DIR + "wresl//";
	public static String LOOKUP_DIR = "//Lookup//";
	public static String Model_w2_WRESL_LOOKUP_DIR = MODEL_W2_WRESL_DIR + LOOKUP_DIR;
	public static String GENERATED_DIR = "//Generated//";
	public static String RUN_DIR = "//Run//";
	// File Names.
	public static String GUI_XML_FILENAME = System.getProperty("user.dir") + "//Config//GUI.xml";
	public static String GUI_LINKS2_FILENAME = System.getProperty("user.dir") + "//Config//GUI_Links2" + CSV_EXT;
	public static String GUI_LINKS3_FILENAME = System.getProperty("user.dir") + "//Config//GUI_Links3" + TABLE_EXT;
	public static String GUI_LINKS4_FILENAME = System.getProperty("user.dir") + "//Config//GUI_Links4" + CSV_EXT;
	public static String TRIGGER_ENABLE_DISABLE_FILENAME = System.getProperty("user.dir") + "//Config//TriggerForDynamicDisplay"
	        + CSV_EXT;
	public static String TRIGGER_CHECK_UNCHECK_FILENAME = System.getProperty("user.dir") + "//Config//TriggerForDymanicSelection"
	        + CSV_EXT;
	public static String IMAGE_PATH = System.getProperty("user.dir") + "//images//CalLiteIcon.png";
	public static String SAVE_FILE = "//save";
	public static final String BATCH_RUN = "Batch Run";
	public static final String BATCH_RUN_WSIDI = "Batch Run wsidi";
	public static final String SAVE = "save";
	public static String DEFAULT = "DEFAULT";
	public static String CURRENT_SCENARIO = "Current_Scenario";
	public static String DELIMITER = ",";
	public static String TAB_SPACE = "\t";
	public static String OLD_DELIMITER = TAB_SPACE;
	public static String TAB_OR_SPACE_DELIMITER = "\t| ";
	public static String N_A = "n/a";
	public static String UNDER_SCORE = "_";
	public static String PIPELINE = "|";
	public static String PIPELINE_DELIMITER = "[|]";
	public static String EXCLAMATION = "!";
	public static String HEADERS = "headers";
	public static String FILE_EXT_TABLE = ".table";
	public static String SPACE = " ";
	public static String DASH = "-";
	public static final String D1641 = "D1641";
	public static final String D1485 = "D1485";
	public static String D1641BO = "1641BO";
	public static final String USER_DEFINED = "UD";
	public static String SEMICOLON = ";";
	public static String NEW_LINE = "\n";
	public static String COMMA = ",";
	public static String EMPTY = "Empty";
	public static String MAIN_PANEL_NAME = "tabbedPane1";
	public static String MAIN_FRAME_NAME = "desktop";

	public static String HYDROCLIMATE_TABNAME = "Hydroclimate";
	public static String REGULATIONS_TABNAME = "Regulations";

	// Radio button
	public static String QUICK_SELECT_RB_D1641 = "rdbRegQS_D1641";
	public static String QUICK_SELECT_RB_D1485 = "rdbRegQS_D1485";
	public static String QUICK_SELECT_RB_D1641_BO = "rdbRegQS_1641BO";

	public static String PANEL_RB_D1641 = "btnRegD1641";
	public static String PANEL_RB_D1485 = "btnRegD1485";
	public static String PANEL_RB_USER_DEFIND = "btnRegUD";

	// Numbers
	public static int ZERO = 0;
	public static int MIN_YEAR = 1921;
	public static int MAX_YEAR = 2003;

}
