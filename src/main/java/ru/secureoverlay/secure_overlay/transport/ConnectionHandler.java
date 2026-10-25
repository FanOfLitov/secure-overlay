package ru.secureoverlay.secure_overlay.transport;

import ru.secureoverlay.secure_overlay.dht.FindNodeService;
import ru.secureoverlay.secure_overlay.dht.RoutingTable;
import ru.secureoverlay.secure_overlay.dht.rpc.FindNodeRequest;
import ru.secureoverlay.secure_overlay.dht.rpc.FindNodeResponse;
import ru.secureoverlay.secure_overlay.dht.rpc.FindValueRequest;
import ru.secureoverlay.secure_overlay.dht.rpc.FindValueResponse;
import ru.secureoverlay.secure_overlay.dht.rpc.StoreRequest;
import ru.secureoverlay.secure_overlay.dht.rpc.StoreResponse;
import ru.secureoverlay.secure_overlay.dht.storage.DhtStorageService;
import ru.secureoverlay.secure_overlay.dht.storage.StoreResult;
import ru.secureoverlay.secure_overlay.identity.NodeId;
import ru.secureoverlay.secure_overlay.protocol.Frame;
import ru.secureoverlay.secure_overlay.protocol.FrameDecoder;
import ru.secureoverlay.secure_overlay.protocol.FrameEncoder;
import ru.secureoverlay.secure_overlay.protocol.FrameType;
import ru.secureoverlay.secure_overlay.protocol.JsonPayloadCodec;

import ru.secureoverlay.secure_overlay.security.ReplayGuard;

import java.io.IOException;
import java.net.Socket;
import java.time.Duration;

