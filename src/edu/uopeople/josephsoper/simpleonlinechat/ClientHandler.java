package edu.uopeople.josephsoper.simpleonlinechat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

/**
 * ClientHandler implements Runnable interface and manages communication
 * with a single client connected to ChatServer. Each instance runs its
 * own thread. Reads messages from client and broadcasts them using ChatServer
 * broadcastMessage().
 */
class ClientHandler implements Runnable {

    private Socket clientSocket;
    private String userID;
    private PrintWriter out;
    private BufferedReader in;

    /**
     * Constructor for new ClientHandler
     * @param socket The Socket representing connection to client.
     * @param userID Unique ID of client
     */
    public ClientHandler(Socket socket, String userID) {
        this.clientSocket = socket;
        this.userID = userID;
    }

    /**
     * Get client userID.
     */
    public String getUserID() {
        return userID;
    }

    /**
     * Main logic for ClientHandler thread. Sets up I/O streams for
     * client communication and reads and broadcasts client messages.
     */
    @Override
    public void run() {
        try {
            in = new BufferedReader
                    (new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            ChatServer.broadcastMessage(userID + " has joined.", this);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String messageToBroadcast = "[" + userID + "]: " + inputLine;
                System.out.println("Received from " + userID + ": " +
                        inputLine + ". Sending Broadcast: " + messageToBroadcast);
                ChatServer.broadcastMessage(messageToBroadcast, this);
            }
        } catch (SocketException se) {
            System.out.println(userID + " disconnected: " + se.getMessage());
        } catch (IOException e) {
            System.err.println("IOException for " + userID + ": " + e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing resources for " + userID +
                        ": " + e.getMessage());
            }
            ChatServer.removeClient(this);
        }
    }

    /**
     * Sends message directly to this client.
     * @param message The message string to be sent to this client.
     */
    public void sendMessage(String message) {
        if (out != null && !out.checkError()) {
            out.println(message);
        }
    }
}
