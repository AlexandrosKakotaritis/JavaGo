package com.nedap.go.model;

/**
 * A class that stores the information needed for a move.
 */
public class GoMove implements Move{
  private final GoPlayer player;
  private final boolean pass;
  private final int index;

  /**
   * When a normal move is made.
   * @param player The player that makes the move
   * @param index The index of the determined move
   */
  public GoMove(GoPlayer player, int index){
    this.player = player;
    this.pass = false;
    this.index = index;
  }

  /**
   * When a passing move is played.
   * @param player The player making the passing move.
   */
  public GoMove(GoPlayer player){
    this.player = player;
    this.pass = true;
    this.index = -1;
  }

  /**
   * A query returning the player that made the move.
   * @return The player that made the move.
   */
  public GoPlayer getPlayer(){return player;}
  /**
   * A query returning whether the player made a passing move.
   * @return True if the move is a passing move.
   */
  public boolean getPass(){return pass;}

  /**
   * A query returning the index of the move.
   * @return The index of the move to be made.
   */
  public int getIndex(){return index;}
}
