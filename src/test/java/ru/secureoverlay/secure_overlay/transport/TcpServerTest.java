package ru.secureoverlay.secure_overlay.transport;

import org.junit.jupiter.api.Test;
import ru.secureoverlay.secure_overlay.protocol.Frame;
import ru.secureoverlay.secure_overlay.protocol.FrameDecoder;
import ru.secureoverlay.secure_overlay.protocol.FrameEncoder;
import ru.secureoverlay.secure_overlay.protocol.FrameType;

import java.net.Socket;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TcpServerTest {

    @Test
    void shouldHandleSeveralClients()
            throws Exception {

        int port = 9100;

        TcpServer server =
                new TcpServer(port);

        Thread serverThread =
                new Thread(server::start);

        serverThread.start();

        Thread.sleep(300);

        FrameEncoder encoder =
                new FrameEncoder();

        FrameDecoder decoder =
                new FrameDecoder();

        for (int i = 0; i < 3; i++) {

            try (Socket socket =
                         new Socket(
                                 "127.0.0.1",
                                 port
                         )) {

                UUID requestId =
                        UUID.randomUUID();

                Frame ping =
                        new Frame(
                                (byte) 1,
                                FrameType.PING,
                                (short) 0,
                                requestId,
                                new byte[0]
                        );

                socket.getOutputStream()
                        .write(
                                encoder.encode(ping)
                        );

                socket.getOutputStream()
                        .flush();

                Frame response =
                        decoder.decode(
                                socket.getInputStream()
                        );

                assertEquals(
                        FrameType.PONG,
                        response.type()
                );

                assertEquals(
                        requestId,
                        response.requestId()
                );
            }
        }

        server.stop();

        serverThread.join(2000);
    }
}