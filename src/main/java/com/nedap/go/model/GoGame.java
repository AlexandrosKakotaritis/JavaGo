package com.nedap.go.model;

import com.nedap.go.model.utils.BoardList;
import com.nedap.go.model.utils.InvalidMoveException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * The class containing the basic Go game logic.
 */
public class GoGame implements Game {

  private final Player player1;
  private final Player player2;
  private final Board board;
  private final Stack<GoMove> lastMoves;
  private final BoardList possibleKoBoards;
  private boolean isPlayer1Turn;

  /**
   * Constructor for creating a new game.
   *
   * @param player1 The player with the black stones.
   * @param player2 The player with the white stones.
   */
  public GoGame(Player player1, Player player2) {
    this(player1, player2, new Board(), true, new BoardList());
  }

  /**
   * Constructor for creating a game in progress (Mainly used for copying).
   *
   * @param player1       The player with the black stones.
   * @param player2       The player with the white stones.
   * @param board         The board of the new game object
   * @param isPlayer1Turn The boolean signifying the players turn.
   */
  public GoGame(Player player1, Player player2, Board board, boolean isPlayer1Turn,
      BoardList possibleKoBoards) {
    this.player1 = player1;
    this.player2 = player2;
    this.board = board;
    this.isPlayer1Turn = isPlayer1Turn;
    this.possibleKoBoards = possibleKoBoards;
    lastMoves = new Stack<>();
  }

  /**
   * Check if the game is over, i.e., there is a winner or no more moves are available.
   *
   * @return whether the game is over
   */
  @Override
  public boolean isGameover() {
    return lastMoves.size() > 2 && lastMoves.pop().getPass() && lastMoves.pop().getPass();
  }

  /**
   * Query whose turn it is.
   *
   * @return the player whose turn it is
   */
  @Override
  public Player getTurn() {
    return isPlayer1Turn ? player1 : player2;
  }

  /**
   * Get the winner of the game. If the game is a draw, then this method returns null.
   *
   * @return the winner, or null if no player is the winner or the game is not over
   */
  @Override
  public Player getWinner() {
    if (isGameover()) {
      if (board.getScore(player1.getStone()) > board.getScore(player2.getStone())) {
        return player1;
      } else if (board.getScore(player1.getStone()) < board.getScore(player2.getStone())) {
        return player2;
      }
    }
    return null;
  }

  /**
   * Return all moves that are valid in the current state of the game.
   *
   * @return the list of currently valid moves
   */
  @Override
  public List<? extends Move> getValidMoves() {
    List<GoMove> validMoves = new ArrayList<>();
    for (int i = 0; i < board.getDim() * board.getDim(); i++) {

      GoMove move = new GoMove(this.getTurn(), i);
      if (isValidMove(move)) {
        validMoves.add(move);
      }
    }
    validMoves.add(new GoMove(this.getTurn()));
    return validMoves;
  }

  /**
   * Check if a move is a valid move.
   *
   * @param move the move to check
   * @return true if the move is a valid move
   */
  @Override
  public boolean isValidMove(Move move) {
    GoMove goMove = (GoMove) move;
    return goMove.getPass() || board.isField(goMove.getIndex()) && board.isEmpty(goMove.getIndex())
        && goMove.getPlayer() == this.getTurn() && isKoRuleOk(goMove);
  }

  private boolean isKoRuleOk(GoMove move) {
    Board newBoard = board.deepCopy();
    newBoard.setField(move.getIndex(), move.getPlayer().getStone());
    newBoard.calculateCaptures(move.getPlayer().getStone().other());
    newBoard.calculateCaptures(move.getPlayer().getStone());
    return !possibleKoBoards.matches(newBoard);
  }

  /**
   * Perform the move, assuming it is a valid move.
   *
   * @param move the move to play
   * @throws InvalidMoveException when an attempt on playing an invalid move is made.
   */
  @Override
  public void doMove(Move move) throws InvalidMoveException {
    GoMove goMove = (GoMove) move;
    if (isValidMove(move)) {
      if (!goMove.getPass()) {
        Board previousBoard = board.deepCopy();
        board.setField(goMove.getIndex(), goMove.getPlayer().getStone());
        if (checkCaptures(goMove.getPlayer().getStone())) {
          possibleKoBoards.add(previousBoard);
        }
      }
      recordLastMove(goMove);
      isPlayer1Turn = !isPlayer1Turn;
    } else {
      throw new InvalidMoveException();
    }
  }

  private boolean checkCaptures(Stone stone) {
    return board.calculateCaptures(stone.other()) || board.calculateCaptures(stone);
  }

  private void recordLastMove(GoMove move) {
    lastMoves.push(move);
  }

  /**
   * Create a copy of the game.
   *
   * @return The copy of the game.
   */
  @Override
  public GoGame deepCopy() {
    BoardList possibleKoBoardsCopy = new BoardList();
    for (Board koBoard : possibleKoBoards) {
      possibleKoBoardsCopy.add(koBoard.deepCopy());
    }
    return new GoGame(player1, player2, board.deepCopy(), isPlayer1Turn, possibleKoBoardsCopy);
  }

  @Override
  public String toString() {
    return scoreBoard() + "\n" + board + "\n" + getTurn() + " it is your turn!";
  }

  private String scoreBoard() {
    return player1 + " " + Stone.BLACK + ": " + board.getScore(Stone.BLACK) + " - "
        + board.getScore(Stone.WHITE) + " :" + Stone.WHITE + " " + player2;
  }
}
