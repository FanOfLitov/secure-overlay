package ru.secureoverlay.node;

import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.secureoverlay.transport.TcpClient;
import ru.secureoverlay.transport.TcpServer;

@Component
public class NetworkDemoRunner implements CommandLineRunner {

    @Value("${overlay.mode:server}")
    private String mode;

    @Value("${overlay.host:127.0.0.1}")
    private String host;

    @Value("${overlay.port:9001}")
    private int port;

    @Override
    public void run(String... args) {

        if ("server".equalsIgnoreCase(mode)) {

            TcpServer server = new TcpServer(port);
            server.start();

        } else if ("client".equalsIgnoreCase(mode)) {

            TcpClient client = new TcpClient(host, port);
            client.start();

        } else {

            System.err.println(
                    "Unknown overlay mode: " + mode
            );
        }
    }
}