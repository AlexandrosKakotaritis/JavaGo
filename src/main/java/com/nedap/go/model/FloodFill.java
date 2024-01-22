package com.nedap.go.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

class FloodFill {


  private static boolean isValidIndex(int index, int delta, int dim) {

    if(0 > index || index >= dim * dim){
      return false;
    }
    int x = index % dim;
    if(delta == -1 && x == dim - 1){
      return false;
    }
    if(delta == +1 && x == 0){
      return false;
    }
    return true;
  }

  private static boolean isValidTarget(int index, Stone[] fields, Stone target){
    if(fields[index] != target){
      return false;
    }
    return true;
  }

  private static boolean isNotAlreadySearched(int index, List<Integer> visitedIdx){
    if(visitedIdx.contains(index)){
      return false;
    }
    return true;
  }

  static List<Integer> BreadthWideSearch(int start, Stone[] fields) {
    int dim = (int) Math.sqrt(fields.length);
    int[] deltasX = new int[]{0, 0, -1, +1};
    int[] deltasY = new int[]{-1, +1, 0, 0};
    Queue<Integer> queue = new LinkedList<>();
    List<Integer> visitedIdx = new ArrayList<>();
    Stone target = fields[start];
    queue.add(start);
    visitedIdx.add(start);
    while(!queue.isEmpty()){
      int current = queue.poll();
      for (int i = 0; i < deltasY.length; i++) {
        int nextX = current % dim + deltasX[i];
        int nextY = current / dim + deltasY[i];
        if(isValidIndex(nextX, nextY, dim)){
          int next = nextY * dim + nextX;
            if(isValidTarget(next, fields, target)
            && isNotAlreadySearched(next, visitedIdx)){
            queue.add(next);
            visitedIdx.add(next);
          }
        }
      }
    }
    return visitedIdx;
  }
}


