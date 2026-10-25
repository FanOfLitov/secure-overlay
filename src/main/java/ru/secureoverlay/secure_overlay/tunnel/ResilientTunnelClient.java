package ru.secureoverlay.secure_overlay.tunnel;

import ru.secureoverlay.secure_overlay.dht.Contact;
import ru.secureoverlay.secure_overlay.tunnel.rpc.TunnelAck;

import java.util.List;

public class ResilientTunnelClient {

    private final TunnelClient tunnelClient;

    private final List<Contact> primaryRoute;

    private final List<Contact> backupRoute;

    private BuiltTunnel activeTunnel;


    public ResilientTunnelClient(
            TunnelClient tunnelClient,
            List<Contact> primaryRoute,
            List<Contact> backupRoute
    ) {

        if (primaryRoute == null
                || primaryRoute.size() < 3) {

            throw new IllegalArgumentException(
                    "Primary route requires at least 2 relays and destination"
            );
        }

        if (backupRoute == null
                || backupRoute.size() < 3) {

            throw new IllegalArgumentException(
                    "Backup route requires at least 2 relays and destination"
            );
        }

        this.tunnelClient =
                tunnelClient;

        this.primaryRoute =
                List.copyOf(
                        primaryRoute
                );

        this.backupRoute =
                List.copyOf(
                        backupRoute
                );
    }


    public TunnelBuildMetric connect()
            throws Exception {

        TunnelBuildMetric metric =
                tunnelClient
                        .buildMeasured(
                                primaryRoute
                        );

        activeTunnel =
                metric.tunnel();

        return metric;
    }


    public ResilientSendResult send(
            byte[] payload
    ) {

        if (activeTunnel == null) {

            return new ResilientSendResult(
                    false,
                    false,
                    0,
                    0,
                    "NO_ACTIVE_TUNNEL"
            );
        }


        long sendStarted =
                System.nanoTime();


        /*
         * Сначала пробуем активный tunnel.
         */
        try {

            TunnelAck ack =
                    tunnelClient.send(
                            activeTunnel,
                            payload
                    );


            if (ack.delivered()) {

                long latency =
                        (
                                System.nanoTime()
                                        - sendStarted
                        )
                                / 1_000_000;


                return new ResilientSendResult(
                        true,
                        false,
                        0,
                        latency,
                        "DELIVERED"
                );
            }

        } catch (Exception ignored) {

            /*
             * Падение relay должно приводить
             * сюда.
             */
        }


        /*
         * Активный tunnel сломан.
         * Начинаем recovery.
         */
        long recoveryStarted =
                System.nanoTime();


        try {

            BuiltTunnel replacement =
                    tunnelClient.build(
                            backupRoute
                    );


            activeTunnel =
                    replacement;


            TunnelAck retry =
                    tunnelClient.send(
                            replacement,
                            payload
                    );


            long recoveryMs =
                    (
                            System.nanoTime()
                                    - recoveryStarted
                    )
                            / 1_000_000;


            long totalLatency =
                    (
                            System.nanoTime()
                                    - sendStarted
                    )
                            / 1_000_000;


            if (!retry.delivered()) {

                return new ResilientSendResult(
                        false,
                        true,
                        recoveryMs,
                        totalLatency,
                        retry.reason()
                );
            }


            return new ResilientSendResult(
                    true,
                    true,
                    recoveryMs,
                    totalLatency,
                    "RECOVERED"
            );


        } catch (Exception e) {

            long recoveryMs =
                    (
                            System.nanoTime()
                                    - recoveryStarted
                    )
                            / 1_000_000;


            return new ResilientSendResult(
                    false,
                    true,
                    recoveryMs,
                    0,
                    "RECOVERY_FAILED: "
                            + e.getMessage()
            );
        }
    }


    public BuiltTunnel activeTunnel() {

        return activeTunnel;
    }
}