package com.nedap.go.networking.client;

import com.nedap.go.model.GoMove;
import com.nedap.go.model.utils.InvalidMoveException;
import com.nedap.go.networking.SocketConnection;
import com.nedap.go.networking.protocol.Protocol;
import com.nedap.go.networking.server.utils.ImproperMessageException;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Class responsible for receiving and sending-encoding messages through the socket.
 */
public class ClientConnection extends SocketConnection {

  private GameClient gameClient;
  private MessageHandlerClient messageHandler;

  /**
   * Make a new TCP connection to the given host and port. The receiving thread is not started yet.
   * Call start on the returned SocketConnection to start receiving messages.
   *
   * @param host the address of the server to connect to
   * @param port the port of the server to connect to
   * @throws IOException if the connection cannot be made or there was some other I/O problem
   */
  protected ClientConnection(InetAddress host, int port) throws IOException {
    super(host, port);
    start();
  }

  /**
   * Set the chatClient object.
   *
   * @param gameClient The chatClient object.
   */
  protected void setGameClient(GameClient gameClient) {
    this.gameClient = gameClient;
    messageHandler = new MessageHandlerClient(gameClient);
  }

  /**
   * Send username through the socket to the server.
   *
   * @param username The username to be sent
   */
  public void sendUsername(String username) {
    sendMessage(Protocol.LOGIN + Protocol.SEPARATOR + username);
  }


  /**
   * Handles a message received from the connection.
   *
   * @param message the message received from the connection
   */
  @Override
  protected void handleMessage(String message) {
    try {
      messageHandler.handleMessage(message);
    } catch (ImproperMessageException | InvalidMoveException e) {
      gameClient.printError("INVALID MESSAGE RECEIVED:" + e.getMessage());
    } catch (ErrorReceivedException e) {
      gameClient.printError("Server Error:" + e.getMessage());
    }
  }


  /**
   * Handles a disconnect from the connection, i.e., when the connection is closed.
   */
  @Override
  protected void handleDisconnect() {
    gameClient.handleDisconnect();
  }

  /**
   * closes the socket.
   */
  @Override
  protected void close() {
    super.close();
  }

  public void sendQueue() {
    sendMessage(Protocol.QUEUE);
  }


  /**
   * Decodes a GoMove object to adhere to the protocol and sends the move.
   *
   * @param myMove The move as a GoMove object.
   */
  public void sendMove(GoMove myMove) {
    if (myMove.isPass()) {
      sendMessage(Protocol.PASS);
    } else {
      sendMessage(Protocol.MOVE + Protocol.SEPARATOR + myMove.getIndex());
    }
  }

  public void sendResign() {
    sendMessage(Protocol.RESIGN);
  }

  public void sendHello() {
    sendMessage(Protocol.HELLO + Protocol.SEPARATOR + "hello");
  }
}
