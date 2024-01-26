package com.nedap.go.networking.server;


import com.nedap.go.model.GoMove;
import com.nedap.go.model.Stone;
import com.nedap.go.model.utils.InvalidMoveException;
import com.nedap.go.networking.SocketServer;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;


public class GameServer extends SocketServer {
  private static int boardDim;
  private final List<ClientHandler> listOfClients;
  private final Queue<ClientHandler> inQueueClients;
  private final List<ServerGameAdapter> listOfGames;

  /**
   * Constructs a new GameServer
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

  public static void main(String[] args) {
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

  /**
   * Adds a ClientHandler to the list of clients.
   *
   * @param clientHandler the ClientHandler object to be added.
   */
  public synchronized boolean addClient(ClientHandler clientHandler) {
    boolean nameOK = true;
    for (ClientHandler handler : listOfClients) {
      if (clientHandler.getUsername() != null && clientHandler.getUsername()
          .equalsIgnoreCase(handler.getUsername())) {
        nameOK = false;
        break;
      }
    }
    if (nameOK) {
      listOfClients.add(clientHandler);
    }
    clientHandler.sendLogin(nameOK, clientHandler.getUsername());
    return nameOK;
  }

  /**
   * Removes a ClientHandler to the list of clients.
   *
   * @param clientHandler the ClientHandler object to be removed.
   */
  public synchronized void removeClient(ClientHandler clientHandler) {
    listOfClients.remove(clientHandler);
    boolean inGame = !inQueueClients.remove(clientHandler);
    ServerGameAdapter gameToEnd = null;
    if (inGame) {
      for (ServerGameAdapter game : listOfGames) {
        if (game.getClients().contains(clientHandler)) {
          gameToEnd = game;
          break;
        }
      }
      if (gameToEnd != null) {
        gameOver(gameToEnd,
            clientHandler.getUsername() + " disconnected. " + gameToEnd.getOtherPlayer(
                clientHandler).getUsername() + " is the Winner!");
      }
    }
  }

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
      clientHandler.sayHello();
    } catch (IOException e) {
      System.out.println("Sorry! Could not connect.");
    }
  }

  public void listReceived(ClientHandler clientHandler) {
    clientHandler.sendList(listOfClients);
  }

  public synchronized void addInQueue(ClientHandler clientHandler) {
    inQueueClients.add(clientHandler);
    clientHandler.sendQeued();
    if (inQueueClients.size() > 1) {
      startGame();
    }
  }

  private void startGame() {
    ClientHandler player1 = inQueueClients.poll();
    ClientHandler player2 = inQueueClients.poll();
    listOfGames.add(new ServerGameAdapter(player1, player2, this, boardDim));
    player1.startGame(player1.getUsername(), player2.getUsername(), boardDim);
    player2.startGame(player1.getUsername(), player2.getUsername(), boardDim);
  }

  public synchronized void removeFromQueue(ClientHandler clientHandler) {
    inQueueClients.remove(clientHandler);
  }

  private ServerGameAdapter findGame(ClientHandler clientHandler) {
    for (ServerGameAdapter game : listOfGames) {
      if (game.getClients().contains(clientHandler)) {
        return game;
      }
    }
    return null;
  }

  public void handleMove(ClientHandler clientHandler, int moveIndex) {
    ServerGameAdapter game = findGame(clientHandler);
    if (game != null) {
      try {
        GoMove move = game.newMove(moveIndex, clientHandler);
        sendMove(game.getClients(), move.getIndex(), move.getPlayer().getStone());
        sendTurn(game.getClients(), game.getTurn());
      } catch (InvalidMoveException e) {
        sendError(clientHandler, e.getMessage());
      } catch (NotYourTurnException e) {
        sendError(clientHandler, e.getMessage());
      }
    }
  }

  public void handleMove(ClientHandler clientHandler, int row, int col) {
    ServerGameAdapter game = findGame(clientHandler);
    if (game != null) {
      try {
        GoMove move = game.newMove(row, col, clientHandler);
        sendMove(game.getClients(), move.getIndex(), move.getPlayer().getStone());
        sendTurn(game.getClients(), game.getTurn());
      } catch (InvalidMoveException e) {
        sendError(clientHandler, e.getMessage());
      } catch (NotYourTurnException e) {
        sendError(clientHandler, e.getMessage());
      }
    }
  }

  public void handlePass(ClientHandler clientHandler) {
    ServerGameAdapter game = findGame(clientHandler);
    if (game != null) {
      try {
        GoMove move = game.passMove(clientHandler);
        sendPass(game.getClients(), move.getPlayer().getStone());
        if(game.isGameOver()){
          game.endGame();
        }else{
          sendTurn(game.getClients(), game.getTurn());
        }

      } catch (InvalidMoveException e) {
        sendError(clientHandler, e.getMessage());
      } catch (NotYourTurnException e) {
        sendError(clientHandler, e.getMessage());
      }
    }
  }

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

  public void gameOver(ServerGameAdapter game, String message) {
    for (ClientHandler clientHandler : game.getClients()) {
      clientHandler.sendGameOver(message);
    }
    listOfGames.remove(game);
  }

  public void sendError(ClientHandler clientHandler, String errorMessage) {
    clientHandler.sendError(errorMessage);
  }

  public void handleResign(ClientHandler clientHandler) {
    ServerGameAdapter game = findGame(clientHandler);
    if (game != null) {
      game.endGame(clientHandler);
    }
  }
}
