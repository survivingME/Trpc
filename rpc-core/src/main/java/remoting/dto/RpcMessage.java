package remoting.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcMessage {
    /**
     * rpc message type
     */
    private byte messageType;
    /**
     * Serialization type
     */
    private byte codec;
    /**
     * compress type
     */
    private byte compress;
    
    private int requestId;
    private Object data;
}
