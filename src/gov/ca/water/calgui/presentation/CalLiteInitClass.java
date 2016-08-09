package gov.ca.water.calgui.presentation;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;

import org.swixml.SwingEngine;

import gov.ca.water.calgui.bo.CalLiteGUIException;
import gov.ca.water.calgui.bus_delegate.IAllButtonsDele;
import gov.ca.water.calgui.bus_delegate.IVerifyControlsDele;
import gov.ca.water.calgui.bus_delegate.impl.AllButtonsDeleImp;
import gov.ca.water.calgui.bus_delegate.impl.VerifyControlsDeleImp;
import gov.ca.water.calgui.bus_service.IDynamicControlSvc;
import gov.ca.water.calgui.bus_service.IResultSvc;
import gov.ca.water.calgui.bus_service.ISeedDataSvc;
import gov.ca.water.calgui.bus_service.ITableSvc;
import gov.ca.water.calgui.bus_service.IXMLParsingSvc;
import gov.ca.water.calgui.bus_service.impl.BatchRunSvcImpl;
import gov.ca.water.calgui.bus_service.impl.DynamicControlSvcImpl;
import gov.ca.water.calgui.bus_service.impl.ResultSvcImpl;
import gov.ca.water.calgui.bus_service.impl.SeedDataSvcImpl;
import gov.ca.water.calgui.bus_service.impl.TableSvcImpl;
import gov.ca.water.calgui.bus_service.impl.XMLParsingSvcImpl;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.tech_service.IAuditSvc;
import gov.ca.water.calgui.tech_service.IErrorHandlingSvc;
import gov.ca.water.calgui.tech_service.impl.AuditSvcImpl;
import gov.ca.water.calgui.tech_service.impl.ErrorHandlingSvcImpl;

public class CalLiteInitClass {
	private SwingEngine swingEngine;
	private IAuditSvc auditSvc;

	public void init() {
		// Building all the Services.
		ISeedDataSvc seedDataSvc = SeedDataSvcImpl.getSeedDataSvcImplInstance();
		IXMLParsingSvc xmlParsingSvc = XMLParsingSvcImpl.getXMLParsingSvcImplInstance();
		IVerifyControlsDele verifyControlsDele = new VerifyControlsDeleImp();
		verifyControlsDele.verifyTheDataBeforeUI(Constant.SCENARIOS_DIR + Constant.DEFAULT + Constant.CLS_EXT);
		IDynamicControlSvc dynamicControlSvc = DynamicControlSvcImpl.getDynamicControlSvcImplInstance();
		ITableSvc tableSvc = TableSvcImpl.getTableSvcImplInstance(seedDataSvc.getUserTables());
		this.swingEngine = xmlParsingSvc.getSwingEngine();
		IResultSvc resultSvc = ResultSvcImpl.getResultSvcImplInstance();
		IAllButtonsDele allButtonsDele = new AllButtonsDeleImp();
		IErrorHandlingSvc errorHandlingSvc = new ErrorHandlingSvcImpl();
		// Set up the GUI
		// Set up month spinners
		JSpinner spnSM1 = (JSpinner) swingEngine.find("spnRunStartMonth");
		setMonthModelAndIndex(spnSM1, 9);
		JSpinner spnEM1 = (JSpinner) swingEngine.find("spnRunEndMonth");
		setMonthModelAndIndex(spnEM1, 8);

		// Set up year spinners
		JSpinner spnSY1 = (JSpinner) swingEngine.find("spnRunStartYear");
		setNumberModelAndIndex(spnSY1, 1921, Constant.MIN_YEAR, Constant.MAX_YEAR, 1, "####");
		JSpinner spnEY1 = (JSpinner) swingEngine.find("spnRunEndYear");
		setNumberModelAndIndex(spnEY1, 2003, Constant.MIN_YEAR, Constant.MAX_YEAR, 1, "####");
		// Setting up all the Listener's.
		swingEngine.setActionListener(swingEngine.find(Constant.MAIN_FRAME_NAME), new GlobalActionListener());
		setCheckBoxorRadioButtonMouseListener(swingEngine.find(Constant.MAIN_FRAME_NAME), new GlobalMouseListener());
		setCheckBoxorRadioButtonItemListener(swingEngine.find(Constant.MAIN_FRAME_NAME), new GlobalItemListener());
		((JTabbedPane) swingEngine.find("reg_tabbedPane")).addChangeListener(new GlobalChangeListener());
		((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME)).setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);// EXIT_ON_CLOSE

