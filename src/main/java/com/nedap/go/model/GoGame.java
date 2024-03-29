package com.nedap.go.model;

import com.nedap.go.model.utils.BoardList;
import com.nedap.go.model.utils.InvalidMoveException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The class containing the basic Go game logic.
 */
public class GoGame implements Game {

  private final Player player1;
  private final Player player2;
  private final Board board;
  private final List<GoMove> last2Moves;
  private final BoardList possibleKoBoards;
  private boolean isPlayer1Turn;

  /**
   * Constructor for creating a new game with a 9x9 board.
   *
   * @param player1 The player with the black stones.
   * @param player2 The player with the white stones.
   */
  public GoGame(Player player1, Player player2) {
    this(player1, player2, new Board(), true, new BoardList(), new LinkedList<>());
  }

  /**
   * Constructor for creating a new game with a chosen dimensions board.
   *
   * @param player1 The player with the black stones.
   * @param player2 The player with the white stones.
   * @param dim     The dimensions of the board.
   */
  public GoGame(Player player1, Player player2, int dim) {
    this(player1, player2, new Board(dim), true, new BoardList(), new LinkedList<>());
  }

  /**
   * Constructor for creating a new game with a 9x9 board.
   *
   * @param player1 The player with the black stones.
   * @param player2 The player with the white stones.
   * @param board   The board of the new game object
   */
  public GoGame(Player player1, Player player2, Board board) {
    this(player1, player2, board, true, new BoardList(), new LinkedList<>());
  }

  /**
   * Constructor for creating a game in progress (Mainly used for copying).
   *
   * @param player1          The player with the black stones.
   * @param player2          The player with the white stones.
   * @param board            The board of the new game object
   * @param isPlayer1Turn    The boolean signifying the players turn.
   * @param last2Moves       A list of the last 2 moves.
   * @param possibleKoBoards A list of boards that is possible that they can be repeated in game.
   */
  public GoGame(Player player1, Player player2, Board board, boolean isPlayer1Turn,
      BoardList possibleKoBoards, List<GoMove> last2Moves) {
    this.player1 = player1;
    this.player2 = player2;
    this.board = board;
    this.isPlayer1Turn = isPlayer1Turn;
    this.possibleKoBoards = possibleKoBoards;
    this.last2Moves = last2Moves;
  }

  /**
   * Getter for board
   *
   * @return The board of the game.
   */
  public Board getBoard() {
    return board;
  }

  /**
   * Check if the game is over, i.e., there is a winner or no more moves are available.
   *
   * @return whether the game is over
   */
  @Override
  public boolean isGameover() {
    return last2Moves.size() == 2 && last2Moves.getLast().isPass() && last2Moves.get(
        last2Moves.size() - 2).isPass();
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
    if (move instanceof GoMoveRowColumn) {
      return isValidGoMove((GoMoveRowColumn) move);
    } else if (move instanceof GoMove) {
      return isValidGoMove((GoMove) move);
    } else {
      return false;
    }
  }

  private boolean isValidGoMove(GoMove move) {
    GoMove goMove = move;
    return goMove.isPass() || board.isField(goMove.getIndex()) && board.isEmpty(goMove.getIndex())
        && goMove.getPlayer() == this.getTurn() && isKoRuleOk(goMove);
  }

  private boolean isValidGoMove(GoMoveRowColumn move) {
    GoMove goMove;
    boolean isFieldOrPass;
    GoMoveRowColumn goMoveRowColumn = move;
    isFieldOrPass = board.isField(goMoveRowColumn.getRow(), goMoveRowColumn.getColumn())
        || goMoveRowColumn.getPass();
    goMove = new GoMove(goMoveRowColumn.getPlayer(),
        board.index(goMoveRowColumn.getRow(), goMoveRowColumn.getColumn()));
    return goMove.isPass() || isFieldOrPass && board.isEmpty(goMove.getIndex())
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
    if (isValidMove(move)) {
      GoMove goMove = moveConversion(move);
      if (!goMove.isPass()) {
        board.setField(goMove.getIndex(), goMove.getPlayer().getStone());
        checkCaptures(goMove.getPlayer().getStone());
        Board previousBoard = board.deepCopy();
        possibleKoBoards.add(previousBoard);
      }
      recordLastMove(goMove);
      isPlayer1Turn = !isPlayer1Turn;
    } else {
      throw new InvalidMoveException();
    }
  }

  private GoMove moveConversion(Move move) {
    if (move instanceof GoMoveRowColumn goMove) {
      return new GoMove(goMove.getPlayer(), board.index(goMove.getRow(), goMove.getColumn()));
    } else {
      return (GoMove) move;
    }
  }

  private boolean checkCaptures(Stone stone) {
    return board.calculateCaptures(stone.other()) || board.calculateCaptures(stone);
  }

  private void recordLastMove(GoMove move) {
    last2Moves.add(move);
    if (last2Moves.size() > 2) {
      last2Moves.removeFirst();
    }
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
    List<GoMove> last2MovesCopy = new LinkedList<>(last2Moves);
    return new GoGame(player1, player2, board.deepCopy(), isPlayer1Turn, possibleKoBoardsCopy,
        last2MovesCopy);
  }

  public int getScore(Stone stone){
    return board.getScore(stone);
  }

  public Move getLastMove(){
    if (last2Moves.size()>0)
      return last2Moves.getLast();
    else
      return null;
  }

  @Override
  public String toString() {
    return scoreBoard() + "\n" + board;
  }

  private String scoreBoard() {
    return player1 + " " + Stone.BLACK + ": " + board.getScore(Stone.BLACK) + " - "
        + board.getScore(Stone.WHITE) + " :" + Stone.WHITE + " " + player2;
  }
}
