package com.nedap.go.networking.server;


import com.nedap.go.model.GoMove;
import com.nedap.go.model.Stone;
import com.nedap.go.model.utils.InvalidMoveException;
import com.nedap.go.networking.SocketServer;
import com.nedap.go.networking.server.utils.GameNotFoundException;
import com.nedap.go.networking.server.utils.NotAppropriateStoneException;
import com.nedap.go.networking.server.utils.NotYourTurnException;
import com.nedap.go.networking.server.utils.PlayerNotFoundException;
import com.nedap.go.networking.server.utils.PlayerState;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

/**
 * Class handling and answering requests from clients playing a game.
 */
public class GameServer extends SocketServer {

  private static int boardDim;
  private final List<ClientHandler> listOfClients;
  private final Queue<ClientHandler> inQueueClients;
  private final List<ServerGameAdapter> listOfGames;

  /**
   * Constructs a new GameServer.
   *
   * @param port the port to listen on
   * @throws IOException if the server socket cannot be created, for example, because the port is
   *                     already bound.
   */
  public GameServer(int port) throws IOException {
    super(port);
    listOfClients = new ArrayList<>();
    inQueueClients = new LinkedList<>();
    listOfGames = new ArrayList<>();
  }

  /**
   * The main method of the server.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    runServer();
  }

  private static void runServer() {
    Scanner sc = new Scanner(System.in);
    GameServer gameServer;
    while (true) {
      System.out.println("Please provide a boardSize");
      boardDim = sc.nextInt();
      System.out.println("Please provide a port");
      int portNumber = sc.nextInt();
      try {
        gameServer = new GameServer(portNumber);
        System.out.println("Connecting via port: " + gameServer.getPort());
        gameServer.acceptConnections();
      } catch (IOException e) {
        System.out.println("Could not connect to port" + portNumber);
      }
    }
  }
  //        CONNECTION METHODS

  /**
   * Returns the port on which this server is listening for connections.
   *
   * @return the port on which this server is listening for connections
   */
  @Override
  public int getPort() {
    return super.getPort();
  }

  /**
   * Accepts connections and starts a new thread for each connection. This method will block until
   * the server socket is closed, for example by invoking closeServerSocket.
   *
   * @throws IOException if an I/O error occurs when waiting for a connection
   */
  @Override
  public void acceptConnections() throws IOException {
    super.acceptConnections();
  }

  /**
   * Closes the server socket. This will cause the server to stop accepting new connections. If
   * called from a different thread than the one running acceptConnections, then that thread will
   * return from acceptConnections.
   */
  @Override
  public synchronized void close() {
    super.close();
  }
  //--------------------LIST HANDLING

  /**
   * Adds a ClientHandler to the list of clients.
   *
   * @param clientHandler the ClientHandler object to be added.
   */
  public synchronized void addClient(ClientHandler clientHandler) {
    boolean nameOk = true;
    for (ClientHandler handler : listOfClients) {
      if (clientHandler.getUsername() != null && clientHandler.getUsername()
          .equalsIgnoreCase(handler.getUsername())) {
        nameOk = false;
        break;
      }
    }
    if (nameOk) {
      listOfClients.add(clientHandler);
    }
    clientHandler.sendLogin(nameOk, clientHandler.getUsername());
  }

  /**
   * Removes a ClientHandler to the list of clients.
   *
   * @param clientHandler the ClientHandler object to be removed.
   */
  public synchronized void removeClient(ClientHandler clientHandler) {
    listOfClients.remove(clientHandler);
    inQueueClients.remove(clientHandler);
    ServerGameAdapter gameToEnd;
    if (clientHandler.getPlayerState() == PlayerState.IN_GAME) {
      try {
        gameToEnd = findGame(clientHandler);
        sendWinner(gameToEnd, gameToEnd.getOtherPlayer(clientHandler));
        listOfGames.remove(gameToEnd);
      } catch (GameNotFoundException e) {
        System.out.println(e.getMessage());
      }

    }
  }

  /**
   * Add to matchmaking queue.
   *
   * @param clientHandler The clientHandler of the client asking to join the queue.
   */
  public synchronized void addInQueue(ClientHandler clientHandler) {
    inQueueClients.add(clientHandler);
    clientHandler.sendQueued();
    if (inQueueClients.size() > 1) {
      try {
        startGame();
      } catch (PlayerNotFoundException e) {
        sendError(clientHandler, e.getMessage());
        System.out.println(e.getMessage());
      }
    }
  }
  //--------------------RECEIVED MESSAGE HANDLERS

