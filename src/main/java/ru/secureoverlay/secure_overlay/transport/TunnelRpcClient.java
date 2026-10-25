package ru.secureoverlay.secure_overlay.transport;

import ru.secureoverlay.secure_overlay.protocol.*;
import ru.secureoverlay.secure_overlay.tunnel.rpc.*;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

public class TunnelRpcClient {

    private final SocketFactory socketFactory;


    public TunnelRpcClient() {

        this(SocketFactory.getDefault());
    }


    public TunnelRpcClient(
            SocketFactory socketFactory
    ) {

        this.socketFactory = socketFactory;
    }


    public TunnelBuildResponse build(String host, int port, TunnelBuildRequest buildRequest) throws IOException {

        JsonPayloadCodec codec = new JsonPayloadCodec();


        Frame response = request(
                        host,
                        port,
                        FrameType.TUNNEL_BUILD,
                        codec.encode(
                                buildRequest
                        )
                );


        if (response.type() == FrameType.TUNNEL_BUILD_FAIL) {

            return new TunnelBuildResponse(
                    buildRequest.tunnelId(),
                    false,
                    null,
                    "Remote tunnel build failed"
            );
        }


        if (response.type()
                != FrameType.TUNNEL_BUILD_OK) {

            throw new IOException(
                    "Expected TUNNEL_BUILD_OK, got "
                            + response.type()
            );
        }


        return codec.decode(
                response.payload(),
                TunnelBuildResponse.class
        );
    }


    public TunnelAck sendData(
            String host,
            int port,
            TunnelDataRequest dataRequest
    ) throws IOException {

        JsonPayloadCodec codec =
                new JsonPayloadCodec();


        Frame response =
                request(
                        host,
                        port,
                        FrameType.TUNNEL_DATA,
                        codec.encode(
                                dataRequest
                        )
                );


        if (response.type()
                != FrameType.TUNNEL_ACK) {

            throw new IOException(
                    "Expected TUNNEL_ACK, got "
                            + response.type()
            );
        }


        return codec.decode(
                response.payload(),
                TunnelAck.class
        );
    }


    public void closeTunnel(
            String host,
            int port,
            UUID tunnelId
    ) throws IOException {

        JsonPayloadCodec codec =
                new JsonPayloadCodec();


        request(
                host,
                port,
                FrameType.TUNNEL_CLOSE,
                codec.encode(
                        tunnelId
                )
        );
    }


    private Frame request(
            String host,
            int port,
            FrameType type,
            byte[] payload
    ) throws IOException {

        try (Socket socket =
                     createSocket(
                             host,
                             port
                     )) {

            socket.setSoTimeout(
                    5000
            );


            Frame request =
                    new Frame(
                            (byte) 1,
                            type,
                            (short) 0,
                            UUID.randomUUID(),
                            payload
                    );


            FrameEncoder encoder =
                    new FrameEncoder();

            FrameDecoder decoder =
                    new FrameDecoder();


            socket.getOutputStream()
                    .write(
                            encoder.encode(
                                    request
                            )
                    );

            socket.getOutputStream()
                    .flush();


            Frame response =
                    decoder.decode(
                            socket.getInputStream()
                    );


            RpcValidator.validateResponse(
                    request,
                    response
            );


            return response;
        }
    }


    private Socket createSocket(
            String host,
            int port
    ) throws IOException {

        Socket socket =
                socketFactory.createSocket(
                        host,
                        port
                );


        if (socket
                instanceof SSLSocket sslSocket) {

            sslSocket.setEnabledProtocols(
                    new String[]{
                            "TLSv1.3"
                    }
            );

            sslSocket.startHandshake();
        }


        return socket;
    }
}