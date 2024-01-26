package com.nedap.go.model.utils;

public class InvalidMoveException extends Exception {
  public InvalidMoveException(){
    super("Invalid Move");
  }

  public InvalidMoveException(String message){
    super(message);
  }

}
