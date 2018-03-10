package controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import events.BaseEvent;
import events.LogInEvent;
import events.LogOutEvent;
import events.MessageEvent;
import events.ResendEvent;
import model.ChatState;
import model.Message;
import view.ClientViewController;

/**
 * Class responsible for client's connection to the server. It sends events and
 * receives status updates from server, which then are sent to the view
 *
 * @author Michal
 */

public class NetworkManager {
    /**
     * ClientViewController communicating with this NetworkManager
     */
    private ClientViewController viewController;
    /**
     * BlockingQueue to which events are send from view
     */
    private BlockingQueue<BaseEvent> blockingQueue;
    /**
     * mapping events to strategies objects that can handle them
     */
    private Map<Class<? extends BaseEvent>, NetworkStrategy> eventToStrategyMap;
    /**
     * stream receiving objects from the server, exactly in ClientManager
     */
    private ObjectInputStream objectInputStream;
    /**
     * stream sending objects to the server, exactly in ClientManager
     */
    private ObjectOutputStream objectOutputStream;
    /**
     * Socket for client-server connections
     */
    private Socket socket;
    /**
     * Date of last received message
     */
    private Date lastMessageDate;
    /**
     * static value defining how long thread should sleep between sending
     * requests
     */
    private static final int SLEEP_MILISECONDS = 2000;

    /**
     * Constructor
     *
     * @param viewController ClientViewController communicating with this manager
     * @param blockingQueue  BlockingQueue storing events from the view. Events are read in
     *                       this manager and send to the server
     */
    public NetworkManager(ClientViewController viewController, BlockingQueue<BaseEvent> blockingQueue) {
        this.viewController = viewController;
        this.blockingQueue = blockingQueue;
        lastMessageDate = null;
        eventToStrategyMap = new HashMap<>();
        eventToStrategyMap.put(LogInEvent.class, new LogInStrategy());
        eventToStrategyMap.put(LogOutEvent.class, new LogOutStrategy());
        eventToStrategyMap.put(MessageEvent.class, new MessageStrategy());
    }

    /**
     * Method that starts this network manager, using new thread
     */
    public void start() {
        new RequestThread().start();
        while (true) {
            BaseEvent event;
            try {
                event = blockingQueue.take();
            } catch (InterruptedException e) {
                continue;
            }

            if (eventToStrategyMap.containsKey(event.getClass()))
                eventToStrategyMap.get(event.getClass()).execute(event);
        }
    }

