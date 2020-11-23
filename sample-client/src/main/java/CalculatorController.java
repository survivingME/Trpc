import annotation.RpcReference;
import org.springframework.stereotype.Component;

@Component
public class CalculatorController {

    @RpcReference(group = "test1", version = "1.0")
    private CalculatorService calculatorService;

    public void test() throws InterruptedException {
        Calresult add = this.calculatorService.add(4, 9);
        System.out.println(add);
        Calresult sub = this.calculatorService.sub(4, 9);
        System.out.println(sub);
        Calresult mul = this.calculatorService.mul(4, 9);
        System.out.println(mul);
        Calresult div = this.calculatorService.div(9, 4);
        System.out.println(div);
    }
}
