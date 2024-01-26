package com.nedap.go.networking.server;

import com.nedap.go.networking.SocketConnection;
import com.nedap.go.networking.protocol.Protocol;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Set;

public class ServerConnection extends SocketConnection {
    private final ClientHandler clientHandler;

    private final MessageHandler messageHandler;


    /**
     * Create a new SocketConnection. This is not meant to be used directly.
     * Instead, the SocketServer and SocketClient classes should be used.
     *
     * @param socket        the socket for this connection
     * @param clientHandler The clientHandler that communicates this connection
     *                      to the server.
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
    public void start(){
        super.start();
    }

    public boolean sayHello(Set<String> runExtensions){
        StringBuilder sb = new StringBuilder(Protocol.HELLO);
        sb.append(Protocol.SEPARATOR);
        sb.append(Protocol.SERVER_DESCRIPTION);
//        if(runExtensions.size()>0)
        for (String s: runExtensions) {
            sb.append(Protocol.SEPARATOR);
            sb.append(s);
        }
        return sendMessage(sb.toString());
    }



    /**
     * Handles a message received from the connection.
     *
     * @param message the message received from the connection.
     */
    @Override
    public void handleMessage(String message) {
        messageHandler.handleMessage(message);
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

    public void sendLogin(boolean nameOK) {
        if(nameOK){
            sendMessage(Protocol.ACCEPTED);
            messageHandler.setPlayerState(PlayerState.PREGAME);
        }
        else sendMessage(Protocol.REJECTED);
    }

    public void sendList(List<ClientHandler> listOfClients) {
        StringBuilder sb = new StringBuilder(Protocol.LIST);

        for(ClientHandler client: listOfClients){
            sb.append(Protocol.SEPARATOR);
            sb.append(client.getUsername());
        }
        super.sendMessage(sb.toString());
    }

    public void startGame(String usernamePlayer1, String usernamePlayer2) {
        sendMessage(Protocol.NEW_GAME + Protocol.SEPARATOR + usernamePlayer1
                            + Protocol.SEPARATOR + usernamePlayer2);
        messageHandler.setPlayerState(PlayerState.INGAME);
    }

    public void sendMove(int moveIndex) {
        sendMessage(Protocol.MOVE + Protocol.SEPARATOR + moveIndex);
    }

    public void sendGameOver(String message) {
        messageHandler.setPlayerState(PlayerState.PREGAME);
        sendMessage(Protocol.GAME_OVER + Protocol.SEPARATOR + message);
    }

    public void sendMove(int row, int col) {
        sendMessage(Protocol.MOVE + Protocol.SEPARATOR + row
            + Protocol.ROW_COL_SEPARATOR + col);
    }
}
