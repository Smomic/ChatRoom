package events;

import java.io.Serializable;

import controller.ClientManager;

/**
 * Base class for events that are sent over network. It allows the server to
 * assign ClientManager object to it, so that it's known where does it come from
 * 
 * @author Michal
 */
public abstract class BaseEvent implements Serializable {
	/** serialVersionUID for this class */
	private static final long serialVersionUID = 1L;
	/** connection to client that has created this event */
	private ClientManager client;

	/**
	 * Basic constructor for this class
	 */
	public BaseEvent() {
		client = null;
	}

	/**
	 * Method that sets the ClientManager that received this event. It should be
	 * used only when the ClientManager is known
	 * 
	 * @param client
	 *            ClientManager that received this event
	 */
	public void setClientManager(ClientManager client) {
		this.client = client;
	};

	/**
	 * Method that returns ClientManager that received this event. It should be
	 * used only when the ClientManager is known
	 * 
	 * @return ClientNetworkManager that received this event
	 */
	public ClientManager getClientManager() {
		return client;
	};
}
