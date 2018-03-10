package model;

import java.util.Date;

/**
 * Model of a client. It contains his name and date of him joining the chat
 *
 * @author Michal
 */
class ClientModel {
    /**
     * name of the user
     */
    private String userName;
    /**
     * Date on which user was added to the model
     */
    private Date addingDate;

    /**
     * Basic constructor that takes the name of the user
     *
     * @param userName name of the user
     */
    ClientModel(String userName) {
        this.userName = userName;
        this.addingDate = new Date();
    }
}
