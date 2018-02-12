package events;

import java.util.Date;

/**
 * Event that is created when client application finds out that some messages
 * are missing and it wants to get the server to resend them
 * 
 * @author Michal
 */
public class ResendEvent extends BaseEvent {
	/** serialVersionUID for this class */
	private static final long serialVersionUID = 1L;
	/** Date since which all messages should be resend */
	private Date lastDateOfMessage;

	/**
	 * Constuructor that sets Date since which all messages should be resend
	 * 
	 * @param lastDateOfMessage
	 *            Date since which all messages should be resend
	 */
	public ResendEvent(Date lastDateOfMessage) {
		this.lastDateOfMessage = lastDateOfMessage;
	}

	/**
	 * Method that returns Date after which all messages should be resend
	 * 
	 * @return Date after which all messages should be resend
	 */
	public Date getLastMessageDate() {
		return this.lastDateOfMessage;
	}
}