package cn.daycode.spring.annotation;

import cn.daycode.spring.repository.RepositoryFactoryBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import java.lang.annotation.*;

/**
 * Use this annotation to register Durex-jdbc repository interfaces when using Java
 *
 * @author zch
 * @see RepositoryScannerRegistrar
 * @see RepositoryFactoryBean
 * @since 2018/6/13
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({RepositoryScannerRegistrar.class})
public @interface RepositoryScan {

    String[] value() default {};

    /**
     * Base packages to scan for Durex-jdbc interfaces. Note that only interfaces
     * with at least one method will be registered; concrete classes will be
     * ignored.
     */
    String[] basePackages() default {};

    /**
     * Specifies a custom RepositoryFactoryBean to return a Durex-jdbc proxy as spring bean.
     */
    Class<? extends RepositoryFactoryBean> factoryBean() default RepositoryFactoryBean.class;

}
