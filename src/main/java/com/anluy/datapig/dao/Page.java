package com.anluy.datapig.dao;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;

/**
 * ${DESCRIPTION}
 *
 * @author hc.zeng
 * @create 2018-10-19 15:56
 */

public class Page<T> implements Serializable, Iterable<T> {
    protected List<T> result;
    protected int pageSize;
    protected int pageNumber;
    protected int totalCount;
    private Object filters;
    private String sortColumns;
    private Map properties;

    public Page() {
        this.result = new ArrayList();
        this.pageSize = 10;
        this.pageNumber = 1;
        this.totalCount = 0;
    }

    public Page(int pageSize) {
        this.result = new ArrayList();
        this.pageSize = 10;
        this.pageNumber = 1;
        this.totalCount = 0;
        this.pageSize = pageSize;
    }

    public Page(int pageNumber, int pageSize, int totalCount) {
        this(pageNumber, pageSize, totalCount, new ArrayList(0));
    }

    public Page(int pageNumber, int pageSize, int totalCount, List<T> result) {
        this.result = new ArrayList();
        this.pageSize = 10;
        this.pageNumber = 1;
        this.totalCount = 0;
        if (pageSize <= 0) {
            throw new IllegalArgumentException("[pageSize] must great than zero");
        } else {
            this.pageSize = pageSize;
            this.pageNumber = PageUtils.computePageNumber(pageNumber, pageSize, totalCount);
            this.totalCount = totalCount;
            this.setResult(result);
        }
    }

    public void setResult(List<T> elements) {
        if (elements == null) {
            throw new IllegalArgumentException("\'result\' must be not null");
        } else {
            this.result = elements;
        }
    }

    public List<T> getResult() {
        return this.result;
    }

    public int getPageNumber() {
        return this.pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        if (pageNumber < 1) {
            this.pageNumber = 1;
        }

        this.pageNumber = pageNumber;
    }

    public void setPageNumber(String pageNumber) {
        if (StringUtils.isBlank(pageNumber)) {
            this.pageNumber = 1;
        } else {
            this.pageNumber = Integer.valueOf(pageNumber);
        }
    }

    public void setPageSize(int pageSize) {
        if (pageSize > 0) {
            this.pageSize = pageSize;
        }

    }

    public void setPageSize(String pageSize) {
        if (StringUtils.isBlank(pageSize)) {
            this.pageSize = 10;
        } else {
            this.pageSize = Integer.valueOf(pageSize);
        }
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public boolean isFirstPage() {
        return this.getThisPageNumber() == 1;
    }

    public boolean isLastPage() {
        return this.getThisPageNumber() >= this.getLastPageNumber();
    }

    public boolean isHasNextPage() {
        return this.getLastPageNumber() > this.getThisPageNumber();
    }

    public boolean isHasPreviousPage() {
        return this.getThisPageNumber() > 1;
    }

    public int getLastPageNumber() {
        return PageUtils.computeLastPageNumber(this.totalCount, this.pageSize);
    }

    public int getTotalCount() {
        return this.totalCount;
    }

    public int getThisPageFirstElementNumber() {
        return (this.getThisPageNumber() - 1) * this.getPageSize() + 1;
    }

    public int getThisPageLastElementNumber() {
        int fullPage = this.getThisPageFirstElementNumber() + this.getPageSize() - 1;
        return this.getTotalCount() < fullPage ? this.getTotalCount() : fullPage;
    }

    public int getNextPageNumber() {
        return this.getThisPageNumber() + 1;
    }

    public int getPreviousPageNumber() {
        return this.getThisPageNumber() - 1;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public int getThisPageNumber() {
        return this.pageNumber;
    }

    public List<Integer> getLinkPageNumbers() {
        return PageUtils.generateLinkPageNumbers(this.getThisPageNumber(), this.getLastPageNumber(), 10);
    }

    public int getFirstResult() {
        return PageUtils.getFirstResult(this.pageNumber, this.pageSize);
    }

    @Override
    public Iterator<T> iterator() {
        return this.result == null ? (new ArrayList()).iterator() : this.result.iterator();
    }

    public Object getFilters() {
        if (this.filters == null) {
            this.filters = new HashMap();
        }

        return this.filters;
    }

    public void setFilters(Object filters) {
        this.filters = filters;
    }

    public String getSortColumns() {
        return this.sortColumns;
    }

    public void setSortColumns(String sortColumns) {
        this.checkSortColumnsSqlInjection(sortColumns);
        if (sortColumns != null && sortColumns.length() > 50) {
            throw new IllegalArgumentException("sortColumns.length() <= 50 must be true");
        } else {
            this.sortColumns = sortColumns;
        }
    }

    private void checkSortColumnsSqlInjection(String sortColumns) {
        if (sortColumns != null) {
            if (sortColumns.indexOf("\'") >= 0 || sortColumns.indexOf("\\") >= 0) {
                throw new IllegalArgumentException("sortColumns:" + sortColumns + " has SQL Injection risk");
            }
        }
    }

    public Map getProperties() {
        if (this.properties == null) {
            this.properties = Maps.newHashMap();
        }

        return this.properties;
    }
}
