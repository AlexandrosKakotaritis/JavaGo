package com.nedap.go.networking.server;


import com.nedap.go.model.Player;
import com.nedap.go.model.Stone;

public class OnlinePlayer implements Player {

    private final String name;
    private final Stone stone;
    public OnlinePlayer(String name, Stone stone) {
        this.name = name;
        this.stone = stone;
    }

    @Override
    public Stone getStone() {
        return stone;
    }

    public String getName(){
        return name;
    }
}