    /**
     * Method that tries connecting this manager to the server
     *
     * @param host host name to connect
     * @param port port to connect
     * @throws IOException when connection attempt fails
     */
    private void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        objectInputStream = new ObjectInputStream(socket.getInputStream());
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
    }

    /**
     * Method that disconnects this manager from the server, without influence
     * in the view
     */
    private void disconnect() {
        try {
            objectInputStream.close();
            objectOutputStream.close();
            socket.close();
        } catch (IOException | NullPointerException e) {

        }

        objectInputStream = null;
        objectOutputStream = null;
        socket = null;
        lastMessageDate = null;
    }

    /**
     * Method that attempts to send event to the server
     *
     * @param event BaseEvent to be send to the server
     */
    private void sendEventToServer(BaseEvent event) {
        if (objectOutputStream == null)
            return;

        try {
            synchronized (objectOutputStream) {
                objectOutputStream.writeObject(event);
            }
        } catch (IOException e) {
            disconnect();
        }
    }

    /**
     * Thread that listens to the server and receives sent objects
     *
     * @author Michal
     */
    private class ServerListener extends Thread {
        @Override
        public void run() {
            while (true) {
                ChatState state;
                try {
                    state = (ChatState) objectInputStream.readObject();

                } catch (IOException | ClassNotFoundException | NullPointerException e) {

                    viewController.setDisconnected();
                    disconnect();
                    return;
                }

                if (!state.isLoggedIn()) {
                    viewController.setBasedOnChatState(state);
                    disconnect();
                    return;
                }
                handleChatStateChange(state);
            }
        }
    }

    /**
     * Method that takes care of all the operations that need to be executed
     * when a new ChatState object is received. It takes care of changing the
     * view and sending request for messages to server if needed
     *
     * @param state newly received ChatState object
     */
    private void handleChatStateChange(ChatState state) {
        if (lastMessageDate == null)
            lastMessageDate = getLatestDate(state.getMessages());

        if (state.isCompatibleWithDate(lastMessageDate)) {
            state.deleteAllMessagesBefore(lastMessageDate);
            viewController.setBasedOnChatState(state);

            Date lastMessageDate = getLatestDate(state.getMessages());
            if (lastMessageDate != null)
                this.lastMessageDate = lastMessageDate;
        }
    }

    /**
     * Method that searches through the list of Messages for a message which was
     * sent later than all the others
     *
     * @param messages list of ChatMessages to be searched
     * @return the latest Date
     */
    private Date getLatestDate(List<Message> messages) {
        if (messages.size() == 0)
            return null;

        Date lastDate = messages.get(0).getSentDate();
        for (Message message : messages) {
            if (message.getSentDate().after(lastDate))
                lastDate = message.getSentDate();
        }
        return lastDate;
    }

    /**
     * Objects of this class are responsible for sending requests for new
     * ChatState to the server every period of time. It helps making sure that
     * user is up to date with his messages
     *
     * @author Michal
     */
    private class RequestThread extends Thread {
        /**
         * Method that sends periodically request to server to state update
         */
        @Override
        public void run() {
            while (true) {
                sendEventToServer(new ResendEvent(lastMessageDate));
                try {
                    Thread.sleep(SLEEP_MILISECONDS);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * Common base for all classes that can handle events from the view
     *
     * @author Michal
     */
    private abstract class NetworkStrategy {
        /**
         * Method that handles event
         *
         * @param event BaseEvent to be handled
         */
        public abstract void execute(BaseEvent event);
    }

    /**
     * Strategy that responds to LogInEvent
     *
     * @author Michal
     */
    private class LogInStrategy extends NetworkStrategy {
        /**
         * Method that handles BaseEvent of type LogInEvent. If connection
         * succeeds, server must first reply with state update message. Next,
         * LogInEvent is passed to the server to be handled properly. In
         * different case execution ends
         *
         * @param event BaseEvent to be handled
         */
        @Override
        public void execute(BaseEvent event) {
            if (!(event instanceof LogInEvent))
                return;

            try {
                LogInEvent logInEvent = (LogInEvent) event;
                String serverName = logInEvent.getServerName();
                int port = Integer.parseInt(logInEvent.getPort());
                connect(serverName, port);
                new ServerListener().start();
            } catch (IOException | NumberFormatException e) {
                disconnect();
                viewController.setDisconnected();
                return;
            }
            sendEventToServer(event);
        }
    }

    /**
     * Strategy that responds to LogOutEvent
     *
     * @author Michal
     */
    private class LogOutStrategy extends NetworkStrategy {
        /**
         * Method that handles BaseEvent of type LogOutEvent. If BaseEvent is
         * type of LogOutEvent is simply forwarded to server. In different case
         * execution ends
         *
         * @param event BaseEvent to be handled
         */
        @Override
        public void execute(BaseEvent event) {
            if (!(event instanceof LogOutEvent))
                return;

            sendEventToServer(event);
        }
    }

    /**
     * Strategy that handles MessageEvent
     *
     * @author Michal
     */
    private class MessageStrategy extends NetworkStrategy {
        /**
         * Method that handles BaseEvent of type MessageEvent. If BaseEvent is
         * type of MessageEvent the date of last received message is attached to
         * it and it is forwarded to the server. In different case execution
         * ends
         *
         * @param event BaseEvent to be handled
         */
        @Override
        public void execute(BaseEvent event) {
            if (!(event instanceof MessageEvent))
                return;

            ((MessageEvent) event).setPreviousMessageDate(lastMessageDate);
            sendEventToServer(event);
        }
    }
}
