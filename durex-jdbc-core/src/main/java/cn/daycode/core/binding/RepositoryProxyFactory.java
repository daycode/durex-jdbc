package cn.daycode.core.binding;

import cn.daycode.core.annotation.CountQuery;
import cn.daycode.core.annotation.Param;
import cn.daycode.core.annotation.SelectQuery;
import cn.daycode.core.annotation.UpdateQuery;
import cn.daycode.core.mapping.Mapper;
import cn.daycode.core.orm.CommonRowMapper;
import cn.daycode.core.orm.RepositoryImpl;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仓库代理
 * Created by jl on 17-7-6.
 */
@Component
public class RepositoryProxyFactory {

    private final static Logger logger = LoggerFactory.getLogger(RepositoryProxyFactory.class);

    // TODO 后续将sql拼装拆分开
    public static Object newProxyInstance(Class<?> interfaceClass, Class<?> entityClass, NamedParameterJdbcTemplate namedParameterJdbcTemplate) throws IllegalArgumentException {
        return Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class[]{interfaceClass},
                (proxy, method, args) -> {
                    //获取sql 如果没有就解析方法名
                    SelectQuery selectQuery = method.getAnnotation(SelectQuery.class);
                    UpdateQuery updateQuery = method.getAnnotation(UpdateQuery.class);
                    CountQuery countQuery = method.getAnnotation(CountQuery.class);
                    if (null != selectQuery) {
                        Parameter[] parameters = method.getParameters();
                        Map<String, Object> params = getParamMap(parameters, args);
                        String sql = selectQuery.value();
                        if (StringUtils.isEmpty(sql) && StringUtils.isNotEmpty(selectQuery.key())) {
                            sql = Mapper.sql(selectQuery.key());
                        }

                        if (!sql.contains("SELECT") && !sql.contains("select") && !sql.contains("from") && !sql.contains("FROM") && !sql.contains("where") && !sql.contains("WHERE")) {
                            String table = Mapper.table(entityClass);
                            sql = fillSelectSQL(table, sql);
                        }
                        logger.info("execute SELECT SQL query:{}, args:{}", sql, params);
                        List<?> results = namedParameterJdbcTemplate.query(sql, params, new CommonRowMapper<>(entityClass));
                        if (null == results || results.isEmpty()) {
                            return null;
                        }

                        if (method.getReturnType().equals(List.class) || method.getReturnType().equals(ArrayList.class)) {
                            return results;
                        } else {
                            return results.get(0);
                        }

                    } else if (null != updateQuery) {
                        String sql = updateQuery.value();
                        if (StringUtils.isEmpty(sql) && StringUtils.isNotEmpty(updateQuery.key())) {
                            sql = Mapper.sql(updateQuery.value());
                        }
                        Parameter[] parameters = method.getParameters();
                        Map<String, Object> params = getParamMap(parameters, args);
                        logger.info("execute UPDATE SQL query:{}, args:{}", updateQuery.value(), params);
                        namedParameterJdbcTemplate.update(sql, params);
                        return null;

                    } else if (null != countQuery) {
                        Parameter[] parameters = method.getParameters();
                        Map<String, Object> params = getParamMap(parameters, args);
                        String sql = countQuery.value();
                        if (StringUtils.isEmpty(sql) && StringUtils.isNotEmpty(countQuery.key())) {
                            sql = Mapper.sql(countQuery.key());
                        }
                        if (!sql.contains("COUNT") && !sql.contains("count") && !sql.contains("from") && !sql.contains("FROM") && !sql.contains("where") && !sql.contains("WHERE")) {
                            String table = Mapper.table(entityClass);
                            sql = fillCountSQL(table, sql);
                        }

                        logger.info("execute COUNT SQL query:{}, args:{}", countQuery.value(), params);
                        List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, params);
                        if (null != results && !results.isEmpty()) {
                            Map<String, Object> map = results.get(0);
                            if (null != map && !map.isEmpty()) {
                                for (Object obj : map.values()) {
                                    if (null == obj) {
                                        return 0;
                                    }
                                    return obj;
                                }
                            } else {
                                return 0;
                            }
                        } else {
                            return 0;
                        }
                        return 0;
                    } else {
                        Method targetMethod = RepositoryImpl.class.getMethod(method.getName(), method.getParameterTypes());
                        return targetMethod.invoke(RepositoryRegister.getRepository(entityClass), args);
                    }
                });
    }

    private static Map<String, Object> getParamMap(Parameter[] parameters, Object[] args) {
        Map<String, Object> paramMap = new HashMap<>();
        for (int index = 0; index < parameters.length; index++) {
            String paramName = null;
            Param param = parameters[index].getAnnotation(Param.class);
            if (null != param) {
                paramName = param.value();
            } else {
                paramName = parameters[index].getName();
            }
            paramMap.put(paramName, args[index]);
        }
        return paramMap;
    }

//    private static NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
//        if(null == namedParameterJdbcTemplate) {
//            namedParameterJdbcTemplate = SpringUtil.getBean(NamedParameterJdbcTemplate.class);
//        }
//        return namedParameterJdbcTemplate;
//    }

    private static String fillSelectSQL(String table, String sql) {
        sql = "SELECT * FROM " + table + " WHERE " + sql;
        return sql;
    }

    private static String fillCountSQL(String table, String sql) {
        sql = "SELECT COUNT(1) FROM " + table + " WHERE " + sql;
        return sql;
    }
}
