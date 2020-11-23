package sample.client;

import annotation.RpcReference;
import org.springframework.stereotype.Component;
import sample.api.CalculatorService;
import sample.api.Calresult;

@Component
public class CalculatorController {

    @RpcReference(group = "test1", version = "1.0")
    private CalculatorService calculatorService;

    public void test() throws InterruptedException {
        Calresult add = calculatorService.add(4, 9);
        System.out.println(add);
        Calresult sub = calculatorService.sub(4, 9);
        System.out.println(sub);
        Calresult mul = calculatorService.mul(4, 9);
        System.out.println(mul);
        Calresult div = calculatorService.div(9, 4);
        System.out.println(div);
    }
}
