package gov.ca.water.calgui.presentation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;

import org.swixml.SwingEngine;

import gov.ca.water.calgui.bo.CalLiteGUIException;
import gov.ca.water.calgui.bus_delegate.IAllButtonsDele;
import gov.ca.water.calgui.bus_delegate.impl.AllButtonsDeleImp;
import gov.ca.water.calgui.bus_service.IMonitorSvc;
import gov.ca.water.calgui.bus_service.IResultSvc;
import gov.ca.water.calgui.bus_service.ITableSvc;
import gov.ca.water.calgui.bus_service.impl.MonitorSvcImpl;
import gov.ca.water.calgui.bus_service.impl.ResultSvcImpl;
import gov.ca.water.calgui.bus_service.impl.TableSvcImpl;
import gov.ca.water.calgui.bus_service.impl.XMLParsingSvcImpl;
import gov.ca.water.calgui.constant.Constant;

public class ProgressDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = -606008444073979623L;
	private static ProgressDialog progressDialog;
	private JList list;
	private JScrollPane listScroller;
	private Map<String, String> scenarioNamesAndAction;
	private IMonitorSvc monitorSvc = new MonitorSvcImpl();
	private ITableSvc tableSvc = TableSvcImpl.getTableSvcImplInstance(null);
	private IResultSvc resultSvc = ResultSvcImpl.getResultSvcImplInstance();
	private IAllButtonsDele allButtonsDele = new AllButtonsDeleImp();
	private SwingEngine swingEngine = XMLParsingSvcImpl.getXMLParsingSvcImplInstance().getSwingEngine();
	private SwingWorker<Void, String> workerScenarioMonitor = new SwingWorker<Void, String>() {

		private String[] oldValue;

		@Override
		protected Void doInBackground() throws InterruptedException {
			while (true) {
				if (isCancelled()) {
					return null;
				}
				Thread.sleep(2000);
				boolean sleepAfterDisplay = false;
				String[] listData = null;
				List<String> data = new ArrayList<String>();
				String text = "";
				if (scenarioNamesAndAction.isEmpty()) {
					listData = new String[1];
					listData[0] = "No active scenarios";
				} else {

					for (String scenarioName : scenarioNamesAndAction.keySet()) {
						switch (scenarioNamesAndAction.get(scenarioName)) {
						case Constant.SAVE:
							text = monitorSvc.save(scenarioName);
							data.add(text);
							if (text.endsWith("Save is completed.")) {
								sleepAfterDisplay = true;
								scenarioNamesAndAction.remove(scenarioName);
							}
							break;
						case Constant.BATCH_RUN:
							text = monitorSvc.batchRun(scenarioName);
							data.add(text);
							if (text.toLowerCase().endsWith("Run completed".toLowerCase())) {
								sleepAfterDisplay = true;
								scenarioNamesAndAction.remove(scenarioName);
							}
							break;
						case Constant.BATCH_RUN_WSIDI:
							text = monitorSvc.batchRunWsidi(scenarioName);
							data.add(text);
							if (text.toLowerCase().endsWith("DONE - run completed".toLowerCase())) {
								sleepAfterDisplay = true;
								loadGeneratedWSIDI(scenarioName);
								scenarioNamesAndAction.remove(scenarioName);
							}
							break;
						}
					}
					listData = new String[data.size()];
					for (int i = 0; i < data.size(); i++) {
						listData[i] = data.get(i);
					}
				}
				if (!Arrays.equals(oldValue, listData)) {
					setList(listData);
					oldValue = listData;
				}
				if (sleepAfterDisplay) {
					Thread.sleep(2000);
					sleepAfterDisplay = false;
				}
			}
		}

		@Override
		protected void done() {
			return;

		}
	};

	public static ProgressDialog getProgressDialogInstance() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog("");
		}
		return progressDialog;
	}

	private ProgressDialog(String title) {
		this.scenarioNamesAndAction = new HashMap<String, String>();
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(400, 210));
		setMinimumSize(new Dimension(400, 210));
		setLayout(new BorderLayout(5, 5));
		setTitle(title);
		String[] data = { "No scenarios active" };
		list = new JList(data);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);
		list.setDragEnabled(true);
		list.setVisible(true);
		listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(350, 150));
		listScroller.setMinimumSize(new Dimension(350, 150));
		listScroller.setVisible(true);
		add(BorderLayout.PAGE_START, listScroller);
		JButton btnClose = new JButton("Stop all runs");
		btnClose.setPreferredSize(new Dimension(50, 20));
		btnClose.setMinimumSize(new Dimension(50, 20));
		btnClose.addActionListener(this);
		btnClose.setActionCommand("Stop");
		btnClose.setVisible(true);
		add(BorderLayout.PAGE_END, btnClose);
		pack();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((dim.width - 400) / 2, (dim.height - 200) / 2);
		java.net.URL imgURL = getClass().getResource("/images/CalLiteIcon.png");
		setIconImage(Toolkit.getDefaultToolkit().getImage(imgURL));
		setAlwaysOnTop(true);
		setModal(false);
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosed(java.awt.event.WindowEvent windowEvent) {
				workerScenarioMonitor.cancel(true);
				progressDialog = null;
			}
		});
		workerScenarioMonitor.execute();
	}

	public void makeDialogVisible() {
		setVisible(true);
		// repaint();
		paintComponents(this.getGraphics());
		// print(this.getGraphics());
		// paintAll(getGraphics());
	}

	public void setList(String[] listData) {
		if (!listScroller.isVisible()) {
			listScroller.setVisible(true);
		}
		list.setListData(listData);
		// repaint();
		paintComponents(this.getGraphics());
		// print(this.getGraphics());
		// paintAll(getGraphics());
	}

	public void addScenarioNamesAndAction(String key, String value) {
		scenarioNamesAndAction.put(key, value);
	}

	public void addScenarioNamesAndAction(List<String> keys, String value) {
		keys.forEach(key -> scenarioNamesAndAction.put(key, value));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if ("Go".equals(e.getActionCommand())) {
			this.setVisible(true);
		} else if ("Stop".equals(e.getActionCommand())) {
			Runtime rt = Runtime.getRuntime();
			Process proc;
			try {
				proc = rt.exec("taskkill /f /t /fi \"WINDOWTITLE eq CalLiteRun*\" ");
				scenarioNamesAndAction.clear();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	public void loadGeneratedWSIDI(String scenarioName) {
		String wsi_di_swp = "wsi_di_swp";
		String wsi_di_cvp_swp = "wsi_di_cvp_sys";
		String wsi_di_swp_path = Paths.get(
		        Constant.RUN_DETAILS_DIR + scenarioName + Constant.RUN_DIR + Constant.LOOKUP_DIR + wsi_di_swp + Constant.TABLE_EXT)
		        .toString();
		String wsi_di_cvp_swp_path = Paths.get(Constant.RUN_DETAILS_DIR + scenarioName + Constant.RUN_DIR + Constant.LOOKUP_DIR
		        + wsi_di_cvp_swp + Constant.TABLE_EXT).toString();
		try {
			resultSvc.addUserDefinedTable(wsi_di_swp, tableSvc.getWsiDiTable(wsi_di_swp_path));
			resultSvc.addUserDefinedTable(wsi_di_cvp_swp, tableSvc.getWsiDiTable(wsi_di_cvp_swp_path));
			tableSvc.setWsidiFileSuffix(Constant.USER_DEFINED);
			JComponent component = (JComponent) swingEngine.find("op_btn1");
			allButtonsDele.editButtonOnOperations(component);
			// JLabel lab = (JLabel) swingEngine.find("op_WSIDI_Status");
			// lab.setText(selHyd + " (Generated via " + wsdiIterations + " iterations)");
		} catch (CalLiteGUIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
