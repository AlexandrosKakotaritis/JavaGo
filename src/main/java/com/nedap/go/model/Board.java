package com.nedap.go.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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

  public Board(Stone[] fields) {
    this.fields = fields;
  }

  private static String numberLine(int line) {
    StringBuilder numberLine = new StringBuilder();
    for (int i = 0; i < DIM - 1; i++) {
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
    return "|    ".repeat(DIM - 1) + "|";
  }

  /**
   * Get the dimension of the board.
   *
   * @return The one side dimension as an integer.
   */
  public int getDim() {
    return DIM;
  }

  public int index(int row, int col) {
    return row * DIM + col;
  }

  public int[] rowCol(int index) {
    return new int[]{index / DIM, index % DIM};
  }

  /**
   * Checks if the given parameter is within the bounds defined by the size of the board (DIM*DIM).
   *
   * @param index The given integer representing the position of the intersection.
   * @return True if the given index is within the limits of the board.
   */
  public boolean isField(int index) {
    return 0 <= index && index < DIM * DIM;
  }

  /**
   * Checks if the given parameter is within the bounds defined by the size of the board (DIM*DIM).
   *
   * @param row the row position of the stone.
   * @param col the column position of the stone.
   * @return True if the given index is within the limits of the board.
   */
  public boolean isField(int row, int col) {
    return 0 <= row && row < DIM && 0 <= col && col < DIM;
  }

  /**
   * Checks if the given position is occupied by a stone.
   *
   * @param index the given position on the board.
   * @return True if the given position is not occupied by a stone of any color.
   */
  public boolean isEmpty(int index) {
    return fields[index] == Stone.EMPTY;
  }

  /**
   * Checks if the given position is occupied by a stone.
   *
   * @param row the row position of the stone.
   * @param col the column position of the stone.
   * @return True if the given position is not occupied by a stone of any color.
   */
  public boolean isEmpty(int row, int col) {
    return isField(row, col) && isEmpty(index(row, col));
  }

  /**
   * Get the Stone placed on a certain intersection of the board.
   *
   * @param index the position of the stone.
   * @return Either empty, black or white.
   */
  public Stone getField(int index) {
    if (isField(index)) {
      return fields[index];
    } else {
      return null;
    }
  }

  /**
   * Get the Stone placed on a certain intersection of the board.
   *
   * @param row the row position of the stone.
   * @param col the column position of the stone.
   * @return Either empty, black or white.
   */
  public Stone getField(int row, int col) {
    if (isField(row, col)) {
      return getField(index(row, col));
    } else {
      return null;
    }

  }

  /**
   * Resets the board in a state where all the intersections are empty.
   */
  void reset() {
    for (int i = 0; i < DIM * DIM; i++) {
      fields[i] = Stone.EMPTY;
    }
  }

  /**
   * Creates a copy of the current state of the board.
   *
   * @return The copy of the board.
   */
  public Board deepCopy() {
    Stone[] copiedFields = new Stone[DIM * DIM];
    for (int i = 0; i < DIM * DIM; i++) {
      copiedFields[i] = fields[i];
    }
    return new Board(copiedFields);
  }

  /**
   * Set the intersection specified by the index to the specified stone color.
   *
   * @param index The index of the position of the stone.
   * @param stone The color of the stone set (black or white).
   */
  public void setField(int index, Stone stone) {
    if (isField(index) && isEmpty(index)) {
      fields[index] = stone;
    }
  }

  /**
   * Set the intersection specified by the index to the specified stone color.
   *
   * @param row   the row position of the stone.
   * @param col   the column position of the stone.
   * @param stone The color of the stone set (black or white).
   */
  public void setField(int row, int col, Stone stone) {
    if (isField(row, col) && isEmpty(row, col)) {
      fields[index(row, col)] = stone;
    }
  }

  /**
   * Get all the stone strings of the board.
   *
   * @return A list containing the sets of position for each stone.
   */
  public List<List<Integer>> getStoneChains(Stone target) {
    Queue<Integer> notVisited = getStonePositions(target);
    List<List<Integer>> listOfChains = new ArrayList<>();
    while (!notVisited.isEmpty()) {
      int next = notVisited.poll();
      List<Integer> chain = FloodFill.BreadthWideSearch(next, fields);
      listOfChains.add(chain);
      notVisited.removeAll(chain);
    }
    return listOfChains;
  }

  private Queue<Integer> getStonePositions(Stone target) {
    Queue<Integer> queue = new LinkedList<>();
    for (int i = 0; i < fields.length; i++) {
      if (fields[i] == target) {
        queue.add(i);
      }
    }
    return queue;
  }

  /**
   * Get the score for a specific stone.
   *
   * @param target The stone color to get score for
   * @return the number of stones put and territory surrounded
   */
  public int getScore(Stone target) {
    int stones = 0;
    for (Stone field : fields) {
      stones = field == target ? stones + 1 : stones;
    }
    getAreaScoring(target);
    return stones + getAreaScoring(target);
  }

  private int getAreaScoring(Stone target) {
    int areaScore = 0;
    for (List<Integer> listOfEmpty : getStoneChains(Stone.EMPTY)) {
      if (getOwner(listOfEmpty) == target) {
        areaScore += listOfEmpty.size();
      }
    }
    return areaScore;
  }

  private Stone getOwner(List<Integer> chain) {
    List<Integer> borders = getAreaBorder(chain);
    Stone owner = fields[borders.getFirst()];
    if (borders.size() > 1) {
      for (Integer index : borders.subList(1, borders.size() - 1)) {
        if (fields[index] != owner) {
          owner = Stone.EMPTY;
          break;
        }
      }
    }
    return owner;
  }

  private List<Integer> getAreaBorder(List<Integer> chain) {
    List<Integer> borders = new ArrayList<>();
    for (Integer indexOfChain : chain) {
      List<Integer> neighbours = getNeighbours(indexOfChain);
      for (Integer indexOfNeighbours : neighbours) {
        if (fields[indexOfNeighbours] != fields[indexOfChain]) {
          borders.add(indexOfNeighbours);
        }
      }
    }
    return borders;
  }

  /**
   * Goes through the board calculates the captures and removes captured pieces.
   */
  public void calculateCaptures(Stone target) {
    List<List<Integer>> listOfChains = getStoneChains(target);
    for (List<Integer> chain : listOfChains) {
      if (getFreedoms(chain) == 0) {
        removeStones(chain);
      }
    }
  }

  private void removeStones(List<Integer> chain) {
    for (Integer index : chain) {
      fields[index] = Stone.EMPTY;
    }
  }

  private int getFreedoms(List<Integer> listOfTarget) {
    int freedoms = 0;
    for (Integer integer : listOfTarget) {
      List<Integer> neighbours = getNeighbours(integer);
      for (Integer index : neighbours) {
        if (fields[index] == Stone.EMPTY) {
          freedoms++;
        }
      }
    }
    return freedoms;
  }

  private List<Integer> getNeighbours(Integer integer) {
    int[] deltaX = new int[]{0, 0, +1, -1};
    int[] deltaY = new int[]{+1, -1, 0, 0};
    List<Integer> neighbours = new ArrayList<>();
    for (int i = 0; i < deltaX.length; i++) {
      int nextX = integer % DIM + deltaX[i];
      int nextY = integer / DIM + deltaY[i];
      if (isField(nextY, nextX)) {
        int next = nextY * DIM + nextX;
        neighbours.add(next);
      }
    }
    return neighbours;
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
  public String toString() {
    StringBuilder boardString = new StringBuilder();
    for (int i = 0; i < DIM - 1; i++) {
      boardString.append(intersectionLine(i)).append(DELIM).append(numberLine(i)).append("\n");
      boardString.append(boxLine()).append(DELIM).append(boxLine()).append("\n");
    }
    int finalLine = DIM - 1;
    boardString.append(intersectionLine(finalLine)).append(DELIM).append(numberLine(finalLine))
        .append("\n");
    return boardString.toString();
  }

  public boolean equals(Board board){
    boolean isEqual = false;
    if (board.getDim() == this.getDim()){
      isEqual = true;
      for (int i = 0; i < this.getDim() * getDim(); i++) {
        isEqual &=  this.getField(i) == board.getField(i);
      }
    }
    return isEqual;
  }
}
