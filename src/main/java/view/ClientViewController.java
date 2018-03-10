package view;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import controller.NetworkManager;
import events.BaseEvent;
import events.LogInEvent;
import events.LogOutEvent;
import events.MessageEvent;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import model.ChatState;
import model.Message;
import model.ChatState.UserStatus;

/**
 * Class that sends events to queue and NetworkManager reads them from queue. It
 * is also responsible for creating NetworkManager
 *
 * @author Michal
 */
public class ClientViewController {

    /**
     * TextField for server address input
     */
    @FXML
    TextField ipTextField;
    /**
     * TextField for server port input
     */
    @FXML
    TextField portTextField;
    /**
     * TextField for username input
     */
    @FXML
    TextField usernameTextField;
    /**
     * Button that clicked fires LoginEvent and sends it to the blockingQueue
     */
    @FXML
    Button loginButton;
    /**
     * Button that clicked fires LogOutEvent and sends it to the blockingQueue
     */
    @FXML
    Button logoutButton;
    /**
     * Button that clicked fires MessageEvent with text inserted on
     * messageTextArea
     */
    @FXML
    Button sendMessageButton;
    /**
     * TextArea in which messages are displayed
     */
    @FXML
    TextArea chatTextArea;
    /**
     * TextArea in which user can write his message that he wants to send
     */
    @FXML
    TextArea messageTextArea;
    /**
     * ListView shows names all logged in users
     */
    @FXML
    ListView<String> usernameListView;

    /**
     * BlockingQueue to which events are sent
     */
    private BlockingQueue<BaseEvent> blockingQueue;
    /**
     * mapping of current user state to string that should be displayed
     */
    private HashMap<ChatState.UserStatus, String> stateToMessageMap;
    /**
     * maximum length of a message that can be sent
     */
    private final static int MESSAGE_MAX_LENGTH = 200;

    /**
     * Method that inits elements of the class, called from Client class
     */
    public void init() {
        blockingQueue = new LinkedBlockingQueue<>();
        startNetworkManager();
        logoutButton.setDisable(true);
        stateToMessageMap = new HashMap<>();
        stateToMessageMap.put(UserStatus.LOGGED_IN, "Connection succeeded!");
        stateToMessageMap.put(UserStatus.LOGGED_OUT, "Logging out succeeded.");
        stateToMessageMap.put(UserStatus.MESSAGE_REJECTED, "Your message wasn't delivered. Try again.");
        stateToMessageMap.put(UserStatus.REJECTED, "You have been removed from the server.");
        stateToMessageMap.put(UserStatus.USERNAME_REJECTED, "Username not available. Try another one.");
    }

