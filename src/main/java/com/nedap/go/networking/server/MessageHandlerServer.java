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
    switch (playerState) {
      case FRESH -> handleInitialization(message);
      case PREGAME -> handlePreGame(message);
      case IN_QUEUE -> handleInQueue(message);
      case IN_GAME -> handleGame(message);
      default -> setPlayerState(PlayerState.FRESH);
    }
  }

  private void handleInQueue(String message) throws ImproperMessageException {
    String[] messageArray = message.split(Protocol.SEPARATOR);
    if(messageArray[0].equals(Protocol.LIST)){
      clientHandler.listReceived();
    } else if (messageArray[0].equals(Protocol.QUEUE)) {
      clientHandler.deQueueReceived();
      setPlayerState(PlayerState.PREGAME);
    } else{
      throw new ImproperMessageException(message);
    }
  }

  private void handleGame(String message) throws ImproperMessageException {
    String[] messageArray = splitMessage(message);
      switch (messageArray[0]) {
        case Protocol.MOVE -> handleMove(messageArray[1]);
        case Protocol.PASS -> clientHandler.receivePass();
        case Protocol.RESIGN -> clientHandler.handleResign();
        case Protocol.ERROR -> {}
        default -> throw new ImproperMessageException(message);
      }
  }

  private void handleMove(String message) {
    String[] moveSplits = message.split(Protocol.ROW_COL_SEPARATOR);
    try{
      if (moveSplits.length > 1) {
        int column = Integer.parseInt(moveSplits[0]);
        int row = Integer.parseInt(moveSplits[1]);
        clientHandler.receiveMove(row, column);
      } else {
        int index = Integer.parseInt(moveSplits[0]);
        clientHandler.receiveMove(index);
      }
    }catch (NumberFormatException e){
      //just send and invalid move
      clientHandler.receiveMove(10000000);

    }
  }

  private void handlePreGame(String message) throws ImproperMessageException {
    String[] messageArray = splitMessage(message);
      switch (messageArray[0]) {
        case Protocol.LIST -> clientHandler.listReceived();
        case Protocol.QUEUE -> clientHandler.queueReceived();
        case Protocol.ERROR -> {}
        default -> throw new ImproperMessageException(message);
      }

  }


  private void handleInitialization(String message) throws ImproperMessageException {
    String[] messageArray = splitMessage(message);
      switch (messageArray[0]){
        case Protocol.LOGIN -> clientHandler.receiveLogin(messageArray[1]);
        case Protocol.ERROR -> {}
        default -> throw new ImproperMessageException(message);
      }
  }

  private String[] splitMessage(String message) {
    return message.split(Protocol.SEPARATOR);
  }

  public PlayerState getPlayerState() {
    return playerState;
  }
}
