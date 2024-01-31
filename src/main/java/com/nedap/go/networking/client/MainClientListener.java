package com.nedap.go.networking.client;

public interface MainClientListener extends ClientListener{

  /**
   * Confirms that log in was successful with the server.
   *
   * @param status   The status. True if successful
   * @param username The username used.
   */
  void logInStatus(boolean status, String username);


  /**
   * Notify listeners of successful connection with the server and propagates server's message.
   *
   * @param message The server's hello message.
   */
  void successfulConnection(String message);
}
