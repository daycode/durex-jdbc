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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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
public class RepositoryProxyFactory {

    private final static Logger logger = LoggerFactory.getLogger(RepositoryProxyFactory.class);

    // TODO 后续将sql拼装拆分开
    public static Object newProxyInstance(Class<?> interfaceClass, Class<?> entityClass, NamedParameterJdbcTemplate namedParameterJdbcTemplate, JdbcTemplate jdbcTemplate) throws IllegalArgumentException {
        return Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class[]{interfaceClass},
                (proxy, method, args) -> {
                    //获取sql 如果没有就解析方法名
                    RepositoryImpl repository =  RepositoryRegister.getRepository(entityClass);
                    repository.setJdbcTemplate(jdbcTemplate);
                    repository.setNamedParameterJdbcTemplate(namedParameterJdbcTemplate);
                    Method targetMethod = RepositoryImpl.class.getMethod(method.getName(), method.getParameterTypes());
                    return targetMethod.invoke(repository, args);
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

    private static String fillSelectSQL(String table, String sql) {
        sql = "SELECT * FROM " + table + " WHERE " + sql;
        return sql;
    }

    private static String fillCountSQL(String table, String sql) {
        sql = "SELECT COUNT(1) FROM " + table + " WHERE " + sql;
        return sql;
    }
}
