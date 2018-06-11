package cn.daycode.orm;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jl on 17-7-7.
 */
public class RepositoryRegister {

    private static Map<Class<?>, Class<?>> repositoryEntityMap = new HashMap<>();

    private static Map<Class<?>, Class<?>> entityRepositoryMap = new HashMap<>();

    private static Map<Class<?>, RepositoryImpl> repositoryMap = new HashMap<>();

    public static void repositoryEntity(Class<?> repositoryClass, Class<?> entityClass) {
        repositoryEntityMap.put(repositoryClass, entityClass);
        entityRepositoryMap.put(entityClass, repositoryClass);
        RepositoryImpl repository = new RepositoryImpl(entityClass, repositoryClass);
        repositoryMap.put(entityClass, repository);
        repositoryMap.put(repositoryClass, repository);
    }

    public static Class<?> entityClass(Class<?> repositoryClass) {
        return repositoryEntityMap.get(repositoryClass);
    }

    public static Class<?> repositoryClass(Class<?> entityClass) {
        return entityRepositoryMap.get(entityClass);
    }

    public static RepositoryImpl getRepository(Class<?> entityRepositoryClass) {
        return repositoryMap.get(entityRepositoryClass);
    }

}
