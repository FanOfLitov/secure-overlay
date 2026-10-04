package ru.secureoverlay.secure_overlay.transport;

import ru.secureoverlay.secure_overlay.protocol.*;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class TcpClient {

    private final String host;
    private final int port;

    public TcpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {

        System.out.println(
                "[CLIENT] Connecting to "
                        + host
                        + ":"
                        + port
        );

        try (Socket socket = new Socket(host, port)) {

            System.out.println("[CLIENT] Connected");

            FrameEncoder encoder = new FrameEncoder();
            FrameDecoder decoder = new FrameDecoder();

            UUID requestId = UUID.randomUUID();

            Frame ping = new Frame(
                    (byte) 1,
                    FrameType.PING,
                    (short) 0,
                    requestId,
                    "HELLO".getBytes(StandardCharsets.UTF_8)
            );

            byte[] encodedPing = encoder.encode(ping);

            socket.getOutputStream().write(encodedPing);
            socket.getOutputStream().flush();

            System.out.println(
                    "[CLIENT] Sent PING, requestId=" + requestId
            );

            Frame response = decoder.decode(
                    socket.getInputStream()
            );

            System.out.println(
                    "[CLIENT] Received frame: "
                            + response.type()
            );

            System.out.println(
                    "[CLIENT] Response requestId: "
                            + response.requestId()
            );

            System.out.println(
                    "[CLIENT] Payload: "
                            + new String(
                            response.payload(),
                            StandardCharsets.UTF_8
                    )
            );

            if (response.type() != FrameType.PONG) {
                throw new IllegalStateException(
                        "Expected PONG but received "
                                + response.type()
                );
            }

            RpcValidator.validateResponse(
                    ping,
                    response
            );

            System.out.println(
                    "[CLIENT] PING/PONG RPC completed successfully"
            );

        } catch (IOException e) {

            System.err.println(
                    "[CLIENT] Network error: "
                            + e.getMessage()
            );

        } catch (RuntimeException e) {

            System.err.println(
                    "[CLIENT] Protocol error: "
                            + e.getMessage()
            );
        }
    }
}