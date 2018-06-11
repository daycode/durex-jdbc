package cn.daycode.orm;

import org.springframework.beans.factory.FactoryBean;

public class RepositoryFactoryBean implements FactoryBean {

    private Class<?> repositoryClass;

    public Class<?> getRepositoryClass() {
        return repositoryClass;
    }

    public void setRepositoryClass(Class<?> repositoryClass) {
        this.repositoryClass = repositoryClass;
    }

    @Override
    public Object getObject() throws Exception {

        return RepositoryFactory.newProxyInstance(this.repositoryClass, RepositoryRegister.entityClass(this.repositoryClass));
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