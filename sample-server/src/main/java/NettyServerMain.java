import annotation.RpcScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import remoting.transport.netty.server.NettyServer;

@RpcScan(basePackage = {""})
public class NettyServerMain {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyServer nettyServer = (NettyServer) applicationContext.getBean("nettyServer");

        nettyServer.start();
    }
}
