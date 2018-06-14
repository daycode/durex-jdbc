package cn.daycode.core.orm;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * 直接查询非实体语句
 * @author JL
 */
public class Freedom {

    private static JdbcTemplate jdbcTemplate;

    private static NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public static void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        Freedom.jdbcTemplate = jdbcTemplate;
    }

    public static void setNamedParameterJdbcTemplate(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        Freedom.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    /**
     * 返回自定义对象
     *
     * @param sql   语句
     * @param clazz 载体类
     * @param <K>   模板
     * @return 模板实例
     */
    public static  <K> K findOne(String sql, Map<String, Object> params, Class<K> clazz) {

        String sqlBuilder = sql + " LIMIT 1";
        List<K> resultList = namedParameterJdbcTemplate.query(sqlBuilder, params, new CommonRowMapper<>(clazz));

        if (null != resultList && !resultList.isEmpty()) {
            return resultList.get(0);
        } else {
            return null;
        }
    }
}
