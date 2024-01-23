package com.nedap.go.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameTest {

  private GoGame game;
  private GoPlayer player1, player2;
  @BeforeEach
  public void setUp(){
    player1 = () -> Stone.BLACK;
    player2 = () -> Stone.WHITE;
    game = new GoGame(player1, player2);
  }
  @Test
  public void testGetTurn() throws InvalidMoveException {
    assertEquals(player1, game.getTurn());
    game.doMove(new GoMove(player1, 10));
    assertEquals(player2, game.getTurn());
  }
  @Test
  public void testIsValidMove(){
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
}
