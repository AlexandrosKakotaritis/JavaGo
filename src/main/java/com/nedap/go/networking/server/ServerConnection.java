package com.nedap.go.networking.server;

import com.nedap.go.model.Stone;
import com.nedap.go.networking.SocketConnection;
import com.nedap.go.networking.protocol.Protocol;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * The class responsible for decoding messages. For decoding receiving messages
 * it delegates to a MessageHandler object.
 */
public class ServerConnection extends SocketConnection {

  private final ClientHandler clientHandler;

  private final MessageHandler messageHandler;


  /**
   * Create a new SocketConnection. This is not meant to be used directly. Instead, the SocketServer
   * and SocketClient classes should be used.
   *
   * @param socket        the socket for this connection
   * @param clientHandler The clientHandler that communicates this connection to the server.
   * @throws IOException if there is an I/O exception while initializing the Reader/Writer objects
   */
  public ServerConnection(Socket socket, ClientHandler clientHandler) throws IOException {
    super(socket);
    this.clientHandler = clientHandler;
    messageHandler = new MessageHandler(clientHandler);
  }


  /**
   * Starts the Socket thread.
   */
  public void start() {
    super.start();
  }

  public boolean sayHello() {
    return sendMessage(Protocol.HELLO + Protocol.SEPARATOR
        + Protocol.SERVER_DESCRIPTION);
  }


  /**
   * Handles a message received from the connection.
   *
   * @param message the message received from the connection.
   */
  @Override
  public void handleMessage(String message) {
    try {
      messageHandler.handleMessage(message);
    } catch (ImproperMessageException e) {
      System.out.println("Improper command skipped");
      sendError("Improper command");
    }
  }

  public void sendError(String errorMessage) {
    sendMessage(Protocol.ERROR + Protocol.SEPARATOR + errorMessage);
  }

  /**
   * Handles a disconnect from the connection, i.e., when the connection is closed.
   */
  @Override
  public void handleDisconnect() {
    clientHandler.handleDisconnect();
  }

  public void sendLogin(boolean nameOK, String username) {
    if (nameOK) {
      sendMessage(Protocol.ACCEPTED + Protocol.SEPARATOR + username);
      messageHandler.setPlayerState(PlayerState.PREGAME);
    } else {
      sendMessage(Protocol.REJECTED + Protocol.SEPARATOR + username);
    }
  }

  public void sendList(List<ClientHandler> listOfClients) {
    StringBuilder sb = new StringBuilder(Protocol.LIST);

    for (ClientHandler client : listOfClients) {
      sb.append(Protocol.SEPARATOR);
      sb.append(client.getUsername());
    }
    super.sendMessage(sb.toString());
  }

  public void startGame(String usernamePlayer1, String usernamePlayer2, int boardDim) {
    sendMessage(Protocol.NEW_GAME + Protocol.SEPARATOR + usernamePlayer1 + Protocol.SEPARATOR
        + usernamePlayer2 + Protocol.SEPARATOR + boardDim);
    messageHandler.setPlayerState(PlayerState.IN_GAME);
  }

  public void sendMove(int moveIndex, Stone stone) throws NotAppropriateStoneException {
    sendMessage(Protocol.MOVE + Protocol.SEPARATOR + moveIndex
        + Protocol.SEPARATOR + getStoneName(stone));
  }

  public void sendGameOver(String message) {
    messageHandler.setPlayerState(PlayerState.PREGAME);
    sendMessage(Protocol.GAME_OVER + Protocol.SEPARATOR + message);
  }

  public void sendPass(Stone stone) throws NotAppropriateStoneException {
    sendMessage(Protocol.PASS + Protocol.SEPARATOR + getStoneName(stone));
  }

  private String getStoneName(Stone stone) throws NotAppropriateStoneException {

    return switch(stone){
      case Stone.BLACK -> Protocol.BLACK;
      case Stone.WHITE -> Protocol.WHITE;
      default -> throw new NotAppropriateStoneException("Your stones are broken");
    };
  }

  public void sendQueued() {
    sendMessage(Protocol.QUEUED);
  }

  public void sendTurn(String name) {
    sendMessage(Protocol.MAKE_MOVE + Protocol.SEPARATOR + name);
  }
}
