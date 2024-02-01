package com.nedap.go.networking.client;

import com.nedap.go.model.GoMove;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible to transfer information to the listeners.
 */
public class GameClient {

  private final ClientConnection clientConnection;
  private final List<ClientListener> listOfListeners;
  private final MainListener mainListener;
  private String username;
  private int playerType;

  /**
   * While constructed constructs a new connection to the specified address\ and port.
   *
   * @param address The address to connect to.
   * @param port    The port to connect to.
   * @throws IOException if there is an I/O exception while initializing the Reader/Writer objects
   *                     of the socket.
   */
  public GameClient(InetAddress address, int port,
      MainListener mainListener) throws IOException {
    clientConnection = new ClientConnection(address, port);
    clientConnection.setGameClient(this);
    listOfListeners = new ArrayList<>();
    this.mainListener = mainListener;

  }

  public String getUsername() {
    return username;
  }


  public void addListener(ClientListener listener) {
    listOfListeners.add(listener);
  }

  public void removeListener(ClientListener listener) {
    listOfListeners.remove(listener);
  }


  public void sendUsername(String username) {
    this.username = username;
    clientConnection.sendUsername(username);
  }

  public void handleDisconnect() {
    close();
    mainListener.connectionLost();
    listOfListeners.forEach(ClientListener::connectionLost);
  }

  public void close() {
    clientConnection.close();
  }

  public void logInStatus(boolean status, String argument) {
    mainListener.logInStatus(status, argument);
  }

  public void successfulConnection(String message) {
    mainListener.successfulConnection(message);
  }

  public void receiveInQueue() {
    mainListener.receiveInQueue();
    listOfListeners.forEach(ClientListener::receiveInQueue);
  }

  public void newGame(String player1Name, String player2Name, int boardDim) {
    mainListener.newGame(player1Name, player2Name, boardDim);
    listOfListeners.forEach(listener -> listener.newGame(player1Name, player2Name, boardDim));
  }

  public void sendQueue() {
    clientConnection.sendQueue();
  }

  public int getPlayerType() {
    return playerType;
  }

  public void setPlayerType(int playerType) {
    this.playerType = playerType;
  }

  public void sendResign() {
    clientConnection.sendResign();
  }

  public void sendMove(GoMove myMove) {
    clientConnection.sendMove(myMove);
  }

  public void receiveMove(int moveIndex, String moveColor) {
    listOfListeners.forEach(listener -> listener.receiveMove(moveIndex, moveColor));
  }

  public void printError(String message) {
    mainListener.printError(message);
    listOfListeners.forEach(listener -> listener.printError(message));
  }

  public void receivePass(String color) {

    listOfListeners.forEach(listener -> listener.receivePass(color));
  }

  /**
   * Receive a draw and handle when server and client state of the game do not agree on the draw.
   */
  public void receiveDraw() {
    listOfListeners.forEach(listener -> {
      try {
        listener.receiveDraw();
      } catch (GameMismatchException e) {
        printError(e.getMessage());
      }
    });
  }

  /**
   * Receive a winner and handle when server and client state of the game do not agree on the draw.
   */
  public void receiveWinner(String winner) {
    listOfListeners.forEach(listener -> {
      try {
        listener.receiveWinner(winner);
      } catch (GameMismatchException e) {
        printError(e.getMessage());
      }
    });
  }
}
