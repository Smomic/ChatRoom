package events;

/**
 * Event that is generated when user attempts to connect to server.
 * ClientViewController creates this event, but doesn't assign ClientManager
 * value. ClientManager value is assigned on the server side, by the
 * ServerManager that receives this event. On the client side it is processed to
 * created Socket object. On the server side it is processed to determine
 * whether chosen user name is available
 * 
 * @author Michal
 */
public class LogInEvent extends BaseEvent {
	/** serialVersionUID for this class */
	private static final long serialVersionUID = 1L;
	/** name with which user attempts to log in */
	private String userName;
	/** server name used to create Socket object */
	private String serverName;
	/** port value used to create Socket object */
	private String port;

	/**
	 * Constructor for LogInEvent
	 * 
	 * @param userName
	 *            name of the client that attempts to log in
	 * @param serverName
	 *            name of the server
	 * @param port
	 *            number of port
	 */
	public LogInEvent(String userName, String serverName, String port) {
		this.userName = userName;
		this.serverName = serverName;
		this.port = port;
	}

	/**
	 * Method that returns user name
	 * 
	 * @return String which represents user name chosen by the client with which
	 *         he attempts to connect to the server
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Method that returns server name
	 * 
	 * @return string which represents name of the server that user attempted to
	 *         connect to
	 */
	public String getServerName() {
		return serverName;
	}

	/**
	 * Method that returns number of port
	 * 
	 * @return string which represents port to which user attempts are connected
	 *         to. The type is string because this event is generated in the
	 *         client view
	 */
	public String getPort() {
		return port;
	}
}
