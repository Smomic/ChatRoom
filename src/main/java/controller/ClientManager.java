package controller;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import events.BaseEvent;
import events.LogOutEvent;
import model.ChatState;

/**
 * Class that is responsible for connection with one client. It is fully
 * connected, when object of this class is created and connection succeeds, if
 * this user name is available
 *
 * @author Michal
 */
public class ClientManager extends Thread {
    /**
     * socket of this client's connection
     */
    private Socket socket;
    /**
     * stream from which objects are read
     */
    private ObjectInputStream objectInputStream;
    /**
     * stream to which objects are sent
     */
    private ObjectOutputStream objectOutputStream;
    /**
     * BlockingQueue to which received events are sent
     */
    private BlockingQueue<BaseEvent> blockingQueue;
    /**
     * boolean value telling if thread should stop
     */
    private volatile boolean stopFlag;
    /**
     * boolean value telling if this client is logged in and can exchange
     * messages
     */
    private volatile boolean loginFlag;
    /**
     * Time in ms to sleep between two messages received by client. Additional
     * defense mechanism to protect clients sending too many messages
     */
    private final static int TIME_BETWEEN_MESSAGES = 100;

    /**
     * Constructor
     *
     * @param socket        socket to which this client is going to connect
     * @param blockingQueue queue to which this client manager is going to send events
     * @throws IOException if could not create ObjectStream
     */
    ClientManager(Socket socket, BlockingQueue<BaseEvent> blockingQueue) throws IOException {
        this.socket = socket;
        this.blockingQueue = blockingQueue;
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectInputStream = new ObjectInputStream(socket.getInputStream());
        stopFlag = true;
        loginFlag = false;
    }

    /**
     * Method that sets loginFlag for true if client is logged in. This method
     * should be invoked when user's name is accepted and he can start to
     * exchange messages.
     */
    public void setLoginFlag() {
        this.loginFlag = true;
    }

    /**
     * Method that returns boolean value representing current status of being
     * logged in for this user.
     *
     * @return true if user is logged in, false if not
     */
    public boolean getLoginFlag() {
        return loginFlag;
    }

    /**
     * Method that sends current ChatState to the client concerned
     *
     * @param state current ChatState
     */
    public void send(ChatState state) {
        try {
            objectOutputStream.writeObject(state);
        } catch (IOException ignored) {

        }
    }

    /**
     * Method that closes connection with the client, should be invoked before
     * removing the client
     */
    public void close() {
        stopFlag = false;
        loginFlag = false;

        try {
            if (objectInputStream != null)
                objectInputStream.close();

            if (objectOutputStream != null)
                objectOutputStream.close();

            if (socket != null)
                socket.close();
        } catch (Exception ignored) {
        }
    }

    /**
     * Method responsible for listening to the client
     */
    @Override
    public void run() {
        while (true) {
            try {
                BaseEvent event = (BaseEvent) objectInputStream.readObject();
                event.setClientManager(this);
                blockingQueue.put(event);
            } catch (ClassNotFoundException | InterruptedException | InvalidClassException e) {
                continue;
            } catch (IOException e) {
                logout();
            }

            try {
                Thread.sleep(TIME_BETWEEN_MESSAGES);
            } catch (InterruptedException ignored) {
            }
        }
    }

    /**
     * Method that tries to log out user by adding LogOutEvent to the
     * blockingQueue
     */
    public void logout() {
        if (!stopFlag)
            return;

        BaseEvent event = new LogOutEvent();
        event.setClientManager(this);
        try {
            blockingQueue.put(event);
        } catch (InterruptedException e) {
            System.err.println("Cannot close clientSocket!");
        }
    }
}
