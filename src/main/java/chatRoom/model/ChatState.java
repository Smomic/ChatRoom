package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

/**
 * Objects of this class represent current state of the chat room. It contains
 * few messages that have recently been send and names of all users that
 * currently are logged in to the server. The ChatState class also contains
 * information about his current status on the chat
 * 
 * @author Michal
 */
public class ChatState implements Serializable {
	/** serialVersionUID for this class */
	private static final long serialVersionUID = 1L;
	/** List of currently exchanged messages, sorted by Date */
	private ArrayList<Message> messages;
	/**
	 * Set of string names of users currently logged in. TreeSet is used so that
	 * the names are sorted - it's easier to display and browse through them
	 */
	private Set<String> names;
	/** Status of the user at the moment */
	private UserStatus userStatus;

	/**
	 * Constructor
	 * 
	 * @param messages
	 *            messages that have been recently exchanged and need to be sent
	 * @param names
	 *            names of users that are currently logged in
	 * @param userStatus
	 *            current status of the user to which this messages is going to
	 *            be sent
	 */
	public ChatState(ArrayList<Message> messages, Set<String> names, UserStatus userStatus) {
		this.messages = messages;
		Collections.sort(messages);
		this.names = names;
		this.userStatus = userStatus;
	}

	/**
	 * Method that returns messages that have recently been exchanged
	 * 
	 * @return messages that have recently been exchanged
	 */
	public ArrayList<Message> getMessages() {
		return new ArrayList<Message>(messages);
	}

	/**
	 * Method that returns a set of names of all the users that are currently
	 * logged in
	 * 
	 * @return a set of names of all the users that are currently logged in
	 */
	public TreeSet<String> getLoggedInUserNames() {
		return new TreeSet<String>(names);
	}

	/**
	 * Method that returns current status of the user that receives this message
	 * 
	 * @return current status of the user that receives this message
	 */
	public UserStatus getUserStatus() {
		return userStatus;
	}

	/**
	 * Enumeration representing current status of client that receives this
	 * status update
	 * 
	 * @author Michal
	 */
	public enum UserStatus {
		/**
		 * User is logged in, nothing special happened, just received state
		 * update
		 */
		JUST_WORKING,
		/**
		 * User just logged in, his connection has been accepted, message
		 * exchange possible
		 */
		LOGGED_IN,
		/**
		 * User sent log out request and this request has been accepted.
		 * Connection will be closed
		 */
		LOGGED_OUT,
		/**
		 * User has sent a message but it has been rejected by the server. But
		 * he is still logged in
		 */
		MESSAGE_REJECTED,
		/**
		 * User attempted on log in, but his username has been rejected. Should
		 * try again
		 */
		USERNAME_REJECTED,
		/**
		 * User has been rejected by the server for unknown reason.
		 */
		REJECTED
	}

	/**
	 * Method that informs if user is logged into te server
	 * 
	 * @return true if user that receives this message is logged into the
	 *         server, false if he has been logged out
	 */
	public boolean isLoggedIn() {
		return userStatus == UserStatus.JUST_WORKING || userStatus == UserStatus.LOGGED_IN
				|| userStatus == UserStatus.MESSAGE_REJECTED;
	}

	/**
	 * Method that determines whether this ChatState update is compatible with
	 * user whose last received message happened on a given date
	 * 
	 * @param lastMessageDate
	 *            Date of last received message by the user
	 * @return true if this ChatState update is compatible, false if it's not
	 */
	public boolean isCompatibleWithDate(Date lastMessageDate) {
		if (lastMessageDate == null || messages.size() == 0)
			return true;

		for (Message message : messages) {
			// the message previously received is contained in this state
			if (message.getSentDate().after(lastMessageDate) == false)
				return true;
		}
		return false;
	}

	/**
	 * Method that deletes from this ChatState update all messages that happened
	 * before given Date
	 * 
	 * @param lastMessageDate
	 *            Date of last received message by the user
	 */
	public void deleteAllMessagesBefore(Date lastMessageDate) {
		for (int i = 0; i < messages.size(); ++i) {
			Message message = messages.get(i);
			if (message.getSentDate().after(lastMessageDate) == false) {
				messages.remove(message);
				--i;
			}
		}
	}
}
