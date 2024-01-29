package com.nedap.go.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BoardTest {

  private Board board;

  private static boolean containsArray(List<List<Integer>> listOfLists, Integer[] arrayToCheck) {
    for (List<Integer> list : listOfLists) {
      if (list.containsAll(Arrays.asList(arrayToCheck))) {
        return true;
      }
    }
    return false;
  }

  @BeforeEach
  public void setUp() {
    board = new Board();
  }

  @Test
  public void testIsField() {
    assertTrue(board.isField(board.getDim() / 2));
    assertFalse(board.isField(board.getDim() * board.getDim() + 1));

    assertTrue(board.isField(1, 1));
    assertFalse(board.isField(1, board.getDim() + 2));
    assertFalse(board.isField(-1, board.getDim() - 1));
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
  public void testSetFieldXY() {
    for (int i = 0; i < board.getDim(); i++) {
      for (int j = 0; j < board.getDim(); j++) {
        board.setField(i, j, Stone.BLACK);
        assertFalse(board.isEmpty(i, j));
        assertEquals(Stone.BLACK, board.getField(i, j));

      }

    }

    board.setField(0, board.getDim() - 1, Stone.WHITE);
    assertEquals(Stone.BLACK, board.getField(0, board.getDim() - 1));

    board.setField(2, board.getDim() - 1, Stone.EMPTY);
    assertEquals(Stone.BLACK, board.getField(2, board.getDim() - 1));
  }

  @Test
  public void testGetStoneChains() {
    Integer[] black = new Integer[]{7, 16, 25, 34, 43, 52, 61, 70, 79};
    Integer[] white = new Integer[]{20, 21, 22, 23, 29, 32, 38, 41, 47, 48, 49, 50};

    for (Integer integer : black) {
      board.setField(integer, Stone.BLACK);
    }
    for (Integer integer : white) {
      board.setField(integer, Stone.WHITE);
    }

    board.setField(80, Stone.WHITE);
    List<List<Integer>> listOfBlackChains = board.getStoneChains(Stone.BLACK);
    List<List<Integer>> listOfWhiteChains = board.getStoneChains(Stone.WHITE);

    assertTrue(containsArray(listOfBlackChains, black));
    assertTrue(containsArray(listOfWhiteChains, white));
    assertTrue(containsArray(listOfWhiteChains, new Integer[]{80}));
  }

  @Test
  public void testScoreSimple(){
    board.setField(0, Stone.BLACK);
    board.setField(1,Stone.WHITE);
    System.out.println(board);
    assertEquals(1, board.getScore(Stone.BLACK));
    assertEquals(1, board.getScore(Stone.WHITE));
  }
  @Test
  public void testScore() {
    board.setField(10, Stone.BLACK);
    System.out.println(board);
    assertEquals(81, board.getScore(Stone.BLACK));
    board.reset();

    int[] blackSquare = new int[]{11, 12, 13, 19, 23, 28, 32, 37, 41, 47, 48, 49};
    for (int index : blackSquare) {
      board.setField(index, Stone.BLACK);
    }
    board.setField(25, Stone.WHITE);
    System.out.println(board);
    assertEquals(21, board.getScore(Stone.BLACK));
    board.reset();

    int[] black = new int[]{21, 22, 23, 29, 33, 37, 38, 42, 46, 51, 56, 57, 58, 59};
    int[] white = new int[]{12, 13, 14, 28, 66, 67, 68, 7, 16, 25, 34, 43, 52, 61, 70, 79};

    for (int index : black) {
      board.setField(index, Stone.BLACK);
    }
    for (int index : white) {
      board.setField(index, Stone.WHITE);
    }

    System.out.println(board);
    assertEquals(24, board.getScore(Stone.BLACK));
    assertEquals(25, board.getScore(Stone.WHITE));
  }

  @Test
  public void testCapture() {
    int[] black = new int[]{21, 22, 23, 29, 33, 39, 43, 49, 53, 58, 61, 68, 69};
    int[] white = new int[]{30, 31, 32, 40, 41, 42, 50, 51, 52, 59, 60};

    for (int index : black) {
      board.setField(index, Stone.BLACK);
    }
    for (int index : white) {
      board.setField(index, Stone.WHITE);
    }
    System.out.println(board);
    board.calculateCaptures(Stone.BLACK);
    board.calculateCaptures(Stone.WHITE);
    System.out.println(board);
    assertEquals(81, board.getScore(Stone.BLACK));
  }

  @Test
  public void testEdgeCapture() {
    int[] black = new int[]{1, 10, 19, 27, 28};
    int[] white = new int[]{0, 9, 18};

    for (int index : black) {
      board.setField(index, Stone.BLACK);
    }
    for (int index : white) {
      board.setField(index, Stone.WHITE);
    }
    String boardBeforeCapture = board.toString();
    board.calculateCaptures(Stone.BLACK);
    board.calculateCaptures(Stone.WHITE);
    assertNotEquals(boardBeforeCapture, board.toString());
    assertEquals(81, board.getScore(Stone.BLACK));

  }

  @Test
  public void testDeepCopy() {
    int[] black = new int[]{21, 22, 23, 29, 33, 37, 38, 42, 46, 51, 56, 57, 58, 59};
    int[] white = new int[]{12, 13, 14, 28, 66, 67, 68, 7, 16, 25, 34, 43, 52, 61, 70, 79};
    for (int index : black) {
      board.setField(index, Stone.BLACK);
    }
    for (int index : white) {
      board.setField(index, Stone.WHITE);
    }
    Board copiedBoard = board.deepCopy();

    assertEquals(board.toString(), copiedBoard.toString());
    assertEquals(board.getScore(Stone.BLACK), copiedBoard.getScore(Stone.BLACK));
    assertEquals(board.getScore(Stone.WHITE), copiedBoard.getScore(Stone.WHITE));

    board.setField(0, Stone.BLACK);

    assertNotEquals(board.toString(), copiedBoard.toString());
    assertNotEquals(board.getScore(Stone.BLACK), copiedBoard.getScore(Stone.BLACK));
    assertEquals(board.getScore(Stone.WHITE), copiedBoard.getScore(Stone.WHITE));
  }
}
