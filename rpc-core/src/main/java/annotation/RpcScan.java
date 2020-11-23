package annotation;

import org.springframework.context.annotation.Import;
import spring.CustomScannerRegister;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Import(CustomScannerRegister.class)
@Documented
public @interface RpcScan {
    String[] basePackage();
}
