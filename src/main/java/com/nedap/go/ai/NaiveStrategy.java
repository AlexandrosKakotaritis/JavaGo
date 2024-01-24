package com.nedap.go.ai;

import com.nedap.go.model.Game;
import com.nedap.go.model.GoGame;
import com.nedap.go.model.GoMove;
import com.nedap.go.model.Move;

public class NaiveStrategy implements Strategy {

    private static final String NAME = "Naive";
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
        int moveIndex = (int) (Math.random()*game.getValidMoves().size());
        return game.getValidMoves().get(moveIndex);
    }
}
