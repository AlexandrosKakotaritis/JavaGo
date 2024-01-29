package com.nedap.go.model;

/**
 * A class that stores the information needed for a move.
 */
public class GoMove implements Move {

  private final Player player;
  private final boolean pass;
  private final int index;

  /**
   * When a normal move is made.
   *
   * @param player The player that makes the move
   * @param index  The index of the determined move
   */
  public GoMove(Player player, int index) {
    this.player = player;
    this.pass = false;
    this.index = index;
  }

  /**
   * When a passing move is played.
   *
   * @param player The player making the passing move.
   */
  public GoMove(Player player) {
    this.player = player;
    this.pass = true;
    this.index = -1;
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
  public boolean isPass() {
    return pass;
  }

  /**
   * A query returning the index of the move.
   *
   * @return The index of the move to be made.
   */
  public int getIndex() {
    return index;
  }

  /**
   * Two moves with equal Players and equal move
   * indices are considered equal.
   * @param o The object to compare with
   * @return True if equal
   */
  @Override
  public boolean equals(Object o){
    if(o instanceof GoMove move){
      return player.equals(move.getPlayer())
          && index == move.getIndex();
    }
    return false;
  }
}