    /**
     * Method that creates NetworkManager and starts it by creating new thread
     */
    private void startNetworkManager() {
        final NetworkManager nManager = new NetworkManager(this, blockingQueue);
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                nManager.start();
                return null;
            }
        };
        new Thread(task).start();
    }

    /**
     * Method invoked by NetworkManager. It sets the view based on the state
     * defined in this objects. This method is thread safe
     *
     * @param state ChatState object
     */
    public void setBasedOnChatState(final ChatState state) {
        Platform.runLater(() -> {
            if (stateToMessageMap.containsKey(state.getUserStatus()))
                print("\n" + stateToMessageMap.get(state.getUserStatus()) + "\n");

            setConnected(state.isLoggedIn());
            clearUserNames();
            if (state.isLoggedIn()) {
                addUserNames(state.getLoggedInUserNames());
                List<Message> messages = state.getMessages();
                Collections.sort(messages);
                for (Message message : messages)
                    addMessage(message);
            }
        });
    }

    /**
     * Method invoked by NetworkManager. It changes the view when connection
     * with the server has been lost for unknown reasons. This method is thread
     * safe
     */
    public void setDisconnected() {
        Platform.runLater(() -> {
            setConnected(false);
            clearUserNames();
            print("\nConnection has been lost. Try reconnecting.\n");
        });
    }

    /**
     * Method that sets elements of the view depending on the connection status
     *
     * @param isConnected true if view should be displayed for connected user
     */
    private void setConnected(boolean isConnected) {
        ipTextField.setDisable(isConnected);
        portTextField.setDisable(isConnected);
        usernameTextField.setDisable(isConnected);
        loginButton.setDisable(isConnected);
        logoutButton.setDisable(!isConnected);
        sendMessageButton.setDisable(!isConnected);
        messageTextArea.setDisable(!isConnected);
    }

    /**
     * Method that adds user names to usernameListView
     *
     * @param userNames set string names to be added
     */
    private void addUserNames(Set<String> userNames) {
        for (String name : userNames)
            usernameListView.getItems().add(name);
    }

    /**
     * Method that clears the displayed user names on the usernameList
     */
    private void clearUserNames() {
        if (!getUsernameListViewItems().isEmpty())
            getUsernameListViewItems().clear();
    }

    private ObservableList<String> getUsernameListViewItems() {
        return usernameListView.getItems();
    }

    /**
     * Method that adds received message to the window
     *
     * @param chatMessage message to be added to view
     */
    private void addMessage(Message chatMessage) {
        SimpleDateFormat format = new SimpleDateFormat("dd MMMM HH:mm:ss", Locale.GERMANY);
        Date date = chatMessage.getSentDate();
        print("\n" + format.format(date) + ", " + chatMessage.getAuthor() + ":");
        print("\n" + chatMessage.getContent() + "\n");
    }

    /**
     * Method that prints String on the chatTextArea
     *
     * @param string String to printed
     */
    private void print(String string) {
        chatTextArea.appendText(string);
    }

    /**
     * Method responsible for listening to the keys pressed on the
     * messageTextArea. It allows send a message pressing ENTER
     *
     * @param event handled event
     */
    public void keyPressedAction(KeyEvent event) {
        String key = event.getCharacter();

        if (Character.isWhitespace(key.charAt(0)) && (event.getCode() == KeyCode.SPACE)) {
            event.consume();
            return;
        }

        if (event.getCode() == KeyCode.ENTER) {
            event.consume();
            sendMessageButtonAction();
        }
    }

    /**
     * Method responsible for listening to the keys pressed on the
     * messageTextArea. It does not allow on white spaces other than SPACE,
     * space as the first character and too long messages
     *
     * @param event handled event
     */
    public void keyTypedAction(KeyEvent event) {
        String message = messageTextArea.getText();
        String key = event.getCharacter();

        // don't allow space as the first character
        if (message.length() == 0 && Character.isWhitespace(key.charAt(0))) {
            event.consume();
            return;
        }

        // text too long
        if (message.length() >= MESSAGE_MAX_LENGTH) {
            event.consume();
            messageTextArea.setText(message.substring(0, MESSAGE_MAX_LENGTH));
        }
    }

    /**
     * Method that creates LogInEvent and sends it to BlockingQueue
     */
    @FXML
    public void loginButtonAction() {
        String ipString = ipTextField.getText();
        String portString = portTextField.getText();
        String usernameString = usernameTextField.getText();

        if (usernameString.isEmpty() || usernameString.charAt(0) == ' ') {
            print("\nWrong username input. One string expected\n");
            return;
        }
        setConnected(true);
        messageTextArea.setDisable(true);
        sendMessageButton.setDisable(true);
        logoutButton.setDisable(true);

        try {
            Integer.parseInt(portString);
            print("\nWaiting for answer from server...\n");
            blockingQueue.put(new LogInEvent(usernameString, ipString, portString));
        } catch (NumberFormatException e) {
            print("\nWrong port input. One number expected\n");
            setConnected(false);
        } catch (InterruptedException e) {
            setConnected(false);
        }
    }

    /**
     * Method that creates LogOutEvent and sends it to BlockingQueue
     */
    @FXML
    public void logoutButtonAction() {
        try {
            blockingQueue.put(new LogOutEvent());
        } catch (InterruptedException e) {
        }
    }

    /**
     * Method that sends messageEvent to the blockingQueue with string that
     * currently has been inserted into messageTextArea. It also makes empty the
     * messageTextArea
     */
    @FXML
    public void sendMessageButtonAction() {
        String message = messageTextArea.getText();
        if (message.length() == 0)
            return;

        messageTextArea.setText("");
        try {
            blockingQueue.put(new MessageEvent(message));
        } catch (InterruptedException e) {
        }
    }
}
