package com.nedap.go.model;

import com.nedap.go.model.BoardList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoveBoardHashMap extends HashMap<GoMove, BoardList> {
  public MoveBoardHashMap(){
    super();
  }

  public BoardList getMatch(Object o){
    for(GoMove move: this.keySet()){
      if(move.equals(o)){
        return this.get(move);
      }
    }
    return null;
  }
  public boolean matchesKey(Object o){
    for(GoMove move: this.keySet()){
      if(move.equals(o)){
        return true;
      }
    }
    return false;
  }

}
