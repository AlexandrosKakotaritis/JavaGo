package com.nedap.go.networking;

import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nedap.go.networking.protocol.Protocol;
import com.nedap.go.networking.server.GameServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServerTest {
    private GameServer server;
    @BeforeEach
    void setUp() throws IOException {

        server = new GameServer(0);

    }

    private void acceptConnections() {
        try {
            server.acceptConnections();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void dummyClient(PrintWriter out, String name){
        out.println(Protocol.LOGIN + Protocol.SEPARATOR + name);
    }

    private static void skipInitialization(BufferedReader bufferedReader, BufferedReader bufferedReader2) throws IOException {
        bufferedReader.readLine();
        bufferedReader.readLine();
        bufferedReader2.readLine();
        bufferedReader2.readLine();
    }

    private void skipNewGame(PrintWriter printWriter, BufferedReader bufferedReader,
                             PrintWriter printWriter2, BufferedReader bufferedReader2)
            throws IOException {
        printWriter.println(Protocol.QUEUE);
        printWriter2.println(Protocol.QUEUE);
        bufferedReader.readLine();
        bufferedReader2.readLine();
    }


//    private void timeout(){
//        long timeInMillis = 5 * 1000;
//        while(currentTimeMillis() - time < timeInMillis){}
//        assertFalse(true);
//        server.close();
//    }

    @Test
    public void testLogIn() throws IOException{
        assertTrue(server.getPort() > 0);
        assertTrue(server.getPort() <= 65535);

        // start the server
        new Thread(this::acceptConnections).start();

        Socket socket = new Socket(InetAddress.getLocalHost(),
                                   server.getPort());  // connect to the server
        String s;

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                socket.getInputStream())); PrintWriter printWriter = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream()), true)) {
            dummyClient(printWriter, "Alex");
//            s = bufferedReader.readLine();
//            assertEquals("HELLO~Server by Alex", s);

            s = bufferedReader.readLine();
            assertEquals("ACCEPTED~Alex", s);

            printWriter.println("LOGIN~Alex");
            socket.close();
        } finally {
            // Stop the server.
            server.close();
        }
    }

    @Test
    public void testList() throws IOException, InterruptedException {
        new Thread(this::acceptConnections).start();
        String s;
        Socket socket = new Socket(InetAddress.getLocalHost(), server.getPort());  // connect to the server
        Socket socket2 = new Socket(InetAddress.getLocalHost(), server.getPort());

        // using a try-with-resources block, we ensure that reader/writer are closed afterwards
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
             PrintWriter printWriter = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream()), true);
             BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(
                     socket2.getInputStream()));
             PrintWriter printWriter2 = new PrintWriter(
                new OutputStreamWriter(socket2.getOutputStream()), true)) {

            Thread t1 = new Thread(() -> dummyClient(printWriter, "Alex"));
            Thread t2 = new Thread(() -> dummyClient(printWriter2, "Nick"));
            t1.start();
            t2.start();
            bufferedReader.readLine();
            bufferedReader.readLine();
            printWriter.println(Protocol.LIST);
            s = bufferedReader.readLine();
            assertTrue(s.contains("Alex"));
            assertTrue(s.contains("Nick"));

            t1.join();
            t2.join();

            socket.close();
            socket2.close();
        }finally {
            server.close();
        }
    }

    @Test
    public void testQueue() throws IOException, InterruptedException {
        new Thread(this::acceptConnections).start();
        String s;
        Socket socket = new Socket(InetAddress.getLocalHost(), server.getPort());  // connect to the server
        Socket socket2 = new Socket(InetAddress.getLocalHost(), server.getPort());

        // using a try-with-resources block, we ensure that reader/writer are closed afterwards
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
             PrintWriter printWriter = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream()), true);
             BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(
                     socket2.getInputStream()));
             PrintWriter printWriter2 = new PrintWriter(
                new OutputStreamWriter(socket2.getOutputStream()), true)) {

            Thread t1 = new Thread(() -> dummyClient(printWriter, "Alex"));
            Thread t2 = new Thread(() -> dummyClient(printWriter2, "Nick"));
            t1.start();
            t2.start();
            skipInitialization(bufferedReader, bufferedReader2);
            printWriter.println(Protocol.QUEUE);
            printWriter2.println(Protocol.QUEUE);
            t1.join();
            t2.join();

            bufferedReader.readLine();
            s = bufferedReader.readLine();
            assertTrue(s.contains(Protocol.NEW_GAME));
            assertTrue(s.contains("Alex"));
            assertTrue(s.contains("Nick"));
            bufferedReader2.readLine();
            s = bufferedReader2.readLine();
            assertTrue(s.contains(Protocol.NEW_GAME));
            assertTrue(s.contains("Alex"));
            assertTrue(s.contains("Nick"));


            socket.close();
            s = bufferedReader2.readLine();

            assertTrue(s.contains(Protocol.GAME_OVER));
            socket2.close();
        }finally {
            server.close();
        }
    }

    @Test
    public void testQueueQuit() throws IOException, InterruptedException {
        new Thread(this::acceptConnections).start();
        String s;
        Socket socket = new Socket(InetAddress.getLocalHost(), server.getPort());  // connect to the server
        Socket socket2 = new Socket(InetAddress.getLocalHost(), server.getPort());

        // using a try-with-resources block, we ensure that reader/writer are closed afterwards
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                socket.getInputStream())); PrintWriter printWriter = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream()), true);
             BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(
                     socket2.getInputStream())); PrintWriter printWriter2 = new PrintWriter(
                new OutputStreamWriter(socket2.getOutputStream()), true)) {

            Thread t1 = new Thread(() -> dummyClient(printWriter, "Alex"));
            Thread t2 = new Thread(() -> dummyClient(printWriter2, "Nick"));
            t1.start();
            t2.start();
            skipInitialization(bufferedReader, bufferedReader2);
            printWriter.println(Protocol.QUEUE);
            printWriter.println(Protocol.QUEUE);
            printWriter2.println(Protocol.QUEUE);
            Thread.sleep(500);
            printWriter.println(Protocol.QUEUE);
            t1.join();
            t2.join();

            s = bufferedReader.readLine();
            String[] splits = s.split(Protocol.SEPARATOR);
            assertTrue(s.contains(Protocol.NEW_GAME));
            assertTrue(s.contains("Alex"));
            assertTrue(s.contains("Nick"));
            assertEquals("Nick", splits[1]);
            assertEquals("Alex", splits[2]);
            s = bufferedReader2.readLine();
            assertTrue(s.contains(Protocol.NEW_GAME));
            assertTrue(s.contains("Alex"));
            assertTrue(s.contains("Nick"));
            assertEquals("Nick", splits[1]);
            assertEquals("Alex", splits[2]);

            socket.close();
            socket2.close();
        }finally {
            server.close();
        }
    }
    @Test
    public void test2NewGames() throws IOException, InterruptedException {
        new Thread(this::acceptConnections).start();
        String s;
        Socket[] sockets = new Socket[4];
        for (int i = 0; i < 4; i++) {
            sockets[i] = new Socket(InetAddress.getLocalHost(), server.getPort());
        }
        BufferedReader[] readers = new BufferedReader[4];
        PrintWriter[] writers = new PrintWriter[4];
        // using a try-with-resources block, we ensure that reader/writer are closed afterwards
        try {
            for (int i = 0; i < 4; i++) {
                readers[i] = new BufferedReader(
                        new InputStreamReader(sockets[i].getInputStream()));
                writers[i] = new PrintWriter(
                        new OutputStreamWriter(sockets[i].getOutputStream()));
            }
            Thread[] threads = new Thread[4];
            for (int i = 0; i < 4; i++) {
                PrintWriter writer = writers[i];
                String name = "dummy" + i;
                threads[i] = new Thread(() -> dummyClient(writer, name));
                threads[i].start();
                System.out.println(readers[i].readLine());
                System.out.println(readers[i].readLine());
                writers[i].println(Protocol.QUEUE);
            }


            s = readers[0].readLine();
            assertTrue(s.contains(Protocol.NEW_GAME));
            assertTrue(s.contains("dummy1"));
            assertTrue(s.contains("dummy2"));
            s = readers[3].readLine();
            assertTrue(s.contains(Protocol.NEW_GAME));
            assertTrue(s.contains("dummy3"));
            assertTrue(s.contains("dummy4"));


            sockets[3].close();
            s = readers[3].readLine();
            assertTrue(s.contains(Protocol.GAME_OVER));

            for (int i = 0; i <3; i++) {
                sockets[i].close();
            }
        }finally {
            for (int i = 0; i < 4; i++) {
                readers[i].close();
                writers[i].close();
            }
            server.close();
        }
    }
    @Test
    public void testMove() throws IOException, InterruptedException {
        new Thread(this::acceptConnections).start();
        String s;
        Socket socket = new Socket(InetAddress.getLocalHost(), server.getPort());  // connect to the server
        Socket socket2 = new Socket(InetAddress.getLocalHost(), server.getPort());

        // using a try-with-resources block, we ensure that reader/writer are closed afterwards
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                socket.getInputStream())); PrintWriter printWriter = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream()), true);
             BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(
                     socket2.getInputStream())); PrintWriter printWriter2 = new PrintWriter(
                new OutputStreamWriter(socket2.getOutputStream()), true)) {

            Thread t1 = new Thread(() -> dummyClient(printWriter, "Alex"));
            Thread t2 = new Thread(() -> dummyClient(printWriter2, "Nick"));
            t1.start();
            t2.start();
            skipInitialization(bufferedReader, bufferedReader2);
            skipNewGame(printWriter, bufferedReader, printWriter2,bufferedReader2);
            printWriter2.println(Protocol.MOVE + Protocol.SEPARATOR + "10");
            printWriter.println(Protocol.MOVE + Protocol.SEPARATOR + "6");

            t1.join();
            t2.join();


            s = bufferedReader.readLine();
            assertTrue(s.contains(Protocol.MOVE));
            assertTrue(s.contains("6"));
            s = bufferedReader2.readLine();
            assertTrue(s.contains(Protocol.MOVE));
            assertTrue(s.contains("6"));
            socket.close();
            socket2.close();
        }finally {
            server.close();
        }
    }
}
