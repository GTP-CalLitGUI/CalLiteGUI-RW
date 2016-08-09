package gov.ca.water.calgui.presentation;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

import gov.ca.water.calgui.bus_service.impl.BatchRunSvcImpl;
import gov.ca.water.calgui.bus_service.impl.XMLParsingSvcImpl;

public class GlobalChangeListener implements ChangeListener {

	private static Logger log = Logger.getLogger(GlobalChangeListener.class.getName());
	private SwingEngine swingEngine;

	public GlobalChangeListener() {
		this.swingEngine = XMLParsingSvcImpl.getXMLParsingSvcImplInstance().getSwingEngine();
	}

	@Override
	public void stateChanged(ChangeEvent changeEvent) {

		String lcName = ((Component) changeEvent.getSource()).getName().toLowerCase();
		log.debug(lcName);
		if (lcName.equals("reg_tabbedpane")) {
			((JComponent) swingEngine.find("scrRegValues")).setVisible(false);
			((JPanel) swingEngine.find("reg_panTab")).setBorder(BorderFactory.createTitledBorder("Values"));
		} else if (lcName.equals("run_sldthreads")) {
			BatchRunSvcImpl.simultaneousRuns = ((JSlider) changeEvent.getSource()).getValue();
			((JLabel) swingEngine.find("run_lblThreads"))
			        .setText(" " + BatchRunSvcImpl.simultaneousRuns + " run" + ((BatchRunSvcImpl.simultaneousRuns > 1) ? "s" : ""));
		}
	}

}
