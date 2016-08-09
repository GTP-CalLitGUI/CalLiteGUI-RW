package gov.ca.water.calgui.presentation;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import gov.ca.water.calgui.bus_delegate.IApplyDynamicConDele;
import gov.ca.water.calgui.bus_delegate.impl.ApplyDynamicConDeleImp;

public class GlobalMouseListener implements MouseListener {

	private static Logger log = Logger.getLogger(GlobalMouseListener.class.getName());
	private IApplyDynamicConDele applyDynamicConDele = new ApplyDynamicConDeleImp();

	@Override
	public void mouseClicked(MouseEvent me) {
		log.debug("mouseClicked");
		if (SwingUtilities.isRightMouseButton(me)) {
			JComponent component = (JComponent) me.getComponent();
			if (((JCheckBox) component).isSelected()) {
				String cName = component.getName();
				applyDynamicConDele.applyDynamicControl(cName, ((JCheckBox) component).isSelected(),
				        ((JCheckBox) component).isEnabled());
				log.debug(cName);
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
	}
}
