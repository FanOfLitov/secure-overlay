package ru.secureoverlay.secure_overlay.node;

import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.secureoverlay.secure_overlay.transport.TcpClient;
import ru.secureoverlay.secure_overlay.transport.TcpServer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Component
@ConditionalOnProperty(
        name = "overlay.enabled",
        havingValue = "true"
)
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