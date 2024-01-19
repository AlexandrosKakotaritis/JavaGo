package com.nedap.go.model;

/**
 * An enum class representing the basic states that an intersection can have.
 */
public enum Stone {
  BLACK, WHITE, EMPTY;

  /**
   * Check if an intersection is filled
   * @return true if not EMPTY.
   */
  public boolean isFilled(){
    return this == BLACK || this == WHITE;
  }
}
