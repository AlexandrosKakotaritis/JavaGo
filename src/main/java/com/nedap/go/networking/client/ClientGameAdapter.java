package com.nedap.go.networking.client;

import com.nedap.go.ai.ComputerPlayer;
import com.nedap.go.ai.NaiveStrategy;
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

public class ClientGameAdapter {

  private final int boardDim;
  private boolean isMoveReceived;
  private GoGame game;
  private GoMove serverMove;
  private  Player myPlayer;
  private Player otherPlayer;
  private final GameClient client;
  private boolean isGameover;
  private String gameEndingMessage;

  public ClientGameAdapter(String player1Name, String player2Name, int boardDim,
      GameClient client)
      throws PlayerNotFoundException {
    this.client = client;
    this.boardDim = boardDim;
    buildGame(player1Name, player2Name);
  }

  private void buildGame(String player1Name, String player2Name)
      throws PlayerNotFoundException {
    if (client.getUsername().equals(player1Name)) {
      createMyPlayer(player1Name, Stone.BLACK);
      otherPlayer = new OnlinePlayer(player2Name, Stone.WHITE);
      game = new GoGame(myPlayer, otherPlayer, boardDim);
    } else if (client.getUsername().equals(player2Name)) {
      createMyPlayer(player2Name, Stone.WHITE);
      otherPlayer = new OnlinePlayer(player1Name, Stone.BLACK);
      game = new GoGame(otherPlayer, myPlayer, boardDim);
    } else {
      throw new PlayerNotFoundException("Neither "
          + player1Name + " nor " + player2Name
          + " is associated with this client");
    }
  }

  private void createMyPlayer(String name, Stone stone) {
    switch (client.getPlayerType()) {
      case "-H" -> myPlayer = new HumanPlayer(name, stone);
      case "-N" -> myPlayer = new ComputerPlayer(name,
          new NaiveStrategy(), stone);
      default -> new HumanPlayer(name, stone);
    }
  }

  public synchronized void playMove()
      throws GameMismatchException, QuitGameException, InvalidMoveException {
    GoMove myMove = null;
    if (isMyMove()) {
      myMove = myMove();
      client.sendMove(myMove);
    }
    while (!isMoveReceived && !game.isGameover()) {
      try {
        this.wait();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    if(isMyMove() && !myMove.equals(serverMove)){
      throw new GameMismatchException("Server move not matching client's move");
    }
    doMove();
  }

  private void doMove() throws InvalidMoveException {
    game.doMove(serverMove);
    isMoveReceived = false;
  }

  private boolean isMyMove() {
    return game.getTurn().equals(myPlayer) && !game.isGameover();
  }

  private GoMove myMove() throws QuitGameException {
    GoMove myMove;

    myMove = (GoMove) ((AbstractPlayer) myPlayer).determineMove(game);

    return myMove;
  }

  public synchronized void receiveMove(int moveIndex, String moveColor){
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
    Stone stone = moveColor.equals(Protocol.BLACK)? Stone.BLACK: Stone.WHITE;
    return myPlayer.getStone().equals(stone)?
        myPlayer: otherPlayer;
  }

  private Player getPlayerFromName(String name) {
    return ((AbstractPlayer) myPlayer).getName().equals(name)?
        myPlayer: otherPlayer;
  }

  public synchronized void receiveDraw() throws GameMismatchException {
    if (game.isGameover() && game.getWinner() == null){
      isGameover = true;
      gameEndingMessage = "It is a draw";
    } else if(isGameover && game.getWinner() != null) {
      throw new GameMismatchException("Server decides DRAW "
          + "while client decides WINNER: " + game.getWinner());
    } else{
      throw new GameMismatchException("Game ended for server and not "
          + "for client");
    }
    notifyAll();
  }

  public synchronized void receiveWinner(String winner) throws GameMismatchException {
    if (game.isGameover()
        && game.getWinner().equals(getPlayerFromName(winner))){
      gameEndingMessage = game.getWinner().equals(myPlayer)
          ? "You win!": "You lose!";
      isGameover = true;
    } else if (game.isGameover()
        && !game.getWinner().equals(getPlayerFromName(winner))) {
      throw new GameMismatchException("Server decides WINNER: "
          + game.getWinner() + "while client decides WINNER: "
          + game.getWinner());
    } else if (!game.isGameover() && myPlayer
        .equals(getPlayerFromName(winner))) {
      gameEndingMessage = "You win, opponent forfeited!";
    } else{
      throw new GameMismatchException("Game ended for server and not "
          + "for client");
    }
    notifyAll();
  }

  public boolean isGameOver() {
    return isGameover;
  }

  public String displayState() {
    return game.toString();
  }

  public synchronized String getGameEndMessage() {
    while(gameEndingMessage == null){
      try {
        this.wait();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    return gameEndingMessage;
  }
}
