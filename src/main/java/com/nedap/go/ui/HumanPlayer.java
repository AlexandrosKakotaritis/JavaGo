package com.nedap.go.ui;

import com.nedap.go.ai.ComputerPlayer;
import com.nedap.go.ai.NaiveStrategy;
import com.nedap.go.model.AbstractPlayer;
import com.nedap.go.model.Game;
import com.nedap.go.model.GoMove;
import com.nedap.go.model.Move;
import com.nedap.go.model.Player;
import com.nedap.go.model.Stone;
import java.util.Scanner;

public class HumanPlayer extends AbstractPlayer implements Player {

  private final Player helper;
  private final Stone stone;
  Scanner playerInput = new Scanner(System.in);

  /**
   * Creates a new Player object.
   *
   * @param name Name of the player
   */
  public HumanPlayer(String name, Stone stone) {
    super(name);
    this.stone = stone;
    helper = new ComputerPlayer(new NaiveStrategy(), stone);
  }

  /**
   * Determines the next move, if the game still has available moves.
   *
   * @param game the current game
   * @return the player's choice
   */
  @Override
  public Move determineMove(Game game) throws ExitGameException {
    GoMove move;
    int input;
    try {
      input = inputManager(game);
      move = inputToMove(input);
    } catch (WrongInputException e) {
      System.out.println(
          "Wrong move input! " + "Moves must be an integer "
              + "corresponding to an intersection"
              + "e.g. 10");
      return determineMove(game);
    }
    if (game.isValidMove(move)) {
      return move;
    } else {
      System.out.println("Invalid Move! Try again");
      System.out.println(game);
      return determineMove(game);
    }
  }

  private GoMove inputToMove(int index) {
    return new GoMove(this, index);
  }

  private int inputManager(Game game) throws WrongInputException, ExitGameException {
    System.out.println("Player " + getName() + " Choose a move");
    System.out.print("-->");
    String input = playerInput.nextLine();
    try {
      return Integer.parseInt(input);
    } catch (NumberFormatException e) {
      switch (input) {
        case "quit" -> throw new ExitGameException();
        case "hint" -> giveHint(game);
        default -> throw new WrongInputException();
      }
      return inputManager(game);
    }
  }

  private void giveHint(Game game) throws ExitGameException {
    Game gameCopy = game.deepCopy();
    Move move = ((AbstractPlayer) helper).determineMove(gameCopy);
    System.out.println("Try playing " + ((GoMove) move).getIndex());
  }


  @Override
  public Stone getStone() {
    return stone;
  }
}