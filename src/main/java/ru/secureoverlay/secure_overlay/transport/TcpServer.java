package ru.secureoverlay.secure_overlay.transport;

import ru.secureoverlay.secure_overlay.dht.RoutingTable;
import ru.secureoverlay.secure_overlay.dht.storage.DhtStorageService;
import ru.secureoverlay.secure_overlay.dht.storage.DhtStore;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.secureoverlay.secure_overlay.tunnel.TunnelService;
import ru.secureoverlay.secure_overlay.tunnel.TunnelStateStore;
import ru.secureoverlay.secure_overlay.security.ReplayGuard;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import java.time.Duration;
public class TcpServer {

    private final int port;
    private final RoutingTable routingTable;
    private final DhtStore store;

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    private volatile boolean running;

    private ServerSocket serverSocket;

    private final ServerSocketFactory serverSocketFactory;
    private final TunnelStateStore tunnelStateStore = new TunnelStateStore();
    TunnelRpcClient tunnelRpcClient = new TunnelRpcClient();
    TunnelService tunnelService = new TunnelService(
                    tunnelRpcClient,
                    tunnelStateStore
            );




    private final ReplayGuard replayGuard =
            new ReplayGuard(
                    Duration.ofMinutes(5)
            );

    public TcpServer(
            int port,
            RoutingTable routingTable
    ) {

        this(
                port,
                routingTable,
                ServerSocketFactory
                        .getDefault()
        );


    }

    public TunnelStateStore getTunnelStateStore() {

        return tunnelStateStore;
    }

    public TcpServer(
            int port,
            RoutingTable routingTable,
            ServerSocketFactory serverSocketFactory
    ) {

        this.port = port;

        this.routingTable =
                routingTable;

        this.store =
                new DhtStore();

        this.serverSocketFactory =
                serverSocketFactory;
    }



    public void start() {

        running = true;

        System.out.println(
                "[SERVER] Starting TCP server on port " + port
        );

        try {

            serverSocket =
                    serverSocketFactory
                            .createServerSocket(
                                    port
                            );

            if (serverSocket
                    instanceof SSLServerSocket sslServerSocket) {

                sslServerSocket.setEnabledProtocols(
                        new String[]{
                                "TLSv1.3"
                        }
                );

                /*
                 * Вот здесь включается mutual TLS:
                 * сервер ОБЯЗАТЕЛЬНО требует
                 * клиентский сертификат.
                 */
                sslServerSocket.setNeedClientAuth(
                        true
                );
            }





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

                    DhtStorageService storageService =
                            new DhtStorageService(
                                    store,
                                    routingTable,
                                    3
                            );

                    TunnelRpcClient tunnelRpcClient =
                            new TunnelRpcClient();

                    TunnelService tunnelService =
                            new TunnelService(
                                    tunnelRpcClient,
                                    tunnelStateStore
                            );

                    executor.submit(
                            new ConnectionHandler(
                                    clientSocket,
                                    routingTable,
                                    storageService,
                                    replayGuard,
                                    tunnelService
                            )
                    );







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
    public DhtStore getStore() {
        return store;
    }
}