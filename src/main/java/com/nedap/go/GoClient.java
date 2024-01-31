package com.nedap.go;

import com.nedap.go.tui.GameClientTui;

public class GoClient {

  public static void main(String[] args) {
    GameClientTui tui = new GameClientTui();
    tui.run();
  }
}
