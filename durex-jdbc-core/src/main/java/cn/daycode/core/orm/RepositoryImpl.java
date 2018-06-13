package cn.daycode.core.orm;

import cn.daycode.core.mapping.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Table;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 公用仓库实现类
 * Created by jl on 17-7-6.
 */
public class RepositoryImpl<T, ID extends Serializable> implements Repository<T, ID> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Class<T> entityClass;

    private Class<?> repository;

    private String tableName;

    private JdbcTemplate jdbcTemplate;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public RepositoryImpl(Class<T> entityClass, Class<?> repositoryClass) {
        this.entityClass = entityClass;
        repository = repositoryClass;
        Table table = entityClass.getAnnotation(Table.class);
        tableName = table.name();
    }

    public RepositoryImpl<T, ID> setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        return this;
    }

    public RepositoryImpl<T, ID> setNamedParameterJdbcTemplate(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        return this;
    }

    private ID insertWithId(String sql, Object... objects) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 1; i <= objects.length; i++) {
                preparedStatement.setObject(i, objects[i - 1]);
            }
            return preparedStatement;
        }, keyHolder);
        return (ID) keyHolder.getKey();
    }

    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }

    @Transactional
    @Override
    public T save(T entity) {
        StringBuilder sql = new StringBuilder("INSERT INTO ")
                .append(tableName)
                .append("(");
        StringBuilder values = new StringBuilder();
        List<Object> params = new ArrayList<>();
        Map<String, String> columnfieldMap = Mapper.columnMap(entityClass);
        for (Map.Entry<String, String> entry : columnfieldMap.entrySet()) {
            Object value = Mapper.value(entity, entry.getValue());
            if (null == value) {
                continue;
            }
            sql.append(entry.getKey())
                    .append(",");
            values.append("?")
                    .append(",");
            params.add(value);
        }
        sql.deleteCharAt(sql.length() - 1)
                .append(")")
                .append(" VALUES (")
                .append(values).deleteCharAt(sql.length() - 1)
                .append(")");
        String idField = Mapper.id(entityClass);
        ID id = insertWithId(sql.toString(), params.toArray());
        Mapper.value(entity, idField, id);
        return entity;
    }


    @Transactional
    @Override
    public T update(T entity) {
        List<Object> params = new ArrayList<>();

        StringBuilder sql = new StringBuilder("UPDATE ")
                .append(tableName)
                .append(" SET ");

        Map<String, String> columnfieldMap = Mapper.columnMap(entityClass);
        for (Map.Entry<String, String> entry : columnfieldMap.entrySet()) {

            sql.append(entry.getKey())
                    .append(" = ?,");
            //获取属性值
            params.add(Mapper.value(entity, entry.getValue()));
        }

        sql.deleteCharAt(sql.length() - 1);

        String idField = Mapper.id(entityClass);

        sql.append(" WHERE ")
                .append(Mapper.getColumnName(entityClass, idField))
                .append(" = ?");

        params.add(getId(entity));
        jdbcTemplate.update(sql.toString(), params.toArray());
        return entity;
    }

    @Override
    public T findById(ID id) {
        String sql = "SELECT * FROM ${TABLE} WHERE ${ID} = ?";
        sql = fillSQL(sql);
        List<T> results = jdbcTemplate.query(sql, new CommonRowMapper<>(entityClass), id);
        if (null == results || results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    @Override
    public List<T> findAll() {
        String sql = "SELECT * FROM ${TABLE} ";
        sql = fillSQL(sql);
        return jdbcTemplate.query(sql, new CommonRowMapper<>(entityClass));
    }

    @Override
    public List<T> find(String sql, Object... params) {
        return jdbcTemplate.query(sql, new CommonRowMapper<>(entityClass), params);
    }

    @Override
    public void delete(ID id) {
        String sql = "DELETE FROM ${TABLE} WHERE ${ID} = ?";
        sql = fillSQL(sql);
        jdbcTemplate.update(sql, id);
    }

    public T findFirst(String sql, Object... objects) {
        List<T> entitys = jdbcTemplate.query(sql, new CommonRowMapper<T>(entityClass), objects);
        if (null == entitys || entitys.isEmpty()) {
            return null;
        }
        return entitys.get(0);
    }

    @Transactional
    public void update(String updateQuery) {
        jdbcTemplate.update(updateQuery);
    }

    @Transactional
    public void update(String updateQuery, Object... params) {
        jdbcTemplate.update(updateQuery, params);
    }

    @Transactional
    @Override
    public void update(Map<String, Object> params, ID id) {
        if (null == id) {
            logger.error("");
            return;
        }
        if (null == params || params.isEmpty()) {
            return;
        }
        StringBuilder sqlBuilder = new StringBuilder("UPDATE ")
                .append(tableName)
                .append(" SET ");
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String colName = Mapper.getColumnName(entityClass, entry.getKey());
            sqlBuilder.append(colName)
                    .append(" = :")
                    .append(entry.getKey())
                    .append(",");
        }
        sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
        sqlBuilder.append(" WHERE ")
                .append(Mapper.id(entityClass))
                .append(" = :id");
        params.put(":id", id);
        namedParameterJdbcTemplate.update(sqlBuilder.toString(), params);
    }

    public List<T> findList(String selectQuery, Object... params) {
        return jdbcTemplate.query(selectQuery, new CommonRowMapper<>(entityClass), params);
    }

    public List<T> findList(String selectQuery, Map<String, Object> params) {
        return namedParameterJdbcTemplate.query(selectQuery, params, new CommonRowMapper<>(entityClass));
    }

    @Override
    public long countByMap(String query, Map<String, Object> params) {
        List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(query, params);
        if (null != results && !results.isEmpty()) {
            Map<String, Object> map = results.get(0);
            if (null != map && !map.isEmpty()) {
                for (Object obj : map.values()) {
                    if (null == obj) {
                        return 0;
                    }
                    return (Long) obj;
                }
            } else {
                return 0;
            }
        } else {
            return 0;
        }
        return 0;
    }

    public long count() {
        String sql = "SELECT COUNT(1) FROM " + tableName + ";";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        if (null != results && !results.isEmpty()) {
            Map<String, Object> map = results.get(0);
            if (null != map && !map.isEmpty()) {
                for (Object obj : map.values()) {
                    if (null == obj) {
                        return 0;
                    }
                    return (Long) obj;
                }
            } else {
                return 0;
            }
        } else {
            return 0;
        }
        return 0;
    }

    public long count(String query, Object... params) {
        List<Map<String, Object>> results = jdbcTemplate.queryForList(query, params);
        if (null != results && !results.isEmpty()) {
            Map<String, Object> map = results.get(0);
            if (null != map && !map.isEmpty()) {
                for (Object obj : map.values()) {
                    if (null == obj) {
                        return 0;
                    }
                    return (Long) obj;
                }
            } else {
                return 0;
            }
        } else {
            return 0;
        }
        return 0;
    }

    public List<T> findByMap(Map<String, Object> params) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM " + tableName + " Where 1 = 1");
        for (Map.Entry<String, Object> param : params.entrySet()) {
            sqlBuilder.append(" AND ")
                    .append(Mapper.getColumnName(entityClass, param.getKey()))
                    .append(" = ")
                    .append(":")
                    .append(param.getKey())
                    .append(" ");
        }

//        Query query = entityManager.createNativeQuery(sqlBuilder.toString(), entityClass);
//        for(Map.Entry<String, Object> entry : params.entrySet()) {
//            query.setParameter(entry.getKey(), entry.getValue());
//        }

        return namedParameterJdbcTemplate.query(sqlBuilder.toString(), params, new CommonRowMapper<>(entityClass));
    }

    public T findFirstByMap(Map<String, Object> params) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM " + tableName + " WHERE 1 = 1");
        for (Map.Entry<String, Object> param : params.entrySet()) {
            sqlBuilder.append(" AND ")
                    .append(Mapper.getColumnName(entityClass, param.getKey()))
                    .append(" = ")
                    .append(":")
                    .append(param.getKey())
                    .append(" ");
        }
        sqlBuilder.append(" LIMIT 1");

        List<T> entitys = namedParameterJdbcTemplate.query(sqlBuilder.toString(), params, new CommonRowMapper<>(entityClass));
        if (null != entitys && !entitys.isEmpty()) {
            return entitys.get(0);
        } else {
            return null;
        }
    }

    /**
     * 返回自定义对象
     *
     * @param sql   语句
     * @param clazz 载体类
     * @param <K>   模板
     * @return 模板实例
     */
    public <K> K findOne(String sql, Map<String, Object> params, Class<K> clazz) {
        StringBuilder sqlBuilder = new StringBuilder(sql)
                .append(" LIMIT 1");

        List<K> resultList = namedParameterJdbcTemplate.query(sqlBuilder.toString(), params, new CommonRowMapper<>(clazz));

        if (null != resultList && !resultList.isEmpty()) {
            return resultList.get(0);
        } else {
            return null;
        }
    }

    public <K> List<K> findList(String sql, Class<K> clazz) {
        return jdbcTemplate.query(sql, new CommonRowMapper<>(clazz));
    }

    /**
     * 返回自定义对象数组数据类型 , 可传参
     *
     * @param sql    语句
     * @param clazz  自定义entity类
     * @param params 参数
     * @param <K>    模板
     * @return List<K>
     */
    public <K> List<K> findList(String sql, Class<K> clazz, Map<String, Object> params) {
        return namedParameterJdbcTemplate.query(sql, params, new CommonRowMapper<>(clazz));

    }

    @Transactional
    public void deleteByMap(Map<String, Object> params) {
        StringBuilder sqlBuilder = new StringBuilder("DELETE FROM " + tableName + " WHERE 1 = 1");
        for (Map.Entry<String, Object> param : params.entrySet()) {
            sqlBuilder.append(" AND ")
                    .append(Mapper.getColumnName(entityClass, param.getKey()))
                    .append(" = ")
                    .append(":")
                    .append(param.getKey())
                    .append(" ");
        }
        namedParameterJdbcTemplate.update(sqlBuilder.toString(), params);
    }

    private ID getId(Object o) {
        try {
            Method getter = Mapper.getGetter(entityClass, Mapper.id(entityClass));
            if (null != getter) {
                return (ID) getter.invoke(o);
            } else {
                getter = Mapper.getGetter(entityClass.getSuperclass(), Mapper.id(entityClass));
                if (null != getter) {
                    return (ID) getter.invoke(o);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("获取id失败 " + entityClass.getClass(), e);
        }
        return null;
    }

    /**
     * 填充SQL变量
     * @param sql sql语句
     * @return 填充后sql语句
     */
    private String fillSQL(String sql) {
        sql = sql.replace("${TABLE}", tableName)
                .replace("${ID}", Mapper.getColumnName(entityClass, Mapper.id(entityClass)));
        return sql;
    }
}
