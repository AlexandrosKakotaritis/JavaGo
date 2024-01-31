package com.nedap.go.gui;

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


public class GoGuiListener implements ClientListener {

  private final GoGuiIntegrator gogui;
  private GoGame game;
  private Player player1;
  private Player player2;
  private int boardSize;

  private Board board;


  public GoGuiListener(){
    gogui = new GoGuiIntegrator(true, true, 9);
    gogui.startGUI();
  }
  /**
   * Confirms that log in was successful with the server.
   *
   * @param status   The status. True if successful
   * @param username The username used.
   */
  @Override
  public void logInStatus(boolean status, String username) {
    
  }

  /**
   * Notify listeners of disconnect.
   */
  @Override
  public void connectionLost() {
    gogui.stopGUI();
  }

  /**
   * Notify listeners of successful connection with the server and propagates server's message.
   *
   * @param message The server's hello message.
   */
  @Override
  public void successfulConnection(String message) {

  }

  /**
   * Receive the playerList.
   *
   * @param playerList The list of players.
   */
  @Override
  public void receiveList(List<String> playerList) {

  }

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
    gogui.setBoardSize(boardDim);
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
    Player player = isWhite? player2: player1;
    try {
      game.doMove(new GoMove(player, moveIndex));
    } catch (InvalidMoveException e) {
      throw new RuntimeException(e);
    }

//    ComputerPlayer hinter = new ComputerPlayer("AI",
//        new NaiveStrategy(), player.getStone());
//    GoMove move = (GoMove) hinter.determineMove(game);
//    if(!move.isPass()) {
//      int[] hintCoordinates = indexToXy(move.getIndex());
//      gogui.addHintIndicator(hintCoordinates[0], hintCoordinates[1]);
//    }

    setGuiBoard(getAreaMarkers());

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
    Player player = color.equals(Protocol.WHITE)? player2: player1;
    try {
      game.doMove(new GoMove(player));
    } catch (InvalidMoveException e) {
      throw new RuntimeException(e);
    }
    setGuiBoard(getAreaMarkers());
  }


  private int[] indexToXy(int moveIndex) {
    return new int[] {moveIndex % boardSize, moveIndex / boardSize};
  }

  private void setGuiBoard(Stone[][] owners) {
    gogui.clearBoard();
    for (int x = 0; x < boardSize; x++) {
      for (int y = 0; y < boardSize; y++) {
        Stone stone = board.getField(y, x);
        switch (stone) {
          case BLACK -> gogui.addStone(x, y, false);
          case WHITE -> gogui.addStone(x, y, true);
          case EMPTY -> {
            gogui.removeStone(x, y);
            gogui.removeStone(x, y);
            setArea(owners, x, y);
          }
        }
      }
    }
  }

  private void setArea(Stone[][] owners, int x, int y) {
    switch(owners[x][y]){
      case BLACK -> gogui.addAreaIndicator(x, y,false);
      case WHITE -> gogui.addAreaIndicator(x, y,true);
    }
  }


  /**
   * Receive the game over with result draw.
   */
  @Override
  public void receiveDraw() throws GameMismatchException {
//    gogui.clearBoard();
  }

  /**
   * Receive the game over with result winner.
   *
   * @param winner The name of the winner.
   */
  @Override
  public void receiveWinner(String winner) throws GameMismatchException {
//    gogui.clearBoard();
  }

  public static void main(String[] args) {
    GoGuiListener GoGui = new GoGuiListener();
    while(true){

    }
  }

}
