package com.nedap.go.tui;

import com.nedap.go.ai.ComputerPlayer;
import com.nedap.go.ai.NaiveStrategy;
import com.nedap.go.model.AbstractPlayer;
import com.nedap.go.model.Game;
import com.nedap.go.model.GoMove;
import com.nedap.go.model.Move;
import com.nedap.go.model.Player;
import com.nedap.go.model.Stone;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Scanner;

/**
 * Class used for enabling humans to play the game through the GoTUI class.
 */
public class HumanPlayer extends AbstractPlayer implements Player {

  private final Player helper;
  private final Stone stone;
  Scanner playerInput;
  PrintWriter output;
  private boolean isConsole;

  /**
   * Creates a new HumanPlayer object. This constructor is used for default play through the
   * console.
   *
   * @param name  Name of the player.
   * @param stone The stone color of the player.
   */
  public HumanPlayer(String name, Stone stone) {
    this(name, stone, new ComputerPlayer(new NaiveStrategy(), stone));
    isConsole = true;
  }

  /**
   * This constructor gives the ability to choose AI helper.
   *
   * @param name   Name of the player
   * @param stone  The stone color of the player.
   * @param helper The AI used for hints.
   */
  public HumanPlayer(String name, Stone stone, ComputerPlayer helper) {
    this(name, stone, helper, new InputStreamReader(System.in), new PrintWriter(System.out));
    isConsole = true;
  }

  /**
   * Generalized constructor which defines the level of the helper and input/output through
   * Reader/PrintWriter.
   *
   * @param name        Name of the player
   * @param stone       The stone color of the player.
   * @param helper      The AI used for hints.
   * @param playerInput The input reader.
   * @param output      The output writer.
   */
  public HumanPlayer(String name, Stone stone, ComputerPlayer helper, Reader playerInput,
      PrintWriter output) {
    super(name);
    this.stone = stone;
    this.helper = helper;
    this.playerInput = new Scanner(playerInput);
    this.output = output;
  }

  /**
   * Determines the next move, if the game still has available moves.
   *
   * @param game the current game
   * @return the player's choice
   */
  @Override
  public Move determineMove(Game game) throws QuitGameException {
    GoMove move;
    int input;
    try {
      input = inputManager(game);
      move = inputToMove(input);
    } catch (WrongInputException e) {
      output.println("Wrong move input! Moves must be an integer "
          + "corresponding to an intersection e.g. 10");
      return determineMove(game);
    }
    if (game.isValidMove(move)) {
      return move;
    } else {
      output.println("Invalid Move! Try again");
      System.out.println(game);
      return determineMove(game);
    }
  }

  private GoMove inputToMove(int index) {
    return new GoMove(this, index);
  }

  private int inputManager(Game game) throws WrongInputException, QuitGameException {
    output.println("Player " + getName() + " Choose a move");
    if (isConsole) {
      output.print("-->");
    }
    String input = playerInput.nextLine();
    try {
      return Integer.parseInt(input);
    } catch (NumberFormatException e) {
      switch (input) {
        case "quit" -> {
          output.close();
          throw new QuitGameException();
        }
        case "hint" -> giveHint(game);
        default -> throw new WrongInputException();
      }
      return inputManager(game);
    }
  }

  private void giveHint(Game game) throws QuitGameException {
    Game gameCopy = game.deepCopy();
    Move move = ((AbstractPlayer) helper).determineMove(gameCopy);
    output.println("Try playing " + ((GoMove) move).getIndex());
  }


  @Override
  public Stone getStone() {
    return stone;
  }
}