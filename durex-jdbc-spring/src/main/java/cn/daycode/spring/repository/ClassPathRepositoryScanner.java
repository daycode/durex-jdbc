package cn.daycode.spring.repository;

import cn.daycode.core.annotation.BindEntity;
import cn.daycode.core.orm.Mapper;
import cn.daycode.core.orm.MapperRegister;
import cn.daycode.core.orm.RepositoryRegister;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Scan repository to spring bean in basePackages
 *
 * @author zch
 * @see RepositoryFactoryBean
 * @since 2018/6/13
 */
public class ClassPathRepositoryScanner extends ClassPathBeanDefinitionScanner {

    private RepositoryFactoryBean repositoryFactoryBean = new RepositoryFactoryBean();

    private Class<? extends Annotation> annotationClass;

    public ClassPathRepositoryScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
    }

    /**
     * 设置过滤器
     */
    public void registerFilters() {

        boolean acceptAllInterfaces = true;

        /**
         * 这里只注册特定注解的类
         */
        if (this.annotationClass != null) {
            addIncludeFilter(new AnnotationTypeFilter(this.annotationClass));
            acceptAllInterfaces = false;
        }

        if (acceptAllInterfaces) {
            // default include filter that accepts all classes
            addIncludeFilter(
                    (MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) -> true);
        }

        // exclude package-info.java
        addExcludeFilter(
                (MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) -> {
                    String className = metadataReader.getClassMetadata().getClassName();
                    return className.endsWith("package-info");
                });

    }

    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

        if (beanDefinitions.isEmpty()) {
            logger.warn("No MyBatis mapper was found in '" +
                    Arrays.toString(basePackages) + "' package. Please check your configuration.");
        } else {
            processBeanDefinitions(beanDefinitions);
        }

        return beanDefinitions;
    }

    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        GenericBeanDefinition definition;
        for (BeanDefinitionHolder holder : beanDefinitions) {
            definition = (GenericBeanDefinition) holder.getBeanDefinition();

            if (logger.isDebugEnabled()) {
                logger.debug("Creating RepositoryFactoryBean with name '" + holder.getBeanName()
                        + "' and '" + definition.getBeanClassName() + "' repositoryClass");
            }

            Class<?> repositoryClass = definition.getBeanClass();
            BindEntity bindEntity = repositoryClass.getAnnotation(BindEntity.class);

            if (null != bindEntity) {
                MapperRegister.registerEntity(bindEntity.value());
                RepositoryRegister.repositoryEntity(repositoryClass, bindEntity.value());
            } else {
                logger.error("missing @BindEntity annotation");
                return;
            }

            /**
             * 将 bean 类名设置成 repository 原始类名
             */
            definition.getConstructorArgumentValues().addGenericArgumentValue(definition.getBeanClassName());
            /**
             * 将 bean 类 改成 repository factoryBean 类型的类
             * Spring 将从 getObject() 方法获取到 代理类作为 bean 实例。
             */
            definition.setBeanClass(this.repositoryFactoryBean.getClass());

        }

    }


    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return super.isCandidateComponent(beanDefinition);
    }

    @Override
    protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
        return super.checkCandidate(beanName, beanDefinition);
    }

    public void setRepositoryFactoryBean(RepositoryFactoryBean repositoryFactoryBean) {
        this.repositoryFactoryBean = repositoryFactoryBean != null ? repositoryFactoryBean : new RepositoryFactoryBean();
    }

    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }
}