  /**
   * Creates a new connection handler for the given socket.
   *
   * @param socket the socket for the connection
   */
  @Override
  protected void handleConnection(Socket socket) {
    try {

      ClientHandler clientHandler = new ClientHandler(this);
      ServerConnection serverConnection = new ServerConnection(socket, clientHandler);
      clientHandler.setServerConnection(serverConnection);
      serverConnection.start();
      System.out.println("New connection");
      Thread.sleep(100);
      clientHandler.sayHello();
    } catch (IOException e) {
      System.out.println("Sorry! Could not connect.");
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Handle LIST command which displays a list of logged in players.
   *
   * @param clientHandler The clientHandler of the client asking for the list.
   */
  public void handleList(ClientHandler clientHandler) {
    clientHandler.sendList(listOfClients);
  }

  /**
   * Method that handles a move based on a single index.
   *
   * @param clientHandler The clientHandler of the client sending the move.
   * @param moveIndex     The index of the move.
   */
  public void handleMove(ClientHandler clientHandler, int moveIndex) {
    try {
      ServerGameAdapter game = findGame(clientHandler);
      GoMove move = game.newMove(moveIndex, clientHandler);
      sendMove(game.getClients(), move.getIndex(), move.getPlayer().getStone());
      sendTurn(game.getClients(), game.getTurn());
    } catch (InvalidMoveException | GameNotFoundException | NotYourTurnException e) {
      sendError(clientHandler, e.getMessage());
    }
  }

  /**
   * Method that handles the row, column type moves.
   *
   * @param clientHandler The client handler of the client sending the move.
   * @param row           The row index of the move.
   * @param col           The column index of the move.
   */
  public void handleMove(ClientHandler clientHandler, int row, int col) {
    try {
      ServerGameAdapter game = findGame(clientHandler);
      GoMove move = game.newMove(row, col, clientHandler);
      sendMove(game.getClients(), move.getIndex(), move.getPlayer().getStone());
      sendTurn(game.getClients(), game.getTurn());
    } catch (InvalidMoveException | NotYourTurnException | GameNotFoundException e) {
      sendError(clientHandler, e.getMessage());
    }
  }

  /**
   * Handle passing moves.
   *
   * @param clientHandler The handler of the client sending the move.
   */
  public void handlePass(ClientHandler clientHandler) {
    try {
      ServerGameAdapter game = findGame(clientHandler);
      GoMove move = game.passMove(clientHandler);
      sendPass(game.getClients(), move.getPlayer().getStone());
      if (game.isGameOver()) {
        game.endGame();
      } else {
        sendTurn(game.getClients(), game.getTurn());
      }
    } catch (InvalidMoveException | GameNotFoundException | NotYourTurnException e) {
      sendError(clientHandler, e.getMessage());
    }
  }

  /**
   * Handle a resignation from a game.
   *
   * @param clientHandler The handler of the client giving the resignation command
   */
  public void handleResign(ClientHandler clientHandler) {
    try {
      ServerGameAdapter game = findGame(clientHandler);
      game.endGameOnResign(clientHandler);
    } catch (GameNotFoundException e) {
      sendError(clientHandler, e.getMessage());
    }
  }

  //--------------------MESSAGE SENDERS
  private void sendPass(List<ClientHandler> clients, Stone stone) {
    for (ClientHandler clientHandler : clients) {
      try {
        clientHandler.sendPass(stone);
      } catch (NotAppropriateStoneException e) {
        System.out.println(e.getMessage());
        sendError(clientHandler, e.getMessage());
      }
    }
  }

  private void sendMove(List<ClientHandler> clients, int moveIndex, Stone stone) {
    for (ClientHandler clientHandler : clients) {
      try {
        clientHandler.sendMove(moveIndex, stone);
      } catch (NotAppropriateStoneException e) {
        System.out.println(e.getMessage());
        sendError(clientHandler, e.getMessage());
      }
    }
  }

  private void sendTurn(List<ClientHandler> clients, OnlinePlayer turn) {
    for (ClientHandler clientHandler : clients) {
      clientHandler.sendTurn(turn.getName());
    }
  }

  /**
   * Informs players of a game ending with a winner.
   *
   * @param game   The game ending.
   * @param winner The winner player.
   */
  public void sendWinner(ServerGameAdapter game, OnlinePlayer winner) {
    for (ClientHandler clientHandler : game.getClients()) {
      clientHandler.sendWinner(winner);
    }
    listOfGames.remove(game);
  }

  /**
   * Informs the players of a game ending in a draw.
   *
   * @param game The game being finished.
   */
  public void sendDraw(ServerGameAdapter game) {
    for (ClientHandler clientHandler : game.getClients()) {
      clientHandler.sendDraw();
    }
    listOfGames.remove(game);
  }

  /**
   * Sends errors.
   *
   * @param clientHandler The Clients handler provoking the error.
   * @param errorMessage  The message describing the error.
   */
  public void sendError(ClientHandler clientHandler, String errorMessage) {
    clientHandler.sendError(errorMessage);
  }

  //--------------------UTILITY METHODS
  private void startGame() throws PlayerNotFoundException {
    ClientHandler player1 = inQueueClients.poll();
    ClientHandler player2 = inQueueClients.poll();
    ServerGameAdapter game = new ServerGameAdapter(player1, player2, this, boardDim);
    listOfGames.add(game);
    if (player1 == null || player2 == null) {
      throw new PlayerNotFoundException("Could not find enough players in queue");
    }
    player1.sendStartGame(player1.getUsername(), player2.getUsername(), boardDim);
    player2.sendStartGame(player1.getUsername(), player2.getUsername(), boardDim);

    sendTurn(game.getClients(), game.getTurn());
  }

  private ServerGameAdapter findGame(ClientHandler clientHandler) throws GameNotFoundException {
    for (ServerGameAdapter game : listOfGames) {
      if (game.getClients().contains(clientHandler)) {
        return game;
      }
    }
    throw new GameNotFoundException();
  }

  public synchronized void removeFromQueue(ClientHandler clientHandler) {
    inQueueClients.remove(clientHandler);
  }
}
