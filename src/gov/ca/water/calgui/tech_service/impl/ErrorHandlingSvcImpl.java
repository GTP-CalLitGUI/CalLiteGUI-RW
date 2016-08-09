package gov.ca.water.calgui.tech_service.impl;

import java.awt.Dimension;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import gov.ca.water.calgui.bo.CalLiteGUIException;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.tech_service.IErrorHandlingSvc;

/**
 * This class will handle errors.
 *
 */
public class ErrorHandlingSvcImpl implements IErrorHandlingSvc {

	private static Logger log = Logger.getLogger(ErrorHandlingSvcImpl.class.getName());
	private static long time = System.currentTimeMillis();

	@Override
	public void validationeErrorHandler(JFrame mainFrame, Throwable aThrowable) {
		List<String> error = getMessageAndStackTraceFromLayeredError(aThrowable);
		displayErrorMessage("Ssystem Error : " + error.get(0), error.get(1), mainFrame);
	}

	@Override
	public void validationeErrorHandler(String displayMessage, String detailMessage, JFrame mainFrame) {
		displayErrorMessage("Validatione Error : " + displayMessage, detailMessage, mainFrame);
	}

	@Override
	public void businessErrorHandler(JFrame mainFrame, Throwable aThrowable) {
		List<String> error = getMessageAndStackTraceFromLayeredError(aThrowable);
		displayErrorMessage("Business Error : " + error.get(0), error.get(1), mainFrame);
	}

	@Override
	public void businessErrorHandler(String displayMessage, String detailMessage, JFrame mainFrame) {
		displayErrorMessage("Business Error : " + displayMessage, detailMessage, mainFrame);
	}

	@Override
	public void systemErrorHandler(JFrame mainFrame, Throwable aThrowable) {
		List<String> error = getMessageAndStackTraceFromLayeredError(aThrowable);
		displayErrorMessage("System Error : " + error.get(0), error.get(1), mainFrame);
		System.exit(-1);
	}

	@Override
	public void systemErrorHandler(String displayMessage, String detailMessage, JFrame mainFrame) {
		displayErrorMessage("System Error : " + displayMessage, detailMessage, mainFrame);
		System.exit(-1);
	}

	@Override
	public String getStackTraceAsString(Throwable aThrowable) {
		Writer result = new StringWriter();
		PrintWriter printWriter = new PrintWriter(result);
		try {
			aThrowable.printStackTrace(printWriter);
			return result.toString();
		} catch (NullPointerException ex) {
			return "";
		}
	}

	@Override
	public void displayErrorMessageBeforeTheUI(CalLiteGUIException ex) {
		log.error(ex.getMessage(), ex);
		JTextArea textArea = new JTextArea(ex.getMessage());
		JScrollPane scrollPane = new JScrollPane(textArea);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		scrollPane.setPreferredSize(new Dimension(500, 300));
		JPanel panel = new JPanel();
		panel.add(scrollPane);
		if (ex.isRequiredToExit()) {
			Object[] options = new Object[1];
			options[0] = "exit";
			JOptionPane.showOptionDialog(null, panel, "Information", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null,
			        options, options[0]);
			System.exit(-1);
		} else {
			Object[] options = new Object[2];
			options[0] = "continue";
			options[1] = "exit";
			int val = JOptionPane.showOptionDialog(null, panel, "Information", JOptionPane.OK_CANCEL_OPTION,
			        JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			if (val != JOptionPane.OK_OPTION) {
				System.exit(-1);
			}
		}
	}

	/**
	 * This method will change the layered exceptions into the message string and also have the stack trace as a string.
	 *
	 * @param aThrowable
	 *            An exception class which has all messages in layer and the stack trace.
	 * @return In the list the 1st value will be the error message and the 2nd value will be the stack trace of the
	 *         {@link Exception}.
	 */
	private List<String> getMessageAndStackTraceFromLayeredError(Throwable aThrowable) {
		StringBuffer errorMessage = new StringBuffer();
		List<String> list = new ArrayList<String>();
		String stackTrace = "";
		while (true) {
			if (aThrowable instanceof CalLiteGUIException) {
				errorMessage.append(aThrowable.getMessage() + Constant.NEW_LINE);
				aThrowable = aThrowable.getCause();
			} else {
				stackTrace = this.getStackTraceAsString(aThrowable);
				break;
			}
		}
		list.add(errorMessage.toString());
		list.add(stackTrace);
		return list;
	}

	/**
	 * This method will display the message to the user in the JOptionPane and send email.
	 *
	 * @param displayMessage
	 *            Message message to display to the user.
	 * @param detailMessage
	 *            Detail message with stack trace for additional information.
	 * @param mainFrame
	 *            For displaying the message.
	 */
	private void displayErrorMessage(String displayMessage, String detailMessage, JFrame mainFrame) {
		if ((System.currentTimeMillis() - ErrorHandlingSvcImpl.time) < 2000) {
			return;
		}
		ErrorHandlingSvcImpl.time = System.currentTimeMillis();
		Object[] options = { "ok", "show details" };
		int n = JOptionPane.showOptionDialog(mainFrame, displayMessage, "CalLite", JOptionPane.YES_NO_OPTION,
		        JOptionPane.ERROR_MESSAGE, null, options, options[1]);
		String emailMessage = "Display Message : " + displayMessage + "\n" + "Detail Message : " + detailMessage;
		JTextArea text = new JTextArea(detailMessage);
		JScrollPane scroll = new JScrollPane(text);
		scroll.setPreferredSize(new Dimension(600, 400));
		if (n == 1) {
			JOptionPane.showMessageDialog(mainFrame, scroll, "Error", JOptionPane.ERROR_MESSAGE);
		}
		log.debug(emailMessage);
		// sendEmail(emailMessage, mainFrame);
	}

	/**
	 * This method will send email.
	 *
	 * @param message
	 *            The detail message which is send in the email.
	 * @param mainFrame
	 */
	private void sendEmail(String message, JFrame mainFrame) {
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.starttls.enable", "true");

		Session session = Session.getDefaultInstance(props);

		try {
			InternetAddress fromAddress = new InternetAddress(Constant.FROM_ADDRESS);
			InternetAddress toAddress = new InternetAddress(Constant.TO_ADDRESS);

			Message mes = new MimeMessage(session);
			mes.setFrom(fromAddress);
			mes.setRecipient(Message.RecipientType.TO, toAddress);
			mes.setSubject(Constant.SUBJECT);
			mes.setText(message);
			Transport.send(mes, Constant.USER_NAME, new String(Constant.PASSWORD));
		} catch (MessagingException ex) {
			log.error(getStackTraceAsString(ex));
			JOptionPane.showMessageDialog(mainFrame, "Can't send email to the developer.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}