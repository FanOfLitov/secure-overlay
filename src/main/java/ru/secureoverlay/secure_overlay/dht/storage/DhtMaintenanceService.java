package ru.secureoverlay.secure_overlay.dht.storage;

public class DhtMaintenanceService {

    private final DhtStore localStore;

    private final ReplicationService replicationService;


    public DhtMaintenanceService(DhtStore localStore, ReplicationService replicationService
    ) {

        this.localStore = localStore;

        this.replicationService = replicationService;
    }


    public ReplicationResult republish(DhtRecord record) {

        if (record.expired()) {

            throw new IllegalArgumentException("Cannot republish expired record");
        }

        return replicationService.replicate(record);
    }


    public int cleanupExpired() {

        return localStore.cleanupExpired();
    }
}