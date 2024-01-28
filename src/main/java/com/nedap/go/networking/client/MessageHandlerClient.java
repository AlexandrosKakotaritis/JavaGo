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
      case Protocol.ACCEPTED -> handleAccept(messageArray);
      case Protocol.REJECTED -> client.logInStatus(false, messageArray[1]);
      default -> throw new ImproperMessageException(message
          + ": Not appropriate at this moment");
    }
  }

  private void handleAccept(String[] messageArray) {
    client.logInStatus(true, messageArray[1]);
    setPlayerState(PlayerState.PREGAME);
  }

  private void handlePreGame(String message) throws ImproperMessageException {
    String[] messageArray = splitMessage(message);
    switch (messageArray[0]) {
      case Protocol.LIST -> client.receiveList((getPlayerList(messageArray)));
      case Protocol.QUEUED -> {
        client.receiveInQueue();
        setPlayerState(PlayerState.IN_QUEUE);
      }
      default -> throw new ImproperMessageException(message
          + ": Not appropriate at this moment");
    }
  }

  private List<String> getPlayerList(String[] messageArray) {
    List<String> listOfPlayersWithCommand = Arrays.asList(messageArray);
    return listOfPlayersWithCommand.subList(1, listOfPlayersWithCommand.size() - 1);
  }

  private void handleInQueue(String message) throws ImproperMessageException {
    String[] messageArray = splitMessage(message);
    switch (messageArray[0]) {
      case Protocol.LIST -> client.receiveList((getPlayerList(messageArray)));
      case Protocol.NEW_GAME -> {
        handleNewGame(messageArray);
        setPlayerState(PlayerState.IN_GAME);
      }
    }
  }

  private void handleNewGame(String[] messageArray) throws ImproperMessageException {
    if(messageArray.length == 4){
      String player1Name = messageArray[1];
      String player2Name = messageArray[2];
      try {
        int boardDim = Integer.parseInt(messageArray[3]);
        client.newGame(player1Name, player2Name, boardDim);
      } catch (NumberFormatException e) {
        throw new ImproperMessageException(messageArray[0]
            + ": Argument 3 must be integer");
      }
    }
    else{
      throw new ImproperMessageException(messageArray[0]
          + ": Needs 3 arguments");
    }
  }

  private void handleGame(String message) {

  }

  private String[] splitMessage(String message) {
    return message.split(Protocol.SEPARATOR);
  }
}
