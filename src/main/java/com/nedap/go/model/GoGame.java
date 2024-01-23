package com.nedap.go.model;

import java.util.ArrayList;
import java.util.List;

public class GoGame implements Game{

  private boolean isPlayer1Turn;
  private GoPlayer player1, player2;
  private Board board;
  private GoMove player1LastMove, player2LastMove;

  public GoGame(GoPlayer player1, GoPlayer player2) {
    this.player1 = player1;
    this.player2 = player2;
    isPlayer1Turn = true;
  }

  /**
   * Check if the game is over, i.e., there is a winner or no more moves are available.
   *
   * @return whether the game is over
   */
  @Override
  public boolean isGameover() {
    return player1LastMove.getPass() && player2LastMove.getPass();
  }

  /**
   * Query whose turn it is
   *
   * @return the player whose turn it is
   */
  @Override
  public GoPlayer getTurn() {
    return isPlayer1Turn? player1: player2;
  }

  /**
   * Get the winner of the game. If the game is a draw, then this method returns null.
   *
   * @return the winner, or null if no player is the winner or the game is not over
   */
  @Override
  public GoPlayer getWinner() {
    if(isGameover()) {
      if (board.getScore(player1.getStone())
          > board.getScore(player2.getStone())) {
        return player1;
      } else if (board.getScore(player1.getStone())
          < board.getScore(player2.getStone())) {
        return player2;
      }
    }
    return null;
  }

  /**
   * Return all moves that are valid in the current state of the game
   *
   * @return the list of currently valid moves
   */
  @Override
  public List<? extends Move> getValidMoves() {
    List<GoMove> validMoves = new ArrayList<>();
    for (int i = 0; i < board.getDim() * board.getDim(); i++) {

      GoMove move = new GoMove(this.getTurn(), i);
      if(isValidMove(move)){
        validMoves.add(move);
      }
    }
    validMoves.add(new GoMove(this.getTurn()));
    return validMoves;
  }

  /**
   * Check if a move is a valid move
   *
   * @param move the move to check
   * @return true if the move is a valid move
   */
  @Override
  public boolean isValidMove(GoMove move) {
    return board.isField(move.getIndex()) && board.isEmpty(move.getIndex())
        && move.getPlayer() == this.getTurn() && this.checkKoRule();
  }

  private boolean checkKoRule() {
    return false;
  }

  /**
   * Perform the move, assuming it is a valid move.
   *
   * @param move the move to play
   */
  @Override
  public void doMove(GoMove move) throws InvalidMoveException {
    if(isValidMove(move)){
      if(!move.getPass()){
        board.setField(move.getIndex(), move.getPlayer().getStone());
      }
    }else{
      throw new InvalidMoveException();
    }
  }

  @Override
  public GoGame deepCopy() {
    return null;
  }
}
