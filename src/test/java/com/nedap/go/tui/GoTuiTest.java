package com.nedap.go.tui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nedap.go.model.Stone;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import org.junit.jupiter.api.Test;

public class GoTuiTest {

  private String mainMenu = """
    Welcome to JavaGO!\s
           Main Menu\s
        1. Play Game\s
        2. Help\s
        3. Quit\s
    """;
  private String helpText = """
    AI players: \s
        Use -N instead of name for the Naive Strategy AI\s
        \s
    How to play: \s
          Type the preferred line number as seen in the numbering grid, \s
          e.g. 6. \s
    """;
  private String player1Naming = "Give name for Player " + 1 + " with " + Stone.BLACK + " stone"
      + "\n" + "(See help for AI players, use exit to quit to main menu)";

  private String player2Naming = "Give name for Player " + 2 + " with " + Stone.WHITE + " stone"
      + "\n" + "(See help for AI players, use exit to quit to main menu)";


  private static String readMenu(BufferedReader br) throws IOException {
    String s = "";
    for (int i = 0; i < 5; i++) {
      s += br.readLine() + "\n";
    }
    br.readLine();
    return s;
  }

  private static String readHelp(BufferedReader br) throws IOException {
    String s = "";
    for (int i = 0; i < 6; i++) {
      s += br.readLine() + "\n";
    }
    return s;
  }

  private static String readPlayerCreation(BufferedReader br) throws IOException {
    String s;
    s = br.readLine() + "\n";
    s += br.readLine();
    return s;
  }

  private static String readMovePrompt(BufferedReader br) throws IOException {
    String s;
    br.readLine();
    s = br.readLine() + "/n";
    s += br.readLine();
    return s;
  }

  private static String readBoard(BufferedReader br) throws IOException {
    String s = "";
    for (int i = 0; i < 18; i++) {
      s += br.readLine() + "\n";
    }
    return s;
  }

  @Test
  public void testMainMenu() throws IOException {
    String s = "";

    try (var pr1 = new PipedReader(); var pw1 = new PipedWriter(pr1);
        var pr2 = new PipedReader(); var pw2 = new PipedWriter(pr2);
        var br = new BufferedReader(pr2); var pw = new PrintWriter(pw1)) {

      GoTui tui = new GoTui(pr1, new PrintWriter(pw2));
      Thread t = new Thread(tui);
      t.start();
//      tui.run();
      pw.println(3);
      s = readMenu(br);
      assertEquals(mainMenu, s);
      pw.println(3);
    }
  }

  @Test
  public void testHelp() throws IOException, InterruptedException {


    try (var pr1 = new PipedReader(); var pw1 = new PipedWriter(pr1);
        var pr2 = new PipedReader(); var pw2 = new PipedWriter(pr2);
        var br = new BufferedReader(pr2); var pw = new PrintWriter(pw1)) {

      GoTui tui = new GoTui(pr1, new PrintWriter(pw2));
      Thread t = new Thread(tui);
      t.start();
      pw.println(2);
      readMenu(br);

      String s = readHelp(br);
      assertEquals(helpText, s);
//      Thread.sleep(200);
      pw.println();

      readMenu(br);
      pw.println(3);
      t.join();
    }
  }


  @Test
  public void testQuitPlayerNaming() throws IOException, InterruptedException {
    String s = "";

    try (var pr1 = new PipedReader(); var pw1 = new PipedWriter(pr1);
        var pr2 = new PipedReader(); var pw2 = new PipedWriter(pr2);
        var br = new BufferedReader(pr2); var pw = new PrintWriter(pw1)) {

      GoTui tui = new GoTui(pr1, new PrintWriter(pw2));
      Thread t = new Thread(tui);
      t.start();
      pw.println(1);

      readMenu(br);
      s = readPlayerCreation(br);

      assertEquals(player1Naming, s);

      pw.println("quit");


      s = readMenu(br);
      assertEquals(mainMenu, s);
      pw.println(3);
      t.join();
    }
  }



