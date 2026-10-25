package ru.secureoverlay.secure_overlay.transport;

import ru.secureoverlay.secure_overlay.dht.rpc.*;
import ru.secureoverlay.secure_overlay.dht.storage.DhtRecord;
import ru.secureoverlay.secure_overlay.identity.NodeId;
import ru.secureoverlay.secure_overlay.protocol.*;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.Socket;
import java.util.UUID;
import ru.secureoverlay.secure_overlay.security.tls.TlsPeerIdentity;
public class DhtRpcClient implements DhtRpc{

    private final SocketFactory socketFactory;

    public DhtRpcClient() {

        this(
                SocketFactory.getDefault()
        );
    }
    public DhtRpcClient(
            SocketFactory socketFactory
    ) {

        this.socketFactory =
                socketFactory;
    }

    @Override
    public boolean ping(String host, int port) throws IOException {

        try (Socket socket =createSocket(host, port)) {

            socket.setSoTimeout(5000);

            FrameEncoder encoder =
                    new FrameEncoder();

            FrameDecoder decoder =
                    new FrameDecoder();

            Frame ping =
                    new Frame((byte) 1, FrameType.PING, (short) 0, UUID.randomUUID(), new byte[0]);

            socket.getOutputStream().write(encoder.encode(ping));

            socket.getOutputStream()
                    .flush();

            Frame response =
                    decoder.decode(
                            socket.getInputStream()
                    );

            RpcValidator.validateResponse(
                    ping,
                    response
            );

            return response.type() == FrameType.PONG;
        }
    }

    @Override
    public FindNodeResponse findNode(String host, int port, NodeId target) throws IOException {

        try (Socket socket = createSocket(
                             host,
                             port
                     )) {

            socket.setSoTimeout(5000);

            FrameEncoder encoder = new FrameEncoder();

            FrameDecoder decoder = new FrameDecoder();

            JsonPayloadCodec jsonCodec = new JsonPayloadCodec();

            FindNodeRequest requestPayload = new FindNodeRequest(target.toHex());

            UUID requestId = UUID.randomUUID();

            Frame request =
                    new Frame(
                            (byte) 1,
                            FrameType.FIND_NODE_REQUEST,
                            (short) 0,
                            requestId,
                            jsonCodec.encode(
                                    requestPayload
                            )
                    );

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

            if (response.type() != FrameType.FIND_NODE_RESPONSE) {

                throw new IllegalStateException(
                        "Expected FIND_NODE_RESPONSE, got "
                                + response.type()
                );
            }

            return jsonCodec.decode(
                    response.payload(),
                    FindNodeResponse.class
            );
        }
    }


    @Override
    public StoreResponse store(
            String host,
            int port,
            DhtRecord record
    ) throws IOException {

        JsonPayloadCodec codec = new JsonPayloadCodec();

        StoreRequest storeRequest = new StoreRequest(record);

        Frame response = request(
                        host,
                        port,
                        FrameType.STORE_REQUEST,
                        codec.encode(storeRequest)
                );

        if (response.type()
                != FrameType.STORE_RESPONSE) {

            throw new IOException(
                    "Expected STORE_RESPONSE, got "
                            + response.type()
            );
        }

        return codec.decode(
                response.payload(),
                StoreResponse.class
        );
    }

    @Override
    public FindValueResponse findValue(String host, int port, String key
    ) throws IOException {

        JsonPayloadCodec codec =
                new JsonPayloadCodec();

        FindValueRequest request =
                new FindValueRequest(
                        key
                );

        Frame response =
                request(
                        host,
                        port,
                        FrameType.FIND_VALUE_REQUEST,
                        codec.encode(request)
                );

        if (response.type()
                != FrameType.FIND_VALUE_RESPONSE) {

            throw new IOException(
                    "Expected FIND_VALUE_RESPONSE"
            );
        }

        return codec.decode(
                response.payload(),
                FindValueResponse.class
        );
    }


    private Frame request(
            String host,
            int port,
            FrameType type,
            byte[] payload
    ) throws IOException {

        try (Socket socket = createSocket(host, port)) {

            socket.setSoTimeout(5000);

            FrameEncoder encoder = new FrameEncoder();

            FrameDecoder decoder = new FrameDecoder();

            Frame request = new Frame(
                            (byte) 1,
                            type,
                            (short) 0,
                            UUID.randomUUID(),
                            payload
                    );

            socket.getOutputStream()
                    .write(
                            encoder.encode(request)
                    );

            socket.getOutputStream().flush();

            Frame response = decoder.decode(socket.getInputStream());

            RpcValidator.validateResponse(request, response);

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


        if (socket instanceof SSLSocket sslSocket) {

            sslSocket.setEnabledProtocols(
                    new String[]{
                            "TLSv1.3"
                    }
            );

            /*
             * Явно выполняем handshake
             * до передачи наших Frame.
             */
            sslSocket.startHandshake();


            System.out.println(
                    "[TLS] Connected using "
                            + sslSocket
                            .getSession()
                            .getProtocol()
            );


            try {

                NodeId peerNodeId =
                        TlsPeerIdentity.nodeId(
                                sslSocket.getSession()
                        );


                System.out.println(
                        "[TLS] Peer NodeID="
                                + peerNodeId.toHex()
                );

            } catch (Exception e) {

                socket.close();

                throw new IOException(
                        "Cannot verify peer identity",
                        e
                );
            }
        }


        return socket;
    }
}