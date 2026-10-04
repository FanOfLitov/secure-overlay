package ru.secureoverlay.secure_overlay.protocol;

public final class RpcValidator {

    private RpcValidator() {
    }

    public static void validateResponse(
            Frame request,
            Frame response
    ) {

        if (!request.requestId()
                .equals(response.requestId())) {

            throw new IllegalArgumentException(
                    "Response request_id does not match request_id"
            );
        }
    }
}