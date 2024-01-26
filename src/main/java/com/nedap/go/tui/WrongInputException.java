package com.nedap.go.tui;

public class WrongInputException extends Exception{
  public WrongInputException(){
    super("Not valid input");
  }

  public WrongInputException(String message){
    super(message);
  }
}
