package com.nedap.go.networking.client;

import com.nedap.go.model.GoGame;
import com.nedap.go.model.GoMove;
import com.nedap.go.model.Player;
import com.nedap.go.model.Stone;
import com.nedap.go.model.utils.InvalidMoveException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.InetAddress;
import java.util.List;
import java.util.Scanner;

/**
 * The class of the TUI used for the chat application
 */
public class GameClientTUI implements ClientListener {
    private final Scanner sc;
    PrintWriter output;
    Reader input;
    private GameClient client;
    private LogListener log;
    private String serverName = "localhost";
    private int portNumber = 8888;
    private boolean logInSuccessful;
    private boolean isSystemOut;
    private static final String QUIT = "quit";


    public GameClientTUI(Reader input, PrintWriter output) {
        this.output = output;
        this.input = input;
        sc = new Scanner(input);
    }
    public GameClientTUI(){
        this(new InputStreamReader(System.in), new PrintWriter(System.out));
        isSystemOut = true;
    }

    /**
     * The run method of the TUI
     */
    public void initializeConnection(){
        initializeClient(serverName, portNumber);
        sendUsername();
        String playerType = selectPlayerType();
        matchMakingMenu();
        boolean run = true;
        while(run){
            String message = sc.nextLine();
            if (message.equals(QUIT))
                run = false;
            log.yourMessage(message);
        }
        client.close();
    }

    private void matchMakingMenu() {
        String matchmaking = """
            Get ready for a Game:
                    1. Find a Game.
                    2. Quit.
            """;
        switch (sc.nextInt()) {
            case 1 -> startQueueing();
            case 2 -> System.exit(0);
            default -> {
                println("Not a valid choice");
                println("");
                matchMakingMenu();
            }
        }
    }

    private void startQueueing() {
        client.sendQueue();
    }

    private String selectPlayerType() {
        String selectPlayerText = """
            Select your player type:\s
                -H for human player via the TUI.\s
                -N for Naive AI player.\s
            """;
        println(selectPlayerText);

        return sc.nextLine();
    }

    private void mainMenu() {
        int choice = menu();
        switch (choice) {
            case 1 -> initializeConnection();
            case 2 -> {
                getHelp();
                mainMenu();
            }
            case 3 -> System.exit(0);
            default -> {
                println("Not a valid choice");
                println("");
                mainMenu();
            }
        }
    }

    private void getHelp() {

        Player player1Help = () -> Stone.BLACK;
        Player player2Help = () -> Stone.WHITE;
        GoGame helpGame = new GoGame(player1Help, player2Help);
        try {
            helpGame.doMove(new GoMove(player1Help, 6));
        } catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        }
        String helpText = """
        How to start a game: \s
            1. Type the ip and the port of the host when asked.\s
            If no host is found, you can ask again.\s
            \s
            2. Choose your unique username.\s
            (If username is already in use you will need to provide a new one).\s
            \s
            3. Choose your player type.\s
            After that you automatically get in the queue.\s
            \s
        players: \s
            Use -H for human player.\s
            Use -N instead of name for the Naive Strategy AI.\s
            \s
        How to play: \s
              Type the preferred line number as seen in the numbering grid, \s
              e.g. 6. \s
        """;
        println(helpText);
        println(helpGame);
        println("Press Enter");
        sc.nextLine();
        println("");
    }
    private int menu() {
        String menu = """
        Welcome to JavaGO!\s
               Main Menu\s
            1. Play Game\s
            2. Help\s
            3. Quit\s
        """;
        println(menu);
        if (isSystemOut) {
            print("-->");
        }
        return Integer.parseInt(sc.nextLine());
    }

    /**
     * username is assigned by the user and communicated to the server.
     */
    private void sendUsername() {
        print("Provide a username: ");
        String username = sc.nextLine();
        client.sendUsername(username);

        checkUsername(username);
    }

    private synchronized void checkUsername(String username) {
        try {
            this.wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (logInSuccessful){
            println("Your new username is: " + username);
            log = new LogListener(client, username);
            client.addListener(log);
            logInSuccessful = true;
        }else{
            println("Username: " + username
                                       + " already exists. Choose a new one.");
            sendUsername();
        }
    }

    /**
     * Initialise the client - server connection.
     */
    private void initializeClient(String serverName, int portNumber) {

        try {
            client = new GameClient(InetAddress.getByName(serverName), portNumber);
            client.addListener(this);
        } catch (IOException e) {
            println("Could not find host " + serverName
                                       + " @ port: " + portNumber);
            initializeClient();
        }
    }

    /**
     * Initialise the client - server connection.
     */
    private void initializeClient() {
        print("Please provide a server ip: ");
        serverName = sc.nextLine();
        print("Now please provide a port: ");
        portNumber = sc.nextInt();
        sc.nextLine();
        try {
            client = new GameClient(InetAddress.getByName(serverName), portNumber);
            client.addListener(this);
        } catch (IOException e){
            println("Could not find host " + serverName
                                       + " @ port: " + portNumber);
            initializeClient();
        }
    }

    /**
     * Confirms that log in was successfull with the server.
     *
     * @param status   The status. True if successful
     * @param username The username used.
     */
    @Override
    public synchronized void logInStatus(boolean status, String username) {
        logInSuccessful = status;
        this.notifyAll();
    }

    /**
     * Print out the message
     * @param sender The username of the sender
     * @param message The message
     */
    @Override
    public void chatMessage(String sender, String message) {
        if(!sender.equals(client.getUsername()))
            System.out.println(sender + ": " + message);
    }

    /**
     * Disconnect notification
     */
    @Override
    public synchronized void connectionLost() {
        println("Disconnected from the server");
        println("Restart client to try and reconnect");
        System.exit(1);
    }

    /**
     * Notify listeners of successful connection with the server and propagates server's message.
     *
     * @param message The server's hello message.
     */
    @Override
    public void successfulConnection(String message) {
        println("Connected to Server");
        if(message != null) {
            println(message);
        }
    }

    /**
     * Receive the playerList.
     *
     * @param playerList The list of players.
     */
    @Override
    public void receiveList(List<String> playerList) {

    }

    /**
     * Receive confirmation of entering matchmaking queue.
     */
    @Override
    public void receiveInQueue() {

    }

    private void println(Object o) {
        output.println(o);
        output.flush();
    }

    private void print(Object o) {
        output.print(o);
        output.flush();
    }

    public static void main(String[] args) {
        GameClientTUI tui = new GameClientTUI();

    }
}
