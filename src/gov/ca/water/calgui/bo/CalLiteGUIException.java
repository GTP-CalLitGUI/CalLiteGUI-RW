package gov.ca.water.calgui.bo;

/**
 * I am the root exception for CalLiteGUI.
 */
public class CalLiteGUIException extends Exception {

	private static final long serialVersionUID = 1L;
	/*
	 * This flag is for knowing the exception is required to close the application or not.
	 */
	private boolean isRequiredToExit = false;

	/**
	 * Instantiates a new CalLiteGUI exception.
	 */
	public CalLiteGUIException() {
		super();
	}

	/**
	 * The Constructor.
	 *
	 * @param message
	 *            The message
	 */
	public CalLiteGUIException(String message) {
		super(message);
	}

	/**
	 * The Constructor.
	 *
	 * @param message
	 *            The message
	 * @param isRequiredToExit
	 *            Is required to close the application or not.
	 */
	public CalLiteGUIException(String message, boolean isRequiredToExit) {
		super(message);
		this.isRequiredToExit = isRequiredToExit;
	}

	/**
	 * The Constructor.
	 *
	 * @param message
	 *            The message
	 * @param cause
	 *            The cause
	 */
	public CalLiteGUIException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * The Constructor.
	 * 
	 * @param message
	 *            The message
	 * @param cause
	 *            The cause
	 * @param isRequiredToExit
	 *            Is required to close the application or not.
	 */
	public CalLiteGUIException(String message, Throwable cause, boolean isRequiredToExit) {
		super(message, cause);
		this.isRequiredToExit = isRequiredToExit;
	}

	/**
	 * The Constructor.
	 *
	 * @param cause
	 *            the cause
	 */
	public CalLiteGUIException(Throwable cause) {
		super(cause);
	}

	public boolean isRequiredToExit() {
		return isRequiredToExit;
	}
}
