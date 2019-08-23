//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.example.demo.shiro.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.util.CollectionUtils;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.SerializeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyRedisCache<K, V> implements Cache<K, V> {
    private Logger logger;
    private RedisManager cache;
    private String keyPrefix;

    public String getKeyPrefix() {
        return this.keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public MyRedisCache(RedisManager cache) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.keyPrefix = "shiro_redis_session:";
        if (cache == null) {
            throw new IllegalArgumentException("Cache argument cannot be null.");
        } else {
            this.cache = cache;
        }
    }

    public MyRedisCache(RedisManager cache, String prefix) {
        this(cache);
        this.keyPrefix = prefix;
    }

    private byte[] getByteKey(K key) {
        if (key instanceof String) {
            String preKey = this.keyPrefix + key;
            return preKey.getBytes();
        } else {
            String preKey = this.keyPrefix + String.valueOf(key);
            return preKey.getBytes();
        }
    }

    public V get(K key) throws CacheException {
        this.logger.debug("根据key从Redis中获取对象 key [" + key + "]");

        try {
            if (key == null) {
                return null;
            } else {
                byte[] rawValue = this.cache.get(this.getByteKey(key));
                V value = (V) SerializeUtils.deserialize(rawValue);
                return value;
            }
        } catch (Throwable var4) {
            throw new CacheException(var4);
        }
    }

    public V put(K key, V value) throws CacheException {
        this.logger.debug("根据key从存储 key [" + key + "]");

        try {
            this.cache.set(this.getByteKey(key), SerializeUtils.serialize(value));
            return value;
        } catch (Throwable var4) {
            throw new CacheException(var4);
        }
    }

    public V remove(K key) throws CacheException {
        this.logger.debug("从redis中删除 key [" + key + "]");

        try {
            V previous = this.get(key);
            this.cache.del(this.getByteKey(key));
            return previous;
        } catch (Throwable var3) {
            throw new CacheException(var3);
        }
    }

    public void clear() throws CacheException {
        this.logger.debug("从redis中删除所有元素");

        try {
            this.cache.flushDB();
        } catch (Throwable var2) {
            throw new CacheException(var2);
        }
    }

    public int size() {
        try {
            Long longSize = new Long(this.cache.dbSize());
            return longSize.intValue();
        } catch (Throwable var2) {
            throw new CacheException(var2);
        }
    }

    public Set<K> keys() {
        try {
            Set<byte[]> keys = this.cache.keys(this.keyPrefix + "*");
            if (CollectionUtils.isEmpty(keys)) {
                return Collections.emptySet();
            } else {
                Set<K> newKeys = new HashSet();
                Iterator i$ = keys.iterator();

                while(i$.hasNext()) {
                    byte[] key = (byte[])i$.next();
                    newKeys.add((K) key);
                }

                return newKeys;
            }
        } catch (Throwable var5) {
            throw new CacheException(var5);
        }
    }

    public Collection<V> values() {
        try {
            Set<byte[]> keys = this.cache.keys(this.keyPrefix + "*");
            if (!CollectionUtils.isEmpty(keys)) {
                List<V> values = new ArrayList(keys.size());
                Iterator i$ = keys.iterator();

                while(i$.hasNext()) {
                    byte[] key = (byte[])i$.next();
                    V value = this.get((K) key);
                    if (value != null) {
                        values.add(value);
                    }
                }

                return Collections.unmodifiableList(values);
            } else {
                return Collections.emptyList();
            }
        } catch (Throwable var6) {
            throw new CacheException(var6);
        }
    }
}
