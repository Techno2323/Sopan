package com.sopan.concurrent;

import com.sopan.engine.ConceptGraph;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * Thread-safe cache for immutable ConceptGraph objects.
 * Employs a ConcurrentHashMap with granular ReentrantReadWriteLock protection
 * for cache invalidation when instructors modify prerequisites or concepts.
 */
public class ConceptGraphCache {

    private final Map<Integer, ConceptGraph> cache = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    public ConceptGraph getOrCompute(int courseId, Supplier<ConceptGraph> loader) {
        rwLock.readLock().lock();
        try {
            ConceptGraph cached = cache.get(courseId);
            if (cached != null) {
                return cached;
            }
        } finally {
            rwLock.readLock().unlock();
        }

        rwLock.writeLock().lock();
        try {
            return cache.computeIfAbsent(courseId, k -> loader.get());
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public void invalidate(int courseId) {
        rwLock.writeLock().lock();
        try {
            cache.remove(courseId);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public void clear() {
        rwLock.writeLock().lock();
        try {
            cache.clear();
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
