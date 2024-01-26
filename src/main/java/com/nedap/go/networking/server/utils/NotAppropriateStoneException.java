package com.nedap.go.networking.server.utils;

public class NotAppropriateStoneException extends Exception {

  public NotAppropriateStoneException(){
    super("NotAppropriateStone");
  }
  public NotAppropriateStoneException(String message){
    super(message);
  }

}
