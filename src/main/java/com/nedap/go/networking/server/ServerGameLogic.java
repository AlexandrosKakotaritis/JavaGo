package com.nedap.go.networking.server;


import com.nedap.go.model.GoGame;
import com.nedap.go.model.GoMove;
import com.nedap.go.model.Move;
import com.nedap.go.model.Stone;
import com.nedap.go.model.utils.InvalidMoveException;
import com.nedap.go.tui.QuitGameException;
import java.util.Arrays;
import java.util.List;

public class ServerGameLogic {
    GameServer server;
    ClientHandler client1, client2;
    OnlinePlayer player1, player2;
    GoGame game;

    public ServerGameLogic(ClientHandler client1, ClientHandler client2, GameServer server) {
        this.client1 = client1;
        this.client2 = client2;
        this.server = server;
        createGame();
    }

    public List<ClientHandler> getClients(){
        return Arrays.asList(client1, client2);
    }

    public int rowColumnToIndex(int row, int col){
        return game.rowColumnToIndex(row, col);
    }

    public boolean newMove(int index, String playerName) throws InvalidMoveException {
        if (((OnlinePlayer) game.getTurn()).getName().equals(playerName)) {
            Move move = new GoMove(game.getTurn(), index);
            game.doMove(move);
            if (game.isGameover()) endGame(false);
            return true;
        }else return false;
    }

    public boolean newMove(int row, int col, String playerName) throws InvalidMoveException {
        if (((OnlinePlayer) game.getTurn()).getName().equals(playerName)) {
            int index = game.rowColumnToIndex(row, col);
            Move move = new GoMove(game.getTurn(), index);
            game.doMove(move);
            if (game.isGameover()) endGame(false);
            return true;
        }else return false;
    }

    public boolean passMove(String playerName) throws QuitGameException, InvalidMoveException {
        if (((OnlinePlayer) game.getTurn()).getName().equals(playerName)) {
            Move move = new GoMove(game.getTurn());
            game.doMove(move);
            if (game.isGameover()) endGame(false);
            return true;
        }else return false;
    }


    private void createGame() {
        player1 = createPlayer(client1.getUsername(), Stone.BLACK);
        player2 = createPlayer(client2.getUsername(), Stone.WHITE);
        game = new GoGame(player1, player2);
    }

    private void endGame(boolean quit) {
        if (quit) server.gameOver(this, getQuitMessage());
        else {
            OnlinePlayer winner = (OnlinePlayer) game.getWinner();
            if (winner == null) {
                server.gameOver(this,"It's a tie");
            } else {
                server.gameOver(this,"Winner is: " + winner.getName() + " GG!");
            }
        }
    }

    private OnlinePlayer createPlayer(String playerName, Stone mark) {
        return new OnlinePlayer(playerName, mark);
    }

    private String getQuitMessage() {
        String message;
        if (game.getTurn().equals(player1)) {
            message = (player1.getName()) + " forfeited the match. " + player2.getName() + " wins";
        } else {
            message = (player2.getName()) + " forfeited the match. " + player1.getName() + " wins";
        }
        return message;
    }

    public ClientHandler getOtherPlayer(ClientHandler clientHandler) {
        if(client1.equals(clientHandler)) return client2;
        else return client1;
    }
}
