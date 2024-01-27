package com.nedap.go.networking.server.utils;

public class PlayerNotFoundException extends Exception {

  public PlayerNotFoundException() {
    super("Could not find player");
  }

  public PlayerNotFoundException(String message) {
    super(message);
  }
}
