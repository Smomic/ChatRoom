package events;

/**
 * This event is sent from the ClientManager to the blocking queue when
 * connection with client is lost, or when user decides to log out, or when
 * connection with client simply is lost
 *
 * @author Michal
 */
public class LogOutEvent extends BaseEvent {
    /**
     * serialVersionUID for this class
     */
    private static final long serialVersionUID = 1L;
}
