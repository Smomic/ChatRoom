package controller;

import java.util.concurrent.BlockingQueue;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

import events.BaseEvent;
import events.LogInEvent;
import events.LogOutEvent;
import events.MessageEvent;
import events.ResendEvent;
import model.Message;
import model.Model;
import model.ChatState.UserStatus;

/**
 * Class responsible for handling connection between the model-view using
 * network mechanisms
 * 
 * @author Michal
 */
public class Controller {
	/** server's model */
	private Model model;
	/** server's ServerManager */
	private ServerManager serverManager;
	/** BlockingQueue from which events are read */
	private BlockingQueue<BaseEvent> blockingQueue;
	/** mapping BaseEvents to Strategy objects that handle them */
	private Map<Class<? extends BaseEvent>, ServerStrategy> eventsToStrategyMap;
	/** mapping ClientManagers to usernames stored in the model */
	private Map<ClientManager, String> clientToUserNameMap;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            model for the server that uses this controller
	 * @param serverManager
	 *            manager used by this controller to send messages over network
	 * @param blockingQueue
	 *            blockingQueue from which events are read
	 */
	public Controller(Model model, ServerManager serverManager, BlockingQueue<BaseEvent> blockingQueue) {
		this.model = model;
		this.serverManager = serverManager;
		this.blockingQueue = blockingQueue;

		clientToUserNameMap = new HashMap<ClientManager, String>();
		eventsToStrategyMap = new HashMap<Class<? extends BaseEvent>, Controller.ServerStrategy>();
		eventsToStrategyMap.put(LogInEvent.class, new LoginInStrategy());
		eventsToStrategyMap.put(LogOutEvent.class, new LogOutStrategy());
		eventsToStrategyMap.put(MessageEvent.class, new MessageStrategy());
		eventsToStrategyMap.put(ResendEvent.class, new ResendStrategy());
	}

	/**
	 * Method that listens for the blockingQueue and handles events
	 */
	public void start() {
		while (true) {
			BaseEvent event = null;
			try {
				event = blockingQueue.take();
			} catch (InterruptedException e) {
				continue;
			}

			eventsToStrategyMap.get(event.getClass()).execute(event);
		}
	}

	/**
	 * Class that provides common base for strategies used to handle events
	 * 
	 * @author Michal
	 */
	private abstract class ServerStrategy {
		/**
		 * Method that is invoked in response to BaseEvent
		 * 
		 * @param event
		 *            ApplicationEvent to be handled
		 */
		public abstract void execute(BaseEvent event);

		protected void sendBroadcast(UserStatus status) {
			serverManager.broadcast(model.getChatStateWithRecentMessages(status));
		}
	}

	/**
	 * Strategy that handles LogInEvent
	 * 
	 * @author Michal
	 */
	private class LoginInStrategy extends ServerStrategy {
		/**
		 * Method that is invoked in response to LogInEvent. It checks
		 * conrrectness of entering username using the model, adds (or not) the
		 * user, and sends status update to all users
		 * 
		 * @param e
		 *            LogInEvent to be handled
		 */
		@Override
		public void execute(BaseEvent e) {
			if (e instanceof LogInEvent == false)
				return;

			LogInEvent event = (LogInEvent) e;
			ClientManager client = event.getClientManager();
			if (model.isUserNameAllowed(event.getUserName()))
				loginUser(event, client);
			else
				removeUser(client);
		}

		private void loginUser(LogInEvent event, ClientManager client) {
			String username = event.getUserName();
			model.addUser(username);
			sendBroadcast(UserStatus.JUST_WORKING);
			clientToUserNameMap.put(client, username);
			client.setLoginFlag();
			client.send(model.getChatStateWithRecentMessages(UserStatus.LOGGED_IN));
		}

		private void removeUser(ClientManager client) {
			serverManager.removeClient(client);
			sendBroadcast(UserStatus.USERNAME_REJECTED);
			client.close();
		}
	}

	/**
	 * Strategy that handles LogOutEvent
	 * 
	 * @author Michal
	 */
	private class LogOutStrategy extends ServerStrategy {
		/**
		 * Method that is invoked in response to LogOutEvent. It disconnects the
		 * user from the server, resends update to him saying that log out
		 * succeeded and resends update to all without this user mentioned in
		 * the logged in users status
		 */
		@Override
		public void execute(BaseEvent e) {
			if (e instanceof LogOutEvent == false)
				return;

			LogOutEvent event = (LogOutEvent) e;
			ClientManager client = event.getClientManager();
			String username = clientToUserNameMap.get(client);
			clientToUserNameMap.remove(client);
			model.removeUser(username);
			serverManager.removeClient(client);
			client.send(model.getChatStateWithRecentMessages(UserStatus.LOGGED_OUT));
			client.close();
			sendBroadcast(UserStatus.JUST_WORKING);
		}
	}

	/**
	 * Strategy that handles MessageEvent
	 * 
	 * @author Michal
	 */
	private class MessageStrategy extends ServerStrategy {
		/**
		 * Method that is invoked in response to MessageEvent. It checks whether
		 * user who sent this message is up to date with received messages, and
		 * then either accepts the message and resends status update to all, or
		 * rejects the message and resends status update only to the author
		 * 
		 * @param e
		 *            MessageEvent to be handled
		 */
		@Override
		public void execute(BaseEvent e) {
			if (e instanceof MessageEvent == false)
				return;

			MessageEvent event = (MessageEvent) e;
			ClientManager client = event.getClientManager();
			if (client.getLoginFlag() == false)
				return;

			Date previousMessageDate = event.getPreviousMessageDate();
			if (model.isCorrectDate(previousMessageDate))
				addMessage(event, client);
			else
				client.send(model.getChatStateWithAllMessages(previousMessageDate, UserStatus.MESSAGE_REJECTED));
		}

		private void addMessage(MessageEvent event, ClientManager client) {
			Message message = new Message(clientToUserNameMap.get(client), new Date(), event.getMessageString());
			model.addMessage(message);
			sendBroadcast(UserStatus.JUST_WORKING);
		}
	}

	/**
	 * Strategy that handle ResendEvent
	 * 
	 * @author Michal
	 */
	private class ResendStrategy extends ServerStrategy {
		/**
		 * Method that is invoked in response to ResendEvent. It sends a status
		 * update to the author of this event with all the messages that he is
		 * missing (based on the date he gave in the ResendEvent)
		 */
		@Override
		public void execute(BaseEvent e) {
			if (e instanceof ResendEvent == false)
				return;

			ResendEvent event = (ResendEvent) e;
			ClientManager client = event.getClientManager();
			if (client.getLoginFlag() == false)
				return;

			client.send(model.getChatStateWithAllMessages(event.getLastMessageDate(), UserStatus.JUST_WORKING));
		}
	}
}
