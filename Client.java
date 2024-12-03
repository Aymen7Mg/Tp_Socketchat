package socketChat.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private String userName;
    private Socket socket;
    private PrintWriter sender;
    private BufferedReader receiver;


    public Client(String userName, String address, int port) {
        try {
            this.userName = userName;
            socket = new Socket(address, port);
            sender = new PrintWriter(socket.getOutputStream(), true);
            receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            
            
            sender.println(userName);
            System.out.println("Connected to the server as " + userName);
        } catch (Exception e) {
            System.out.println("Error connecting to the server: " + e.getMessage());
        }
    }

    public void startChat() {
        try (Scanner scanner = new Scanner(System.in)) {
            Thread sendThread = new Thread(() -> {
                try {
                    while (true) {
                        String message = scanner.nextLine();
                        sender.println(userName + ": " + message);
                    }
                } catch (Exception e) {
                    System.out.println("Error sending message: " + e.getMessage());
                }
            });

            Thread receiveThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = receiver.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (Exception e) {
                    System.out.println("Error receiving message: " + e.getMessage());
                }
            });

            sendThread.start();
            receiveThread.start();

            sendThread.join();
            receiveThread.join();
        } catch (Exception e) {
            System.out.println("Chat error: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private void closeConnection() {
        try {
            if (socket != null) socket.close();
            System.out.println("Disconnected from the server.");
        } catch (Exception e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter your username: ");
            String userName = scanner.nextLine();

            Client client = new Client(userName, "localhost", 8000);
            client.startChat();
        }
    }
}
