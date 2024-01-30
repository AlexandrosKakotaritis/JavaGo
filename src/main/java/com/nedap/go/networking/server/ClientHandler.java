package com.nedap.go.networking.server;

import com.nedap.go.model.Stone;
import com.nedap.go.networking.server.utils.NotAppropriateStoneException;
import com.nedap.go.networking.server.utils.PlayerState;
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
    username = this.toString();
  }

  public String getUsername() {
    return username;
  }

  public void receiveLogin(String username) {
    this.username = username;
    server.addClient(this);
  }

  public void handleDisconnect() {
    System.out.println("Client " + this.getUsername() + " disconnected");
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

  public void sendStartGame(String usernamePlayer1, String usernamePlayer2, int boardDim) {
    serverConnection.sendStartGame(usernamePlayer1, usernamePlayer2, boardDim);
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

  public void sendWinner(OnlinePlayer winner) {
    serverConnection.sendWinner(winner);
  }

  public void sendQueued() {
    serverConnection.sendQueued();
  }

  public void sendTurn(String name) {
    serverConnection.sendTurn(name);
  }

  public void sendDraw() {
    serverConnection.sendDraw();
  }

  public PlayerState getPlayerState() {
    return serverConnection.getPlayerState();
  }

  public void deQueueReceived() {
    server.removeFromQueue(this);
  }
}




