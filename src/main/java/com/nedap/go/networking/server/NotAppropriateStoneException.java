package com.nedap.go.networking.server;

public class NotAppropriateStoneException extends Exception {

  public NotAppropriateStoneException(){
    super("NotAppropriateStone");
  }
  public NotAppropriateStoneException(String message){
    super(message);
  }

}
