package cn.daycode.spring.support;

import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DaoSupport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import static org.springframework.util.Assert.notNull;

/**
 * JdbcTemplate 提供支持类
 *
 * @author zch
 * @since 2018/6/13
 */
public class JdbcTemplateDaoSupport extends DaoSupport {

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private JdbcTemplate jdbcTemplate;

    /**
     * bean 注册时，Spring 会自动将 bean 的属性自动注入
     *
     * @see PropertyAccessor
     */
    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return namedParameterJdbcTemplate;
    }

    @Autowired
    public JdbcTemplateDaoSupport setNamedParameterJdbcTemplate(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        return this;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    @Autowired
    public JdbcTemplateDaoSupport setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        return this;
    }

    /**
     * jdbcTemplate 实现类不可为空，否则抛异常
     */
    @Override
    protected void checkDaoConfig() {
        notNull(this.jdbcTemplate, "jdbcTemplate are required");
        notNull(this.namedParameterJdbcTemplate, "namedParameterJdbcTemplate are required");
    }
}
