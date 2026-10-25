package ru.secureoverlay.secure_overlay.experiment;

import ru.secureoverlay.secure_overlay.identity.NodeId;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class ExperimentNodeIds {

    private ExperimentNodeIds() {
    }


    public static NodeId forIndex(
            int index
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );


            byte[] value =
                    (
                            "secure-overlay-experiment-node-"
                                    + index
                    )
                            .getBytes(
                                    StandardCharsets.UTF_8
                            );


            return new NodeId(
                    digest.digest(value)
            );


        } catch (Exception e) {

            throw new IllegalStateException(
                    "Cannot generate experiment NodeID",
                    e
            );
        }
    }
}