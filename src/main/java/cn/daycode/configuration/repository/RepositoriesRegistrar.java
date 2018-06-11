package cn.daycode.configuration.repository;

import cn.daycode.annotation.BindEntity;
import cn.daycode.orm.*;
import cn.daycode.orm.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class RepositoriesRegistrar implements ImportBeanDefinitionRegistrar {

    private Logger logger = LoggerFactory.getLogger(RepositoriesRegistrar.class);

    RepositoriesRegistrar() {

    }

    protected Class<? extends Annotation> getAnnotation() {
        return EnableRepositories.class;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(EnableRepositories.class.getName()));
        String basePackage = attributes.getString("basePackage");
        //扫描包里类
        List<Class<?>> repositoryClasses = Scanner.getClasses(basePackage);

        for (Class<?> repositoryClass : repositoryClasses) {
            registerRepository(repositoryClass, beanDefinitionRegistry);
        }

        logger.info(" - 注册仓库完成");
    }

    private void registerRepository(Class<?> repositoryClass, BeanDefinitionRegistry registry) {
        Class<?>[] interfaces = repositoryClass.getInterfaces();

        if (null == interfaces || 0 == interfaces.length) {
            return;
        }
        for (Class<?> interfaceClass : interfaces) {
            if (interfaceClass.equals(Repository.class)) {

                BindEntity bindEntity = repositoryClass.getAnnotation(BindEntity.class);

                if (null != bindEntity) {
                    registerEntity(bindEntity.value());
                    RepositoryRegister.repositoryEntity(repositoryClass, bindEntity.value());
                }else {
                    logger.error("missing @BindEntity annotation");
                    return;
                }

                RootBeanDefinition beanDefinition = new RootBeanDefinition();
                beanDefinition.setBeanClass(RepositoryFactoryBean.class);
                beanDefinition.setLazyInit(true);
                beanDefinition.getPropertyValues().addPropertyValue("repositoryClass", repositoryClass);
                String beanName = getBeanName(repositoryClass.getSimpleName());
                registry.registerBeanDefinition(beanName, beanDefinition);
                logger.info(" - {} - load repository success", repositoryClass.getName());
            }
        }
    }

    private void registerEntity(Class<?> entityClass) {
        Field[] fields = entityClass.getDeclaredFields();
        Map<String, String> fieldMap = new HashMap<>();
        Map<String, String> colMap = new HashMap<>();
        Map<String, Method> setterMap = new HashMap<>();
        Map<String, Method> getterMap = new HashMap<>();

        List<Field> fieldList = new ArrayList<>();

        fieldList.addAll(Arrays.asList(fields));

        Table table = entityClass.getAnnotation(Table.class);
        if (null != table) {
            String tableName = table.name();
            Mapper.tableMap(entityClass, tableName);
        } else {
            logger.error(entityClass.getName() + " does not have @Table");
        }

        Class<?> superClass = entityClass.getSuperclass();
        if (null != superClass) {
            Field[] superFields = superClass.getDeclaredFields();

            fieldList.addAll(Arrays.asList(superFields));
        }

        for (Field field : fieldList) {
            Column column = field.getAnnotation(Column.class);

            if (null == column) {
                continue;
            }

            Transient transientType = field.getAnnotation(Transient.class);
            if(null != transientType) {
                continue;
            }

            String colName = column.name();

            Id id = field.getAnnotation(Id.class);
            if (null != id) {
                Mapper.id(entityClass, field.getName());
            }

            //obj field to data col
            fieldMap.put(field.getName(), colName);

            //data col to object field
            colMap.put(colName, field.getName());
            //setter
            String setterName = toSetterName(field.getName());

            //get field and type
            Mapper.fieldTypeMap(field.getName(), field.getType());

            try {
                Method setter = entityClass.getMethod(setterName, field.getType());
                setterMap.put(field.getName(), setter);
            } catch (NoSuchMethodException e) {
                logger.error("method not found :{}", setterName, e);
            }

            //getter
            String getterName = toGetterName(field.getName());
            try {
                Method getter = entityClass.getMethod(getterName);
                getterMap.put(field.getName(), getter);
            } catch (NoSuchMethodException e) {
                logger.error("method not found :{}", getterName, e);
            }
        }

        Mapper.fieldMap(entityClass, fieldMap);
        Mapper.columnMap(entityClass, colMap);
        Mapper.setterMap(entityClass, setterMap);
        Mapper.getterMap(entityClass, getterMap);
        logger.info(" - {} - load entity success", entityClass.getName());
    }

    private static String toSetterName(String fieldName) {
        String firstLetter = fieldName.substring(0, 1);
        return "set" + fieldName.replaceFirst(firstLetter, firstLetter.toUpperCase());
    }

    private static String toGetterName(String fieldName) {
        String firstLetter = fieldName.substring(0, 1);
        return "get" + fieldName.replaceFirst(firstLetter, firstLetter.toUpperCase());
    }

    private String getBeanName(String text) {
        String firstLetter = text.substring(0, 1);
        String firstLow = firstLetter.toLowerCase();
        text = text.replaceFirst(firstLetter, firstLow);
        return text;
    }
}
