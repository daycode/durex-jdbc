package cn.daycode.orm;

import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 公用仓库接口
 * Created by jl on 17-7-6.
 */
public interface Repository<T, ID extends Serializable> {

    Class<T> getEntityClass();

    T save(T entity);

    T update(T entity);

    T findById(ID id);

    List<T> findAll();

    List<T> find(String sql, Object... params);

    void delete(ID id);

    long countByMap(String query, Map<String, Object> params);

    long count();

    @Transactional
    void update(Map<String, Object> params, ID id);

    List<T> findList(String selectQuery, Object... params);

    List<T> findList(String selectQuery, Map<String, Object> params);
}
