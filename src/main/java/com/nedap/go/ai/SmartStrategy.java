package com.nedap.go.ai;

import com.nedap.go.model.Game;
import com.nedap.go.model.GoGame;
import com.nedap.go.model.GoMove;
import com.nedap.go.model.Move;
import com.nedap.go.model.Stone;
import com.nedap.go.model.utils.InvalidMoveException;
import java.util.Iterator;
import java.util.List;

public class SmartStrategy implements Strategy{
  private static final String NAME = "Smart";
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
  public Move determineMove(Game game) {
    Move scoringMove;
    Move opponentNotScoring;
    GoMove lastMove = (GoMove) ((GoGame)game).getLastMove();
    if(lastMove != null && lastMove.isPass() && betterScore(game)){
      return new GoMove(game.getTurn());
    }
    if (game.getValidMoves().size() < 144) {
      try {
        scoringMove = findScoringMove(game);
        opponentNotScoring = findOpponentNotScoring(game);
      } catch (InvalidMoveException e) {
        throw new RuntimeException(e);
      }
      if (scoringMove != null)
        return scoringMove;
      else if (opponentNotScoring != null)
        return opponentNotScoring;
      else if (((GoGame) game).getScore(game.getTurn().getStone())
          > 0.5*((GoGame) game).getBoard().getDim() * ((GoGame) game).getBoard().getDim()){
        return new GoMove(game.getTurn());
      }

    }
    return randomMove((List<Move>) game.getValidMoves()) ;
  }

  private boolean betterScore(Game game){
    return ((GoGame) game).getScore(game.getTurn().getStone())
        > ((GoGame) game).getScore(game.getTurn().getStone().other());
  }

  /**
   * Find the move that does not give the opponent a scoring chance.
   * @param game The game being played.
   * @return The move that does not give a scoring chance. Null if the opponents scoring
   * is inevitable.
   */
  private Move findOpponentNotScoring(Game game) throws InvalidMoveException {
    List<Move> validMoves = (List<Move>) game.getValidMoves();
    Iterator<Move> iterator = validMoves.iterator();
    while(iterator.hasNext()){
      Move move = iterator.next();
      Game gameCopy = game.deepCopy();
      gameCopy.doMove(move);
      if(findScoringMove(gameCopy) != null) iterator.remove();
    }
    if (validMoves.isEmpty()) return null;
    return randomMove(validMoves);
  }

  /**
   * Find the move that scores a point
   * @param game The game object.
   * @return The move that scores or null if scoring is impossible in one move
   */
  private Move findScoringMove(Game game) throws InvalidMoveException {
    List<Move> validMoves = (List<Move>) game.getValidMoves();
    Iterator<Move> iterator = validMoves.iterator();
    Stone stone = game.getTurn().getStone();
    while(iterator.hasNext()) {
      int score = ((GoGame) game).getScore(stone);
      Move move = iterator.next();
      Game gameCopy = game.deepCopy();
      gameCopy.doMove(move);
      int newScore = ((GoGame) gameCopy).getScore(stone);
      if (score + 2 > newScore) iterator.remove();
    }
    if (validMoves.isEmpty()) return null;
    return randomMove(validMoves);
  }
  private static Move randomMove(List<Move> validMoves) {
    int index = (int) (Math.random() * validMoves.size());
    return validMoves.get(index);
  }
}
