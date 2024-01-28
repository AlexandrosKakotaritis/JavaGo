package com.nedap.go.networking.client;

import com.nedap.go.networking.protocol.Protocol;
import com.nedap.go.networking.server.utils.ImproperMessageException;
import com.nedap.go.networking.server.utils.PlayerState;
import java.util.Arrays;
import java.util.List;

public class MessageHandlerClient {

  private final GameClient client;
  private PlayerState playerState;

  MessageHandlerClient(GameClient client) {
    this.client = client;
    this.playerState = PlayerState.FRESH;
  }

  void setPlayerState(PlayerState playerState) {
    this.playerState = playerState;
  }

  void handleMessage(String message) throws ImproperMessageException {
    switch (playerState) {
      case FRESH -> handleHandshake(message);
      case PREGAME -> handlePreGame(message);
      case IN_QUEUE -> handleInQueue(message);
      case IN_GAME -> handleGame(message);
      default -> setPlayerState(PlayerState.FRESH);
    }
  }

  private void handleHandshake(String message) throws ImproperMessageException {
    String[] messageArray = splitMessage(message);
    switch (messageArray[0]) {
      case Protocol.HELLO -> client.successfulConnection(messageArray[1]);
      case Protocol.ACCEPTED -> client.logInStatus(true, messageArray[1]);
      case Protocol.REJECTED -> client.logInStatus(false, messageArray[1]);
      default -> throw new ImproperMessageException(message
          + ": Not appropriate at this moment");
    }
  }

  private void handlePreGame(String message) throws ImproperMessageException {
    String[] messageArray = splitMessage(message);
    switch (messageArray[0]) {
      case Protocol.LIST -> client.receiveList((getPlayerList(messageArray)));
      case Protocol.QUEUED -> client.receiveInQueue();
      default -> throw new ImproperMessageException(message
          + ": Not appropriate at this moment");
    }
  }

  private List<String> getPlayerList(String[] messageArray) {
    List<String> listOfPlayersWithCommand = Arrays.asList(messageArray);
    return listOfPlayersWithCommand.subList(1, listOfPlayersWithCommand.size() - 1);
  }

  private void handleInQueue(String message) {
    String[] messageArray = splitMessage(message);
    switch (messageArray[0]) {
      case Protocol.LIST -> client.receiveList((getPlayerList(messageArray)));
      case Protocol.NEW_GAME -> handleNewGame(messageArray);
    }
  }

  private void handleNewGame(String[] messageArray) {
    if(messageArray.length == 3){

    }
  }

  private void handleGame(String message) {

  }

  private String[] splitMessage(String message) {
    return message.split(Protocol.SEPARATOR);
  }
}
