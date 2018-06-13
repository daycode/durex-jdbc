package cn.daycode.core.mapping;

import cn.daycode.core.parsing.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义实体注册
 * Created by jason on 17-4-14.
 */
public class Mapper {

    private static final Logger logger = LoggerFactory.getLogger(Mapper.class);

    //repository 注册包
    public static String basePackages;

    //自定义Query路径
    public static String baseDirectory;

    private static Map<Class<?>, Map<String, String>> classFieldMap = new HashMap<>();

    private static Map<Class<?>, Map<String, String>> classColMap = new HashMap<>();

    private static Map<Class<?>, Map<String, Method>> classGetterMap = new HashMap<>();

    private static Map<Class<?>, Map<String, Method>> classSetterMap = new HashMap<>();

    private static Map<String, Class<?>> fieldTypeMap = new HashMap<>();

    private static Map<Class<?>, String> idMap = new HashMap<>();

    private static Map<Class<?>, String> tableMap = new HashMap<>();

    private static Map<String, String> SQLMap = new HashMap<>();

    public static Class<?> getFieldType(String field) {
        return fieldTypeMap.get(field);
    }

    public static String getColumnName(Class<?> clazz, String fieldName) {
        return classFieldMap.get(clazz).get(fieldName);
    }

    public static void fieldMap(Class<?> clazz, Map<String, String> map) {
        classFieldMap.put(clazz, map);
    }

    public static Map<String, String> fieldMap(Class<?> clazz) {
        return classFieldMap.get(clazz);
    }

    public static void columnMap(Class<?> clazz, Map<String, String> map) {
        classColMap.put(clazz, map);
    }

    public static Map<String, String> columnMap(Class<?> clazz) {
        return classColMap.get(clazz);
    }

    public static String getFieldName(Class<?> clazz, String columnName) {
        return classColMap.get(clazz).get(columnName);
    }

    public static Method getGetter(Class<?> clazz, String fieldName) {
        return classGetterMap.get(clazz).get(fieldName);
    }

    public static Method getSetter(Class<?> clazz, String fieldName) {
        return classSetterMap.get(clazz).get(fieldName);
    }

    private static String toSetterName(String fieldName) {
        String firstLetter = fieldName.substring(0, 1);
        return "set" + fieldName.replaceFirst(firstLetter, firstLetter.toUpperCase());
    }

    private static String toGetterName(String fieldName) {
        String firstLetter = fieldName.substring(0, 1);
        return "get" + fieldName.replaceFirst(firstLetter, firstLetter.toUpperCase());
    }

    public static Method getter(Class<?> entityClass, String field) {
        return classGetterMap.get(entityClass).get(field);
    }

    public static Method getter(Class<?> entityClass, String field, Method method) {
        return classGetterMap.get(entityClass).put(field, method);
    }

    public static Method setter(Class<?> entityClass, String field) {
        return classSetterMap.get(entityClass).get(field);
    }

    public static void setter(Class<?> entityClass, String field, Method method) {
        classSetterMap.get(entityClass).put(field, method);
    }

    public static <K> void value(K k, String field, Object value) {
        Method method = setter(k.getClass(), field);

        try {
            if (Number.class.equals(value.getClass().getSuperclass())) {
                method.invoke(k, (Number) value);
            } else {
                method.invoke(k, value);
            }
        } catch (Exception e) {
            logger.error("set value 失败 class:" + k.getClass() + " field:" + field + " valueType:" + value.getClass() + " value:" + value, e);
        }
    }

    public static <K> Object value(K k, String field) {
        Method method = getter(k.getClass(), field);
        try {
            return method.invoke(k);
        } catch (Exception e) {
            logger.error("get value 失败", e);
        }
        return null;
    }

    /**
     * 保存entity的id名
     *
     * @param entityClass 实体类
     * @param name        id属性名
     */
    public static void id(Class<?> entityClass, String name) {
        idMap.put(entityClass, name);
    }

    /**
     * 获取entity的id属性名
     *
     * @param entityClass 实体类
     * @return id属性名
     */
    public static String id(Class<?> entityClass) {
        return idMap.get(entityClass);
    }

    public static void setterMap(Class<?> entityClass, Map<String, Method> setterMap) {
        classSetterMap.put(entityClass, setterMap);
    }

    public static void getterMap(Class<?> entityClass, Map<String, Method> getterMap) {
        classGetterMap.put(entityClass, getterMap);
    }

    public static void tableMap(Class<?> entityClass, String table) {
        tableMap.put(entityClass, table);
    }

    public static void fieldTypeMap(String fieldName, Class<?> type) {
        fieldTypeMap.put(fieldName, type);
    }

    public static String table(Class<?> entityClass) {
        return tableMap.get(entityClass);
    }

    public static void sql(String key, String value) {
        SQLMap.put(key, value);
    }

    public static String sql(String key) {
        return SQLMap.get(key);
    }

    public static void loadSql(String directory, String suffix) {

        List<File> files = Scanner.getFiles(directory, suffix);

        for (File file : files) {
            List<String> lines;
            try {
                lines = Files.readAllLines(Paths.get(file.getPath()));
                boolean skipNext = false;
                for (int index = 0; index < lines.size(); index++) {
                    if (lines.get(index).trim().isEmpty()) {
                        continue;
                    }

                    if (skipNext) {
                        skipNext = false;
                        continue;
                    }
                    if (lines.get(index).startsWith("--")) {
                        Mapper.sql(lines.get(index).replace("--", ""), lines.get(index + 1));
                        skipNext = true;
                    }
                }
            } catch (IOException e) {
                logger.error("读取文件失败", e);
            }
            logger.info("load " + file.getName() + " finish");
        }
    }
}
