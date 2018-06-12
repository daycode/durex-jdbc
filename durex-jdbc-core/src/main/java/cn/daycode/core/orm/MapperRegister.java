package cn.daycode.core.orm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Mapper 注册器，将一个自定义实体类信息封装到 Mapper 里
 *
 * @author zch
 * @since 2018/6/13
 */
public class MapperRegister {

    private final static Logger logger = LoggerFactory.getLogger(MapperRegister.class);

    public static void registerEntity(Class<?> entityClass) {
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
            if (null != transientType) {
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
                logger.error("method not found : " + setterName, e);
            }

            //getter
            String getterName = toGetterName(field.getName());
            try {
                Method getter = entityClass.getMethod(getterName);
                getterMap.put(field.getName(), getter);
            } catch (NoSuchMethodException e) {
                logger.error("method not found :" + getterName, e);
            }
        }

        Mapper.fieldMap(entityClass, fieldMap);
        Mapper.columnMap(entityClass, colMap);
        Mapper.setterMap(entityClass, setterMap);
        Mapper.getterMap(entityClass, getterMap);
        logger.info(entityClass.getName() + "- load entity success");
    }

    private static String toSetterName(String fieldName) {
        String firstLetter = fieldName.substring(0, 1);
        return "set" + fieldName.replaceFirst(firstLetter, firstLetter.toUpperCase());
    }

    private static String toGetterName(String fieldName) {
        String firstLetter = fieldName.substring(0, 1);
        return "get" + fieldName.replaceFirst(firstLetter, firstLetter.toUpperCase());
    }

    private static String getBeanName(String text) {
        String firstLetter = text.substring(0, 1);
        String firstLow = firstLetter.toLowerCase();
        text = text.replaceFirst(firstLetter, firstLow);
        return text;
    }
}
