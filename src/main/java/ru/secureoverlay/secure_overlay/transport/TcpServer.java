package ru.secureoverlay.secure_overlay.transport;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpServer {

    private final int port;

    private final ExecutorService executor =
            Executors.newFixedThreadPool(4);

    private volatile boolean running;

    private ServerSocket serverSocket;

    public TcpServer(int port) {
        this.port = port;
    }

    public void start() {

        running = true;

        System.out.println(
                "[SERVER] Starting TCP server on port " + port
        );

        try {

            serverSocket = new ServerSocket(port);

            System.out.println(
                    "[SERVER] Waiting for connections..."
            );

            while (running) {

                try {

                    Socket clientSocket =
                            serverSocket.accept();

                    System.out.println(
                            "[SERVER] Accepted connection from "
                                    + clientSocket.getRemoteSocketAddress()
                    );

                    ConnectionHandler handler =
                            new ConnectionHandler(clientSocket);

                    executor.submit(handler);

                } catch (SocketException e) {

                    if (running) {
                        throw e;
                    }
                }
            }

        } catch (IOException e) {

            if (running) {
                System.err.println(
                        "[SERVER] Network error: "
                                + e.getMessage()
                );
            }

        } finally {

            stop();
        }
    }

    public void stop() {

        running = false;

        if (serverSocket != null
                && !serverSocket.isClosed()) {

            try {
                serverSocket.close();
            } catch (IOException e) {

                System.err.println(
                        "[SERVER] Error while closing server socket: "
                                + e.getMessage()
                );
            }
        }

        executor.shutdown();

        System.out.println("[SERVER] Stopped");
    }
}