public class ConnectionHandler
        implements Runnable {

    private static final byte SUPPORTED_VERSION = 1;

    private static final int FIND_NODE_LIMIT = 3;


    private final Socket socket;

    private final RoutingTable routingTable;

    private final DhtStorageService storageService;

    private final ReplayGuard replayGuard;

    public ConnectionHandler(
            Socket socket,
            RoutingTable routingTable,
            DhtStorageService storageService,
            ReplayGuard replayGuard
    ) {

        this.socket = socket;

        this.routingTable = routingTable;

        this.storageService = storageService;

        this.replayGuard=replayGuard;
    }
    public ConnectionHandler(
            Socket socket,
            RoutingTable routingTable,
            DhtStorageService storageService
    ) {

        this(
                socket,
                routingTable,
                storageService,
                new ReplayGuard(
                        Duration.ofMinutes(5)
                )
        );
    }


    @Override
    public void run() {

        try (socket) {

            socket.setSoTimeout(5000);


            FrameDecoder decoder =  new FrameDecoder();

            FrameEncoder encoder = new FrameEncoder();

            JsonPayloadCodec jsonCodec = new JsonPayloadCodec();


            Frame request = decoder.decode(socket.getInputStream());

            if (!replayGuard.accept(
                    request.requestId()
            )) {

                throw new IllegalArgumentException(
                        "Replay detected: "
                                + request.requestId()
                );
            }

            System.out.println(
                    "[HANDLER] Received: "
                            + request.type()
                            + ", requestId="
                            + request.requestId()
            );


            if (request.version() != SUPPORTED_VERSION) {

                throw new IllegalArgumentException(
                        "Unsupported protocol version: "
                                + request.version()
                );
            }


            switch (request.type()) {

                case PING ->
                        handlePing(
                                request,
                                encoder
                        );


                case FIND_NODE_REQUEST ->
                        handleFindNode(
                                request,
                                encoder,
                                jsonCodec
                        );


                case STORE_REQUEST ->
                        handleStore(
                                request,
                                encoder,
                                jsonCodec
                        );


                case FIND_VALUE_REQUEST ->
                        handleFindValue(
                                request,
                                encoder,
                                jsonCodec
                        );


                default ->
                        throw new IllegalArgumentException(
                                "Unsupported frame type: "
                                        + request.type()
                        );
            }


        } catch (IOException e) {

            System.err.println(
                    "[HANDLER] Network error: "
                            + e.getClass()
                            .getSimpleName()
                            + ": "
                            + e.getMessage()
            );


        } catch (RuntimeException e) {

            System.err.println(
                    "[HANDLER] Protocol error: "
                            + e.getClass()
                            .getSimpleName()
                            + ": "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }


    private void handlePing(
            Frame request,
            FrameEncoder encoder
    ) throws IOException {

        Frame pong =  new Frame(
                        SUPPORTED_VERSION,
                        FrameType.PONG,
                        (short) 0,
                        request.requestId(),
                        new byte[0]
                );


        send(
                pong,
                encoder
        );


        System.out.println(
                "[HANDLER] Sent PONG"
        );
    }


    private void handleFindNode(
            Frame request,
            FrameEncoder encoder,
            JsonPayloadCodec jsonCodec
    ) throws IOException {

        FindNodeRequest findRequest =
                jsonCodec.decode(
                        request.payload(),
                        FindNodeRequest.class
                );


        NodeId target =
                NodeId.fromHex(
                        findRequest.targetNodeId()
                );


        FindNodeService findNodeService =
                new FindNodeService(
                        routingTable,
                        FIND_NODE_LIMIT
                );


        FindNodeResponse response =
                findNodeService.find(
                        target
                );


        Frame responseFrame =
                new Frame(
                        SUPPORTED_VERSION,
                        FrameType.FIND_NODE_RESPONSE,
                        (short) 0,
                        request.requestId(),
                        jsonCodec.encode(
                                response
                        )
                );


        send(
                responseFrame,
                encoder
        );


        System.out.println(
                "[HANDLER] Sent FIND_NODE_RESPONSE: "
                        + response.contacts()
                        .size()
                        + " contacts"
        );
    }


    private void handleStore(
            Frame request,
            FrameEncoder encoder,
            JsonPayloadCodec jsonCodec
    ) throws IOException {

        StoreRequest storeRequest =
                jsonCodec.decode(
                        request.payload(),
                        StoreRequest.class
                );


        System.out.println(
                "[HANDLER] STORE key="
                        + storeRequest
                        .record()
                        .key()
        );


        StoreResult result =
                storageService.store(
                        storeRequest.record()
                );


        StoreResponse response =
                new StoreResponse(
                        result.stored(),
                        result.reason()
                );


        Frame responseFrame =
                new Frame(
                        SUPPORTED_VERSION,
                        FrameType.STORE_RESPONSE,
                        (short) 0,
                        request.requestId(),
                        jsonCodec.encode(
                                response
                        )
                );


        send(
                responseFrame,
                encoder
        );


        System.out.println(
                "[HANDLER] Sent STORE_RESPONSE: "
                        + result.reason()
        );
    }


    private void handleFindValue(Frame request, FrameEncoder encoder, JsonPayloadCodec jsonCodec
    ) throws IOException {

        FindValueRequest findRequest = jsonCodec.decode(request.payload(), FindValueRequest.class);


        System.out.println("[HANDLER] FIND_VALUE key=" + findRequest.key()
        );


        FindValueResponse response = storageService.findValue(findRequest.key()
                );


        Frame responseFrame = new Frame(
                        SUPPORTED_VERSION,
                        FrameType.FIND_VALUE_RESPONSE,
                        (short) 0,
                        request.requestId(),
                        jsonCodec.encode(
                                response
                        )
                );


        send(
                responseFrame,
                encoder
        );


        System.out.println(
                "[HANDLER] Sent FIND_VALUE_RESPONSE, found="
                        + response.found()
        );
    }


    private void send(
            Frame frame,
            FrameEncoder encoder
    ) throws IOException {

        socket.getOutputStream()
                .write(
                        encoder.encode(
                                frame
                        )
                );

        socket.getOutputStream()
                .flush();
    }
}