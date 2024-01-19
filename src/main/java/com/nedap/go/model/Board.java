package com.nedap.go.model;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/**
 * A class representing a Go board with modifiable dimensions.
 */
public class Board {
  private static final String DELIM = "      ";
  private static final int DIM = 9;
  private Stone[] fields;

  /**
   * Constructs a new board with every intersection empty.
   */
  public Board() {
    fields = new Stone[DIM * DIM];
    reset();
  }

  public int getDim(){
    return DIM;
  }

  public int index(int row, int col) {
    return row * DIM + col;
  }

  /**
   * Checks if the given parameter is within the bounds defined by the size of the board
   * (DIM*DIM).

   * @param index The given integer representing the position of the intersection.
   * @return True if the given index is within the limits of the board.
   */
  public boolean isField(int index) {
    return false;
  }

  /**
   * Checks if the given parameter is within the bounds defined by the size of the board
   * (DIM*DIM).

   * @param row the row position of the stone.
   * @param col the column position of the stone.
   * @return True if the given index is within the limits of the board.
   */
  public boolean isField(int row, int col) {
    return false;
  }

  /**
   * Checks if the given position is occupied by a stone.

   * @param index the given position on the board.
   * @return True if the given position is not occupied by a stone of any color.
   */
  public boolean isEmpty(int index) {
    return false;
  }

  /**
   * Checks if the given position is occupied by a stone.

   * @param row the row position of the stone.
   * @param col the column position of the stone.
   * @return True if the given position is not occupied by a stone of any color.
   */
  public boolean isEmpty(int row, int col) {
    return false;
  }

  /**
   * Get the Stone placed on a certain intersection of the board.

   * @param index the position of the stone.
   * @return Either empty, black or white.
   */
  public Stone getField(int index) {
    return null;
  }

  /**
   * Get the Stone placed on a certain intersection of the board.

   * @param row the row position of the stone.
   * @param col the column position of the stone.
   * @return Either empty, black or white.
   */
  public Stone getField(int row, int col) {
    return null;
  }

  /**
   * Resets the board in a state where all the intersections are empty.
   */
  private void reset() {
    for (int i = 0; i < DIM * DIM; i++) {
      fields[i] = Stone.EMPTY;
    }
  }

  /**
   * Creates a copy of the current state of the board.

   * @return The copy of the board.
   */
  public Board deepCopy() {
    return null;
  }

  /**
   * Set the intersection specified by the index to the specified stone color.

   * @param index The index of the position of the stone.
   * @param stone The color of the stone set (black or white).
   */
  public void setField(int index, Stone stone) {
    fields[index] = stone;
  }
  /**
   * Set the intersection specified by the index to the specified stone color.

   * @param row the row position of the stone.
   * @param col the column position of the stone.
   * @param stone The color of the stone set (black or white).
   */
  public void setField(int row, int col, Stone stone) {

  }
  /**
   * Find the indices of the free squares of a stone.

   * @param index The position of the stone in the board.
   * @return A list containing the indices of the free intersections around the stone.
   */
  private List<Integer> getFreedom(int index) {
    return null;
  }

  private boolean hasFreedom(int index) {
    return false;
  }

  /**
   * Get all the stone strings of the board.

   * @return A list containing the sets of position for each stone.
   */
  public List<Set<Integer>> getStoneChains() {
    return null;
  }

  /**
   * Get the score for a specific stone.

   * @param stone The stone color to get score for
   * @return the number of stones put and territory surrounded
   */
  public int getScore(Stone stone) {
    return 0;
  }

  /**
   * Goes through the board calculates the captures and removes captured pieces.
   */
  public void calculateCaptures() {

  }

  private static String numberLine(int line) {
    StringBuilder numberLine = new StringBuilder();
    for (int i = 0; i < DIM-1; i++) {
      int number = line * DIM + i;
      if (number < 10) {
        numberLine.append(number).append("----");
      } else {
        numberLine.append(number).append("---");
      }
    }
    numberLine.append(line * DIM + DIM - 1);
    return numberLine.toString();
  }

  private static String boxLine() {
    StringBuilder boxLine = new StringBuilder();
    boxLine.append("|    ".repeat(DIM - 1));
    boxLine.append("|");
    return boxLine.toString();
  }

  private String intersectionLine(int line) {
    StringBuilder intersectionLine = new StringBuilder();
    for (int i = 0; i < DIM - 1; i++) {
      int index = line * DIM + i;
      intersectionLine.append(fields[index].toString()).append("----");
    }
    intersectionLine.append(fields[line * DIM + DIM - 1].toString());
    return intersectionLine.toString();
  }
  @Override
  public String toString(){
    StringBuilder boardString = new StringBuilder();
    for (int i = 0; i < DIM - 1; i++) {
      boardString.append(intersectionLine(i)).append(DELIM).append(numberLine(i)).append("\n");
      boardString.append(boxLine()).append(DELIM).append(boxLine()).append("\n");
    }
    int finalLine = DIM - 1;
    boardString.append(intersectionLine(finalLine)).append(DELIM)
        .append(numberLine(finalLine)).append("\n");
    return boardString.toString();
  }
}
