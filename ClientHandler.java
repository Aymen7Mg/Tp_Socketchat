package Server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final Set<ClientHandler> clients = new HashSet<>();
    private static int clientIdCounter = 1;
    private static final int PORT = 8000;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            while (true) {
                new ClientHandler(serverSocket.accept(), clientIdCounter++).start();
            }
        } catch (IOException e) {
            System.out.println("Error starting server: " + e.getMessage());
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket clientSocket;
        private final int clientId;
        private PrintWriter out;
        private BufferedReader in;
        private String userName;

        public ClientHandler(Socket socket, int id) {
            this.clientSocket = socket;
            this.clientId = id;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                out.println(clientId);

                userName = in.readLine();
                System.out.println(userName + " joined (ID: " + clientId + ")");
                broadcast(userName + " has joined the chat");

                String message;
                while ((message = in.readLine()) != null) {
                    broadcast(message);
                }
            } catch (IOException e) {
                System.out.println("Error with client " + userName + ": " + e.getMessage());
            } finally {
                closeConnection();
            }
        }

        private void broadcast(String message) {
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    client.out.println(message);
                }
            }
        }

        private void closeConnection() {
            if (userName != null) {
                broadcast(userName + " has left the chat");
            }
            clients.remove(this);
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Start the client handler thread
        public void start() {
            clients.add(this);
            super.start();
        }
    }
}
