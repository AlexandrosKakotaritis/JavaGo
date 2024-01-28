package com.nedap.go.networking.client;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class GameClient {

    private ClientConnection clientConnection;

    private String username;

    private final List<ClientListener> listOfListeners;

    public GameClient(InetAddress address, int port) throws IOException {
            clientConnection = new ClientConnection(address, port);
            clientConnection.setChatClient(this);
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
        listOfListeners.forEach(listener -> logInStatus(true, "he"));
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
        listOfListeners.forEach(listener ->
            listener.receiveInQueue());
    }
}
