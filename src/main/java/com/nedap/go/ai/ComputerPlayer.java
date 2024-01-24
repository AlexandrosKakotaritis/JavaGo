package com.nedap.go.ai;


import com.nedap.go.model.AbstractPlayer;
import com.nedap.go.model.Game;
import com.nedap.go.model.Move;
import com.nedap.go.model.Stone;

public class ComputerPlayer extends AbstractPlayer {

    Stone stone;

    Strategy strategy;

    public ComputerPlayer(Strategy strategy, Stone stone){
        super(strategy.getName() + "-" + stone);
        this.strategy = strategy;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy){
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

    public Stone getStone() {
        return stone;
    }
}
