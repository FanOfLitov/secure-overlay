package ru.secureoverlay.secure_overlay.identity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;


class IdentityServiceTest {
    @TempDir
    Path tempDirectory;

    @Test
    void shouldKeepSameNodeIdAfterRestart(){
        IdentityService service = new IdentityService();

        NodeIdentity first=service.loadOrCreate(tempDirectory);

        NodeIdentity second = service.loadOrCreate(tempDirectory);

        assertEquals(first.nodeId(), second.nodeId());

        assertArrayEquals(first.keyPair()
                .getPublic()
                .getEncoded(),

                second.keyPair()
                        .getPublic()
                        .getEncoded()
        )   ;
    }

    @Test
    void nodeIdShouldMatchPublicKey() {

        IdentityService service =
                new IdentityService();

        NodeIdentity identity =
                service.loadOrCreate(
                        tempDirectory
                );

        boolean matches =
                service.matches(
                        identity.nodeId(),
                        identity.keyPair()
                                .getPublic()
                );

        assertTrue(matches);
    }


    @Test
    void shouldRejectDifferentPublicKey(){
        IdentityService service = new IdentityService();
        NodeIdentity identityA =
                service.loadOrCreate(
                        tempDirectory.resolve("A")
                );

        NodeIdentity identityB =
                service.loadOrCreate(
                        tempDirectory.resolve("B")
                );

        boolean matches = service.matches(identityA.nodeId(), identityB.keyPair().getPublic());


    }
}



