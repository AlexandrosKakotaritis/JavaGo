package com.nedap.go.model.utils;

import com.nedap.go.model.Stone;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Flood fill algorithm used for tracking stone chains in a game of Go.
 */
public class FloodFillGo {


  private static boolean isValidIndex(int index, int delta, int dim) {

    if (0 > index || index >= dim * dim) {
      return false;
    }
    int x = index % dim;
    if (delta == -1 && x == dim - 1) {
      return false;
    }
    if (delta == 1 && x == 0){
      return false;
    }
    return true;
  }

  private static boolean isValidTarget(int index, Stone[] fields, Stone target) {
    return fields[index] == target;
  }

  private static boolean isNotAlreadySearched(int index, List<Integer> visitedIdx) {
    return !visitedIdx.contains(index);
  }

  /**
   * Use the breadth wide search algorithm to find all stones that are connected and have the same
   * color.
   *
   * <p>
   * From a start point all the neighbours are checked for being in the confines of the board, being
   * of the same color as the starting point and then have not being visited before. If a point
   * meets the criteria is then saved in a List which tracks the already visited and a Queue which
   * serves as the tracker of which elements' neighbours should be checked next. Finally, the List
   * of the visited points is returned.</p>
   *
   * @param start  The start point of the algorithm.
   * @param fields The array representing the state of the board.
   * @return A list of integers containing the indices of the searched stones.
   */
  public static List<Integer> breadthWideSearch(int start, Stone[] fields) {
    int dim = (int) Math.sqrt(fields.length);
    int[] deltas = new int[]{dim, -dim, -1, +1};
    Queue<Integer> queue = new LinkedList<>();
    List<Integer> visitedIdx = new ArrayList<>();
    Stone target = fields[start];
    queue.add(start);
    visitedIdx.add(start);
    while (!queue.isEmpty()) {
      int current = queue.poll();
      for (int delta : deltas) {
        int next = current + delta;
        if (isValidIndex(next, delta, dim)
            && isValidTarget(next, fields, target)
            && isNotAlreadySearched(next, visitedIdx)) {
          queue.add(next);
          visitedIdx.add(next);
        }
      }
    }
    return visitedIdx;
  }
}


