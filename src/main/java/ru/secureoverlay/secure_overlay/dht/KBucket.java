package ru.secureoverlay.secure_overlay.dht;

import ru.secureoverlay.secure_overlay.identity.NodeId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KBucket {

    private final int capacity;
    private final NodeId localNodeId;

    /*
     * index 0 = least recently seen
     * последний = most recently seen
     */
    private final List<Contact> contacts =
            new ArrayList<>();


    public KBucket(
            NodeId localNodeId,
            int capacity
    ) {

        if (localNodeId == null) {
            throw new IllegalArgumentException(
                    "Local NodeId cannot be null"
            );
        }

        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "Capacity must be positive"
            );
        }

        this.localNodeId =
                localNodeId;

        this.capacity =
                capacity;
    }


    public synchronized boolean addOrRefresh(
            Contact contact
    ) {

        if (contact.nodeId()
                .equals(localNodeId)) {

            return false;
        }

        int existingIndex =
                findIndex(
                        contact.nodeId()
                );

        if (existingIndex >= 0) {

            contacts.remove(existingIndex);

            contacts.add(
                    contact.refreshed()
            );

            return true;
        }

        if (contacts.size() >= capacity) {


            return false;
        }

        contacts.add(contact);

        return true;
    }


    public synchronized Contact leastRecentlySeen() {

        if (contacts.isEmpty()) {
            return null;
        }

        return contacts.get(0);
    }


    public synchronized boolean remove(
            NodeId nodeId
    ) {

        return contacts.removeIf(
                contact ->
                        contact.nodeId()
                                .equals(nodeId)
        );
    }


    public synchronized int size() {
        return contacts.size();
    }


    public int capacity() {
        return capacity;
    }


    public synchronized boolean contains(
            NodeId nodeId
    ) {

        return findIndex(nodeId) >= 0;
    }


    public synchronized List<Contact> contacts() {

        return Collections.unmodifiableList(
                new ArrayList<>(contacts)
        );
    }


    private int findIndex(
            NodeId nodeId
    ) {

        for (int i = 0;
             i < contacts.size();
             i++) {

            if (contacts
                    .get(i)
                    .nodeId()
                    .equals(nodeId)) {

                return i;
            }
        }

        return -1;
    }
}