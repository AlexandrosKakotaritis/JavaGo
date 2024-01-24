package com.nedap.go.ai;

import com.nedap.go.model.Game;
import com.nedap.go.model.GoGame;
import com.nedap.go.model.GoMove;
import com.nedap.go.model.Move;

/**
 * An interface to implement different strategies for the TicTacToe game.
 */
public interface Strategy {

    /**
     * Get the name of the strategy used.
     * @return The name of the strategy.
     */
    public String getName();

    /**
     * Determine the move based on the chosen strategy
     * @param game the game in which the move should be determined
     * @return the move.
     */
    public Move determineMove(Game game);

}
