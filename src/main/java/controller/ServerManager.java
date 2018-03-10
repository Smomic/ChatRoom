package controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import events.BaseEvent;
import model.ChatState;

/**
 * Class responsible for listening for new client connections. It has a
 * collection of all connected clients. It can broadcast messages to all
 * connected clients
 *
 * @author Michal
 */
public class ServerManager extends Thread {
    /**
     * set of clients connected to this manager (has to be synchronized)
     */
    private Set<ClientManager> clients;
    /**
     * BlockingQueue to which this manager sends its events
     */
    private BlockingQueue<BaseEvent> blockingQueue;
    /**
     * ServerSocket to listen for clients
     */
    private ServerSocket serverSocket;
    /**
     * maximum number of clients currently connected to server
     */
    private static final int CLIENTS_MAX_COUNT = 50;

    /**
     * Constructor
     *
     * @param port          port on which the manager will listen for users
     * @param blockingQueue queue with events
     * @throws IOException when ServerSocket cannot be created on selected port
     */
    public ServerManager(int port, BlockingQueue<BaseEvent> blockingQueue) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.blockingQueue = blockingQueue;
        clients = new HashSet<>();
    }

    /**
     * Method that sends a message to every user connected to the server
     *
     * @param state ChatState to be broadcasted
     */
    public void broadcast(ChatState state) {
        synchronized (clients) {
            for (ClientManager client : clients) {
                if (client.getLoginFlag())
                    client.send(state);
            }
        }
    }

    /**
     * Method that removes given client from the clients set.
     *
     * @param client client to be removed
     */
    public void removeClient(ClientManager client) {
        synchronized (clients) {
            clients.remove(client);
        }
    }

    /**
     * Method responsible for listening for new clients connections.
     */
    @Override
    public void run() {
        while (true) {
            Socket clientSocket = acceptNewClientSocket();
            synchronized (clients) {
                ClientManager clientManager;
                try {
                    clientManager = new ClientManager(clientSocket, blockingQueue);

                } catch (IOException e) {
                    try {
                        clientSocket.close();

                    } catch (IOException e1) {
                        System.err.println("Cannot close clientSocket!");
                    }

                    continue;
                }
                clients.add(clientManager);
                clientManager.start();
            }
        }
    }

    /**
     * Method that accepts new client connection and returns the socket to which
     * client is connected
     *
     * @return clientSocket the socket to which client is connected
     */
    private Socket acceptNewClientSocket() {
        while (true) {
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                continue;
            }

            if (clients.size() > CLIENTS_MAX_COUNT) {
                try {
                    clientSocket.close();

                } catch (IOException e) {
                    continue;
                }
            }
            return clientSocket;
        }
    }

    /**
     * Method using only in ServerViewController to get serverSocet
     *
     * @return serverSocket the socket with all clients
     */
    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    /**
     * Method using in case stopping server during logged in any client
     */
    public void close() {
        for (ClientManager c : clients)
            c.logout();
    }

    /**
     * Method that close server socket
     *
     * @throws IOException if socket cannot be closed
     */
    public void closeSocket() throws IOException {
        serverSocket.close();
    }
}
