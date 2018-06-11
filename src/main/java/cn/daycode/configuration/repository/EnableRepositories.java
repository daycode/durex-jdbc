package cn.daycode.configuration.repository;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import java.lang.annotation.*;

/**
 * 自动配置仓库
 * Created by jl on 17-7-11.
 */
@Order(value = -Integer.MAX_VALUE)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({RepositoriesRegistrar.class})
public @interface EnableRepositories {

    String basePackage() default "";

}
