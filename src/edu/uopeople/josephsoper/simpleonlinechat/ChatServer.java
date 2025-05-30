package edu.uopeople.josephsoper.simpleonlinechat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Main entry point for chat application. Listens for incoming client
 * connections on specified port and creates a new ClientHandler
 * thread for each client.
 */
public class ChatServer {

    private static final int PORT = 3314;
    private static List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    /**
     * Initializes a ServerSocket and enters loop to accept new client
     * connections. Each connection is handed off to new ClientHandler
     * thread.
     */
    public static void main(String[] args) {
        System.out.println("Chat server started...");
        System.out.println("Listening on port " + PORT);

        try (ServerSocket listener = new ServerSocket(PORT)) {

            int clientCount = 0;

            while (true) {
                Socket clientSocket = listener.accept();
                clientCount++;
                String userID = "User" + clientCount;
                System.out.println("New client connected: " + userID +
                        " from " + clientSocket.getInetAddress().getHostAddress());

                // Create a new handler for client
                ClientHandler clientThread = new ClientHandler(clientSocket, userID);
                clients.add(clientThread);

                // Start ClientHandler thread to manage I/O
                new Thread(clientThread).start();
            }

        } catch (IOException e) {
            System.err.println("Server Error: " + e.getmessage());
        }
    }

    /**
     * Broadcasts a message to all connected clients except sender.
     * @param message The message to be broadcast
     * @param sender The ClientHandler who sent the message
     */
    public static synchronized void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    /**
     * Removes a ClientHandler from the list of clients, such as when
     * client disconnects.
     * @param client The ClientHandler to be removed.
     */
    public static synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println(client.getUserID() + " has disconnected.");
        broadcastMessage(client.getUserID() + " has left.", null);
    }
}
