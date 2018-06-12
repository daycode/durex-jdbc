package cn.daycode.spring.support;

import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DaoSupport;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import static org.springframework.util.Assert.notNull;

/**
 * JdbcTemplate 提供支持类
 *
 * @author zch
 * @since 2018/6/13
 */
public class JdbcTemplateDaoSupport extends DaoSupport {

    private NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * bean 注册时，Spring 会自动将 bean 的属性自动注入
     *
     * @see PropertyAccessor
     */
    @Autowired
    public void setJdbcTemplate(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = namedParameterJdbcTemplate;
    }

    public NamedParameterJdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    /**
     * jdbcTemplate 实现类不可为空，否则抛异常
     */
    @Override
    protected void checkDaoConfig() {
        notNull(this.jdbcTemplate, "jdbcTemplate are required");
    }
}
