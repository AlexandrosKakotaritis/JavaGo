package com.nedap.go.networking.server.utils;

public class GameNotFoundException extends Exception {
  public GameNotFoundException(){
    super("Someone lost your board!");
  }

  public GameNotFoundException(String message){
    super(message);
  }
}
