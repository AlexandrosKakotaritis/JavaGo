package com.nedap.go.ai;

import com.nedap.go.model.Game;
import com.nedap.go.model.GoMove;
import com.nedap.go.model.Move;

public class PassStrategy implements Strategy{

  private static final String NAME = "Pass";

  /**
   * Get the name of the strategy used.
   *
   * @return The name of the strategy.
   */
  @Override
  public String getName() {
    return NAME;
  }

  /**
   * Determine the move based on the chosen strategy
   *
   * @param game the game in which the move should be determined
   * @return the move.
   */
  @Override
  public Move determineMove(Game game) {
    GoMove move = new GoMove(game.getTurn());
    return move;
  }
}
