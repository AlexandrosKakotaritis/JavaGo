package com.nedap.go.model;

/**
 * An enum class representing the basic states that an intersection can have.
 */
public enum Stone {
  BLACK, WHITE, EMPTY;

  /**
   * Give the opposite stone.

   * @return Black if it is white and vice versa.
   */
  public Stone other(){
    if(this==BLACK){
      return WHITE;
    }else{
      return BLACK;
    }
  }
  @Override
  public String toString(){
    return switch (this){
      case BLACK -> "X";
      case WHITE -> "O";
      case EMPTY -> "*";
    };
  }
}
