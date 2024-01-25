package com.nedap.go.tui;


import com.nedap.go.ai.ComputerPlayer;
import com.nedap.go.ai.NaiveStrategy;
import com.nedap.go.model.AbstractPlayer;
import com.nedap.go.model.GoGame;
import com.nedap.go.model.GoMove;
import com.nedap.go.model.Move;
import com.nedap.go.model.Player;
import com.nedap.go.model.Stone;
import com.nedap.go.model.utils.InvalidMoveException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Scanner;

/**
 * TUI class for the game Go.
 */
public class GoTui implements Runnable {

  Scanner sc;
  PrintWriter output;

  Reader input;
  private GoGame game;
  private Player player1;
  private Player player2;
  private boolean isSystemOut = false;

  public GoTui(Reader input, PrintWriter output) {
    this.output = output;
    this.input = input;
    sc = new Scanner(input);
  }

  public GoTui() {
    this(new InputStreamReader(System.in), new PrintWriter(System.out));
    isSystemOut = true;
  }

  public static void main(String[] args) {
    GoTui tui = new GoTui();
    tui.run();
  }

  /**
   * The method that runs the game.
   */
  public void run() {
    boolean running = true;

    while (running) {
      boolean playGame = false;
      int runState = menu();
      switch (runState) {
        case 1:
          playGame = true;
          break;
        case 2:
          getHelp();
          break;
        case 3:
          running = false;
          break;
        default:
      }
      play(playGame);
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
        AI players: \s
            Use -N instead of name for the Naive Strategy AI\s
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

  /**
   * Method responsible for controlling the game cycle.
   *
   * @param playGame True if the game starts.
   */
  private void play(boolean playGame) {

    playGame = startNewGame(playGame);
    while (playGame) {
      setUpGame(player1, player2);
      boolean quit = runGame();
      displayState();
      displayWinner(quit);
      playGame = retry();
    }
  }

  private boolean runGame() {
    boolean quit = false;
    try {
      while (!game.isGameover()) {
        displayState();
        displayTurn();
        newMove();
      }
    } catch (QuitGameException e) {
      quit = true;
    } catch (InvalidMoveException e) {
      throw new RuntimeException(e);
    }
    return quit;
  }

  private void displayTurn() {
    println(game.getTurn() + " it is your turn!");
  }

  private boolean startNewGame(boolean playGame) {
    if (playGame) {
      try {
        player1 = createPlayer(1, Stone.BLACK);
        player2 = createPlayer(2, Stone.WHITE);
      } catch (QuitGameException e) {
        playGame = false;
      }
    }
    return playGame;
  }

  /**
   * Either reruns the game or exits to main menu.
   *
   * @return true if a new game is played or false to exit to menu
   */
  private boolean retry() {
    println("Game is over! Want to play again? (Y/N)");
    String choice = sc.nextLine().trim().split("\\s+")[0];
    Player tempPlayer = player1;
    player1 = player2;
    player2 = tempPlayer;
    return choice.equalsIgnoreCase("y");
  }

  /**
   * Prints the Winner of the game.
   */
  private void displayWinner(boolean quit) {
    if (quit) {
      String message = getQuitMessage();
      println(message);
    } else {
      Player winner = game.getWinner();
      if (winner == null) {
        println("It's a tie GG!");
      } else {
        println("Winner is: " + ((AbstractPlayer) winner).getName() + " GG!");
      }
    }
  }

  private String getQuitMessage() {
    String message;
    if (game.getTurn().equals(player1)) {
      message = (((AbstractPlayer) player1).getName()) + " forfeited the match. "
          + (((AbstractPlayer) player2).getName()) + " wins";
    } else {
      message = (((AbstractPlayer) player2).getName()) + " forfeited the match. "
          + (((AbstractPlayer) player1).getName()) + " wins";
    }
    return message;
  }

  /**
   * Plays the next move specified by the player.
   */
  private void newMove() throws InvalidMoveException, QuitGameException {
    Move move = ((AbstractPlayer) game.getTurn()).determineMove(game);
    game.doMove(move);
  }

  /**
   * Displays the state of the boardBox and the scoreboard.
   */
  private void displayState() {
    println(game);
  }

  /**
   * Creates the game instance.
   *
   * @param player1 The first player.
   * @param player2 The second player.
   */
  private void setUpGame(Player player1, Player player2) {
    game = new GoGame(player1, player2);
    println("Begin!");
  }

  private Player createPlayer(int index, Stone stone) throws QuitGameException {

    println("Give name for Player " + index + " with " + stone + " stone");
    println("(See help for AI players, use exit to quit to main menu)");
    if (isSystemOut) {
      print("-->");
//      output.flush();
    }

    String playerName = sc.nextLine();
    Player player;
    player = switch (playerName) {
      case "quit" -> throw new QuitGameException();
      case "-N" -> new ComputerPlayer(new NaiveStrategy(), stone);
      default -> createHumanPlayer(playerName, stone);
    };
    return player;
  }

  private Player createHumanPlayer(String playerName, Stone stone) {
    return new HumanPlayer(playerName, stone, new ComputerPlayer(new NaiveStrategy(), stone), input,
        output);
  }

  /**
   * Method responsible for the main menu.
   *
   * @return The choice from the menu.
   */
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

  private void println(Object o) {
    output.println(o);
    output.flush();
  }

  private void print(Object o) {
    output.print(o);
    output.flush();
  }
}



