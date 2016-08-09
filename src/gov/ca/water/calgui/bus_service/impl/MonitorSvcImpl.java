package gov.ca.water.calgui.bus_service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import gov.ca.water.calgui.bus_service.IMonitorSvc;
import gov.ca.water.calgui.constant.Constant;

/**
 * This class is used for getting the String for Monitor the process which is done behind the seen.
 */
public class MonitorSvcImpl implements IMonitorSvc {

	private static Logger log = Logger.getLogger(MonitorSvcImpl.class.getName());

	@Override
	public String save(String scenarioName) {
		return scenarioName + " - Saving - "
		        + lastLine(Constant.RUN_DETAILS_DIR + scenarioName + Constant.RUN_DIR + Constant.SAVE_FILE + Constant.TXT_EXT);

	}

	@Override
	public String batchRunWsidi(String scenarioName) {
		String scenPROGRESSFile = Constant.RUN_DETAILS_DIR + scenarioName + Constant.RUN_DIR + "//PROGRESS.txt";
		String scenWRESLCHECK_WSIDIFile = Constant.RUN_DETAILS_DIR + scenarioName + Constant.RUN_DIR
		        + "//=WreslCheck_main_wsidi=.log";
		String scenWSIDIIterationFile = Constant.RUN_DETAILS_DIR + scenarioName + Constant.RUN_DIR + "//wsidi_iteration.log";
		String infoWSIDI = "";
		String line = "";
		if (Paths.get(scenWSIDIIterationFile).toFile().exists())
			infoWSIDI = "(WSIDI " + lastLine(scenWSIDIIterationFile) + ") ";
		else
			infoWSIDI = "(WSIDI) ";
		if (Paths.get(scenPROGRESSFile).toFile().exists()) {
			line = lastLine(scenPROGRESSFile);
			line = progressString(line);
			return scenarioName + " " + infoWSIDI + " - " + line;
		} else {
			line = lastLine(scenWRESLCHECK_WSIDIFile);
			if (line.endsWith("====================")) {
				line = lastButOneLine(scenWRESLCHECK_WSIDIFile);
			}
			line = parsingString(line);
			return scenarioName + " " + infoWSIDI + " - " + line;
		}
	}

	@Override
	public String batchRun(String scenarioName) {
		String scenPROGRESSFile = Constant.RUN_DETAILS_DIR + scenarioName + Constant.RUN_DIR + "//PROGRESS.txt";
		String scenWRESLCHECKFile = Constant.RUN_DETAILS_DIR + scenarioName + Constant.RUN_DIR + "//=WreslCheck_main=.log";
		String line = "";
		FileTime fileTime = null;
		try {
			fileTime = Files.getLastModifiedTime(Paths.get(scenPROGRESSFile));
		} catch (IOException e) {
			// no need to handle because the file is not yet there.
		}
		if (Paths.get(scenPROGRESSFile).toFile().exists() && (System.currentTimeMillis() - fileTime.toMillis() < 300000)) {
			line = lastLine(scenPROGRESSFile);
			line = progressString(line);
			return scenarioName + " - " + line;
		} else {
			line = lastLine(scenWRESLCHECKFile);
			if (line.endsWith("====================")) {
				line = lastButOneLine(scenWRESLCHECKFile);
			}
			line = parsingString(line);
			return scenarioName + " - " + line;
		}
	}

	/**
	 * This method will convert the batch string from the progrtss.txt file into the detail Message to display.
	 *
	 * @param value
	 * @return
	 */
	private String progressString(String value) {
		if (value.contains("unopenable!"))
			return "RUNNING - unable to read progress.txt";
		if (value.contains("Empty!"))
			return "RUNNING - run starting";
		if (value.contains("Run completed."))
			return "DONE - run completed";
		if (value.contains("Run failed."))
			return "DONE - run failed.";
		else {
			String parts[] = value.split(" ");
			if (parts.length == 4) {
				try {
					int totalMonths = 12 * (Integer.parseInt(parts[1]) - Integer.parseInt(parts[0]));
					int months = Math.min(totalMonths,
					        Integer.parseInt(parts[3]) + 12 * (Integer.parseInt(parts[2]) - Integer.parseInt(parts[0])));
					value = parts[3] + "/" + parts[2] + " (" + (100 * months / totalMonths) + "%)";
				} catch (NumberFormatException e) {
					value = "There is a error in formating the numbers.";
				}
			}
			return "RUNNING - " + value;
		}
	}

	/**
	 * This method will convert the batch string into the detail Message to display.
	 *
	 * @param value
	 * @return
	 */
	private String parsingString(String value) {
		if (value.contains("unopenable!"))
			return "PARSING - unable to read parsing log";
		if (value.contains("Empty!"))
			return "PARSING - parsing started";
		if (!value.contains("Total errors:"))
			return "PARSING - " + value;
		else
			return "PARSING - Parsing complete - " + value;
	}

	/**
	 * This will open the file and read the last line and return it.
	 *
	 * @param fileName
	 *            file name with whole path.
	 * @return
	 */
	public String lastLine(String fileName) {
		String value = "";
		File file = Paths.get(fileName).toFile();
		try {
			FileReader fileReader = new FileReader(file);
			BufferedReader br = new BufferedReader(fileReader);
			List<String> list = br.lines().collect(Collectors.toList());
			value = list.get(list.size() - 1);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			value = e.getMessage();
		}
		return value;
	}

	/**
	 * This will open the file and read the last but one line and return it.
	 *
	 * @param fileName
	 *            file name with whole path.
	 * @return
	 */
	public String lastButOneLine(String fileName) {
		String value = "";
		try {
			List<String> list = Files.lines(Paths.get(fileName)).collect(Collectors.toList());
			value = list.get(list.size() - 2);
		} catch (NoSuchFileException e) {
			log.error(e.getMessage(), e);
			value = "The file is missing. The file path is " + fileName;
		} catch (AccessDeniedException e) {
			log.error(e.getMessage(), e);
			value = "The Access is denied for this file " + fileName;
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			value = e.getMessage();
		}
		return value;
	}
}
