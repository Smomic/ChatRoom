package events;

import java.util.Date;

/**
 * Event that is created when user sends a message to the server to be
 * broadcasted. Content of the message is appling in the constructor, but the
 * Date of last received message by client is seting by the method. The date is
 * needed for the server to determine whether user is up to date with his
 * messages
 *
 * @author Michal
 */
public class MessageEvent extends BaseEvent {
    /**
     * serialVersionUID for this class
     */
    private static final long serialVersionUID = 1L;
    /**
     * String content of the message
     */
    private String messageString;
    /**
     * Date of last received message by the client
     */
    private Date previousMessageDate;

    /**
     * Basic constructor that sets the value of message content
     *
     * @param messageString content of the message
     */
    public MessageEvent(String messageString) {
        this.messageString = messageString;
    }

    /**
     * Method that should be invoked in the network manager on client side to
     * add the date of last received message
     *
     * @param previouseMessageDate date of last received message by the client
     */
    public void setPreviousMessageDate(Date previouseMessageDate) {
        this.previousMessageDate = previouseMessageDate;
    }

    /**
     * Method that returns content of the message that was sent
     *
     * @return content of the message that was sent
     */
    public String getMessageString() {
        return messageString;
    }

    /**
     * Method that returns date of previously received message by client
     *
     * @return date of previously received message by client
     */
    public Date getPreviousMessageDate() {
        return previousMessageDate;
    }

}
