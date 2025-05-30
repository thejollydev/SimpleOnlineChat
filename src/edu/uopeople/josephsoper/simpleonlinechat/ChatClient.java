package edu.uopeople.josephsoper.simpleonlinechat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * The ChatClient class allows a user to connect to the ChatServer
 * to send and receive messages in a chat room.
 */
public class ChatClient {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 3314; // Must match ChatServer.PORT

    /**
     * The main method for the chat client.
     * It attempts to connect to the server, sets up I/O streams,
     * starts a listener thread for server messages, and then enters a loop
     * to read and send user input.
     */
    public static void main(String[] args) {

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner consoleScanner = new Scanner(System.in)) {

            System.out.println("Connected to the chat server at " + SERVER_ADDRESS + ":" + SERVER_PORT);
            System.out.println("You can start typing messages. Type 'quit' to exit.");

            ServerListener serverListener = new ServerListener(socket);
            new Thread(serverListener).start();

            // Main loop to read user input and send it to the server.
            while (true) {
                String userInput = consoleScanner.nextLine();

                out.println(userInput);

                if ("quit".equalsIgnoreCase(userInput)) {
                    System.out.println("Disconnecting...");
                    break;
                }
            }

        } catch (UnknownHostException e) {
            System.err.println("Server not found: " + SERVER_ADDRESS + ". " + e.getMessage());
        } catch (IOException e) {
            // This can happen if the server is not running or refuses connection. [cite: 705, 710]
            System.err.println("Couldn't connect to server or I/O error: " + e.getMessage());
        }
        System.out.println("Client shut down.");
    }
}

/**
 * The ServerListener class implements Runnable and is responsible for
 * listening to messages broadcast by the server. It runs in a separate thread
 * within the ChatClient to allow non-blocking message reception.
 */
class ServerListener implements Runnable {
    private final Socket socket;
    private BufferedReader serverIn;

    /**
     * Constructs a new ServerListener.
     * @param s The Socket representing the client's connection to the server.
     */
    public ServerListener(Socket s) {
        this.socket = s;
    }

    /**
     * The main execution logic for the server listener thread.
     * It initializes a BufferedReader to read from the server's output stream
     * and then enters a loop, continuously reading and displaying messages from the server.
     * The loop terminates if the connection is closed or an error occurs.
     */
    @Override
    public void run() {
        try {
            serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String serverResponse;
            while ((serverResponse = serverIn.readLine()) != null) {
                System.out.println(serverResponse);
            }
        } catch (SocketException se) {
            System.out.println("Connection to server closed.");
        } catch (IOException e) {
            System.err.println("Error reading from server: " + e.getMessage());
        } finally {
            try {
                if (serverIn != null) {
                    serverIn.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing server input stream: " + e.getMessage());
            }
        }
    }
}