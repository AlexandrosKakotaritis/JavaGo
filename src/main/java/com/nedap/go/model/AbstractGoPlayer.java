package com.nedap.go.model;

/**
 * A player of a game.
 */
public abstract class AbstractGoPlayer implements GoPlayer {

  private final String name;
  private final Stone stone;


  /**
   * Creates a new Player object.
   */
  public AbstractGoPlayer(String name, Stone stone) {
    this.name = name;
    this.stone = stone;
  }

  /**
   * Returns the name of the player.
   *
   * @return the name of the player
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the stone of the player.
   *
   * @return the stone of the player.
   */
  public Stone getStone() {
    return stone;
  }

  /**
   * Determines the next move, if the game still has available moves.
   *
   * @param game the current game
   * @return the player's choice
   */
  public abstract Move determineMove(Game game);

  /**
   * Returns a representation of a player, i.e., their name
   *
   * @return the String representation of this object
   */
  @Override
  public String toString() {
    return "Player " + name;
  }
}
