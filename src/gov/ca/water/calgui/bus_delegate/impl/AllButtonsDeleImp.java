package gov.ca.water.calgui.bus_delegate.impl;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

import gov.ca.water.calgui.bo.CalLiteGUIException;
import gov.ca.water.calgui.bus_delegate.IAllButtonsDele;
import gov.ca.water.calgui.bus_service.IBatchRunSvc;
import gov.ca.water.calgui.bus_service.IResultSvc;
import gov.ca.water.calgui.bus_service.ISeedDataSvc;
import gov.ca.water.calgui.bus_service.ITableSvc;
import gov.ca.water.calgui.bus_service.impl.BatchRunSvcImpl;
import gov.ca.water.calgui.bus_service.impl.ResultSvcImpl;
import gov.ca.water.calgui.bus_service.impl.SeedDataSvcImpl;
import gov.ca.water.calgui.bus_service.impl.TableSvcImpl;
import gov.ca.water.calgui.bus_service.impl.XMLParsingSvcImpl;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.presentation.DataTableModle;
import gov.ca.water.calgui.presentation.GlobalActionListener;
import gov.ca.water.calgui.presentation.ProgressDialog;
import gov.ca.water.calgui.tech_service.IAuditSvc;
import gov.ca.water.calgui.tech_service.IErrorHandlingSvc;
import gov.ca.water.calgui.tech_service.impl.AuditSvcImpl;
import gov.ca.water.calgui.tech_service.impl.CalLiteHelp;
import gov.ca.water.calgui.tech_service.impl.ErrorHandlingSvcImpl;

public class AllButtonsDeleImp implements IAllButtonsDele {
	private static Logger log = Logger.getLogger(AllButtonsDeleImp.class.getName());
	private SwingEngine swingEngine;
	private IBatchRunSvc batchRunSvc;
	private IResultSvc resultSvc;
	private ISeedDataSvc seedDataSvc;
	private ITableSvc tableSvc;
	private IErrorHandlingSvc errorHandlingSvc;
	private Properties properties = new Properties();
	private IAuditSvc auditSvc;

	public AllButtonsDeleImp() {
		this.swingEngine = XMLParsingSvcImpl.getXMLParsingSvcImplInstance().getSwingEngine();
		this.batchRunSvc = new BatchRunSvcImpl();
		this.resultSvc = ResultSvcImpl.getResultSvcImplInstance();
		this.seedDataSvc = SeedDataSvcImpl.getSeedDataSvcImplInstance();
		this.tableSvc = TableSvcImpl.getTableSvcImplInstance(seedDataSvc.getSeedDataBOList());
		this.auditSvc = AuditSvcImpl.getAuditSvcImplInstance();
		this.errorHandlingSvc = new ErrorHandlingSvcImpl();
		try {
			properties.load(GlobalActionListener.class.getClassLoader().getResourceAsStream("callite-gui.properties"));
		} catch (Exception e) {
			log.debug("Problem loading properties. " + e.getMessage());
		}
	}

