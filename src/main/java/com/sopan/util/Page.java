package com.sopan.util;

import java.util.Collections;
import java.util.List;

public class Page<T> {

    private final List<T> items;
    private final int pageNumber;
    private final int pageSize;
    private final long totalItems;

    public Page(List<T> items, int pageNumber, int pageSize, long totalItems) {
        this.items = items != null ? Collections.unmodifiableList(items) : Collections.emptyList();
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
    }

    public List<T> getItems() { return items; }
    public int getPageNumber() { return pageNumber; }
    public int getPageSize() { return pageSize; }
    public long getTotalItems() { return totalItems; }

    public int getTotalPages() {
        if (pageSize <= 0) return 1;
        return (int) Math.ceil((double) totalItems / pageSize);
    }

    public boolean hasPrevious() { return pageNumber > 1; }
    public boolean hasNext() { return pageNumber < getTotalPages(); }
}
