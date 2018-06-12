package cn.daycode.spring.annotation;

import cn.daycode.spring.repository.ClassPathRepositoryScanner;
import cn.daycode.spring.repository.RepositoryFactoryBean;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 仓库注册扫描类
 *
 * @author zch
 * @see RepositoryFactoryBean
 * @see ClassPathRepositoryScanner
 * @since 2018/6/13
 */
public class RepositoryScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private ResourceLoader resourceLoader;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {

        AnnotationAttributes annoAttrs = AnnotationAttributes
                .fromMap(annotationMetadata.getAnnotationAttributes(RepositoryScan.class.getName()));

        // 创建扫描类
        ClassPathRepositoryScanner scanner = new ClassPathRepositoryScanner(registry);

        // this check is needed in Spring 3.1
        if (resourceLoader != null) {
            scanner.setResourceLoader(resourceLoader);
        }

        /**
         * 设置 RepositoryFactoryBean
         */
        Class<? extends RepositoryFactoryBean> mapperFactoryBeanClass = annoAttrs.getClass("factoryBean");
        if (!RepositoryFactoryBean.class.equals(mapperFactoryBeanClass)) {
            scanner.setRepositoryFactoryBean(BeanUtils.instantiateClass(mapperFactoryBeanClass));
        }

        /**
         * 解析包名
         */
        List<String> basePackages = new ArrayList<>();
        for (String pkg : annoAttrs.getStringArray("value")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (String pkg : annoAttrs.getStringArray("basePackages")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }

        /**
         * 扫描过滤
         */
        scanner.registerFilters();
        /**
         * 开始扫描注册
         */
        scanner.doScan(StringUtils.toStringArray(basePackages));

    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
