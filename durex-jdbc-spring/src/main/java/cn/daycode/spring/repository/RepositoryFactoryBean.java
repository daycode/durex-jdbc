package cn.daycode.spring.repository;

import cn.daycode.core.orm.RepositoryProxyFactory;
import cn.daycode.core.orm.RepositoryRegister;
import cn.daycode.spring.support.JdbcTemplateDaoSupport;
import org.springframework.beans.factory.FactoryBean;

/**
 * BeanFactory that enables injection of Durex-jdbc repository interfaces.
 * It can be set up with a spring jdbcTemplate.
 *
 * @author zch
 * @since 2018/6/13
 */
public class RepositoryFactoryBean extends JdbcTemplateDaoSupport implements FactoryBean {

    private Class<?> repositoryClass;

    public RepositoryFactoryBean() {
        //intentionally empty
    }

    public RepositoryFactoryBean(Class<?> repositoryClass) {
        this.repositoryClass = repositoryClass;
    }

    public Class<?> getRepositoryClass() {
        return repositoryClass;
    }

    public void setRepositoryClass(Class<?> repositoryClass) {
        this.repositoryClass = repositoryClass;
    }

    @Override
    public Object getObject() throws Exception {
        return RepositoryProxyFactory
                .newProxyInstance(this.repositoryClass,
                        RepositoryRegister.entityClass(this.repositoryClass), getJdbcTemplate());
    }

    @Override
    public Class getObjectType() {
        return repositoryClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}