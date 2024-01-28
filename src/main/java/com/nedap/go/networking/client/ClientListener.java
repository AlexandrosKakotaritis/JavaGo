package com.nedap.go.networking.client;

import java.util.List;

/**
 * Interface for Listener design pattern on ChatClientTUI
 */
public interface ClientListener {
    /**
     * Confirms that log in was successful with the server.
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

    /**
     * Notify listeners of successful connection with the server
     * and propagates server's message.
     *
     * @param message The server's hello message.
     */
    void successfulConnection(String message);

    /**
     * Receive the playerList.
     *
     * @param playerList The list of players.
     */
    void receiveList(List<String> playerList);

    /**
     * Receive confirmation of entering matchmaking queue.
     */
    void receiveInQueue();

    /**
     * Starts new game.
     * @param player1Name The name of the first player with black
     * @param player2Name The name of the second player with white.
     * @param boardDim The dimension of the board.
     */
    void newGame(String player1Name, String player2Name, int boardDim);

    void printError(String message);
}
