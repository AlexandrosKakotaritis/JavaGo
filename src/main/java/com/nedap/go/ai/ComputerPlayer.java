package com.nedap.go.ai;


import com.nedap.go.model.AbstractPlayer;
import com.nedap.go.model.Game;
import com.nedap.go.model.Move;
import com.nedap.go.model.Stone;

/**
 * Class for AI players.
 */
public class ComputerPlayer extends AbstractPlayer {

  Stone stone;

  Strategy strategy;

  /**
   * Constructs the ComputerPlayer with the desired stone and strategy.
   *
   * @param strategy The desired strategy.
   * @param stone    The desired stone.
   */
  public ComputerPlayer(Strategy strategy, Stone stone) {
    super(strategy.getName() + "-" + stone);
    this.strategy = strategy;
    this.stone = stone;
  }

  /**
   * Getter for the strategy.
   *
   * @return The strategy of the computer player.
   */
  public Strategy getStrategy() {
    return strategy;
  }

  /**
   * Setter for the strategy.
   *
   * @param strategy The strategy to use.
   */
  public void setStrategy(Strategy strategy) {
    this.strategy = strategy;
  }

  /**
   * Determines the next move, if the game still has available moves.
   *
   * @param game the current game
   * @return the player's choice
   */
  @Override
  public Move determineMove(Game game) {
    return strategy.determineMove(game);

  }

  /**
   * Return the stone of the player.
   *
   * @return The color of the stone.
   */
  public Stone getStone() {
    return stone;
  }
}
