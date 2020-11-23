package sample.server;

import annotation.RpcService;
import lombok.extern.slf4j.Slf4j;
import sample.api.CalculatorService;
import sample.api.Calresult;

@Slf4j
@RpcService(group = "test1", version = "1.0")
public class CalculatorServiceImpl implements CalculatorService {
    static {
        log.info("CalculatorServiceImpl创建");
    }

    @Override
    public Calresult add(Integer a, Integer b) {
        if(b > Integer.MAX_VALUE - a) {
            return Calresult.builder().message("result overflow").result(null).build();
        } else {
            Integer res = a + b;
            return Calresult.builder().message("ok").result(res.longValue()).build();
        }
    }

    @Override
    public Calresult sub(Integer a, Integer b) {
        Integer res = a - b;
        return Calresult.builder().message("ok").result(res.longValue()).build();
    }

    @Override
    public Calresult mul(Integer a, Integer b) {
        if(Math.abs(b) > Math.abs(Integer.MAX_VALUE / a)) {
            return Calresult.builder().message("result overflow").result(null).build();
        } else {
            Integer res = a * b;
            return Calresult.builder().message("ok").result(res.longValue()).build();
        }
    }

    @Override
    public Calresult div(Integer a, Integer b) {
        if(Math.abs(b) > Math.abs(Integer.MAX_VALUE * a)) {
            return Calresult.builder().message("result overflow").result(null).build();
        } else {
            Integer res = a / b;
            return Calresult.builder().message("ok").result(res.longValue()).build();
        }
    }
}
