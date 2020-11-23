package sample.server;

import annotation.RpcScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import remoting.transport.netty.server.NettyServer;
import sample.api.CalculatorService;

@RpcScan(basePackage = {"sample"})
public class NettyServerMain {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyServer nettyServer = (NettyServer) applicationContext.getBean("nettyServer");

        nettyServer.start();
    }
}
