package com.nedap.go.networking.server;

import com.nedap.go.model.Stone;
import java.util.List;

/**
 * Client handler for game application. Responsible for forwarding decoded instructions to the
 * server
 */
public class ClientHandler {

  private final GameServer server;
  private String username;

  private ServerConnection serverConnection;

  public ClientHandler(GameServer server) {
    this.server = server;
  }

  /**
   * Set the ServerConnection object responsible for decoding the messages, maintain the conection
   * to the client and handling the input output streams.
   *
   * @param serverConnection the ServerConnection object.
   */
  public void setServerConnection(ServerConnection serverConnection) {
    this.serverConnection = serverConnection;
  }

  /**
   * Get the username set by the client.
   *
   * @return The username as a string.
   */
  public String getUsername() {
    return username;
  }

  public boolean receiveLogin(String username) {
    this.username = username;
    return server.addClient(this);
  }

  /**
   * Inform the server of the disconnection of the client.
   */
  public void handleDisconnect() {
    server.removeClient(this);
  }

  public void sayHello() {
    serverConnection.sayHello();
  }

  public void queueReceived() {
    server.addInQueue(this);
  }

  public void listReceived() {
    server.handleList(this);
  }

  public void receiveMove(int moveIndex) {
    server.handleMove(this, moveIndex);
  }

  public void receiveMove(int row, int col) {
    server.handleMove(this, row, col);
  }

  public void receivePass() {
    server.handlePass(this);
  }

  public void handleResign() {
    server.handleResign(this);
  }

  public void sendLogin(boolean nameOk, String username) {
    serverConnection.sendLogin(nameOk, username);
  }

  public void sendList(List<ClientHandler> listOfClients) {
    serverConnection.sendList(listOfClients);
  }

  public void startGame(String usernamePlayer1, String usernamePlayer2, int boardDim) {
    serverConnection.startGame(usernamePlayer1, usernamePlayer2, boardDim);
  }


  public void sendError(String errorMessage) {
    serverConnection.sendError(errorMessage);
  }

  public void sendMove(int moveIndex, Stone stone) throws NotAppropriateStoneException {
    serverConnection.sendMove(moveIndex, stone);
  }

  public void sendPass(Stone stone) throws NotAppropriateStoneException {
    serverConnection.sendPass(stone);
  }

  public void sendGameOver(String message) {
    serverConnection.sendGameOver(message);
  }

  public void sendQueued() {
    serverConnection.sendQueued();
  }

  public void sendTurn(String name) {
    serverConnection.sendTurn(name);
  }
}