		((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME)).addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				// allButtonsDele.windowClosing();
				System.exit(0);
			}
		});

		// Loading the default cls file.
		resultSvc.applyClsFile(Constant.SCENARIOS_DIR + Constant.DEFAULT + Constant.CLS_EXT, swingEngine,
		        seedDataSvc.getTableIdMap());
		// check
		checkForNewUserDefinedTables(xmlParsingSvc.getNewUserDefinedTables(), resultSvc, tableSvc, swingEngine);
		// Display the GUI
		swingEngine.find(Constant.MAIN_FRAME_NAME).setVisible(true);

		tableSvc.setWsidiFileSuffix(Constant.USER_DEFINED); // TODO : This is for cls file. write comment
		auditSvc = AuditSvcImpl.getAuditSvcImplInstance(); // TODO : write comment.
		auditSvc.clearAudit();
		addJTextFieldListener();

		// Count threads and update selector appropriately

		int maxThreads = Math.max(1, Runtime.getRuntime().availableProcessors());
		BatchRunSvcImpl.simultaneousRuns = maxThreads;
		((JSlider) swingEngine.find("run_sldThreads")).addChangeListener(new GlobalChangeListener());
		((JSlider) swingEngine.find("run_sldThreads")).setEnabled(maxThreads > 1);
		((JSlider) swingEngine.find("run_sldThreads")).setMaximum(maxThreads);
		((JLabel) swingEngine.find("run_lblThreads")).setText(" " + maxThreads + ((maxThreads > 1) ? " runs" : " run"));
		((JLabel) swingEngine.find("run_lblThreadsInfo"))
		        .setText("Simultaneous runs " + ((maxThreads > 1) ? "(1-" + maxThreads + ")" : "(1)"));
	}

	public void checkForNewUserDefinedTables(List<String> newUserDefinedIds, IResultSvc resultSvc, ITableSvc tableSvc,
	        SwingEngine swingEngine) {
		DataTableModle dtm = null;
		for (String newUserDefinedId : newUserDefinedIds) {
			if (resultSvc.hasUserDefinedTable(newUserDefinedId)) {
				dtm = resultSvc.getUserDefinedTable(newUserDefinedId);
			} else {
				try {
					dtm = tableSvc.handleTableFileWithColumnNumber(newUserDefinedId);
					dtm.setCellEditable(true);
					resultSvc.addUserDefinedTable(newUserDefinedId, dtm);
				} catch (CalLiteGUIException e) {
					// TODO Handle the file not found.
					e.printStackTrace();
				}
			}
			((JTable) swingEngine.find(newUserDefinedId)).setModel(dtm);
		}
	}

	public void setMonthModelAndIndex(JSpinner jspn, int idx) {
		String[] monthNames = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
		try {
			SpinnerListModel monthModel = new SpinnerListModel(monthNames);
			jspn.setModel(monthModel);
			jspn.setValue(monthNames[idx]);
		} catch (Exception e) {
		}
	}

	public void setNumberModelAndIndex(JSpinner jspn, int val, int min, int max, int step, String format) {
		SpinnerModel spnmod = new SpinnerNumberModel(val, min, max, step);
		jspn.setModel(spnmod);
		jspn.setEditor(new JSpinner.NumberEditor(jspn, format));
	}

	public void setCheckBoxorRadioButtonItemListener(Component component, Object obj) {
		if (component instanceof JCheckBox || component instanceof JRadioButton) {
			((AbstractButton) component).addItemListener((ItemListener) obj);
		}
		for (Component child : ((Container) component).getComponents()) {
			setCheckBoxorRadioButtonItemListener(child, obj);
		}
	}

	public void setCheckBoxorRadioButtonMouseListener(Component component, Object obj) {
		if (component instanceof JCheckBox) {
			((AbstractButton) component).addMouseListener((MouseListener) obj);
		}
		for (Component child : ((Container) component).getComponents()) {
			setCheckBoxorRadioButtonMouseListener(child, obj);
		}
	}

	private void addJTextFieldListener() {
		FocusListener focusListener = new FocusListener() {
			String oldValue = "";

			@Override
			public void focusLost(FocusEvent e) {
				JTextField field = ((JTextField) e.getComponent());
				String newValue = field.getText();
				if (!oldValue.equals(newValue)) {
					auditSvc.addAudit(field.getName(), oldValue, newValue);
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				oldValue = ((JTextField) e.getComponent()).getText();
			}
		};
		JTextField demtxt1 = (JTextField) swingEngine.find("demtxt1");
		demtxt1.addFocusListener(focusListener);
		JTextField demtxt2 = (JTextField) swingEngine.find("demtxt2");
		demtxt2.addFocusListener(focusListener);
		JTextField demtxt3 = (JTextField) swingEngine.find("demtxt3");
		demtxt3.addFocusListener(focusListener);
		JTextField demtxt3a = (JTextField) swingEngine.find("demtxt3a");
		demtxt3a.addFocusListener(focusListener);
		JTextField demtxt4 = (JTextField) swingEngine.find("demtxt4");
		demtxt4.addFocusListener(focusListener);
		JTextField demtxt5 = (JTextField) swingEngine.find("demtxt5");
		demtxt5.addFocusListener(focusListener);
		JTextField demtxt6 = (JTextField) swingEngine.find("demtxt6");
		demtxt6.addFocusListener(focusListener);
		JTextField hyd_DSS_SV = (JTextField) swingEngine.find("hyd_DSS_SV");
		hyd_DSS_SV.addFocusListener(focusListener);
		JTextField hyd_DSS_SV_F = (JTextField) swingEngine.find("hyd_DSS_SV_F");
		hyd_DSS_SV_F.addFocusListener(focusListener);
		JTextField hyd_DSS_Init = (JTextField) swingEngine.find("hyd_DSS_Init");
		hyd_DSS_Init.addFocusListener(focusListener);
		JTextField hyd_DSS_Init_F = (JTextField) swingEngine.find("hyd_DSS_Init_F");
		hyd_DSS_Init_F.addFocusListener(focusListener);
	}
}
