package ru.secureoverlay.transport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TcpClient {

    private final String host;
    private final int port;

    public TcpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        System.out.println(
                "[CLIENT] Connecting to " + host + ":" + port
        );

        try (Socket socket = new Socket(host, port);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(
                             socket.getInputStream(),
                             StandardCharsets.UTF_8
                     )
             );
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(
                             socket.getOutputStream(),
                             StandardCharsets.UTF_8
                     )
             )) {

            System.out.println("[CLIENT] Connected");

            writer.write("HELLO");
            writer.newLine();
            writer.flush();

            System.out.println("[CLIENT] Sent: HELLO");

            String response = reader.readLine();

            System.out.println("[CLIENT] Received: " + response);

        } catch (IOException e) {
            System.err.println("[CLIENT] Network error: " + e.getMessage());
        }
    }
}