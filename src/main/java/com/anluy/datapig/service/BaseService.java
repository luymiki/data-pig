package com.anluy.datapig.service;

import com.anluy.datapig.dao.BaseDao;
import com.anluy.datapig.dao.Page;
import org.apache.ibatis.session.RowBounds;
import org.springframework.dao.DataAccessException;

import java.util.Collection;
import java.util.List;

/**
 * ${DESCRIPTION}
 *
 * @author hc.zeng
 * @create 2018-10-19 15:50
 */
public interface BaseService<T,PK> {
    boolean remove(PK var1) throws DataAccessException;

    int removeAll(Collection<PK> var1) throws DataAccessException;

    T get(PK var1) throws DataAccessException;

    T save(T var1) throws DataAccessException;

    int saveAll(Collection<T> var1) throws DataAccessException;

    T update(T var1) throws DataAccessException;

    List<T> getList(T var1) throws DataAccessException;

    Page<T> listPage(Page<T> var1) throws DataAccessException;

    T saveOrUpdate(T var1) throws DataAccessException;

    void preSave(T var1);
}
