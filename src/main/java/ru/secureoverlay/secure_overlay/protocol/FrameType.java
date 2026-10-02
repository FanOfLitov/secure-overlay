package ru.secureoverlay.secure_overlay.protocol;

public enum FrameType {

    PING(1),
    PONG(2),

    FIND_NODE_REQUEST(5),
    FIND_NODE_RESPONSE(4),

    STORE_REQUEST(5),
    STORE_RESPONSE(6),

    FIND_VALUE_REQUEST(7),
    FIND_VALUE_RESPONSE(8),

    TUNNEL_BUILD(9),
    TUNNEL_BUILD_OK(10),
    TUNNEL_BUILD_FALL(11),

    TUNNEL_DATA(12),
    TUNNEL_ACK(13),
    APP_MESSAGE(15),
    APP_ACK(16),

    ERROR(17);

    private final int code;

    FrameType(int code){
        this.code=code;
    }

    public int getCode(){
        return code;
    }

    public static FrameType fromCode(int code){
        for(FrameType type :values()){
            if(type.code==code){
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown frame type " + code);
    }

}
