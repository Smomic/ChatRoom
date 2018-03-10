package model;

import java.io.Serializable;
import java.util.Date;

/**
 * Class that contains information about string message: the author, the date of
 * sending and content of the message. Objects of this type can be compared
 *
 * @author Michal
 */
public class Message implements Serializable, Comparable<Message> {
    /**
     * serialVersionUID for this class
     */
    private static final long serialVersionUID = 1L;

    /**
     * author of the message
     */
    private String author;
    /**
     * Date of sending the message
     */
    private Date sentDate;
    /**
     * content of the message
     */
    private String content;

    /**
     * Constructor
     *
     * @param content  content of the message to be send
     * @param author   user name of the sending person
     * @param sentDate when was this message sent
     */
    public Message(String author, Date sentDate, String content) {
        this.author = author;
        this.sentDate = sentDate;
        this.content = content;
    }

    /**
     * Copy constructor for a chatMessage
     *
     * @param chatMessage ChatMessage to be copied
     */
    Message(Message chatMessage) {
        this.author = chatMessage.author;
        this.sentDate = chatMessage.sentDate;
        this.content = chatMessage.content;
    }

    /**
     * Method that compares two messages, by their sending dates
     */
    @Override
    public int compareTo(Message o) {
        if (this.sentDate.equals(o.sentDate))
            return 0;

        if (this.sentDate.before(o.sentDate))
            return -1;

        return 1;
    }

    /**
     * Method returning author of the message
     *
     * @return author of the message
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Method returning Date on which this message was sent
     *
     * @return Date on which this message was sent
     */
    public Date getSentDate() {
        return sentDate;
    }

    /**
     * Method returning content of the message
     *
     * @return content of the message
     */
    public String getContent() {
        return content;
    }

}
