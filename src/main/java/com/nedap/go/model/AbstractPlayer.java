package com.nedap.go.model;

import com.nedap.go.tui.QuitGameException;

/**
 * A player of a game.
 */
public abstract class AbstractPlayer implements Player {

  private final String name;

  /**
   * Creates a new Player object.
   */
  public AbstractPlayer(String name) {
    this.name = name;
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
   * Determines the next move, if the game still has available moves.
   *
   * @param game the current game
   * @return the player's choice
   */
  public abstract Move determineMove(Game game) throws QuitGameException;

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
