package com.nedap.go.networking.client;

import java.util.List;

/**
 * Interface for Listener design pattern on ChatClientTUI
 */
public interface ClientListener {
    /**
     * Confirms that log in was successfull with the server.
     * @param status The status. True if successful
     * @param username The username used.
     */
    void logInStatus(boolean status, String username);
    /**
     * Notify listeners of message
     * @param sender The username of the sender
     * @param message The message
     */
    void chatMessage(String sender, String message);

    /**
     * Notify listeners of disconnect.
     */
    void connectionLost();

    void successfulConnection(String message);

    void receiveList(List<String> playerList);

    void receiveInQueue();
}
