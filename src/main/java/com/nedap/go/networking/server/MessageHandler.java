package com.nedap.go.networking.server;

import com.nedap.go.networking.protocol.Protocol;

public class MessageHandler {
    private PlayerState playerState;
    private final ClientHandler clientHandler;

    MessageHandler(ClientHandler clientHandler){
        this.clientHandler = clientHandler;
        playerState = PlayerState.FRESH;
    }

    PlayerState getPlayerState(){
        return playerState;
    }

    void setPlayerState(PlayerState playerState){
        this.playerState = playerState;
    }

    void handleMessage(String message) {
        switch (playerState){
            case FRESH -> handleInitialization(message);
            case PREGAME -> handlePreGame(message);
            case INGAME -> handleGame(message);
        }
    }

    private void handleGame(String message){
        String[] messageArray = splitMessage(message);
        if(messageArray.length > 1){
            switch (messageArray[0]) {
                case Protocol.MOVE -> handleMove(messageArray[1]);
                case Protocol.PASS -> clientHandler.receiveMove();
            }
        }
    }

    private void handleMove(String s) {

    }

    private void handlePreGame(String message) {
        String[] messageArray = splitMessage(message);
        if(messageArray.length == 1) {
            switch(messageArray[0]){
                case Protocol.LIST -> clientHandler.listReceived();
                case Protocol.QUEUE -> clientHandler.queueReceived();
            }
        }
    }


    private void handleInitialization(String message) {
        String[] messageArray = splitMessage(message);
        if (messageArray.length > 1) {
          if (messageArray[0].equals(Protocol.LOGIN)) {
            clientHandler.receiveLogin(messageArray[1]);
          }
        }
    }

    private String[] splitMessage(String message){
        return message.split(Protocol.SEPARATOR);
    }
}
