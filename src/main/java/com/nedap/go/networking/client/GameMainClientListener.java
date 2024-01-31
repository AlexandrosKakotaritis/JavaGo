package com.nedap.go.networking.client;

import com.nedap.go.gui.GoGuiListener;
import com.nedap.go.model.Stone;
import com.nedap.go.model.utils.InvalidMoveException;
import com.nedap.go.networking.server.utils.PlayerNotFoundException;
import com.nedap.go.tui.GameClientTui;
import com.nedap.go.tui.QuitGameException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.InetAddress;
import java.util.List;
import java.util.Scanner;

/**
 * The class of the TUI used for the chat application.
 */
public class GameMainClientListener implements MainClientListener {

  private final Scanner sc;
  private final PrintWriter output;
  private GameClient client;
  private boolean isLogIn;
  private boolean isConnected;
  private boolean hasResigned;
  private ClientGameAdapter game;
  private GoGuiListener gui;

  private GameClientTui tui;


  public GameMainClientListener(Reader input, PrintWriter output) {
    this.output = output;
    sc = new Scanner(input);
  }

  public GameMainClientListener() {
    this(new InputStreamReader(System.in), new PrintWriter(System.out));
  }

  public void setTui(GameClientTui tui) {
    this.tui = tui;
  }

  /**
   * Runs the Matchmaking and the game instance.
   */
  public void runGame() {
    boolean playGame = true;
    while (playGame) {
      selectPlayerType();
      playGame = matchMakingMenu();
      if (playGame) {
        try {
          play();
          println(game.displayState());
          println(game.getGameEndMessage());
          game = null;
        } catch (InvalidMoveException | GameMismatchException e) {
          printError(e.getMessage());
        } catch (QuitGameException e) {
          handleResignation();
          game = null;
        }
      }
    }
    tui.exit();
  }


  private synchronized void play()
      throws QuitGameException, GameMismatchException, InvalidMoveException {
    while (game == null || game.isGameOver()) {
      try {
        this.wait(500);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    while (!game.isGameOver()) {
      println(game.displayState());
      game.playMove();
    }
  }

  private void handleResignation() {
    client.sendResign();
    hasResigned = true;
    println("You resigned!");
  }

  private boolean matchMakingMenu() {
    String matchmaking = """
        Get ready for a Game:\s
                1. Find a Game.\s
                2. Quit.\s
        """;
    println(matchmaking);
    switch (getIntMenuChoice()) {
      case 1 -> client.sendQueue();
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

  private void selectPlayerType() {
    String selectPlayerText = """
        Select your player type:\s
            1. for human player via the TUI.\s
            2. for Naive AI player.\s
            3. for Pass AI player.\s
        """;
    println(selectPlayerText);
    String playerType;
    if ((playerType = sc.nextLine()).isEmpty()) {
      playerType = sc.nextLine();
    }
    client.setPlayerType(Integer.parseInt(playerType));
  }


  public void initializeGui() {
    gui = new GoGuiListener();
    client.addListener(gui);
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
   * username is assigned by the user and communicated to the server.
   */
  public synchronized void sendUsername(String username) {
    while (!isConnected) {
      try {
        this.wait();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    client.sendUsername(username);

    checkUsername(username);
  }

  private synchronized void checkUsername(String username) {
    try {
      this.wait();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    if (isLogIn) {
      println("Your new username is: " + username);
      isLogIn = true;
    } else {
      println("Username: " + username + " already exists. Choose a new one.");
      tui.sendUsername();
    }
  }

  /**
   * Initialise the client - server connection.
   */
  public void initializeClient(String serverName, int portNumber) {
    try {
      client = new GameClient(InetAddress.getByName(serverName), portNumber, this);
    } catch (IOException e) {
      println("Could not find host " + serverName + " @ port: " + portNumber);
      tui.initializeClient();
    }
  }

  /**
   * Confirms that log in was successful with the server.
   *
   * @param status   The status. True if successful
   * @param username The username used.
   */
  @Override
  public synchronized void logInStatus(boolean status, String username) {
    isLogIn = status;
    this.notifyAll();
  }

  /**
   * Disconnect notification.
   */
  @Override
  public void connectionLost() {
    println("Disconnected from the server");
    println("Restart the client");
    System.exit(0);
  }

  /**
   * Notify listeners of successful connection with the server and propagates server's message.
   *
   * @param message The server's hello message.
   */
  @Override
  public synchronized void successfulConnection(String message) {
    println("Connected to Server");
    if (message != null) {
      println(message);
    }
    isConnected = true;
    notifyAll();
  }

  /**
   * Receive the playerList.
   *
   * @param playerList The list of players.
   */
  @Override
  public void receiveList(List<String> playerList) {

  }

  /**
   * Receive confirmation of entering matchmaking queue.
   */
  @Override
  public void receiveInQueue() {
    println("Waiting for game");
  }

  /**
   * Starts new game.
   *
   * @param player1Name The name of the first player with black
   * @param player2Name The name of the second player with white.
   * @param boardDim    The dimension of the board.
   */
  @Override
  public synchronized void newGame(String player1Name, String player2Name, int boardDim) {
    try {
      game = new ClientGameAdapter(player1Name, player2Name, boardDim, client);
    } catch (PlayerNotFoundException e) {
      println(e.getMessage());
    }
    println("New game between " + player1Name + " " + Stone.BLACK + " - " + Stone.WHITE + " "
        + player2Name + " in a " + boardDim + "x" + boardDim + " board!");
    notifyAll();
  }

  @Override
  public void printError(String message) {
    println(message);
  }

  /**
   * Receiving moves from the server.
   *
   * @param moveIndex The index of the move.
   * @param moveColor The color of the stone.
   */
  @Override
  public void receiveMove(int moveIndex, String moveColor) {
    game.receiveMove(moveIndex, moveColor);
  }

  /**
   * Receive a pass.
   *
   * @param color The color of the player passing
   */
  @Override
  public void receivePass(String color) {
    game.receivePass(color);
  }

  /**
   * Receive the game over with result draw.
   */
  @Override
  public void receiveDraw() throws GameMismatchException {
    game.receiveDraw();
  }

  /**
   * Receive the game over with result winner.
   *
   * @param winner The name of the winner.
   */
  @Override
  public void receiveWinner(String winner) throws GameMismatchException {
    if (!hasResigned) {
      game.receiveWinner(winner);
    }
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
