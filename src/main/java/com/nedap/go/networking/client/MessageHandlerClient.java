package com.nedap.go.networking.client;

import com.nedap.go.model.utils.InvalidMoveException;
import com.nedap.go.networking.protocol.Protocol;
import com.nedap.go.networking.server.utils.ImproperMessageException;
import com.nedap.go.networking.server.utils.PlayerState;
import java.util.Arrays;
import java.util.List;

/**
 * Class responsible for decoding incoming messages.
 *
 * <p>
 * Using the player state it only handles messages received at a proper moment.
 * </p>
 */
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

  void handleMessage(String message)
      throws ImproperMessageException, InvalidMoveException, ErrorReceivedException {
    switch (playerState) {
      case FRESH -> handleHandshake(message);
      case PREGAME -> handlePreGame(message);
      case IN_QUEUE -> handleInQueue(message);
      case IN_GAME -> handleGame(message);
      default -> setPlayerState(PlayerState.FRESH);
    }
  }

  private void handleHandshake(String message)
      throws ImproperMessageException, ErrorReceivedException {
    String[] messageArray = splitMessage(message);
    switch (messageArray[0]) {
      case Protocol.HELLO -> {
        if (messageArray.length > 1) {
          client.successfulConnection(messageArray[1]);
        } else {
          client.successfulConnection("No message");
        }
      }
      case Protocol.ACCEPTED -> handleAccept(messageArray);
      case Protocol.REJECTED -> client.logInStatus(false, messageArray[1]);
      case Protocol.ERROR -> throw new ErrorReceivedException(messageArray[1]);
      default -> throw new ImproperMessageException(message);
    }
  }

  private void handlePreGame(String message)
      throws ImproperMessageException, ErrorReceivedException {
    String[] messageArray = splitMessage(message);
    switch (messageArray[0]) {
      case Protocol.LIST -> client.receiveList((getPlayerList(messageArray)));
      case Protocol.QUEUED -> {
        client.receiveInQueue();
        setPlayerState(PlayerState.IN_QUEUE);
      }
      case Protocol.ERROR -> throw new ErrorReceivedException(messageArray[1]);
      default -> throw new ImproperMessageException(message);
    }
  }

  private void handleInQueue(String message)
      throws ImproperMessageException, ErrorReceivedException {
    String[] messageArray = splitMessage(message);
    switch (messageArray[0]) {
      case Protocol.LIST -> client.receiveList((getPlayerList(messageArray)));
      case Protocol.NEW_GAME -> {
        handleNewGame(messageArray);
        setPlayerState(PlayerState.IN_GAME);
      }
      case Protocol.MAKE_MOVE -> {
        // Not used in this implementation. Just ignored.
      }
      case Protocol.ERROR -> throw new ErrorReceivedException(messageArray[1]);
      default -> throw new ImproperMessageException(message);
    }
  }

  private void handleGame(String message)
      throws InvalidMoveException, ImproperMessageException, ErrorReceivedException {
    String[] messageArray = splitMessage(message);
    switch (messageArray[0]) {
      case Protocol.MOVE -> handleMove(messageArray);
      case Protocol.PASS -> handlePass(messageArray);
      case Protocol.GAME_OVER -> handleGameOver(messageArray);
      case Protocol.ERROR -> throw new ErrorReceivedException(messageArray[1]);
      case Protocol.MAKE_MOVE -> {
        // Not used in this implementation. Just ignored.
      }
      default -> throw new ImproperMessageException(message + ": Not appropriate at this moment");
    }
  }

  private void handleAccept(String[] messageArray) {
    client.logInStatus(true, messageArray[1]);
    setPlayerState(PlayerState.PREGAME);
  }


  private List<String> getPlayerList(String[] messageArray) {
    List<String> listOfPlayersWithCommand = Arrays.asList(messageArray);
    return listOfPlayersWithCommand.subList(1, listOfPlayersWithCommand.size() - 1);
  }


  private void handleNewGame(String[] messageArray) throws ImproperMessageException {
    if (messageArray.length == 3) {
      String[] players = messageArray[1].split(",");
      String player1Name = players[0];
      String player2Name = players[1];
      try {
        int boardDim = Integer.parseInt(messageArray[2]);
        client.newGame(player1Name, player2Name, boardDim);
      } catch (NumberFormatException e) {
        throw new ImproperMessageException(messageArray[0] + ": Argument 3 must be integer");
      }
    } else {
      throw new ImproperMessageException(messageArray[0] + ": Needs 3 arguments");
    }
  }


  private void handleGameOver(String[] messageArray)
      throws ImproperMessageException, ErrorReceivedException {
    switch (messageArray[1]) {
      case Protocol.DRAW -> client.receiveDraw();
      case Protocol.WINNER -> client.receiveWinner(messageArray[2]);
      case Protocol.ERROR -> throw new ErrorReceivedException(messageArray[1]);
      default -> throw new ImproperMessageException(
          "Only DRAW or WINNER " + "are allowed as arguments to GAME OVER!");
    }
    playerState = PlayerState.PREGAME;
  }

  private void handlePass(String[] messageArray) throws InvalidMoveException {
    String color = messageArray[1];
    checkColor(color);
    client.receivePass(color);
  }

  private void handleMove(String[] messageArray) throws InvalidMoveException {
    if (messageArray.length != 3) {
      throw new InvalidMoveException("Server sent invalid move");
    }
    try {
      int moveIndex = Integer.parseInt(messageArray[1]);
      String moveColor = messageArray[2];
      checkColor(moveColor);
      client.receiveMove(moveIndex, moveColor);
    } catch (NumberFormatException e) {
      throw new InvalidMoveException();
    }

  }

  private void checkColor(String moveColor) throws InvalidMoveException {
    if (!moveColor.equals(Protocol.BLACK) && !moveColor.equals(Protocol.WHITE)) {
      throw new InvalidMoveException("Invalid stone color");
    }
  }

  private String[] splitMessage(String message) {
    return message.split(Protocol.SEPARATOR);
  }
}
