package com.nedap.go.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameTest {

  private GoGame game;
  private GoPlayer player1, player2;
  private Board board;
  @BeforeEach
  public void setUp(){
    player1 = () -> Stone.BLACK;
    player2 = () -> Stone.WHITE;
    board = new Board();
    game = new GoGame(player1, player2, board, true,
        new HashMap<>());
  }
  @Test
  public void testGetTurn() throws InvalidMoveException {
    assertEquals(player1, game.getTurn());
    game.doMove(new GoMove(player1, 10));
    assertEquals(player2, game.getTurn());
  }
  @Test
  public void testIsValidMove() throws InvalidMoveException {
    assertTrue(game.isValidMove(new GoMove(player1, 60)));
    assertTrue(game.isValidMove(new GoMove(player2)));
    assertFalse(game.isValidMove(new GoMove(player1, -1)));
    assertFalse(game.isValidMove(new GoMove(player2, 10)));
  }
  @Test
  public void testIsGameOver() throws InvalidMoveException {
    game.doMove(new GoMove(player1, 0));
    assertFalse(game.isGameover());
    game.doMove(new GoMove(player2));
    game.doMove(new GoMove(player1));
    assertTrue(game.isGameover());
  }
  @Test
  public void testGetWinner() throws InvalidMoveException {
    int[] black = new int[]{21, 22, 23, 29, 33, 37, 38, 42, 46, 51, 56, 57, 58, 59};
    int[] white = new int[]{12, 13, 14, 28, 66, 67, 68, 7, 16, 25, 34, 43, 52, 61, 70, 79};

    for (int i = 0; i < white.length; i++) {
      if(i < black.length){
        game.doMove(new GoMove(player1, black[i]));
      }else{game.doMove(new GoMove(player1));}

      game.doMove(new GoMove(player2, white[i]));
      assertNull(game.getWinner());
    }

    game.doMove(new GoMove(player1));
    game.doMove(new GoMove(player2));

    assertEquals(player2, game.getWinner());
  }
  @Test
  public void testTie() throws InvalidMoveException {
    int[] black = new int[]{7, 16, 25, 34, 43, 52, 61, 70, 79};
    int[] white = new int[]{1, 10, 19, 28, 37, 46, 55, 64, 73};
    for (int i = 0; i < black.length; i++) {
      game.doMove(new GoMove(player1, black[i]));
      game.doMove(new GoMove(player2, white[i]));
    }
    game.doMove(new GoMove(player1));
    game.doMove(new GoMove(player2));
    assertNull(game.getWinner());
  }

  @Test
  public void testGetValidMoves() throws InvalidMoveException {
    game.doMove(new GoMove(player1, 0));
    assertEquals(81, game.getValidMoves().size());
    game.doMove(new GoMove(player2));
    assertEquals(81, game.getValidMoves().size());
    game.doMove(new GoMove(player1, 1));
    assertEquals(80, game.getValidMoves().size());
  }

  @Test
  public void testDeepCopy() throws InvalidMoveException {
    game.doMove(new GoMove(player1, 0));
    GoGame gameCopy = game.deepCopy();
    assertEquals(game.getValidMoves().size(), gameCopy.getValidMoves().size());

    game.doMove(new GoMove(player2, 6));
    assertNotEquals(game.getValidMoves().size(), gameCopy.getValidMoves().size());
  }

  @Test
  public void testDoMove() throws InvalidMoveException {
    assertEquals(82, game.getValidMoves().size());
    game.doMove(new GoMove(player1, 0));
    System.out.println(game);
    assertEquals(81, game.getValidMoves().size());
    assertThrows(InvalidMoveException.class, () -> game.doMove(new GoMove(player2, 0)));

  }

  @Test
  public void testCapturingMove() throws InvalidMoveException{
    Board board = new Board();
    int[] black = new int[]{21, 22, 23, 29, 33, 39, 43, 49, 53, 58, 61, 68, 69};
    int[] white = new int[]{30, 31, 32, 40, 41, 42, 50, 51, 52, 59, 60};
    for (int i = 0; i < black.length - 2; i++) {
      board.setField(black[i], Stone.BLACK);
      board.setField(white[i], Stone.WHITE);
    }
    GoGame newGame = new GoGame(player1, player2, board, true,
        new HashMap<>());
    newGame.doMove(new GoMove(player1, 68));
    newGame.doMove(new GoMove(player2));
    System.out.println(newGame);
    assertEquals(12, board.getScore(Stone.BLACK));
    assertEquals(11, board.getScore(Stone.WHITE));
    newGame.doMove(new GoMove(player1, 69));
    System.out.println(newGame);
    assertEquals(81, board.getScore(Stone.BLACK));
    assertEquals(0, board.getScore(Stone.WHITE));
  }
  @Test
  public void testSuicide() throws InvalidMoveException {
    Board newBoard = new Board();
    int[] black = new int[]{21, 22, 23, 29, 33, 39, 43, 49, 53, 58, 61, 68, 69};
    int[] white = new int[]{0, 30, 31, 32, 40, 41, 42, 50, 51, 52, 59, 60};
    for (int i = 0; i < black.length; i++) {
      newBoard.setField(black[i], Stone.BLACK);
      if (i < white.length - 1) {
        newBoard.setField(white[i], Stone.WHITE);
      }
    }
    GoGame newGame = new GoGame(player1, player2, newBoard, false,
        new HashMap<>());
    System.out.println(newGame);
    assertEquals(13, newBoard.getScore(Stone.BLACK));
    assertEquals(11, newBoard.getScore(Stone.WHITE));
    newGame.doMove(new GoMove(player2, 60));
    System.out.println(newGame);
    assertEquals(24, newBoard.getScore(Stone.BLACK));
    assertEquals(1, newBoard.getScore(Stone.WHITE));
  }

  @Test
  public void testSuicideCapture() throws InvalidMoveException {
    Board newBoard = new Board();
    int[] black = new int[]{29, 37, 47};
    int[] white = new int[]{30, 38, 40, 48};
    for (int i = 0; i < black.length; i++) {
      newBoard.setField(black[i], Stone.BLACK);
      newBoard.setField(white[i], Stone.WHITE);
    }
    newBoard.setField(48, Stone.WHITE);
    GoGame newGame = new GoGame(player1, player2, newBoard, true,
        new HashMap<>());
    System.out.println(newGame);
    assertEquals(3, newBoard.getScore(Stone.BLACK));
    assertEquals(5, newBoard.getScore(Stone.WHITE));
    newGame.doMove(new GoMove(player1, 39));
    System.out.println(newGame);
    assertEquals(5, newBoard.getScore(Stone.BLACK));
    assertEquals(3, newBoard.getScore(Stone.WHITE));
  }

  @Test
  public void testKoRule() throws InvalidMoveException {
    int[] black = new int[]{29, 37, 47, 39};
    int[] white = new int[]{30, 40, 48, 38};
    for (int i = 0; i < black.length; i++) {
      game.doMove(new GoMove(player1, black[i]));
      game.doMove(new GoMove(player2, white[i]));
    }
    System.out.println(game);
    GoMove move = new GoMove(player1, 39);
    assertFalse(game.isValidMove(move));
  }
}
