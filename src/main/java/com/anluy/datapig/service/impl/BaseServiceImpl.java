package com.anluy.datapig.service.impl;

import com.anluy.datapig.dao.BaseDao;
import com.anluy.datapig.dao.Page;
import com.anluy.datapig.dao.PropertyUtils;
import com.anluy.datapig.service.BaseService;
import org.apache.ibatis.session.RowBounds;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ${DESCRIPTION}
 *
 * @author hc.zeng
 * @create 2018-10-19 15:52
 */
public abstract class BaseServiceImpl<T, PK> implements BaseService<T, PK> {

    public abstract BaseDao<T,PK> getDao();

    @Override
    public boolean remove(PK var1) throws DataAccessException {
        int t = getDao().remove(var1);
        return t > 0;
    }

    @Override
    public int removeAll(Collection<PK> var1) throws DataAccessException {
        return getDao().removeAll(var1);
    }

    @Override
    public T get(PK var1) throws DataAccessException {
        return getDao().get(var1);
    }

    @Override
    public T save(T var1) throws DataAccessException {
        preSave(var1);
        getDao().save(var1);
        return  var1;
    }

    @Override
    public int saveAll(Collection<T> var1) throws DataAccessException {
        return 0;
    }

    @Override
    public T update(T var1) throws DataAccessException {
        getDao().update(var1);
        return var1;
    }

    @Override
    public List getList(T var1) throws DataAccessException {
        return  getDao().getList(var1);
    }

    @Override
    public Page listPage(Page<T> page) throws DataAccessException {
        HashMap filters = new HashMap();
        filters.put("sortColumns", page.getSortColumns());
        Map parameterObject = PropertyUtils.describe(page.getFilters());
        filters.putAll(parameterObject);
        int totalCount = getDao().listPageCount(filters);
        page.setTotalCount(totalCount);
        if(totalCount < 1) {
            return page;
        } else {
            List list = getDao().listPage(filters, new RowBounds(page.getFirstResult(), page.getPageSize()));
            page.setResult(list);
            return page;
        }
    }

    @Override
    public T saveOrUpdate(T var1) throws DataAccessException {
        return null;
    }
}
