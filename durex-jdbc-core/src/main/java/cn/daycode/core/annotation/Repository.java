package cn.daycode.core.annotation;

import java.lang.annotation.*;

/**
 * 标记仓库类
 *
 * @author zch
 * @since 2018/6/13
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface Repository {

}