	@Override
	public void loadScenarioButton() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JFileChooser fChooser = new JFileChooser(Constant.SCENARIOS_DIR);
		fChooser.setMultiSelectionEnabled(false);
		int val = fChooser.showOpenDialog(frame);
		if (val == JFileChooser.APPROVE_OPTION) {
			String fileName = fChooser.getSelectedFile().getName();
			log.debug("loading this cls file " + fileName);
			fileName = FilenameUtils.removeExtension(fileName);
			this.resultSvc.applyClsFile(Constant.SCENARIOS_DIR + fileName + Constant.CLS_EXT, swingEngine,
			        seedDataSvc.getTableIdMap());
			((JTextField) swingEngine.find("run_txfScen")).setText(fileName + Constant.CLS_EXT);
			((JTextField) swingEngine.find("run_txfoDSS")).setText(fileName + Constant.DV_NAME + Constant.DSS_EXT);
			auditSvc.clearAudit();
		}
	}

	@Override
	public void saveAsButton() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JFileChooser saver = new JFileChooser(Constant.SCENARIOS_DIR);
		saver.setMultiSelectionEnabled(false);
		int val = saver.showSaveDialog(frame);
		if (val == JFileChooser.APPROVE_OPTION) {
			String newScrName = saver.getSelectedFile().getName();
			newScrName = FilenameUtils.removeExtension(newScrName);
			saveCurrentStateToFile(newScrName, true);
		}
	}

	@Override
	public boolean saveCurrentStateToFile() {
		String clsFileName = ((JTextField) swingEngine.find("run_txfScen")).getText();
		return saveCurrentStateToFile(FilenameUtils.removeExtension(clsFileName), false);
	}

	@Override
	public boolean saveForViewScen() {
		try {
			resultSvc.saveToCLSFile(Constant.SCENARIOS_DIR + Constant.CURRENT_SCENARIO + Constant.CLS_EXT, swingEngine,
			        seedDataSvc.getSeedDataBOList());
			return true;
		} catch (CalLiteGUIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean saveCurrentStateToFile(String clsFileName, boolean isSaveAs) {
		if (!isSaveAs) {
			if (!auditSvc.hasValues()) {
				JOptionPane.showMessageDialog(swingEngine.find(Constant.MAIN_FRAME_NAME), "The File is up-to-date");
				return true;
			}
		}
		ProgressDialog progressDialog = ProgressDialog.getProgressDialogInstance();
		// ScenarioMonitorSwingWorker monitorSwingWorker = ScenarioMonitorSwingWorker.getProgressDialogInstance();
		String tempName = Constant.SCENARIOS_DIR + clsFileName + Constant.CLS_EXT;
		boolean proceed = true;
		if ((new File(tempName)).exists())
			proceed = (JOptionPane.showConfirmDialog(swingEngine.find(Constant.MAIN_FRAME_NAME),
			        "The scenario file '" + tempName + "' already exists. Press OK to overwrite.",
			        "CalLite GUI - " + clsFileName + Constant.CLS_EXT, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION);
		if (proceed) {
			((JTextField) swingEngine.find("run_txfScen")).setText(clsFileName + Constant.CLS_EXT);
			((JTextField) swingEngine.find("run_txfoDSS")).setText(clsFileName + Constant.DV_NAME + Constant.DSS_EXT);
			progressDialog.addScenarioNamesAndAction(clsFileName, Constant.SAVE);
			progressDialog.makeDialogVisible();
			// monitorSwingWorker.addScenarioNamesAndAction(clsFileName, Constant.SAVE);
			// monitorSwingWorker.execute();
			proceed = ResultSvcImpl.getResultSvcImplInstance().save(clsFileName,
			        XMLParsingSvcImpl.getXMLParsingSvcImplInstance().getSwingEngine(),
			        SeedDataSvcImpl.getSeedDataSvcImplInstance().getSeedDataBOList());

			log.debug("Save Complete. " + clsFileName);
			auditSvc.clearAudit();
			return proceed;
		}
		return false;
	}

	@Override
	public void runSingleBatch() {
		String clsFileName = ((JTextField) swingEngine.find("run_txfScen")).getText();
		clsFileName = FilenameUtils.removeExtension(clsFileName);
		ProgressDialog progressDialog = ProgressDialog.getProgressDialogInstance();
		boolean isSaved = saveCurrentStateToFile(clsFileName, false);
		if (isSaved) {
			List<String> fileName = Arrays.asList(clsFileName);
			progressDialog.addScenarioNamesAndAction(clsFileName, Constant.BATCH_RUN);
			progressDialog.makeDialogVisible();
			batchRunSvc.doBatch(fileName, swingEngine, false);
			log.debug("Check$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		}
	}

	@Override
	public void runSingleBatchForWsiDi() {
		String clsFileName = ((JTextField) swingEngine.find("run_txfScen")).getText();
		clsFileName = FilenameUtils.removeExtension(clsFileName);
		ProgressDialog progressDialog = ProgressDialog.getProgressDialogInstance();
		boolean isSaved = saveCurrentStateToFile(clsFileName, false);
		if (isSaved) {
			List<String> fileName = Arrays.asList(clsFileName);
			progressDialog.addScenarioNamesAndAction(clsFileName, Constant.BATCH_RUN_WSIDI);
			progressDialog.makeDialogVisible();
			batchRunSvc.doBatch(fileName, swingEngine, true);
		}
	}

	@Override
	public void runMultipleBatch() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JFileChooser fileChooser = new JFileChooser(Constant.SCENARIOS_DIR);
		fileChooser.setMultiSelectionEnabled(true);
		ProgressDialog progressDialog = ProgressDialog.getProgressDialogInstance();
		int val = fileChooser.showOpenDialog(frame);
		if (val == JFileChooser.APPROVE_OPTION) {
			File[] files = fileChooser.getSelectedFiles();
			boolean isClsfiles = true;
			List<String> fileNames = new ArrayList<String>();
			String errorMessage = "";
			for (File file : files) {
				if (!file.getName().toLowerCase().endsWith(".cls")) {
					isClsfiles = false;
					errorMessage = "Please select ClS files only. The file you selected is " + file.getName();
					break;
				}
				fileNames.add(FilenameUtils.removeExtension(file.getName()));
			}
			if (isClsfiles) {
				progressDialog.addScenarioNamesAndAction(fileNames, Constant.BATCH_RUN);
				progressDialog.makeDialogVisible();
				batchRunSvc.doBatch(fileNames, swingEngine, false);
			} else {
				errorHandlingSvc.validationeErrorHandler(errorMessage, errorMessage,
				        ((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME)));
			}
		}
	}

	@Override
	public void copyTableValues(JTable table) {
		try {
			StringBuffer buffer = new StringBuffer();
			int numcols = table.getSelectedColumnCount();
			int numrows = table.getSelectedRowCount();
			int[] rowsselected = table.getSelectedRows();
			int[] colsselected = table.getSelectedColumns();
			if (!((numrows - 1 == rowsselected[rowsselected.length - 1] - rowsselected[0] && numrows == rowsselected.length)
			        && (numcols - 1 == colsselected[colsselected.length - 1] - colsselected[0]
			                && numcols == colsselected.length))) {
				JOptionPane.showMessageDialog(null, "Invalid Copy Selection", "Invalid Copy Selection", JOptionPane.ERROR_MESSAGE);
				return;
			}
			for (int i = 0; i < numrows; i++) {
				for (int j = 0; j < numcols; j++) {
					buffer.append(table.getValueAt(rowsselected[i], colsselected[j]));
					if (j < numcols - 1)
						buffer.append(Constant.TAB_SPACE);
				}
				buffer.append(Constant.NEW_LINE);
			}
			StringSelection stringSelection = new StringSelection(buffer.toString());
			Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			systemClipboard.setContents(stringSelection, stringSelection);
		} catch (ArrayIndexOutOfBoundsException ex) {
			errorHandlingSvc.validationeErrorHandler("Please select the field from where you want to copy the data",
			        "Please select the field from where you want to copy the data",
			        (JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME));
		}
	}

	@Override
	public void pasteTableValues(JTable table) {
		try {
			int startRow = (table.getSelectedRows())[0];
			int startCol = (table.getSelectedColumns())[0];

			// get data from the clipboard.
			String totalData = "";
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			// odd: the Object param of getContents is not currently used
			Transferable contents = clipboard.getContents(null);
			boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
			if (hasTransferableText) {
				try {
					totalData = (String) contents.getTransferData(DataFlavor.stringFlavor);
				} catch (UnsupportedFlavorException ex) {
					// highly unlikely since we are using a standard DataFlavor
					log.debug(ex.getMessage()); // TODO
				} catch (IOException ex) {
					log.debug(ex.getMessage()); // TODO
				}
			}
			totalData = totalData.replaceAll("(?sm)\t\t", "\t \t");
			totalData = totalData.replaceAll("(?sm)\t\n", "\t \n");
			// verify the row's and column's.
			int rowCount = new StringTokenizer(totalData, Constant.NEW_LINE).countTokens();
			int colCount = new StringTokenizer(new StringTokenizer(totalData, Constant.NEW_LINE).nextToken(), Constant.TAB_SPACE)
			        .countTokens();
			if (colCount > table.getColumnCount()) {
				// JOptionPane.showMessageDialog(null, "The column's you selected is more then the column's of the table.", "Error",
				// JOptionPane.ERROR_MESSAGE);
				errorHandlingSvc.validationeErrorHandler("The column's you selected is more then the column's of the table.",
				        "The column's you selected is more then the column's of the table.",
				        (JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME));
				return;
			}
			// poplute data.
			StringTokenizer st1 = new StringTokenizer(totalData, Constant.NEW_LINE);
			for (int i = 0; st1.hasMoreTokens(); i++) {
				String rowstring = st1.nextToken();
				StringTokenizer st2 = new StringTokenizer(rowstring, Constant.TAB_SPACE);
				for (int j = 0; st2.hasMoreTokens(); j++) {
					String value = st2.nextToken();
					if (startRow + i < table.getRowCount() && startCol + j < table.getColumnCount())
						table.setValueAt(value, startRow + i, startCol + j);
					table.repaint();
				}
			}
		} catch (ArrayIndexOutOfBoundsException ex) {
			errorHandlingSvc.validationeErrorHandler("Please select the field from where you want to paste the data",
			        "Please select the field from where you want to paste the data",
			        (JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME));
		} catch (Exception ex) {
			log.debug(ex.getMessage()); // TODO
		}

	}

	@Override
	public void editButtonOnOperations(JComponent component) {
		String tableName = "";
		String fileName = "";
		DataTableModle dtm = null;
		try {
			if (component instanceof JButton) {
				JButton btn = (JButton) component;
				String titleStr = btn.getText();
				titleStr = titleStr.substring(5);
				TitledBorder title = BorderFactory.createTitledBorder(titleStr);
				JPanel pan = (JPanel) swingEngine.find("op_panTab");
				pan.setBorder(title);
				String comId = component.getName();
				tableName = seedDataSvc.getObjByGuiId(comId).getDataTables();
				String wsidiSuggix = tableSvc.getWsidiFileSuffix();
				if (wsidiSuggix.equals(Constant.USER_DEFINED)) {
					dtm = resultSvc.getUserDefinedTable(tableName);
				} else {
					if (titleStr.equalsIgnoreCase("SWP")) {
						fileName = Constant.Model_w2_WRESL_LOOKUP_DIR + "\\WSIDI\\" + tableName + Constant.UNDER_SCORE + wsidiSuggix
						        + Constant.FILE_EXT_TABLE;
					} else {
						fileName = Constant.Model_w2_WRESL_LOOKUP_DIR + "\\WSIDI\\" + tableName + Constant.UNDER_SCORE + wsidiSuggix
						        + Constant.FILE_EXT_TABLE;
					}
					dtm = tableSvc.getWsiDiTable(fileName);
					resultSvc.addUserDefinedTable(tableName, dtm);
				}
				component = (JComponent) swingEngine.find("scrOpValues");
				JTable table = (JTable) swingEngine.find("tblOpValues");
				table.setModel(dtm);
				component.setVisible(true);
				component.setEnabled(true);
				table.setVisible(true);
			}
		} catch (CalLiteGUIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void helpButton() {
		try {
			JTabbedPane jtp = (JTabbedPane) swingEngine.find("tabbedPane1");
			String label = jtp.getTitleAt(jtp.getSelectedIndex());
			CalLiteHelp calLiteHelp = new CalLiteHelp();
			calLiteHelp.showHelp(label);
		} catch (Exception e) {
			log.debug("Problem with CalLite Help " + e.getMessage());
		}
	}

	@Override
	public void aboutButton() {
		Long longTime = new File(Constant.GUI_XML_FILENAME).lastModified();
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("PST"));
		calendar.setTimeInMillis(longTime);
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
		String guiXmlDate = sdf.format(calendar.getTime());
		JOptionPane
		        .showMessageDialog(null,
		                "CalLite v. " + properties.getProperty("version.id") + "\nBuild date: "
		                        + properties.getProperty("build.date") + "\nYour last GUI xml revision date: " + guiXmlDate,
		                "About CalLite", JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public void windowClosing() {
		if (auditSvc.hasValues()) {
			Object[] options = { "save & exit", "exit without save", "cancel" };
			int n = JOptionPane.showOptionDialog((swingEngine.find(Constant.MAIN_FRAME_NAME)),
			        "The state is not saved do you want to save it.", "CalLite", JOptionPane.YES_NO_CANCEL_OPTION,
			        JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (n == JOptionPane.YES_OPTION) {
				boolean isSaved = saveCurrentStateToFile();
				if (!isSaved)
					errorHandlingSvc.businessErrorHandler("We encounter a problem when saveing the file.", "",
					        (JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME));
				System.exit(0);
			} else if (n == JOptionPane.NO_OPTION) {
				System.exit(0);
			}
		} else {
			Object[] options = { "ok", "cancel" };
			int n = JOptionPane.showOptionDialog((swingEngine.find(Constant.MAIN_FRAME_NAME)),
			        "Are you sure that you want to exit.", "CalLite", JOptionPane.YES_NO_CANCEL_OPTION,
			        JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (n == JOptionPane.YES_OPTION) {
				System.exit(0);
			}
		}
	}
}