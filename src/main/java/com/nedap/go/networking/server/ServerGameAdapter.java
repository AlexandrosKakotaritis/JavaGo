package com.nedap.go.networking.server;


import com.nedap.go.model.GoGame;
import com.nedap.go.model.GoMove;
import com.nedap.go.model.GoMoveRowColumn;
import com.nedap.go.model.Stone;
import com.nedap.go.model.utils.InvalidMoveException;
import com.nedap.go.networking.server.utils.NotYourTurnException;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Connecting the server to the Go game model.
 */
public class ServerGameAdapter {

  private final GameServer server;
  private final ClientHandler client1;
  private final ClientHandler client2;
  private final int boardDim;
  private OnlinePlayer player1;
  private OnlinePlayer player2;
  private GoGame game;

  private Timer timer;
  private static final long timePerMove = 120000;

  /**
   * Construct a game server adapter object.
   *
   * @param client1  The client representing the first player.
   * @param client2  The client representing the second player.
   * @param server   The server handling the clients.
   * @param boardDim The dimension of the board of the game.
   */
  public ServerGameAdapter(ClientHandler client1, ClientHandler client2, GameServer server,
      int boardDim) {
    this.client1 = client1;
    this.client2 = client2;
    this.server = server;
    this.boardDim = boardDim;
    createGame();
  }

  private int rowColumnToIndex(int row, int column) {
    return row * boardDim + column;
  }

  public boolean isGameOver() {
    return game.isGameover();
  }

  public List<ClientHandler> getClients() {
    return Arrays.asList(client1, client2);
  }

  /**
   * Checks and plays a new move from a client using the index format.
   *
   * @param index         The index of the move.
   * @param clientHandler The handler of the client playing the move.
   * @return The move played
   * @throws InvalidMoveException If a move that cannot be player occurs
   * @throws NotYourTurnException If a client tries to play when not their turn.
   */
  public GoMove newMove(int index, ClientHandler clientHandler)
      throws InvalidMoveException, NotYourTurnException {
    if (isYourTurn(clientHandler)) {
      cancelTimer();
      GoMove move = new GoMove(game.getTurn(), index);
      game.doMove(move);
      createTimer(clientHandler);
      return move;
    } else {
      throw new NotYourTurnException();
    }
  }

  /**
   * Checks and plays a new move from a client using the row column format.
   *
   * @param row           The row index of the move.
   * @param col           The column index of the move
   * @param clientHandler The handler of the client playing the move.
   * @return The move played
   * @throws InvalidMoveException If a move that cannot be player occurs
   * @throws NotYourTurnException If a client tries to play when not their turn.
   */
  public GoMove newMove(int row, int col, ClientHandler clientHandler)
      throws InvalidMoveException, NotYourTurnException {
    if (isYourTurn(clientHandler)) {
      cancelTimer();
      GoMoveRowColumn move = new GoMoveRowColumn(game.getTurn(), row, col);
      game.doMove(move);
      createTimer(getOtherClient(clientHandler));
      return new GoMove(move.getPlayer(), rowColumnToIndex(move.getRow(), move.getColumn()));
    } else {
      throw new NotYourTurnException();
    }
  }

  private void cancelTimer() {
    if(timer != null) {
      timer.cancel();
    }
  }

  private void createTimer(ClientHandler clientHandler) {
    timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        endGameOnResign(getOtherClient(clientHandler));
      }
    }, timePerMove ); //1 min
  }

  /**
   * Checks and plays a new pass move from a client.
   *
   * @param clientHandler The handler of the client playing the move.
   * @return The move played
   * @throws InvalidMoveException If a move that cannot be player occurs.
   * @throws NotYourTurnException If a client tries to play when not their turn.
   */
  public GoMove passMove(ClientHandler clientHandler)
      throws InvalidMoveException, NotYourTurnException {
    if (isYourTurn(clientHandler)) {
      cancelTimer();
      GoMove move = new GoMove(game.getTurn());
      game.doMove(move);
      createTimer(getOtherClient(clientHandler));
      return move;
    } else {
      throw new NotYourTurnException();
    }
  }

  public OnlinePlayer getTurn() {
    return (OnlinePlayer) game.getTurn();
  }


  private void createGame() {
    player1 = createPlayer(client1.getUsername(), Stone.BLACK);
    player2 = createPlayer(client2.getUsername(), Stone.WHITE);
    game = new GoGame(player1, player2, boardDim);
  }

  /**
   * Queries the terms of a finished games and propagates the information
   * to the server.
   */
  public void endGame() {
    cancelTimer();
    OnlinePlayer winner = (OnlinePlayer) game.getWinner();
    System.out.println(game);
    if (winner == null) {
      server.sendDraw(this);
    } else {
      server.sendWinner(this, winner);
    }
  }

  public void endGameOnResign(ClientHandler clientHandler) {
    cancelTimer();
    server.sendWinner(this, getWinnerOnResign(clientHandler));
  }

  private OnlinePlayer createPlayer(String username, Stone mark) {
    return new OnlinePlayer(username, mark);
  }

  private OnlinePlayer getWinnerOnResign(ClientHandler clientHandler) {
    return getOtherPlayer(clientHandler);
  }

  public OnlinePlayer getOtherPlayer(ClientHandler clientHandler) {
    return clientHandler.getUsername().equals(player1.getName()) ? player2 : player1;
  }

  private boolean isYourTurn(ClientHandler clientHandler) {
    return ((OnlinePlayer) game.getTurn()).getName()
        .equals(clientHandler.getUsername());
  }

  public ClientHandler getOtherClient(ClientHandler clientHandler) {
    return clientHandler.equals(client1)? client2: client1;
  }
}
