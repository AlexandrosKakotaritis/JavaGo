package com.nedap.go.gui;

import com.nedap.go.ai.ComputerPlayer;
import com.nedap.go.ai.NaiveStrategy;
import com.nedap.go.model.Board;
import com.nedap.go.model.GoGame;
import com.nedap.go.model.GoMove;
import com.nedap.go.model.Player;
import com.nedap.go.model.Stone;
import com.nedap.go.model.utils.InvalidMoveException;
import com.nedap.go.networking.client.ClientListener;
import com.nedap.go.networking.client.GameMismatchException;
import com.nedap.go.networking.protocol.Protocol;
import java.util.List;

/**
 * Class that listens to the server and plays the move in a 2d board in a gui.
 */
public class GoGuiListener implements ClientListener {

  private final GoGuiIntegrator goGui;
  private GoGame game;
  private Player player1;
  private Player player2;
  private int boardSize;

  private Board board;


  public GoGuiListener() {
    goGui = new GoGuiIntegrator(true, true, 9);
    goGui.startGUI();
  }

  public static void main(String[] args) {
    GoGuiListener goGui = new GoGuiListener();
    while (true) {

    }
  }

  /**
   * Notify listeners of disconnect.
   */
  @Override
  public void connectionLost() {
    goGui.stopGUI();
  }

  /**
   * Receive the playerList.
   *
   * @param playerList The list of players.
   */

  /**
   * Receive confirmation of entering matchmaking queue.
   */
  @Override
  public void receiveInQueue() {

  }

  /**
   * Starts new game.
   *
   * @param player1Name The name of the first player with black
   * @param player2Name The name of the second player with white.
   * @param boardDim    The dimension of the board.
   */
  @Override
  public void newGame(String player1Name, String player2Name, int boardDim) {
    player1 = () -> Stone.BLACK;
    player2 = () -> Stone.WHITE;
    this.board = new Board(boardDim);
    game = new GoGame(player1, player2, board);
    boardSize = boardDim;
    goGui.setBoardSize(boardDim);
  }

  /**
   * Prints error messages.
   *
   * @param message The error message.
   */
  @Override
  public void printError(String message) {

  }

  /**
   * Receiving moves from the server.
   *
   * @param moveIndex The index of the move.
   * @param moveColor The color of the stone.
   */
  @Override
  public void receiveMove(int moveIndex, String moveColor) {

    boolean isWhite = moveColor.equals(Protocol.WHITE);
    Player player = isWhite ? player2 : player1;
    try {
      game.doMove(new GoMove(player, moveIndex));
    } catch (InvalidMoveException e) {
      throw new RuntimeException(e);
    }

    giveHint(player);

    setGuiBoard(getAreaMarkers());

  }

  private void giveHint(Player player) {
    ComputerPlayer hinter = new ComputerPlayer("AI", new NaiveStrategy(), player.getStone());
    GoMove move = (GoMove) hinter.determineMove(game);
    if (!move.isPass()) {
      int[] hintCoordinates = indexToXy(move.getIndex());
      goGui.addHintIndicator(hintCoordinates[0], hintCoordinates[1]);
    }
  }

  private Stone[][] getAreaMarkers() {
    Stone[][] owners = new Stone[boardSize][boardSize];
    List<List<Integer>> emptyAreaChains = board.getStoneChains(Stone.EMPTY);
    for (List<Integer> chain : emptyAreaChains) {
      Stone owner = board.getOwner(chain);
      for (Integer index : chain) {
        int[] xy = indexToXy(index);
        owners[xy[0]][xy[1]] = owner;
      }
    }
    return owners;
  }

  /**
   * Receive a pass.
   *
   * @param color The color of the player passing
   */
  @Override
  public void receivePass(String color) {
    Player player = color.equals(Protocol.WHITE) ? player2 : player1;
    try {
      game.doMove(new GoMove(player));
    } catch (InvalidMoveException e) {
      throw new RuntimeException(e);
    }
  }

  private int[] indexToXy(int moveIndex) {
    return new int[]{moveIndex % boardSize, moveIndex / boardSize};
  }

  private void setGuiBoard(Stone[][] owners) {
    goGui.clearBoard();
    for (int x = 0; x < boardSize; x++) {
      for (int y = 0; y < boardSize; y++) {
        Stone stone = board.getField(y, x);
        switch (stone) {
          case BLACK -> goGui.addStone(x, y, false);
          case WHITE -> goGui.addStone(x, y, true);
          case EMPTY -> {
            goGui.removeStone(x, y);
            setArea(owners, x, y);
          }
        }
      }
    }
  }

  private void setArea(Stone[][] owners, int x, int y) {
    switch (owners[x][y]) {
      case BLACK -> goGui.addAreaIndicator(x, y, false);
      case WHITE -> goGui.addAreaIndicator(x, y, true);
    }
  }

  /**
   * Receive the game over with result draw.
   */
  @Override
  public void receiveDraw() throws GameMismatchException {

  }

  /**
   * Receive the game over with result winner.
   *
   * @param winner The name of the winner.
   */
  @Override
  public void receiveWinner(String winner) throws GameMismatchException {

  }

}
