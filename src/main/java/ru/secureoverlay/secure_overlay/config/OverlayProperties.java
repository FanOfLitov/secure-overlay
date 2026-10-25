package ru.secureoverlay.secure_overlay.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "overlay")
public class OverlayProperties {

    private boolean enabled = true;

    private int port = 9001;

    private int nodeIdBits = 256;

    private int kBucketSize = 3;

    private int alpha = 3;

    private int replicationFactor = 3;

    private int tunnelRelays = 2;

    private int socketTimeoutMs = 5000;

    private long recordTtlMs = 300_000;

    private long replayTtlMs = 300_000;


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getNodeIdBits() {
        return nodeIdBits;
    }

    public void setNodeIdBits(int nodeIdBits) {
        this.nodeIdBits = nodeIdBits;
    }

    public int getKBucketSize() {
        return kBucketSize;
    }

    public void setKBucketSize(int kBucketSize) {
        this.kBucketSize = kBucketSize;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public int getReplicationFactor() {
        return replicationFactor;
    }

    public void setReplicationFactor(
            int replicationFactor
    ) {
        this.replicationFactor =
                replicationFactor;
    }

    public int getTunnelRelays() {
        return tunnelRelays;
    }

    public void setTunnelRelays(
            int tunnelRelays
    ) {
        this.tunnelRelays =
                tunnelRelays;
    }

    public int getSocketTimeoutMs() {
        return socketTimeoutMs;
    }

    public void setSocketTimeoutMs(
            int socketTimeoutMs
    ) {
        this.socketTimeoutMs =
                socketTimeoutMs;
    }

    public long getRecordTtlMs() {
        return recordTtlMs;
    }

    public void setRecordTtlMs(
            long recordTtlMs
    ) {
        this.recordTtlMs =
                recordTtlMs;
    }

    public long getReplayTtlMs() {
        return replayTtlMs;
    }

    public void setReplayTtlMs(
            long replayTtlMs
    ) {
        this.replayTtlMs =
                replayTtlMs;
    }
}