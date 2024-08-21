package com.github.esoty6.upgradablefurnaces.util;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import com.google.common.collect.TreeMultimap;

public class Cache<K, V> {

  public static class CacheBuilder<K, V> {
    private Clock clock = Clock.systemUTC();
    private long retention = 300_000L;
    private long lazyFrequency = 10_000L;
    private BiFunction<K, Boolean, V> load;
    private BiPredicate<K, V> inUseCheck;
    private BiConsumer<K, V> postRemoval;

    public CacheBuilder<K, V> withLoadFunction(final BiFunction<K, Boolean, V> load) {
      this.load = load;
      return this;
    }

    public CacheBuilder<K, V> withInUseCheck(final BiPredicate<K, V> inUse) {
      this.inUseCheck = inUse;
      return this;
    }

    public CacheBuilder<K, V> withPostRemoval(final BiConsumer<K, V> postRemove) {
      this.postRemoval = postRemove;
      return this;
    }

    public CacheBuilder<K, V> withRetention(final long retention) {
      this.retention = Math.max(60_000, retention);
      return this;
    }

    public CacheBuilder<K, V> withLazyFrequency(final long lazyFrequency) {
      this.lazyFrequency = Math.max(0, lazyFrequency);
      return this;
    }

    public Cache<K, V> build() {
      return new Cache<>(clock, retention, lazyFrequency, load, inUseCheck, postRemoval);
    }
  }

  private final Clock clock;
  private final Map<K, V> internal;
  private final TreeMultimap<Long, K> expiry;
  private final long retention;
  private final long lazyFrequency;
  private final AtomicLong lastLazyCheck;
  private final BiFunction<K, Boolean, V> load;
  private final BiPredicate<K, V> inUseCheck;
  private final BiConsumer<K, V> postRemoval;

  private Cache(final Clock clock, final long retention, long lazyFrequency,
      final BiFunction<K, Boolean, V> load, final BiPredicate<K, V> inUseCheck,
      final BiConsumer<K, V> postRemoval) {
    internal = new HashMap<>();
    this.clock = clock;

    expiry = TreeMultimap.create(Comparator.naturalOrder(),
        (k1, k2) -> k1 == k2 || k1.equals(k2) ? 0 : 1);

    this.load = load;
    this.retention = retention;
    this.lazyFrequency = lazyFrequency;
    lastLazyCheck = new AtomicLong(0);
    this.inUseCheck = inUseCheck;
    this.postRemoval = postRemoval;
  }

  public void put(final K key, final V value) {
    lazyCheck();

    synchronized (this.internal) {
      internal.put(key, value);
      expiry.put(clock.millis() + retention, key);
    }
  }


  public V get(final K key) {
    return get(key, true);
  }

  public V get(final K key, final boolean create) {
    lazyCheck();

    synchronized (this.internal) {
      V value;
      if (!internal.containsKey(key) && load != null) {
        value = load.apply(key, create);
        if (value != null) {
          internal.put(key, value);
        }
      } else {
        value = this.internal.get(key);
      }

      if (value != null) {
        expiry.put(clock.millis() + retention, key);
      }

      return value;
    }
  }


  public boolean containsKey(final K key) {
    lazyCheck();

    synchronized (this.internal) {
      return internal.containsKey(key);
    }
  }

  public void invalidate(final K key) {
    synchronized (internal) {
      if (!internal.containsKey(key)) {
        return;
      }

      internal.remove(key);

      expiry.entries().removeIf(entry -> entry.getValue().equals(key));
    }

    lazyCheck();
  }

  public void expireAll() {
    synchronized (internal) {
      expiry.clear();
      internal.keySet().forEach(key -> expiry.put(0L, key));
    }

    lastLazyCheck.set(0);

    lazyCheck();
  }

  private void lazyCheck() {
    long now = clock.millis();

    if (lastLazyCheck.get() > now - lazyFrequency) {
      return;
    }

    lastLazyCheck.set(now);

    synchronized (this.internal) {
      SortedMap<Long, Collection<K>> subMap = this.expiry.asMap().headMap(now);
      Collection<K> keys =
          subMap.values().stream().collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);

      subMap.clear();

      long nextExpiry = now + this.retention;

      keys.forEach(key -> {

        V value = this.internal.get(key);
        if (value != null && this.inUseCheck != null && this.inUseCheck.test(key, value)) {
          this.expiry.put(nextExpiry, key);
          return;
        }

        this.internal.remove(key);

        if (value == null) {
          return;
        }

        if (this.postRemoval != null) {
          this.postRemoval.accept(key, value);
        }
      });
    }
  }

}
