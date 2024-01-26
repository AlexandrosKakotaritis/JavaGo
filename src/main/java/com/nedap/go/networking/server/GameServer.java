package com.nedap.go.networking.server;


import com.nedap.go.model.utils.InvalidMoveException;
import com.nedap.go.networking.SocketServer;
import com.nedap.go.networking.protocol.Protocol;
import com.nedap.go.tui.QuitGameException;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class GameServer extends SocketServer {
    private List<ClientHandler> listOfClients;
    private Queue<ClientHandler> inQueueClients;
    private List<ServerGameLogic> listOfGames;
    private Set runExtensions;
    private static final Set supportedExtensions = null;
    private int gamesStarted;
    private static int boardDim;

    /**
     * Constructs a new GameServer
     * @param port the port to listen on
     * @throws IOException if the server socket cannot be created, for example, because the port is already bound.
     */
    public GameServer(int port) throws IOException {
        super(port);
        listOfClients = new ArrayList<>();
        inQueueClients = new LinkedList<>();
        listOfGames = new ArrayList<>();
        gamesStarted = 0;
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
     * Accepts connections and starts a new thread for each connection.
     * This method will block until the server socket is closed, for example by invoking closeServerSocket.
     *
     * @throws IOException if an I/O error occurs when waiting for a connection
     */
    @Override
    public void acceptConnections() throws IOException {
        super.acceptConnections();
    }

    /**
     * Closes the server socket. This will cause the server to stop accepting new connections.
     * If called from a different thread than the one running acceptConnections, then that thread will return from
     * acceptConnections.
     */
    @Override
    public synchronized void close() {
        super.close();
    }

    public void helloReceived(ClientHandler client, String information) {
        runExtensions = new HashSet<>();
        String[] splits = information.split(Protocol.SEPARATOR);
        if(splits.length > 1){
            for (int i = 1; i < splits.length ; i++) {
                if(supportedExtensions.contains(splits[i]))
                    runExtensions.add(splits[i]);
            }
        }
        client.sayHello(runExtensions);
    }

    /**
     * Adds a ClientHandler to the list of clients.
     * @param clientHandler the ClientHandler object to be added.
     */
    public synchronized boolean addClient(ClientHandler clientHandler){
        boolean nameOK = true;
        for(ClientHandler handler: listOfClients){
            if (clientHandler.getUsername() != null && clientHandler.getUsername()
                    .equalsIgnoreCase(handler.getUsername())){
                nameOK = false;
                break;
            }
        }
        if(nameOK) listOfClients.add(clientHandler);
        clientHandler.sendLogin(nameOK);
        return nameOK;
    }

    /**
     * Removes a ClientHandler to the list of clients.
     * @param clientHandler the ClientHandler object to be removed.
     */
    public synchronized void removeClient(ClientHandler clientHandler){
        listOfClients.remove(clientHandler);
        boolean inGame = !inQueueClients.remove(clientHandler);
        ServerGameLogic gameToEnd = null;
        if(inGame){
            for(ServerGameLogic game: listOfGames){
                if(game.getClients().contains(clientHandler)){
                    gameToEnd = game;
                    break;
                }
            }
            if(gameToEnd != null)
                gameOver(gameToEnd, clientHandler.getUsername() + " disconnected. "
                    + gameToEnd.getOtherPlayer(clientHandler).getUsername() + " is the Winner!");
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
        } catch (IOException e) {
            System.out.println("Sorry! Could not connect.");
        }
    }

    public void listReceived(ClientHandler clientHandler) {
        clientHandler.sendList(listOfClients);
    }

    public synchronized void addInQueue(ClientHandler clientHandler) {
        inQueueClients.add(clientHandler);
        if(inQueueClients.size()>1){
            startGame();
        }
    }

    private void startGame() {
        ClientHandler player1 = inQueueClients.poll();
        ClientHandler player2 = inQueueClients.poll();
        listOfGames.add(new ServerGameLogic(player1,
                                            player2, this, boardDim));
        player1.startGame(player1.getUsername(), player2.getUsername());
        player2.startGame(player1.getUsername(), player2.getUsername());
    }

    public synchronized void removeFromQueue(ClientHandler clientHandler) {
        inQueueClients.remove(clientHandler);
    }

    private ServerGameLogic findGame(ClientHandler clientHandler) {
        for (ServerGameLogic game : listOfGames) {
            if (game.getClients().contains(clientHandler)) {
                return game;
            }
        }
        return null;
    }

    public void handleMove(ClientHandler clientHandler, int moveIndex) {
            ServerGameLogic game = findGame(clientHandler);
            if (game != null) {
                try {
                    if (game.newMove(moveIndex, clientHandler))
                        sendMove(game.getClients(), moveIndex);
                } catch (InvalidMoveException e) {
                    sendError(clientHandler, "Invalid Move!");
                }
            }
    }

    public void handleMove(ClientHandler clientHandler, int row, int col) {
        ServerGameLogic game = findGame(clientHandler);
        if (game != null) {
            try {
                if (game.newMove(row, col, clientHandler))
                    sendMove(game.getClients(), row, col);
            } catch (InvalidMoveException e) {
                sendError(clientHandler, "Invalid Move!");
            }
        }
    }

    public void handlePass(ClientHandler clientHandler) {
        ServerGameLogic game = findGame(clientHandler);
        if (game != null) {
            try {
                if (game.passMove(clientHandler))
                    sendPass(game.getClients());
            } catch (InvalidMoveException e) {
                sendError(clientHandler, "Invalid Move!");
            }
        }
    }

    private void sendPass(List<ClientHandler> clients) {
        for(ClientHandler clientHandler: clients){
            clientHandler.sendPass();
        }
    }

    private void sendMove(List<ClientHandler> clients, int row, int col) {
        for(ClientHandler clientHandler: clients){
            clientHandler.sendMove(row, col);
        }
    }

    private void sendMove(List<ClientHandler> clients, int moveIndex) {
        for(ClientHandler clientHandler: clients){
            clientHandler.sendMove(moveIndex);
        }
    }

    public void gameOver(ServerGameLogic game, String message) {
        for(ClientHandler clientHandler: game.getClients()){
            clientHandler.sendGameOver(message);
        }
        listOfGames.remove(game);
    }

    public void sendError(ClientHandler clientHandler, String errorMessage) {
        clientHandler.sendError(errorMessage);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        GameServer gameServer;
        while(true) {
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
}
