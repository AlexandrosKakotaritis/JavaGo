package com.nedap.go.networking.client;

import com.nedap.go.ai.ComputerPlayer;
import com.nedap.go.ai.NaiveStrategy;
import com.nedap.go.ai.PassStrategy;
import com.nedap.go.model.AbstractPlayer;
import com.nedap.go.model.GoGame;
import com.nedap.go.model.GoMove;
import com.nedap.go.model.Player;
import com.nedap.go.model.Stone;
import com.nedap.go.model.utils.InvalidMoveException;
import com.nedap.go.networking.protocol.Protocol;
import com.nedap.go.networking.server.OnlinePlayer;
import com.nedap.go.networking.server.utils.PlayerNotFoundException;
import com.nedap.go.tui.HumanPlayer;
import com.nedap.go.tui.QuitGameException;

/**
 * The class that works as an adapter of routing
 * incoming and outgoing moves properly and game states.
 */
public class ClientGameAdapter {

  private final int boardDim;
  private final GameClient client;
  private boolean isMoveReceived;
  private GoGame game;
  private GoMove serverMove;
  private Player myPlayer;
  private Player otherPlayer;
  private boolean isGameOver;
  private String gameEndingMessage;

  private long moveTimer = 10000;

  /**
   * Main constructor.
   * @param player1Name The name of the player with black
   * @param player2Name The name of the player with black
   * @param boardDim Dimension of the board.
   * @param client The client playing the game.
   * @throws PlayerNotFoundException When one of the player names is not
   *     equal to the client's username.
   */
  public ClientGameAdapter(String player1Name, String player2Name, int boardDim, GameClient client)
      throws PlayerNotFoundException {
    this.client = client;
    this.boardDim = boardDim;
    buildGame(player1Name, player2Name);
  }

  private void buildGame(String player1Name, String player2Name) throws PlayerNotFoundException {
    if (client.getUsername().equals(player1Name)) {
      createMyPlayer(player1Name, Stone.BLACK);
      otherPlayer = new OnlinePlayer(player2Name, Stone.WHITE);
      game = new GoGame(myPlayer, otherPlayer, boardDim);
    } else if (client.getUsername().equals(player2Name)) {
      createMyPlayer(player2Name, Stone.WHITE);
      otherPlayer = new OnlinePlayer(player1Name, Stone.BLACK);
      game = new GoGame(otherPlayer, myPlayer, boardDim);
    } else {
      throw new PlayerNotFoundException(
          "Neither " + player1Name + " nor " + player2Name + " is associated with this client");
    }
  }

  private void createMyPlayer(String name, Stone stone) {
    myPlayer = switch (client.getPlayerType()) {
      case 2 -> new ComputerPlayer(name, new NaiveStrategy(), stone);
      case 3 -> new ComputerPlayer(name, new PassStrategy(), stone);
      default -> new HumanPlayer(name, stone);
    };
  }

  public synchronized void playMove()
      throws GameMismatchException, QuitGameException, InvalidMoveException {
    GoMove myMove = null;
    if (isMyMove()) {
      myMove = myMove();
      client.sendMove(myMove);
    }
    GoMove previousServerMove = null;
    while (!isMoveReceived && !isGameOver()) {
      try {
        previousServerMove = serverMove;
        this.wait(500);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    if (isMyMove() && myMove != null  && !myMove.equals(serverMove)) {
      throw new GameMismatchException("Server move not matching client's move");
    }
    doMove(previousServerMove);
  }

  private void doMove(GoMove previousMove) throws InvalidMoveException {
    if (isMoveReceived || !isGameOver()
        && !previousMove.equals(serverMove)) {
      game.doMove(serverMove);
    }
    isMoveReceived = false;
  }

  private boolean isMyMove() {
    return game.getTurn().equals(myPlayer) && !isGameOver;
  }

  private GoMove myMove() throws QuitGameException {
    GoMove myMove;
    myMove = (GoMove) ((AbstractPlayer) myPlayer).determineMove(game);
    return myMove;
  }

  public synchronized void receiveMove(int moveIndex, String moveColor) {
    Player player = getPlayerFromColor(moveColor);
    serverMove = new GoMove(player, moveIndex);
    isMoveReceived = true;
    notifyAll();
  }

  public synchronized void receivePass(String color) {
    Player player = getPlayerFromColor(color);
    serverMove = new GoMove(player);
    isMoveReceived = true;
    notifyAll();
  }

  private Player getPlayerFromColor(String moveColor) {
    Stone stone = moveColor.equals(Protocol.BLACK) ? Stone.BLACK : Stone.WHITE;
    return myPlayer.getStone().equals(stone) ? myPlayer : otherPlayer;
  }

  private Player getPlayerFromName(String name) {
    return ((AbstractPlayer) myPlayer).getName().equals(name) ? myPlayer : otherPlayer;
  }

  public synchronized void receiveDraw() throws GameMismatchException {
    if (game.isGameover() && game.getWinner() == null) {
      isGameOver = true;
      gameEndingMessage = "It is a draw";
    } else if (isGameOver && game.getWinner() != null) {
      throw new GameMismatchException(
          "Server decides DRAW " + "while client decides WINNER: " + game.getWinner());
    } else {
      throw new GameMismatchException("Game ended for server and not " + "for client");
    }
    notifyAll();
  }

  public synchronized void receiveWinner(String winner) throws GameMismatchException {
    if (sameWinner(winner)) {
      gameEndingMessage = game.getWinner().equals(myPlayer) ? "You win!" : "You lose!";
      isGameOver = true;
    } else if (notSameWinner(winner)) {
      throw new GameMismatchException(
          "Server decides WINNER: " + game.getWinner() + "while client decides WINNER: "
              + game.getWinner());
    } else if (isMyPlayerAndGameNotOver(winner)) {
      gameEndingMessage = "You win, opponent forfeited!";
      isGameOver = true;
    } else {
      gameEndingMessage = "You forfeited";
      isGameOver = true;
    }
    notifyAll();
  }

  private boolean isMyPlayerAndGameNotOver(String winner) {
    return !game.isGameover() && myPlayer.equals(getPlayerFromName(winner));
  }

  private boolean notSameWinner(String winner) {
    return game.isGameover() && !game.getWinner().equals(getPlayerFromName(winner));
  }

  private boolean sameWinner(String winner) {
    return game.isGameover() && game.getWinner().equals(getPlayerFromName(winner));
  }

  public boolean isGameOver() {
    return isGameOver || game.isGameover();
  }

  public String displayState() {
    return game.toString();
  }

  public synchronized String getGameEndMessage() {
    while (gameEndingMessage == null) {
      try {
        this.wait(500);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    return gameEndingMessage;
  }
}
