package com.nedap.go.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class BoardTest {
  private Board board;
  @BeforeEach
  public void setUp(){
    board = new Board();
  }

  @Test
  public void testIsField() {
    assertTrue(board.isField(board.getDim()/2));
    assertFalse(board.isField(board.getDim() * board.getDim() + 1));

    assertTrue(board.isField(1,1));
    assertFalse(board.isField(1, board.getDim() + 2));
    assertFalse(board.isField(-1, board.getDim()-1));
  }

  @Test
  public void testSetField() {
    for (int i = 0; i < board.getDim() * board.getDim(); i++) {
      board.setField(i, Stone.BLACK);
      assertFalse(board.isEmpty(i));
      assertEquals(Stone.BLACK, board.getField(i));
    }

    board.setField(board.getDim(), Stone.WHITE);
    assertEquals(board.getField(board.getDim()), Stone.BLACK);

    board.setField(board.getDim() + 1, Stone.EMPTY);
    assertEquals(Stone.BLACK, board.getField(board.getDim() + 1));
  }

  @Test
  public void testScore() {
    int[] black = new int[]{21, 22, 23, 29, 33, 37, 38, 42, 46, 51, 56, 57, 58, 59};
    int[] white = new int[]{12, 13, 14, 28, 66, 67, 68, 7, 16, 25, 34, 43, 52, 61, 70, 79};

    for (int i = 0; i < black.length; i++) {
      board.setField(black[i], Stone.BLACK);
    }
    for (int i = 0; i < white.length; i++) {
      board.setField(white[i], Stone.WHITE);
    }

    System.out.println(board);
    assertEquals(24, board.getScore(Stone.BLACK));
    assertEquals(25, board.getScore(Stone.WHITE));
  }

  @Test
  public void testCapture(){
    int[] black = new int[]{21, 22, 23, 29, 33, 39, 43, 49, 53, 58, 61, 68, 69};
    int[] white = new int[]{30, 31, 32, 40, 41, 42, 50, 51, 59, 60};

    for (int i = 0; i < black.length; i++) {
      board.setField(black[i], Stone.BLACK);
    }
    for (int i = 0; i < white.length; i++) {
      board.setField(white[i], Stone.WHITE);
    }
    System.out.println(board);
    board.calculateCaptures();
    assertEquals(81, board.getScore(Stone.BLACK));
  }
}
