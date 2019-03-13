package com.anluy.datapig.dao;

import org.apache.ibatis.session.RowBounds;
import org.springframework.dao.DataAccessException;

import java.util.Collection;
import java.util.List;

/**
 * ${DESCRIPTION}
 *
 * @author hc.zeng
 * @create 2018-10-19 15:46
 */

public interface BaseDao<T, PK> {
    int save(T entity) throws DataAccessException;

    int saveAll(Collection<T> list) throws DataAccessException;

    int update(T entity) throws DataAccessException;

    int remove(PK id) throws DataAccessException;

    int removeAll(Iterable<PK> idIterable) throws DataAccessException;

    T get(PK id) throws DataAccessException;

    List<T> getList(T entity) throws DataAccessException;

    List listPage(Object params, RowBounds rowBounds) throws DataAccessException;

    int listPageCount(Object params) throws DataAccessException;

    Integer getSequence() throws DataAccessException;
}
