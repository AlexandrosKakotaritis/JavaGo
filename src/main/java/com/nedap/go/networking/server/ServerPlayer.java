package com.nedap.go.networking.server;


import com.nedap.go.model.Player;
import com.nedap.go.model.Stone;

public class ServerPlayer implements Player {

    private final ClientHandler clientHandler;
    private final Stone stone;
    public ServerPlayer(ClientHandler clientHandler, Stone stone) {
        this.clientHandler = clientHandler;
        this.stone = stone;
    }

    @Override
    public Stone getStone() {
        return stone;
    }

    public ClientHandler getClientHandler() {return clientHandler;}

    public String getName(){
        return clientHandler.getUsername();
    }
}