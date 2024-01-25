package com.nedap.go.model.utils;

import com.nedap.go.model.Board;
import java.util.ArrayList;

/**
 * An Array list containing Board objects and implementing a method to
 * find if a given board is contained in the list. Used in enforcing the
 * ko rule.
 */
public class BoardList extends ArrayList<Board> {

  /**\
   * Create an Arraylist of Board objects.
   */
  public BoardList() {
    super();
  }

  /**
   * Finds whether a board which equals the object given as parameter is contained within the list
   * (See com.nedap.go.model.Board.equals for board equality).
   *
   * @param o The object of which a match is searched.
   * @return True if the given object has an equal contained in the list
   */
  public boolean matches(Object o) {
    for (Board board : this) {
      if (board.equals(o)) {
        return true;
      }
    }
    return false;
  }
}