package com.nedap.go.networking.client;

import com.nedap.go.model.GoGame;
import com.nedap.go.model.GoMove;
import com.nedap.go.model.Player;
import com.nedap.go.model.Stone;
import com.nedap.go.model.utils.InvalidMoveException;
import com.nedap.go.networking.server.utils.PlayerNotFoundException;

import com.nedap.go.tui.QuitGameException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.InetAddress;
import java.util.List;
import java.util.Scanner;

/**
 * The class of the TUI used for the chat application
 */
public class GameClientTUI implements ClientListener {

  private final Scanner sc;
  private final PrintWriter output;
  private GameClient client;
  private String serverName = "localhost";
  private int portNumber = 8888;
  private boolean logInSuccessful;
  private boolean isSystemOut;
  private boolean isGameStarted;
  private boolean isConnected;
  private ClientGameAdapter game;


  public GameClientTUI(Reader input, PrintWriter output) {
    this.output = output;
    sc = new Scanner(input);
  }

  public GameClientTUI() {
    this(new InputStreamReader(System.in), new PrintWriter(System.out));
    isSystemOut = true;
  }

  public static void main(String[] args) {
    GameClientTUI tui = new GameClientTUI();
    tui.run();
  }

  public void runGame() {
    selectPlayerType();
    if (matchMakingMenu()) {
      play();
    }
    println(game.getGameEndMessage());
    println(game.displayState());
    sc.nextLine();
    runGame();
  }


  private synchronized void play() {
    while (!isGameStarted) {
      try {
        this.wait();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    isGameStarted = false;
    while(!game.isGameOver()) {
      try {
        game.playMove();
        println(game.displayState());
      } catch (GameMismatchException | InvalidMoveException e) {
        printError(e.getMessage());
        client.sendError(e.getMessage());
      } catch (QuitGameException e) {
        client.sendResign();
      }
    }
  }

  private boolean matchMakingMenu() {
    String matchmaking = """
        Get ready for a Game:\s
                1. Find a Game.\s
                2. Quit.\s
        """;
    println(matchmaking);
    switch (sc.nextInt()) {
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
            -H for human player via the TUI.\s
            -N for Naive AI player.\s
        """;
    println(selectPlayerText);
    print("-->");
    String playerType = sc.nextLine();
    client.setPlayerType(playerType);
  }

  private void run() {
    int choice = menu();
    switch (choice) {
      case 1 -> {
        initializeClient(serverName, portNumber);
        sendUsername();
        runGame();
      }
      case 2 -> {
        getHelp();
        //TODO: Why exception
        run();
      }
      case 3 -> client.close();
      default -> {
        println("Not a valid choice");
        println("");
        run();
      }
    }
  }

  private void getHelp() {
    Player player1Help = () -> Stone.BLACK;
    Player player2Help = () -> Stone.WHITE;
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

  private int menu() {
    String menu = """
        Welcome to JavaGO!\s
               Main Menu\s
            1. Play Game\s
            2. Help\s
            3. Quit\s
        """;
    println(menu);
    if (isSystemOut) {
      print("-->");
    }
    return Integer.parseInt(sc.nextLine());
  }

  /**
   * username is assigned by the user and communicated to the server.
   */
  private synchronized void sendUsername() {
    while(!isConnected){
      try {
        this.wait();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    print("Provide a username: ");
    String username = sc.nextLine();
    client.sendUsername(username);

    checkUsername(username);
  }

  private synchronized void checkUsername(String username) {
    try {
      this.wait();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    if (logInSuccessful) {
      println("Your new username is: " + username);
      logInSuccessful = true;
    } else {
      println("Username: " + username
          + " already exists. Choose a new one.");
      sendUsername();
    }
  }

  /**
   * Initialise the client - server connection.
   */
  private void initializeClient(String serverName, int portNumber) {

    try {
      client = new GameClient(InetAddress.getByName(serverName), portNumber);
      client.addListener(this);
    } catch (IOException e) {
      println("Could not find host " + serverName
          + " @ port: " + portNumber);
      initializeClient();
    }
  }

  /**
   * Initialise the client - server connection.
   */
  private void initializeClient() {
    print("Please provide a server ip: ");
    serverName = sc.nextLine();
    print("Now please provide a port: ");
    portNumber = sc.nextInt();
    sc.nextLine();
    try {
      client = new GameClient(InetAddress.getByName(serverName), portNumber);
      client.addListener(this);
    } catch (IOException e) {
      println("Could not find host " + serverName
          + " @ port: " + portNumber);
      initializeClient();
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
    logInSuccessful = status;
    this.notifyAll();
  }

  /**
   * Disconnect notification
   */
  @Override
  public synchronized void connectionLost() {
    println("Disconnected from the server");
    println("Restart client to try and reconnect");
    System.exit(1);
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
  public synchronized void newGame(String player1Name,
      String player2Name, int boardDim) {
    try {
      game = new ClientGameAdapter(player1Name, player2Name,
          boardDim, client);
    } catch (PlayerNotFoundException e) {
      client.sendError(e.getMessage());
      print(e.getMessage());
    }
    isGameStarted = true;
    println("New game between " + player1Name + " "
        + Stone.BLACK + " - " + Stone.WHITE + " " + player2Name
        + " in a " + boardDim + "x" + boardDim + " board!");
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
   * Receive a pass
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
    game.receiveWinner(winner);
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
