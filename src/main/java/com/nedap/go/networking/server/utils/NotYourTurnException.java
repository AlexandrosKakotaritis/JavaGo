package com.nedap.go.networking.server.utils;

public class NotYourTurnException extends Exception {
  public NotYourTurnException(){
    super("It's not your turn!");
  }
  public NotYourTurnException(String message){
    super(message);
  }

}
