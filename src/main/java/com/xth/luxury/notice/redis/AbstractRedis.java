package com.xth.luxury.notice.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

//@Component
public abstract class AbstractRedis<V> {
    @Resource
    private RedisTemplate<String, V> redisTemplate;
    public String prefix;

    AbstractRedis() {

    }

    AbstractRedis(String prefix) {
        this.prefix = prefix;
        this.prefix += ":";
    }

    /**
     * 设定永久内容
     *
     * @param key   key
     * @param value value
     */
    public void setValueForever(String key, V value) {
        try {
            redisTemplate.opsForValue().set(this.prefix.concat(key), value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     * @return 成功与否
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(this.prefix.concat(this.prefix.concat(key)), time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setRightPushAll(String key, List<V> arrayValue) {
        redisTemplate.opsForList().rightPushAll(key, arrayValue);
    }

    public long getRedisAtomicLong(String key) {
        if (redisTemplate == null || redisTemplate.getConnectionFactory() == null) {
            return 0L;
        }
        RedisAtomicLong redisAtomicLong = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        return redisAtomicLong.getAndIncrement();
    }

    public long decrementRedisAtomicLong(String key) {
        if (redisTemplate == null || redisTemplate.getConnectionFactory() == null) {
            return 0L;
        }
        RedisAtomicLong redisAtomicLong = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        return redisAtomicLong.decrementAndGet();
    }

    @SafeVarargs
    public final Long sAdd(String key, V... val) {
        if (redisTemplate == null) {
            return 0L;
        }
        return redisTemplate.opsForSet().add(this.prefix.concat(key), val);
    }

    public List<V> sPop(String key, int count) {
        return redisTemplate.opsForSet().pop(this.prefix.concat(key), count);
    }

    public Long sRemove(String key, V val) {
        return redisTemplate.opsForSet().remove(this.prefix.concat(key), val);
    }
}
