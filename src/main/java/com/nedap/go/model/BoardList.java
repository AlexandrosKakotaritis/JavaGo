package com.nedap.go.model;

import java.util.ArrayList;

public class BoardList extends ArrayList<Board> {
  public BoardList(){
    super();
  }
  public boolean matches(Object o){
    for(Board board: this){
      if(board.equals(o)){
        return true;
      }
    }
    return false;
  }
}