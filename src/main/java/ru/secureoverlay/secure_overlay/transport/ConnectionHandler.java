package ru.secureoverlay.secure_overlay.transport;

import ru.secureoverlay.secure_overlay.protocol.Frame;
import ru.secureoverlay.secure_overlay.protocol.FrameDecoder;
import ru.secureoverlay.secure_overlay.protocol.FrameEncoder;
import ru.secureoverlay.secure_overlay.protocol.FrameType;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ConnectionHandler implements Runnable {

    private static final byte SUPPORTED_VERSION = 1;

    private final Socket socket;

    public ConnectionHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        try (socket) {

            System.out.println(
                    "[HANDLER] Connection from "
                            + socket.getRemoteSocketAddress()
            );

            FrameDecoder decoder = new FrameDecoder();
            FrameEncoder encoder = new FrameEncoder();

            Frame request =
                    decoder.decode(socket.getInputStream());

            if (request.version() != SUPPORTED_VERSION) {

                throw new IllegalArgumentException(
                        "Unsupported protocol version: "
                                + request.version()
                );
            }

            System.out.println(
                    "[HANDLER] Received: "
                            + request.type()
            );

            if (request.type() == FrameType.PING) {

                Frame pong = new Frame(
                        SUPPORTED_VERSION,
                        FrameType.PONG,
                        (short) 0,
                        request.requestId(),
                        "WORLD".getBytes(
                                StandardCharsets.UTF_8
                        )
                );

                socket.getOutputStream()
                        .write(encoder.encode(pong));

                socket.getOutputStream()
                        .flush();

                System.out.println(
                        "[HANDLER] PONG sent, requestId="
                                + pong.requestId()
                );

            } else {

                System.err.println(
                        "[HANDLER] Unsupported frame type: "
                                + request.type()
                );
            }

        } catch (IOException e) {

            System.err.println(
                    "[HANDLER] Network error: "
                            + e.getMessage()
            );

        } catch (RuntimeException e) {

            System.err.println(
                    "[HANDLER] Protocol error: "
                            + e.getMessage()
            );
        }
    }
}