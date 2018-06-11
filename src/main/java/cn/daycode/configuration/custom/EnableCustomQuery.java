package cn.daycode.configuration.custom;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 自动配置自定义sql
 * Created by jl on 17-7-11.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({CustomQueryRegistrar.class})
public @interface EnableCustomQuery {

    String directory() default "";

    String suffix() default ".sql";

}
