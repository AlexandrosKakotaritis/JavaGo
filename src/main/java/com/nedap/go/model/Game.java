package com.nedap.go.model;

import java.util.List;

/**
 * A simple turn-based game.
 */
public interface Game {

    /**
     * Check if the game is over, i.e., there is a winner or no more moves are available.
     * @return whether the game is over
     */
    boolean isGameover();

    /**
     * Query whose turn it is
     * @return the player whose turn it is
     */
    Player getTurn();

    /**
     * Get the winner of the game. If the game is a draw, then this method returns null.
     * @return the winner, or null if no player is the winner or the game is not over
     */
    Player getWinner();

    /**
     * Return all moves that are valid in the current state of the game
     * @return the list of currently valid moves
     */
    List<? extends Move> getValidMoves();

    /**
     * Check if a move is a valid move
     * @param move the move to check
     * @return true if the move is a valid move
     */
    boolean isValidMove(Move move);

    /**
     * Perform the move, assuming it is a valid move.
     * @param move the move to play
     */
    void doMove(Move move);

    Game deepCopy();
}
