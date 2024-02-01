package com.nedap.go.tui;

import com.nedap.go.networking.server.GameServer;
import java.io.IOException;
import java.util.Scanner;

/**
 * The class responsible for starting the server.
 */
public class ServerTui {
  /**
   * The method that runs the server of the Go game.
   */
  public void runServer() {
    int boardDim;
    Scanner sc = new Scanner(System.in);
    GameServer gameServer;
    while (true) {
      System.out.println("Please provide a boardSize");
      boardDim = sc.nextInt();
      System.out.println("Please provide a port");
      int portNumber = sc.nextInt();
      try {
        gameServer = new GameServer(portNumber, boardDim);
        System.out.println("Connecting via port: " + gameServer.getPort());
        gameServer.acceptConnections();
      } catch (IOException e) {
        System.out.println("Could not connect to port" + portNumber);
      }
    }
  }
}
