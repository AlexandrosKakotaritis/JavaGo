package com.nedap.go.tui;

public class QuitGameException extends Exception {
  public QuitGameException(){
    super("Someone quited!");
  }

  public QuitGameException(String message){
    super(message);
  }
}
