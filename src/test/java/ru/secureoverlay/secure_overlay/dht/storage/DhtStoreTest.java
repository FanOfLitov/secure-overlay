package ru.secureoverlay.secure_overlay.dht.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.secureoverlay.secure_overlay.identity.IdentityService;
import ru.secureoverlay.secure_overlay.identity.NodeIdentity;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DhtStoreTest {

    @TempDir
    Path tempDir;


    @Test
    void shouldStoreValidSignedRecord() {

        NodeIdentity identity = new IdentityService().loadOrCreate(tempDir);

        DhtRecord record = DhtRecordSigner.sign("test-key", "HELLO", 1, System.currentTimeMillis() + 60_000, identity);

        DhtStore store = new DhtStore();

        StoreResult result = store.put(record);

        assertTrue(result.stored());

        assertEquals("HELLO", store.get("test-key").orElseThrow().value());
    }


    @Test
    void shouldRejectOldVersion() {

        NodeIdentity identity = new IdentityService().loadOrCreate(tempDir);

        DhtStore store =  new DhtStore();

        DhtRecord version2 =  DhtRecordSigner.sign("key", "NEW", 2, System.currentTimeMillis() + 60_000, identity);

        DhtRecord version1 =
                DhtRecordSigner.sign("key", "OLD", 1, System.currentTimeMillis() + 60_000, identity
                );

        assertTrue(store.put(version2).stored());

        StoreResult result = store.put(version1);

        assertFalse(result.stored());

        assertEquals("STALE_VERSION", result.reason()
        );
    }


    @Test
    void shouldRejectModifiedRecord() {

        NodeIdentity identity = new IdentityService().loadOrCreate(tempDir);

        DhtRecord original = DhtRecordSigner.sign(
                        "key",
                        "ORIGINAL",
                        1,
                        System.currentTimeMillis()
                                + 60_000,
                        identity
                );

        DhtRecord modified = new DhtRecord(
                        original.key(),
                        "HACKED",
                        original.version(),
                        original.expiresAtEpochMs(),
                        original.ownerPublicKey(),
                        original.signature()
                );

        DhtStore store = new DhtStore();

        assertFalse(store.put(modified).stored());
    }
    @Test
    void shouldAcceptSameRecordAgain() {

        NodeIdentity identity =
                new IdentityService()
                        .loadOrCreate(
                                tempDir
                        );

        DhtRecord record =
                DhtRecordSigner.sign(
                        "key",
                        "VALUE",
                        1,
                        System.currentTimeMillis()
                                + 60_000,
                        identity
                );


        DhtStore store =
                new DhtStore();


        StoreResult first =
                store.put(record);

        StoreResult second =
                store.put(record);


        assertTrue(
                first.stored()
        );

        assertTrue(
                second.stored()
        );

        assertEquals(
                "ALREADY_STORED",
                second.reason()
        );
    }

    @Test
    void shouldRejectDifferentOwner() {

        NodeIdentity owner =
                new IdentityService()
                        .loadOrCreate(
                                tempDir.resolve(
                                        "owner"
                                )
                        );

        NodeIdentity attacker =
                new IdentityService()
                        .loadOrCreate(
                                tempDir.resolve(
                                        "attacker"
                                )
                        );


        DhtRecord original =
                DhtRecordSigner.sign(
                        "important-key",
                        "ORIGINAL",
                        1,
                        System.currentTimeMillis()
                                + 60_000,
                        owner
                );


        DhtRecord attack =
                DhtRecordSigner.sign(
                        "important-key",
                        "HACKED",
                        999,
                        System.currentTimeMillis()
                                + 60_000,
                        attacker
                );


        DhtStore store =
                new DhtStore();


        assertTrue(
                store.put(original)
                        .stored()
        );


        StoreResult result =
                store.put(attack);


        assertFalse(
                result.stored()
        );

        assertEquals(
                "OWNER_MISMATCH",
                result.reason()
        );


        assertEquals(
                "ORIGINAL",
                store.get(
                                "important-key"
                        )
                        .orElseThrow()
                        .value()
        );
    }



    @Test
    void shouldRemoveExpiredRecord() {

        NodeIdentity identity =
                new IdentityService()
                        .loadOrCreate(
                                tempDir
                        );


        DhtRecord expired =
                DhtRecordSigner.sign(
                        "expired-key",
                        "VALUE",
                        1,
                        System.currentTimeMillis()
                                - 1,
                        identity
                );


        DhtStore store =
                new DhtStore();


        StoreResult result =
                store.put(
                        expired
                );


        assertFalse(
                result.stored()
        );

        assertEquals(
                "EXPIRED",
                result.reason()
        );

        assertTrue(
                store.get(
                        "expired-key"
                ).isEmpty()
        );
    }


}

