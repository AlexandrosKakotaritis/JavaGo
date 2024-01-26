package com.nedap.go.model;

import com.nedap.go.model.Move;
import com.nedap.go.model.Player;

public class GoMoveRowColumn implements Move {

  private final Player player;
  private final boolean pass;
  private final int row;
  private final int column;

  /**
   * When a normal move is made.
   *
   * @param player The player that makes the move
   * @param row  The row index of the move to be made.
   * @param column The column index of the move to be made.
   */
  public GoMoveRowColumn(Player player, int row, int column) {
    this.player = player;
    this.pass = false;
    this.row = row;
    this.column = column;
  }

  /**
   * When a passing move is played.
   *
   * @param player The player making the passing move.
   */
  public GoMoveRowColumn(Player player) {
    this.player = player;
    this.pass = true;
    this.row = -1;
    this.column = -1;
  }

  /**
   * A query returning the player that made the move.
   *
   * @return The player that made the move.
   */
  public Player getPlayer() {
    return player;
  }

  /**
   * A query returning whether the player made a passing move.
   *
   * @return True if the move is a passing move.
   */
  public boolean getPass() {
    return pass;
  }

  /**
   * A query returning the row index of the move.
   *
   * @return The row index of the move to be made.
   */
  public int getRow() {
    return row;
  }

  /**
   * A query returning the column index of the move.
   *
   * @return The column index of the move to be made.
   */
  public int getColumn() {
    return column;
  }
}


