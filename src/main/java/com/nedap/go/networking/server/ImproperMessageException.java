package com.nedap.go.networking.server;

public class ImproperMessageException extends Exception {

  public ImproperMessageException() {
    super("Not a proper message");
  }
  public ImproperMessageException(String s) {
    super(s);
  }
}
