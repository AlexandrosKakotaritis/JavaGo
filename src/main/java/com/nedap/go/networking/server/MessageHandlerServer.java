package com.nedap.go.networking.server;

import com.nedap.go.networking.protocol.Protocol;
import com.nedap.go.networking.server.utils.ImproperMessageException;
import com.nedap.go.networking.server.utils.PlayerState;

/**
 * class for decoding incoming messages from the client.
 */
public class MessageHandlerServer {

  private final ClientHandler clientHandler;
  private PlayerState playerState;

  /**
   * Constructor of the message handler.
   *
   * @param clientHandler The ClientHandler object delegating to this Message Handler
   */
  MessageHandlerServer(ClientHandler clientHandler) {
    this.clientHandler = clientHandler;
    playerState = PlayerState.FRESH;
  }

  /**
   * Change the player's state as dictated from the server.
   *
   * @param playerState The new player state.
   */
  void setPlayerState(PlayerState playerState) {
    this.playerState = playerState;
  }

  void handleMessage(String message) throws ImproperMessageException {
    System.out.println(message);
    switch (playerState) {
      case FRESH -> handleInitialization(message);
      case PREGAME -> handlePreGame(message);
      case IN_QUEUE -> handleInQueue(message);
      case IN_GAME -> handleGame(message);
      default -> setPlayerState(PlayerState.FRESH);
    }
  }

  private void handleInQueue(String message) throws ImproperMessageException {
    if(message.equals(Protocol.LIST)){
      clientHandler.listReceived();
    }
    else{
      throw new ImproperMessageException(message
          + ": Not appropriate at this moment");
    }
  }

  private void handleGame(String message) throws ImproperMessageException {
    String[] messageArray = splitMessage(message);
    if (messageArray.length >= 1) {
      switch (messageArray[0]) {
        case Protocol.MOVE -> handleMove(messageArray[1]);
        case Protocol.PASS -> clientHandler.receivePass();
        case Protocol.RESIGN -> clientHandler.handleResign();
        default -> throw new ImproperMessageException(message
            + ": Not appropriate at this moment");
      }
    }
  }

  private void handleMove(String s) {
    String[] moveSplits = s.split(Protocol.ROW_COL_SEPARATOR);
    if (moveSplits.length > 1) {
      int column = Integer.parseInt(moveSplits[0]);
      int row = Integer.parseInt(moveSplits[1]);
      clientHandler.receiveMove(row, column);
    } else {
      int index = Integer.parseInt(moveSplits[0]);
      clientHandler.receiveMove(index);
    }
  }

  private void handlePreGame(String message) throws ImproperMessageException {
    String[] messageArray = splitMessage(message);
    if (messageArray.length == 1) {
      switch (messageArray[0]) {
        case Protocol.LIST -> clientHandler.listReceived();
        case Protocol.QUEUE -> clientHandler.queueReceived();
        default -> throw new ImproperMessageException(message
            + ": Not appropriate at this moment");
      }
    }
  }


  private void handleInitialization(String message) throws ImproperMessageException {
    String[] messageArray = splitMessage(message);
    if (messageArray.length > 1) {
      if (messageArray[0].equals(Protocol.LOGIN)) {
        clientHandler.receiveLogin(messageArray[1]);
      }else{
        throw new ImproperMessageException(message
            + ": Not appropriate at this moment");
      }
    }
  }

  private String[] splitMessage(String message) {
    return message.split(Protocol.SEPARATOR);
  }

  public PlayerState getPlayerState() {
    return playerState;
  }
}
