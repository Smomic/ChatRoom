package view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.concurrent.Task;
import model.Model;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import controller.Controller;
import controller.ServerManager;
import events.BaseEvent;

/**
 * Class responsible for creating all objects that the server composes of. It
 * creates Model, LinkedBlockingQueue type of BaseEvent and ServerManager to
 * communicate with server. ClientView sends events to queue and NetworkManager
 * reads them from the queue
 *
 * @author Michal
 */
public class ServerViewController {
    @FXML
    TextField portTextField;
    @FXML
    Button runButton;
    @FXML
    Button stopButton;
    @FXML
    TextArea status;
    @FXML
    ProgressBar progressBar;
    @FXML
    Label portNumberLabel;
    /**
     * ServerManager which is starting by clicking runButton and correctly
     * iniciasizing in the same place
     */
    private ServerManager serverManager = null;
    /**
     * BlockingQueue used to create Controller and ServerManager
     */
    private BlockingQueue<BaseEvent> blockingQueue = null;
    /**
     * boolean value telling if Model and Controller have been created
     */
    private volatile boolean hasInitFlag = false;
    /**
     * table of announcement used to communicate
     */
    private static String announcementTab[] = {"Server is running", "Server stopped",
            "Wrong input. One number expected", "Couldn't create server on selected port",
            "Couldn't stop server on selected port"};

    /**
     * Method inits elements of the class, called from Server class
     */
    public void init() {
        blockingQueue = new LinkedBlockingQueue<>();
        status.setText("");
        stopButton.setDisable(true);
    }

    /**
     * Method checks correctness of number of, in case of success server is
     * initiating and networkManager is creating and starting
     */
    @FXML
    public void runButtonAction() {
        try {
            initServerManager(readPort());
            if (!hasInitFlag)
                initServer();

            setRunning(true);
            status.setText(announcementTab[0]);
            progressBar.setProgress(-1.0f);
            hideWarning();
        } catch (NumberFormatException e) {
            showWarning(announcementTab[2]);
        } catch (Exception e) {
            showWarning(announcementTab[3]);
        }
    }

    /**
     * Method reads port number from user
     *
     * @throws NumberFormatException when value of port is incorrect
     */
    private int readPort() throws NumberFormatException {
        String portString = portTextField.getText();
        return Integer.parseInt(portString);
    }

    /**
     * Method that inits ServerManager
     *
     * @param port number of port
     * @throws IOException when value of port is taken
     */
    private void initServerManager(int port) throws IOException {
        serverManager = new ServerManager(port, blockingQueue);
        serverManager.start();
    }

    /**
     * Method that inits server, strictly Model and Controller
     */
    private void initServer() {
        Model model = new Model();
        final Controller controller = new Controller(model, serverManager, blockingQueue);
        hasInitFlag = true;
        startController(controller);
    }

    /**
     * Method starts Controller by creating new thread
     *
     * @param controller created Controller
     */
    private void startController(final Controller controller) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                controller.start();
                return null;
            }
        };

        new Thread(task).start();
    }

    /**
     * Method that sets elements of the view depending on the run status
     *
     * @param isRunning true if the server is running
     */
    private void setRunning(boolean isRunning) {
        runButton.setDisable(isRunning);
        stopButton.setDisable(!isRunning);
        portTextField.setEditable(!isRunning);
    }

    /**
     * Method that stops ability to add new users to server. It is helpful in
     * case of running server using another port
     */
    @FXML
    public void stopButtonAction() {
        if (runButton.isDisabled()) {
            try {
                setRunning(false);
                serverManager.closeSocket();
                serverManager.close();
                status.setText(announcementTab[1]);
                progressBar.setProgress(0.0f);

            } catch (IOException e) {

                setRunning(true);
                status.setText(announcementTab[4]);
            }
        }
    }

    /**
     * Method shows announcement in portNumberLabel
     *
     * @param info announcement
     */
    private void showWarning(String info) {
        portNumberLabel.setText(info);
    }

    /**
     * Method hides announcement in portNumberLabel
     */
    private void hideWarning() {
        portNumberLabel.setText("");
    }
}
