package com.nedap.go;

import com.nedap.go.networking.server.GameServer;
import com.nedap.go.tui.ServerTui;

public class GoServer {

  public static void main(String[] args) {
    ServerTui tui = new ServerTui();
    tui.runServer();
  }

}
