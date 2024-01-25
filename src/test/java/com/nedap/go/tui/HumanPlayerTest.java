package com.nedap.go.tui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nedap.go.ai.ComputerPlayer;
import com.nedap.go.ai.NaiveStrategy;
import com.nedap.go.model.Board;
import com.nedap.go.model.GoGame;
import com.nedap.go.model.Move;
import com.nedap.go.model.Player;
import com.nedap.go.model.Stone;
import com.nedap.go.model.utils.BoardList;
import com.nedap.go.model.utils.InvalidMoveException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.util.LinkedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HumanPlayerTest {
  private GoGame game;

  private Player player2;
  private HumanPlayer human;
  private Board board;

  @BeforeEach
  public void setUp() {
    board = new Board();
    player2 = () -> Stone.WHITE;
  }

  @Test
  public void testNormalPlay() throws QuitGameException, IOException, InvalidMoveException {
    String s;
    try (var pr1 = new PipedReader(); var pw1 = new PipedWriter(pr1);
        var pr2 = new PipedReader(); var pw2 = new PipedWriter(pr2);
        var br = new BufferedReader(pr2); var pw = new PrintWriter(pw1)) {
      human = new HumanPlayer("Henk", Stone.BLACK,
          new ComputerPlayer(new NaiveStrategy(), Stone.BLACK), pr1, new PrintWriter(pw2));
      game = new GoGame(human, player2, board, true, new BoardList(),
          new LinkedList());

      pw.println("2");
      Move move = human.determineMove(game);
      game.doMove(move);
      s = br.readLine();
      assertEquals("Player " + human.getName() + " Choose a move", s);
      System.out.println(game);
    }
  }

  @Test
  public void testHint() throws IOException, QuitGameException, InvalidMoveException {
    String s;
    try (var pr1 = new PipedReader(); var pw1 = new PipedWriter(pr1);
        var pr2 = new PipedReader(); var pw2 = new PipedWriter(pr2);
        var br = new BufferedReader(pr2); var pw = new PrintWriter(pw1)) {
      human = new HumanPlayer("Henk", Stone.BLACK,
          new ComputerPlayer(new NaiveStrategy(), Stone.BLACK), pr1, new PrintWriter(pw2));
      game = new GoGame(human, player2, board, true, new BoardList(),
          new LinkedList());

      pw.println("hint");
      pw.println(6);
      Move move = human.determineMove(game);
      game.doMove(move);
      s = br.readLine();
      assertEquals("Player " + human.getName() + " Choose a move", s);
      s = br.readLine();
      assertTrue(s.contains("Try playing"));
    }
  }
  @Test
  public void testWrongInput() throws IOException, QuitGameException, InvalidMoveException {
    String s;
    try (var pr1 = new PipedReader(); var pw1 = new PipedWriter(pr1);
        var pr2 = new PipedReader(); var pw2 = new PipedWriter(pr2);
        var br = new BufferedReader(pr2); var pw = new PrintWriter(pw1)) {
      human = new HumanPlayer("Henk", Stone.BLACK,
          new ComputerPlayer(new NaiveStrategy(), Stone.BLACK), pr1, new PrintWriter(pw2));
      game = new GoGame(human, player2, board, true, new BoardList(),
          new LinkedList());

      pw.println("why?");
      pw.println(6);
      Move move = human.determineMove(game);
      game.doMove(move);
      s = br.readLine();
      assertEquals("Player " + human.getName() + " Choose a move", s);
      s = br.readLine();
      String helpLine = "Wrong move input! Moves must be an integer "
          + "corresponding to an intersection e.g. 10";
      assertEquals(helpLine, s);
    }
  }

  @Test
  public void testExitCommand() throws IOException {
    String s;
    try (var pr1 = new PipedReader(); var pw1 = new PipedWriter(pr1);
        var pr2 = new PipedReader(); var pw2 = new PipedWriter(pr2);
        var br = new BufferedReader(pr2); var pw = new PrintWriter(pw1)) {
      human = new HumanPlayer("Henk", Stone.BLACK,
          new ComputerPlayer(new NaiveStrategy(), Stone.BLACK), pr1, new PrintWriter(pw2));
      game = new GoGame(human, player2, board, true, new BoardList(),
          new LinkedList());

      pw.println("quit");
      assertThrows(QuitGameException.class, () -> human.determineMove(game));
    }
  }
}
