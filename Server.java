package socketChat.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 8000;
    private static final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet(); // Thread-safe set of clients
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10); // Thread pool for handling clients

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat Server is running on port " + PORT);

            int clientId = 0;
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                ClientHandler clientHandler = new ClientHandler(clientSocket, clientId++);
                clients.add(clientHandler);

                threadPool.execute(clientHandler);
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }

    private static class ClientHandler implements Runnable {
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
                System.out.println(userName + " has joined the chat (ID: " + clientId + ")");
                broadcastMessage(userName + " has joined the chat");

                String message;
                while ((message = in.readLine()) != null) {
                    broadcastMessage(message);
                }
            } catch (IOException e) {
                System.out.println("Error handling client " + userName + ": " + e.getMessage());
            } finally {
                if (userName != null) {
                    broadcastMessage(userName + " has left the chat");
                }
                clients.remove(this);
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void broadcastMessage(String message) {
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    client.out.println(message);
                }
            }
        }
    }
}
