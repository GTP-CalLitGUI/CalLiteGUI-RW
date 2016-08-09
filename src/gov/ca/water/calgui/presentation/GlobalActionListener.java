package gov.ca.water.calgui.presentation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

import gov.ca.water.calgui.bus_delegate.IAllButtonsDele;
import gov.ca.water.calgui.bus_delegate.IScenarioDele;
import gov.ca.water.calgui.bus_delegate.IVerifyControlsDele;
import gov.ca.water.calgui.bus_delegate.impl.AllButtonsDeleImp;
import gov.ca.water.calgui.bus_delegate.impl.ScenarioDeleImp;
import gov.ca.water.calgui.bus_delegate.impl.VerifyControlsDeleImp;
import gov.ca.water.calgui.bus_service.IResultSvc;
import gov.ca.water.calgui.bus_service.ISeedDataSvc;
import gov.ca.water.calgui.bus_service.impl.ResultSvcImpl;
import gov.ca.water.calgui.bus_service.impl.SeedDataSvcImpl;
import gov.ca.water.calgui.bus_service.impl.XMLParsingSvcImpl;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.tech_service.IAuditSvc;
import gov.ca.water.calgui.tech_service.IErrorHandlingSvc;
import gov.ca.water.calgui.tech_service.impl.AuditSvcImpl;
import gov.ca.water.calgui.tech_service.impl.ErrorHandlingSvcImpl;

public class GlobalActionListener implements ActionListener {
	private static Logger log = Logger.getLogger(GlobalActionListener.class.getName());
	private IScenarioDele scenarioDele;
	private IAllButtonsDele allButtonsDele;
	private IResultSvc resultSvc;
	private IAuditSvc auditSvc;
	private SwingEngine swingEngine;
	private ISeedDataSvc seedDataSvc;
	private IVerifyControlsDele verifyControlsDele;
	private IErrorHandlingSvc errorHandlingSvc;

	public GlobalActionListener() {
		this.allButtonsDele = new AllButtonsDeleImp();
		this.swingEngine = XMLParsingSvcImpl.getXMLParsingSvcImplInstance().getSwingEngine();
		this.scenarioDele = new ScenarioDeleImp();
		this.resultSvc = ResultSvcImpl.getResultSvcImplInstance();
		this.seedDataSvc = SeedDataSvcImpl.getSeedDataSvcImplInstance();
		this.auditSvc = AuditSvcImpl.getAuditSvcImplInstance();
		this.verifyControlsDele = new VerifyControlsDeleImp();
		this.errorHandlingSvc = new ErrorHandlingSvcImpl();
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		log.debug(ae.getActionCommand());
		JTable table = null;
		switch (ae.getActionCommand()) {
		case "AC_SaveScen":
			this.allButtonsDele.saveCurrentStateToFile();
			break;
		case "AC_SaveScenAs":
			this.allButtonsDele.saveAsButton();
			break;
		case "AC_ViewScen":
			loadViewScen();
			break;
		case "AC_LoadScen":
			loadScenarioButton();
			break;
		case "AC_Help":
			this.allButtonsDele.helpButton();
			break;
		case "AC_RUN":
			this.allButtonsDele.runSingleBatch();
			break;
		case "AC_BATCH":
			this.allButtonsDele.runMultipleBatch();
			break;
		case "AC_About":
			this.allButtonsDele.aboutButton();
			break;
		case "AC_Exit":
			this.allButtonsDele.windowClosing();
			break;
		case "AC_Select_DSS_SV":
			break;
		case "AC_Select_DSS_Init":
			break;
		case "Op_TableEdit":
			this.allButtonsDele.editButtonOnOperations((JComponent) ae.getSource());
			break;
		case "Op_Generate":
			this.allButtonsDele.runSingleBatchForWsiDi();
			break;
		case "Reg_Copy":
			table = (JTable) swingEngine.find("tblRegValues");
			this.allButtonsDele.copyTableValues(table);
			break;
		case "Reg_Paste":
			table = (JTable) swingEngine.find("tblRegValues");
			JRadioButton userDefined = (JRadioButton) swingEngine.find("btnRegUD");
			if (userDefined.isSelected()) {
				this.allButtonsDele.pasteTableValues(table);
			} else {
				// JOptionPane.showMessageDialog(null, "You can't paste untel you select user defined.", "Error",
				// JOptionPane.ERROR_MESSAGE);
				errorHandlingSvc.validationeErrorHandler("You can't paste untel you select user defined.",
				        "You can't paste untel you select user defined.", (JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME));
			}
			break;
		case "Op_Copy":
			table = (JTable) swingEngine.find("tblOpValues");
			this.allButtonsDele.copyTableValues(table);
			break;
		case "Op_Paste":
			table = (JTable) swingEngine.find("tblOpValues");
			this.allButtonsDele.pasteTableValues(table);
			break;
		}
	}

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
			verifyControlsDele.verifyTheDataBeforeUI(Constant.SCENARIOS_DIR + fileName + Constant.CLS_EXT);
			this.resultSvc.applyClsFile(Constant.SCENARIOS_DIR + fileName + Constant.CLS_EXT, swingEngine,
			        seedDataSvc.getTableIdMap());
			((JTextField) swingEngine.find("run_txfScen")).setText(fileName + Constant.CLS_EXT);
			((JTextField) swingEngine.find("run_txfoDSS")).setText(fileName + Constant.DV_NAME + Constant.DSS_EXT);
			auditSvc.clearAudit();
		}
	}

	public void loadViewScen() {
		boolean pro = this.allButtonsDele.saveForViewScen();
		if (pro) {
			List<DataTableModle> dtmList = scenarioDele.getScenarioTableData(null);
			ScenarioFrame scenarioFrame = new ScenarioFrame(dtmList);
			scenarioFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			scenarioFrame.setVisible(true);
			try {
				Files.delete(Paths.get(Constant.SCENARIOS_DIR + Constant.CURRENT_SCENARIO + Constant.CLS_EXT));
			} catch (IOException ex) {
				log.debug(ex);
			}
		}
	}
}
