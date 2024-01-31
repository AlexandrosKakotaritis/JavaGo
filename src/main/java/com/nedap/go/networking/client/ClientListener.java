package com.nedap.go.networking.client;

import java.util.List;

/**
 * Interface for Listener design pattern on ChatClientTUI.
 */
public interface ClientListener {

  /**
   * Notify listeners of disconnect.
   */
  void connectionLost();


  /**
   * Receive the playerList.
   *
   * @param playerList The list of players.
   */
  void receiveList(List<String> playerList);

  /**
   * Receive confirmation of entering matchmaking queue.
   */
  void receiveInQueue();

  /**
   * Starts new game.
   *
   * @param player1Name The name of the first player with black
   * @param player2Name The name of the second player with white.
   * @param boardDim    The dimension of the board.
   */
  void newGame(String player1Name, String player2Name, int boardDim);

  /**
   * Prints error messages.
   *
   * @param message The error message.
   */
  void printError(String message);

  /**
   * Receiving moves from the server.
   *
   * @param moveIndex The index of the move.
   * @param moveColor The color of the stone.
   */
  void receiveMove(int moveIndex, String moveColor);

  /**
   * Receive a pass.
   *
   * @param color The color of the player passing
   */
  void receivePass(String color);

  /**
   * Receive the game over with result draw.
   */
  void receiveDraw() throws GameMismatchException;

  /**
   * Receive the game over with result winner.
   *
   * @param winner The name of the winner.
   */
  void receiveWinner(String winner) throws GameMismatchException;
}
