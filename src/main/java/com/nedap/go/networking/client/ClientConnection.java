package com.nedap.go.networking.client;

import com.nedap.go.networking.server.utils.ImproperMessageException;
import com.nedap.go.networking.server.utils.PlayerState;
import java.io.IOException;
import java.net.InetAddress;
import com.nedap.go.networking.SocketConnection;
import com.nedap.go.networking.protocol.Protocol;

public class ClientConnection extends SocketConnection {

    private GameClient gameClient;
    private MessageHandlerClient messageHandler;
    /**
     * Make a new TCP connection to the given host and port.
     * The receiving thread is not started yet. Call start on the returned SocketConnection to start receiving messages.
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
     * @param gameClient The chatClient object.
     */
    protected void setGameClient(GameClient gameClient){
        this.gameClient = gameClient;
        messageHandler = new MessageHandlerClient(gameClient);
    }

    /**
     * Send username through the socket to the server
     * @param username The username to be sent
     */
    public void sendUsername(String username){
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
      } catch (ImproperMessageException e) {
          System.out.println(e.getMessage());
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
    protected void close(){
        super.close();
    }

    public void sendQueue() {
        sendMessage(Protocol.QUEUE);
    }

    public void sendError(String message) {
        sendMessage(Protocol.ERROR + Protocol.SEPARATOR + message);
    }
}