  @Test
  public void testHumanVSNaive() throws IOException, InterruptedException {
    String s = "";
    String name = "Name";

    try (var pr1 = new PipedReader(); var pw1 = new PipedWriter(pr1);
        var pr2 = new PipedReader(); var pw2 = new PipedWriter(pr2);
        var br = new BufferedReader(pr2); var pw = new PrintWriter(pw1)) {

      GoTui tui = new GoTui(pr1, new PrintWriter(pw2));
      Thread t = new Thread(tui);
      t.start();

      pw.println(1);
      pw.println(name);
      pw.println("-N");

      readMenu(br);
      s = readPlayerCreation(br);
      assertEquals(player1Naming, s);

      s = readPlayerCreation(br);
      assertEquals(player2Naming, s);

      s = br.readLine();
      assertEquals("Begin!", s);

      String scoreboardMove0 = "Player Name ○: 0 - 0 :● Player Naive-●";
      s = br.readLine();
      assertEquals(scoreboardMove0, s);

      String scoreboardMove1 = "Player Name ○: 81 - 0 :● Player Naive-●";
      // 12 lines since the next scoreboard
      readBoard(br);
      s = br.readLine();
      assertEquals("Player " + name + " it is your turn!", s);
      pw.println(3);

      br.readLine();
      s = br.readLine();
      assertEquals(scoreboardMove1, s);

      String scoreboardMove2 = "Player Name ○: 1 - 1 :● Player Naive-●";
      // 11 lines since the next scoreboard
      readBoard(br);
      s = br.readLine();
      assertEquals("Player Naive-● it is your turn!", s);
      s = br.readLine();
      assertEquals(scoreboardMove2, s);

      pw.println("quit");
      pw.println(3);
    }
  }
  @Test
  public void testWinner() throws IOException, InterruptedException {
    String s;
    String name1 = "Player1";
    String name2 = "Player2";
    try (var pr1 = new PipedReader(); var pw1 = new PipedWriter(pr1);
        var pr2 = new PipedReader(); var pw2 = new PipedWriter(pr2);
        var br = new BufferedReader(pr2); var pw = new PrintWriter(pw1)) {

      GoTui tui = new GoTui(pr1, new PrintWriter(pw2));
      Thread t = new Thread(tui);
      t.start();

      pw.println(1);
      pw.println(name1);
      pw.println(name2);

      readMenu(br);
      readPlayerCreation(br);
      readPlayerCreation(br);
      br.readLine();


      pw.println(0);
      readBoard(br);
      for (int i = 0; i < 3; i++) {
        s = br.readLine();
        s = br.readLine();
        s = br.readLine();
        s = readBoard(br);
        if( i<2){
          pw.println("pass");
        }

      }
      br.readLine();
      s = br.readLine();
      assertEquals("Winner is: " + name1 + " GG!", s);
      pw.println("N");
      pw.println(3);
      t.join();
    }
  }

  @Test
  public void testTie() throws IOException, InterruptedException {
    String s;
    String name1 = "Player1";
    String name2 = "Player2";
    try (var pr1 = new PipedReader(); var pw1 = new PipedWriter(pr1);
        var pr2 = new PipedReader(); var pw2 = new PipedWriter(pr2);
        var br = new BufferedReader(pr2); var pw = new PrintWriter(pw1)) {

      GoTui tui = new GoTui(pr1, new PrintWriter(pw2));
      Thread t = new Thread(tui);
      t.start();

      pw.println(1);
      pw.println(name1);
      pw.println(name2);

      readMenu(br);
      readPlayerCreation(br);
      readPlayerCreation(br);

      pw.println(0);
      br.readLine();
      readBoard(br);
      for (int i = 0; i < 4; i++) {
        s = readMovePrompt(br);
        s = readBoard(br);
        if(i < 1){
          pw.println(2);
        } else if (i < 3) {
          pw.println("pass");

        }

      }
      br.readLine();
      s = br.readLine();
      assertEquals("It's a tie GG!", s);
      pw.println("N");
      pw.println(3);
      t.join();
    }
  }

  @Test
  public void testQuitGame() throws InterruptedException, IOException {
    String s;
    String name1 = "Player1";
    String name2 = "Player2";
    try (var pr1 = new PipedReader(); var pw1 = new PipedWriter(pr1);
        var pr2 = new PipedReader(); var pw2 = new PipedWriter(pr2);
        var br = new BufferedReader(pr2); var pw = new PrintWriter(pw1)) {

      GoTui tui = new GoTui(pr1, new PrintWriter(pw2));
      Thread t = new Thread(tui);
      t.start();

      pw.println(1);
      pw.println(name1);
      pw.println(name2);

      readMenu(br);
      readPlayerCreation(br);
      readPlayerCreation(br);

      pw.println(0);
      br.readLine();
      readBoard(br);
      for (int i = 0; i < 2; i++) {
        s = readMovePrompt(br);
        s = readBoard(br);
        if(i<1){
          pw.println("quit");
        }
      }
      br.readLine();
      s = br.readLine();
      assertEquals(name2 + " forfeited the match. " + name1 + " wins", s);
      pw.println("N");
      pw.println(3);
      t.join();
    }
  }

  @Test
  public void testRematch() throws IOException, InterruptedException {
    String s;
    String name1 = "Player1";
    String name2 = "Player2";
    try (var pr1 = new PipedReader(); var pw1 = new PipedWriter(pr1);
        var pr2 = new PipedReader(); var pw2 = new PipedWriter(pr2);
        var br = new BufferedReader(pr2); var pw = new PrintWriter(pw1)) {

      GoTui tui = new GoTui(pr1, new PrintWriter(pw2));
      Thread t = new Thread(tui);
      t.start();

      pw.println(1);
      pw.println(name1);
      pw.println(name2);

      readMenu(br);
      readPlayerCreation(br);
      readPlayerCreation(br);

      pw.println(0);
      br.readLine();
      readBoard(br);
      for (int i = 0; i < 4; i++) {
        s = readMovePrompt(br);
        s = readBoard(br);
        if (i < 1) {
          pw.println(2);
        } else if (i < 3) {
          pw.println("pass");
        }

      }
      br.readLine();
      s = br.readLine();
      assertEquals("It's a tie GG!", s);
      pw.println("Y");
      br.readLine();

      s = br.readLine();
      assertEquals("Begin!", s);
      pw.println("quit");
      s = br.readLine();
      s = readBoard(br);


      s = readMovePrompt(br);
      s = readBoard(br);

      br.readLine();
      br.readLine();
      pw.println("N");
      pw.println(3);
      t.join();
    }
  }
}
