package remoting.transport.netty.codec;

import enums.CompressTypeEnum;
import enums.SerializationTypeEnum;
import extension.ExtensionLoader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import remoting.RpcConstants;
import remoting.dto.RpcMessage;
import remoting.dto.RpcRequest;
import remoting.dto.RpcResponse;
import utils.compress.Compress;
import utils.serialize.Serializer;

import java.util.Arrays;

/**
 * <p>
 * custom protocol decoder
 * <p>
 * <pre>
 *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 * body（object类型数据）
 * </pre>
 * @see <a href="https://zhuanlan.zhihu.com/p/95621344">LengthFieldBasedFrameDecoder解码器</a>
 */

@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    public RpcMessageDecoder() {
        // lengthFieldOffset: magic code is 4B, and version is 1B, and then full length. so value is 5
        // lengthFieldLength: full length is 4B. so value is 4
        // lengthAdjustment: full length include all data and read 9 bytes before, so the left length is (fullLength-9). so values is -9
        // initialBytesToStrip: we will check magic code and version manually, so do not strip any bytes. so values is 0
        this(RpcConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }

    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                             int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if(decoded instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decoded;
            if(frame.readableBytes() >= RpcConstants.TOTAL_LENGTH) {
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("Decode frame error: ", e);
                    throw e;
                } finally {
                    frame.release();
                }
            }
        }
        return decoded;
    }

    private Object decodeFrame(ByteBuf byteBuf) {
        //check magic number
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] magic_number = new byte[len];
        byteBuf.readBytes(len);
        for(int i = 0;i < len;i++) {
            if(magic_number[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Error magic number: " + Arrays.toString(magic_number));
            }
        }
        //check version
        byte version = byteBuf.readByte();
        if(version != RpcConstants.VERSION) {
            throw new IllegalArgumentException("Error version: " + version);
        }
        //build RpcMessage
        int fullLength = byteBuf.readInt();
        byte messageType = byteBuf.readByte();
        byte codecType = byteBuf.readByte();
        byte compressType = byteBuf.readByte();
        int requestId = byteBuf.readInt();
        RpcMessage rpcMessage = RpcMessage.builder()
                .messageType(messageType)
                .codec(codecType)
                .compress(compressType)
                .requestId(requestId).build();
        if(messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
            rpcMessage.setData(RpcConstants.PING);
        } else if(messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
            rpcMessage.setData(RpcConstants.PONG);
        } else {
            int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
            if(bodyLength > 0) {
                byte[] data = new byte[bodyLength];
                byteBuf.readBytes(data);
                //decompress
                String compressName = CompressTypeEnum.getName(compressType);
                Compress compress = ExtensionLoader.getExtensionLoader(Compress.class).getExtension(compressName);
                data = compress.decompress(data);
                //decompress
                String codecName = SerializationTypeEnum.getName(codecType);
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(codecName);
                if(messageType == RpcConstants.REQUEST_TYPE) {
                    RpcRequest rpcRequest = serializer.deserialize(data, RpcRequest.class);
                    rpcMessage.setData(rpcRequest);
                } else {
                    RpcResponse rpcResponse = serializer.deserialize(data, RpcResponse.class);
                    rpcMessage.setData(rpcResponse);
                }
            }
        }
        return rpcMessage;
    }
}
