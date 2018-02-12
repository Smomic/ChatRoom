package model;

import java.util.ArrayList;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import java.util.Date;
import java.util.TreeSet;

import model.ChatState.UserStatus;

/**
 * Model of this application. It stores names of all users currently connected,
 * and all messages that have been exchanged
 * 
 * @author Michal
 */

public class Model {
	/** list of all delivered messages */
	private ArrayList<Message> messages;
	/** Mapping client's names to their models */
	private Map<String, ClientModel> nameToModelMap;
	/** maximum length of user's name */
	private static final int NAME_MAX_LENGTH = 15;
	/** time in ms acceptable between two messages without resending */
	private static final int TIME_MAX_DIFFERENCE = 500;

	/**
	 * Constructor
	 */
	public Model() {
		messages = new ArrayList<Message>();
		nameToModelMap = new HashMap<String, ClientModel>();

		// adding first message for using it to compares with next messages
		addMessage(new Message("Server", new Date(), "Server has been created"));
	}

	/**
	 * Method that adds new client's to the set
	 * 
	 * @param userName
	 *            name of user
	 */
	public void addUser(String userName) {
		nameToModelMap.put(userName, new ClientModel(userName));
	}

	/**
	 * Method that removes given client's name from the list
	 * 
	 * @param userName
	 *            name of user
	 */
	public void removeUser(String userName) {
		nameToModelMap.remove(userName);
	}

	/**
	 * Method that adds message to the messages container
	 * 
	 * @param message
	 *            adding message
	 */
	public void addMessage(Message message) {
		messages.add(message);
	}

	/**
	 * Method that returns all user names of connected clients
	 * 
	 * @return set of all user names
	 */
	public TreeSet<String> getAllUserNames() {
		return new TreeSet<String>(nameToModelMap.keySet());
	}

	/**
	 * Method that returns all messages that happened after specified date
	 * 
	 * @param date
	 *            Date object after all messages should be returned
	 * @return list of messages that happened after specified date
	 */
	private ArrayList<Message> getAllMessagesAfter(Date date) {
		ArrayList<Message> list = new ArrayList<>();
		if (date == null)
			return list;

		for (int i = messages.size() - 1; i >= 0; --i) {
			Message message = messages.get(i);
			if (message.getSentDate().before(date) == false)
				list.add(new Message(message));

			else
				break;
		}
		Collections.sort(list);
		return list;
	}

	/**
	 * Method that returns list of messages that have recently been exchanged.
	 * 
	 * @return list of messages that have recently been exchanged
	 */
	private ArrayList<Message> getRecentMessages() {
		ArrayList<Message> list = new ArrayList<>();
		if (messages.size() > 1) {
			list.add(messages.get(messages.size() - 2));
			list.add(messages.get(messages.size() - 1));
		} else if (messages.size() == 1)
			list.add(messages.get(messages.size() - 1));

		return list;
	}

	/**
	 * Method returning current chat state
	 * 
	 * @param userStatus
	 *            status of the user who is concerned by this ChatState update
	 * @return current chat state
	 */
	public ChatState getChatStateWithRecentMessages(UserStatus userStatus) {
		return new ChatState(getRecentMessages(), new TreeSet<String>(nameToModelMap.keySet()), userStatus);
	}

	/**
	 * Method returning current chat state with all the messages that happened
	 * after given date
	 * 
	 * @param date
	 *            since when messages should be included
	 * @param userStatus
	 *            status of the user who is concerned by this ChatState update
	 * @return current chat state with all expected messages
	 */
	public ChatState getChatStateWithAllMessages(Date date, UserStatus userStatus) {
		return new ChatState(getAllMessagesAfter(date), new TreeSet<String>(nameToModelMap.keySet()), userStatus);
	}

	/**
	 * Method that checks the correctness user name of client, who wants to
	 * connect with this server
	 * 
	 * @param userName
	 *            name of the client
	 * @return true if client can connect with this server, false in other case
	 */
	public boolean isUserNameAllowed(String userName) {
		if (nameToModelMap.containsKey(userName) || userName.length() == 0 || userName.length() > NAME_MAX_LENGTH)
			return false;

		return true;
	}

	/**
	 * Method that checks whether date give as argument is close enough the date
	 * of last received message
	 * 
	 * @param date
	 *            date to be checked
	 * @return true if given date is close enough or after the date of last
	 *         received message. false if this date is before last received
	 *         message date
	 */
	public boolean isCorrectDate(Date date) {
		if (date == null)
			return false;

		if (messages.size() == 0)
			return true;

		Date lastMessageDate = messages.get(messages.size() - 1).getSentDate();
		long differenceInMiliseconds = lastMessageDate.getTime() - date.getTime();
		if (differenceInMiliseconds > TIME_MAX_DIFFERENCE)
			return false;

		return true;
	}

}