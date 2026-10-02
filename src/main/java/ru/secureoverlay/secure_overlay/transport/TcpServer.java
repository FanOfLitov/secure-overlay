package ru.secureoverlay.secure_overlay.transport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TcpServer {

    private final int port;

    public TcpServer(int port) {
        this.port = port;
    }

    public void start() {
        System.out.println("[SERVER] Starting TCP server on port " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("[SERVER] Waiting for connection...");

            try (Socket clientSocket = serverSocket.accept();
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(
                                 clientSocket.getInputStream(),
                                 StandardCharsets.UTF_8
                         )
                 );
                 BufferedWriter writer = new BufferedWriter(
                         new OutputStreamWriter(
                                 clientSocket.getOutputStream(),
                                 StandardCharsets.UTF_8
                         )
                 )) {

                System.out.println(
                        "[SERVER] Client connected: "
                                + clientSocket.getRemoteSocketAddress()
                );

                String message = reader.readLine();

                System.out.println("[SERVER] Received: " + message);

                writer.write("WORLD");
                writer.newLine();
                writer.flush();

                System.out.println("[SERVER] Sent: WORLD");
            }

        } catch (IOException e) {
            System.err.println("[SERVER] Network error: " + e.getMessage());
        }
    }
}