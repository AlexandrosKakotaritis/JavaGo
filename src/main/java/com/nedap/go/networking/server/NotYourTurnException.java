package com.nedap.go.networking.server;

public class NotYourTurnException extends Exception {
  public NotYourTurnException(){
    super("It's no your turn!");
  }
  public NotYourTurnException(String message){
    super(message);
  }

}
