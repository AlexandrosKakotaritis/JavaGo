package com.nedap.go.networking.server;

import java.util.List;
import java.util.Set;

/**
 * Client handler for game application. Responsible for forwarding decoded instructions to the server
 */
public class ClientHandler {

    private final GameServer server;
    private String username;

    private ServerConnection serverConnection;

    public ClientHandler(GameServer server) {
        this.server = server;
    }

    /**
     * Set the ServerConnection object responsible for decoding the messages, maintain the conection
     * to the client and handling the input output streams.
     *
     * @param serverConnection the ServerConnection object.
     */
    public void setServerConnection(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    /**
     * Received hello and sent to the server.
     */
    public void helloReceived(String information) {
        server.helloReceived(this, information);
    }

    /**
     * Get the username set by the client.
     *
     * @return The username as a string.
     */
    public String getUsername() {
        return username;
    }

    public boolean receiveLogin(String username) {
        this.username = username;
        return server.addClient(this);
    }

    /**
     * Inform the server of the disconnection of the client.
     */
    public void handleDisconnect() {
        server.removeClient(this);
    }


    public void sendLogin(boolean nameOK) {
        serverConnection.sendLogin(nameOK);
    }


    public void sayHello(Set runExtensions) {
        serverConnection.sayHello(runExtensions);
    }

    public void queueReceived() {
        server.addInQueue(this);
    }

    public void listReceived() {
        server.listReceived(this);
    }

    public void sendList(List<ClientHandler> listOfClients) {
        serverConnection.sendList(listOfClients);
    }

    public void startGame(String usernamePlayer1, String usernamePlayer2, int boardDim) {
        serverConnection.startGame(usernamePlayer1, usernamePlayer2, boardDim);
    }

    public void exitQueue() {
        server.removeFromQueue(this);
    }

    public void receiveMove(int moveIndex) {
        server.handleMove(this, moveIndex);
    }

    public void receiveMove(int row, int col) {
        server.handleMove(this, row, col);
    }

    public void receivePass() {
        server.handlePass(this);
    }

    public void sendError(String errorMessage) {
        serverConnection.sendError(errorMessage);
    }

    public void sendMove(int moveIndex) {
        serverConnection.sendMove(moveIndex);
    }

    public void sendMove(int row, int col) {
        serverConnection.sendMove(row, col);
    }

    public void sendPass() {
        serverConnection.sendPass();
    }

    public void sendGameOver(String message) {
        serverConnection.sendGameOver(message);
    }
}




