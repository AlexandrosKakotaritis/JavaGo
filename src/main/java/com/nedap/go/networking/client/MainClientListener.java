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
public class MainClientListener implements MainListener {

  private final Scanner sc;
  private final PrintWriter output;
  private GameClient client;
  private boolean isLogIn;
  private boolean isConnected;

  private boolean hasResigned;
  private GameListener game;
  private GoGuiListener gui;
  private GameClientTui tui;


  public MainClientListener(Reader input, PrintWriter output) {
    this.output = output;
    sc = new Scanner(input);
  }

  public MainClientListener() {
    this(new InputStreamReader(System.in), new PrintWriter(System.out));
  }

  public void setTui(GameClientTui tui) {
    this.tui = tui;
  }

  /**
   * Runs the Matchmaking and the game instance.
   */
  public void runConnection() {
    boolean playGame = true;
    while (playGame) {
      client.setPlayerType(tui.selectPlayerType());
      playGame = tui.matchMakingMenu();
      if (playGame) {
        try {
          playGame();
          println(game.displayState());
          println(game.getGameEndMessage());
        } catch (InvalidMoveException | GameMismatchException e) {
          printError(e.getMessage());
        } catch (QuitGameException e) {
          handleResignation();
        }
      }
    }
    tui.exit();
  }

  /**
   * Waits for a move and calls the GameListener to produce its move if necessary.
   *
   * @throws QuitGameException When the player forfeits
   * @throws GameMismatchException When the client and server state do not coincide.
   * @throws InvalidMoveException When an invalid move is produced from the client.
   */
  private synchronized void playGame()
      throws QuitGameException,
      GameMismatchException, InvalidMoveException {
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
   * Receive confirmation of entering matchmaking queue.
   */
  @Override
  public void receiveInQueue() {
    println("Waiting for game");
  }

  public void sendQueue() {
    client.sendQueue();
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
      game = new GameListener(player1Name, player2Name, boardDim, client);
      client.addListener(game);
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

  }

  /**
   * Receive a pass.
   *
   * @param color The color of the player passing
   */
  @Override
  public void receivePass(String color) {

  }

  /**
   * Receive the game over with result draw.
   */
  @Override
  public void receiveDraw() {

  }

  /**
   * Receive the game over with result winner.
   *
   * @param winner The name of the winner.
   */
  @Override
  public void receiveWinner(String winner)  {
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
