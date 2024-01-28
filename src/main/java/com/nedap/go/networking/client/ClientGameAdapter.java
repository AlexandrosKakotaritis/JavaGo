package com.nedap.go.networking.client;

import com.nedap.go.ai.ComputerPlayer;
import com.nedap.go.ai.NaiveStrategy;
import com.nedap.go.model.AbstractPlayer;
import com.nedap.go.model.GoGame;
import com.nedap.go.model.GoMove;
import com.nedap.go.model.Player;
import com.nedap.go.model.Stone;
import com.nedap.go.networking.server.OnlinePlayer;
import com.nedap.go.networking.server.utils.PlayerNotFoundException;
import com.nedap.go.tui.HumanPlayer;
import com.nedap.go.tui.QuitGameException;

public class ClientGameAdapter {

  private final int boardDim;
  private boolean isMoveReceived;
  private GoGame game;
  private GoMove moveReceived;
  private Player myPlayer;
  private Player otherPlayer;
  private GameClient client;

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
      game = new GoGame(myPlayer, otherPlayer, boardDim);
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

  public void play() {
    GoMove myMove;
    if (isMyMove()) {
      myMove = myMove();
      client.sendMove(myMove);
    }
    while (!isMoveReceived) {
      try {
        this.wait();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private boolean isMyMove() {
    return game.getTurn().equals(myPlayer);
  }

  private GoMove myMove() {
    GoMove myMove = null;
    try {
      myMove = (GoMove) ((AbstractPlayer) myPlayer).determineMove(game);
    } catch (QuitGameException e) {
      client.sendResign();
    }
    return myMove;
  }
}
