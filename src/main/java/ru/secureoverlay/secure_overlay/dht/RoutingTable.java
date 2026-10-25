package ru.secureoverlay.secure_overlay.dht;


import ru.secureoverlay.secure_overlay.identity.NodeId;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RoutingTable {
    private static final int NODE_ID_BITS =256;
    private final NodeId localNodeId;
    private final List<KBucket> buckets;

    public RoutingTable(
            NodeId localNodeId,
            int bucketCapacity
    ){
        if(localNodeId ==null){
            throw new IllegalArgumentException("Lcal, NodeId cannot be null");
        }
        if(bucketCapacity<=0){
            throw new IllegalArgumentException("Bucket capacity must be possitive");
        }

        this.localNodeId =localNodeId;


        this.buckets=new ArrayList<>(NODE_ID_BITS);

        for(int i=0; i< NODE_ID_BITS;i++){
            buckets.add(new KBucket(localNodeId, bucketCapacity));
        }
    }

    public boolean addOrRefresh(
            Contact contact
    ) {

        if (contact.nodeId()
                .equals(localNodeId)) {

            return false;
        }

        int bucketIndex =
                bucketIndexFor(
                        contact.nodeId()
                );

        return buckets
                .get(bucketIndex)
                .addOrRefresh(contact);
    }

    public int bucketIndexFor(NodeId nodeId) {

        BigInteger distance =
                XorDistance.between(
                        localNodeId,
                        nodeId
                );

        if (distance.equals(BigInteger.ZERO)) {

            throw new IllegalArgumentException(
                    "Local node has no bucket"
            );
        }

        return distance.bitLength() - 1;
    }

    public List<Contact> findClosest(NodeId target, int limit){
        if(target==null){
            throw new IllegalArgumentException("Target cannot be null");
        }
        if(limit<=0){
            throw new IllegalArgumentException("Limit must be positive");
        }

        List<Contact> allContacts=new ArrayList<>();

        for(KBucket bucket:buckets){
            allContacts.addAll(bucket.contacts());
        }

        allContacts.sort(
                Comparator.comparing(contact->XorDistance.between(contact.nodeId(), target))
        );

        return allContacts.stream().limit(limit).toList();
    }


    public int totalContacts(){
        int count=0;

        for (KBucket bucket :buckets){
            count += bucket.size();
        }

        return count;
    }

    public KBucket getBucket(
            int index
    ){
        return buckets.get(index);
    }




}
