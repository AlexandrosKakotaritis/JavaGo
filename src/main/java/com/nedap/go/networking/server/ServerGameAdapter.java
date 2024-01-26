package com.nedap.go.networking.server;


import com.nedap.go.model.GoGame;
import com.nedap.go.model.GoMove;
import com.nedap.go.model.GoMoveRowColumn;
import com.nedap.go.model.Move;
import com.nedap.go.model.Stone;
import com.nedap.go.model.utils.InvalidMoveException;
import java.util.Arrays;
import java.util.List;

public class ServerGameAdapter {

  private final GameServer server;
  private final ClientHandler client1;
  private final ClientHandler client2;
  private OnlinePlayer player1, player2;
  private GoGame game;
  private final int boardDim;

  public ServerGameAdapter(ClientHandler client1, ClientHandler client2, GameServer server,
      int boardDim) {
    this.client1 = client1;
    this.client2 = client2;
    this.server = server;
    this.boardDim = boardDim;
    createGame();
  }

  private int rowColumnToIndex(int row, int column){
    return row * boardDim + column;
  }

  public boolean isGameOver() {
    return game.isGameover();
  }

  public List<ClientHandler> getClients() {
    return Arrays.asList(client1, client2);
  }

  public GoMove newMove(int index, ClientHandler clientHandler)
      throws InvalidMoveException, NotYourTurnException {
      if (((OnlinePlayer) game.getTurn()).getClientHandler().equals(clientHandler)) {
          GoMove move = new GoMove(game.getTurn(), index);
          game.doMove(move);
          return move;
      } else {
          throw new NotYourTurnException();
      }
  }

  public GoMove newMove(int row, int col, ClientHandler clientHandler)
      throws InvalidMoveException, NotYourTurnException {
      if (((OnlinePlayer) game.getTurn()).getClientHandler().equals(clientHandler)) {
        GoMoveRowColumn move = new GoMoveRowColumn(game.getTurn(), row, col);
        game.doMove(move);
        return new GoMove(move.getPlayer(),
            rowColumnToIndex(move.getRow(), move.getColumn()));
      } else {
          throw new NotYourTurnException();
      }
  }

  public GoMove passMove(ClientHandler clientHandler)
      throws InvalidMoveException, NotYourTurnException {
      if (((OnlinePlayer) game.getTurn()).getClientHandler().equals(clientHandler)) {
          GoMove move = new GoMove(game.getTurn());
          game.doMove(move);
          return move;
      } else {
        throw new NotYourTurnException();
      }
  }

  public OnlinePlayer getTurn(){
    return (OnlinePlayer) game.getTurn();
  }


  private void createGame() {
    player1 = createPlayer(client1, Stone.BLACK);
    player2 = createPlayer(client2, Stone.WHITE);
    game = new GoGame(player1, player2, boardDim);
  }

  public void endGame(boolean quit) {
      if (quit) {
          server.gameOver(this, getQuitMessage());
      } else {
          OnlinePlayer winner = (OnlinePlayer) game.getWinner();
          if (winner == null) {
              server.gameOver(this, "It's a tie");
          } else {
              server.gameOver(this, "Winner is: " + winner.getName() + " GG!");
          }
      }
  }

  private OnlinePlayer createPlayer(ClientHandler clientHandler, Stone mark) {
    return new OnlinePlayer(clientHandler, mark);
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
      if (client1.equals(clientHandler)) {
          return client2;
      } else
          return client1;
  }

}
