package sample.client;

import annotation.RpcReference;
import annotation.RpcScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import sample.api.CalculatorService;

@RpcScan(basePackage = {"sample"})
public class NettyClientMain {
    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyClientMain.class);
        CalculatorController controller = (CalculatorController) applicationContext.getBean("CalculatorController");
        controller.test();
    }
}
