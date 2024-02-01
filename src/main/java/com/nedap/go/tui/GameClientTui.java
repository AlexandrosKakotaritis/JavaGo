package com.nedap.go.tui;

import com.nedap.go.model.GoGame;
import com.nedap.go.model.GoMove;
import com.nedap.go.model.Stone;
import com.nedap.go.model.utils.InvalidMoveException;
import com.nedap.go.networking.client.MainClientListener;
import com.nedap.go.networking.server.OnlinePlayer;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Scanner;

/**
 * The TUI of the client.
 */
public class GameClientTui implements Runnable {

  private final Scanner sc;
  private final PrintWriter output;
  MainClientListener mainClientListener;
  private int portNumber;
  private String serverName;

  /**
   * Main constructor of the TUI with specified input and output.
   *
   * @param input  The reader that inputs data.
   * @param output The printwriter that outputs data
   */
  public GameClientTui(Reader input, PrintWriter output) {
    this.output = output;
    sc = new Scanner(input);
    mainClientListener = new MainClientListener(input, output);
    mainClientListener.setTui(this);
  }

  public GameClientTui() {
    this(new InputStreamReader(System.in), new PrintWriter(System.out));
  }

  /**
   * Runs this operation.
   */
  @Override
  public void run() {
    menu();

    switch (getIntMenuChoice()) {
      case 1 -> {
        initializeClient();
        mainClientListener.initializeGui();
        sendUsername();
        mainClientListener.runConnection();
      }
      case 2 -> {
        getHelp();
        run();
      }
      case 3 -> exit();
      default -> {
        println("Not a valid choice");
        println("");
        run();
      }
    }
  }

  /**
   * Send the prefered username to the main client listener.
   */
  public void sendUsername() {
    print("Provide a username: ");
    String username = sc.nextLine();
    mainClientListener.sendUsername(username);
  }

  private void menu() {
    String menu = """
        Welcome to JavaGO!\s
               Main Menu\s
            1. Play Game\s
            2. Help\s
            3. Quit\s
        """;
    println(menu);
  }

  public int selectPlayerType() {
    String selectPlayerText = """
        Select your player type:\s
            1. for human player via the TUI.\s
            2. for Naive AI player.\s
            3. for Smart AI player.\s
            3. for Smarter AI player.\s
        """;
    println(selectPlayerText);
    return getIntMenuChoice();
  }

  private int getIntMenuChoice() {
    int choice;
    try {
      choice = Integer.parseInt(sc.nextLine());
    } catch (NumberFormatException e) {
      choice = 10;
    }
    return choice;
  }

  /**
   * Receive the essential information of the host and send them to the main listener.
   */
  public void initializeClient() {
    print("Please provide a server ip: ");
    serverName = sc.nextLine();
    print("Now please provide a port: ");
    portNumber = sc.nextInt();
    sc.nextLine();
    mainClientListener.initializeClient(serverName, portNumber);
  }

  private void getHelp() {
    OnlinePlayer player1Help = new OnlinePlayer("Player 1", Stone.BLACK);
    OnlinePlayer player2Help = new OnlinePlayer("Player 2", Stone.WHITE);
    GoGame helpGame = new GoGame(player1Help, player2Help);
    try {
      helpGame.doMove(new GoMove(player1Help, 6));
    } catch (InvalidMoveException e) {
      throw new RuntimeException(e);
    }
    String helpText = """
        How to start a game: \s
            1. Type the ip and the port of the host when asked.\s
            If no host is found, you can ask again.\s
            \s
            2. Choose your unique username.\s
            (If username is already in use you will need to provide a new one).\s
            \s
            3. Choose your player type.\s
            After that you automatically get in the queue.\s
            \s
        players: \s
            Use -H for human player.\s
            Use -N instead of name for the Naive Strategy AI.\s
            \s
        How to play: \s
              Type the preferred line number as seen in the numbering grid, \s
              e.g. 6. \s
        """;
    println(helpText);
    println(helpGame);
    println("Press Enter");
    sc.nextLine();
    println("");
  }

  public boolean matchMakingMenu() {
    String matchmaking = """
        Get ready for a Game:\s
                1. Find a Game.\s
                2. Quit.\s
        """;
    println(matchmaking);
    switch (getIntMenuChoice()) {
      case 1 -> mainClientListener.sendQueue();
      case 2 -> {
        return false;
      }
      default -> {
        println("Not a valid choice");
        println("");
        matchMakingMenu();
      }
    }
    return true;
  }

  public void exit() {
    println("Goodbye!");
    System.exit(0);
  }

  private void println(Object o) {
    output.println(o);
    output.flush();
  }

  private void print(Object o) {
    output.print(o);
    output.flush();
  }

}
