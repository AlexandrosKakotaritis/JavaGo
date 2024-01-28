package com.nedap.go.networking.client;

import com.nedap.go.networking.server.utils.PlayerNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class GameClient {

    private final ClientConnection clientConnection;

    private String username;

    private final List<ClientListener> listOfListeners;
    private String playerType;

    public GameClient(InetAddress address, int port) throws IOException {
            clientConnection = new ClientConnection(address, port);
            clientConnection.setGameClient(this);
            listOfListeners = new ArrayList<>();
    }

    public String getUsername(){
        return username;
    }


    public void addListener(ClientListener listener){
        listOfListeners.add(listener);
    }

    public void removeListener(ClientListener listener){
        listOfListeners.remove(listener);
    }


    public void sendUsername(String username){
        this.username = username;
        clientConnection.sendUsername(username);
    }

    public void handleDisconnect() {
        listOfListeners.forEach(ClientListener::connectionLost);
    }

    public void close(){
        clientConnection.close();
    }

    public void logInStatus(boolean status, String argument) {
        listOfListeners.forEach(listener
            -> listener.logInStatus(status, argument));
    }

    public void successfulConnection(String message) {
       listOfListeners.forEach(listener ->
           listener.successfulConnection(message));
    }

    public void receiveList(List<String> playerList) {
        listOfListeners.forEach(listener ->
            listener.receiveList(playerList));
    }

    public void receiveInQueue() {
        listOfListeners.forEach(ClientListener::receiveInQueue);
    }

    public void newGame(String player1Name, String player2Name, int boardDim) {
        listOfListeners.forEach(listener
            -> listener.newGame(player1Name, player2Name, boardDim));
    }

    public void sendError(String message) {
        clientConnection.sendError(message);
    }

    public void sendQueue() {
        clientConnection.sendQueue();
    }

    public void setPlayerType(String playerType) {
        this.playerType = playerType;
    }

    public String getPlayerType() {
        return playerType;
    }

    public void sendResign() {

    }
}
