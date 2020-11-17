package annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcReference {

    /**
     * service version
     * @return
     */
    String version() default "";

    /**
     * service group
     * @return
     */
    String group() default "";

}
