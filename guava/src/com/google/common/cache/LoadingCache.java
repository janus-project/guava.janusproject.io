/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.cache;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.UncheckedExecutionException;

import java.util.concurrent.ExecutionException;

/**
 * A semi-persistent mapping from keys to values. Values are automatically loaded by the cache,
 * and are stored in the cache until either evicted or manually invalidated.
 *
 * <p>All methods other than {@link #get} and {@link #getUnchecked} are optional.
 *
 * <p>When evaluated as a {@link Function}, a cache yields the same result as invoking
 * {@link #getUnchecked}.
 *
 * @author Charles Fry
 * @since 11.0
 */
@Beta
public interface LoadingCache<K, V> extends Cache<K, V>, Function<K, V> {

  /**
   * Returns the value associated with {@code key} in this cache, first loading that value if
   * necessary. No observable state associated with this cache is modified until loading completes.
   *
   * @throws ExecutionException if a checked exception was thrown while loading the value
   * @throws UncheckedExecutionException if an unchecked exception was thrown while loading the
   *     value
   * @throws ExecutionError if an error was thrown while loading the value
   */
  V get(K key) throws ExecutionException;

  /**
   * Returns the value associated with {@code key} in this cache, first loading that value if
   * necessary. No observable state associated with this cache is modified until computation
   * completes. Unlike {@link #get}, this method does not throw a checked exception, and thus should
   * only be used in situations where checked exceptions are not thrown by the cache loader.
   *
   * <p><b>Warning:</b> this method silently converts checked exceptions to unchecked exceptions,
   * and should not be used with cache loaders which throw checked exceptions.
   *
   * @throws UncheckedExecutionException if an exception was thrown while loading the value,
   *     regardless of whether the exception was checked or unchecked
   * @throws ExecutionError if an error was thrown while loading the value
   */
  V getUnchecked(K key);

  /**
   * Returns a map of the values associated with {@code keys}, creating or retrieving those values
   * if necessary. The returned map contains entries that were already cached, combined with newly
   * loaded entries; it will never contain null keys or values.
   *
   * <p>Caches loaded by a {@link CacheLoader} will issue a single request to
   * {@link CacheLoader#loadAll} for all keys which are not already present in the cache. All
   * entries returned by {@link CacheLoader#loadAll} will be stored in the cache, over-writing
   * any previously cached values. This method will throw an exception if
   * {@link CacheLoader#loadAll} returns {@code null}, returns a map containing null keys or values,
   * or fails to return an entry for each requested key.
   *
   * <p>Note that duplicate elements in {@code keys}, as determined by {@link Object#equals}, will
   * be ignored.
   *
   * @throws ExecutionException if a checked exception was thrown while loading the values
   * @throws UncheckedExecutionException if an unchecked exception was thrown while loading the
   *     values
   * @throws ExecutionError if an error was thrown while loading the values
   * @since 11.0
   */
  ImmutableMap<K, V> getAll(Iterable<? extends K> keys) throws ExecutionException;

  /**
   * Discouraged. Provided to satisfy the {@code Function} interface; use {@link #get} or
   * {@link #getUnchecked} instead.
   *
   * @throws UncheckedExecutionException if an exception was thrown while loading the value,
   *     regardless of whether the exception was checked or unchecked
   * @throws ExecutionError if an error was thrown while loading the value
   */
  @Override
  V apply(K key);

  /**
   * Loads a new value for key {@code key}. While the new value is loading the previous value (if
   * any) will continue to be returned by {@code get(key)} unless it is evicted. If the new
   * value is loaded succesfully it will replace the previous value in the cache; if an exception is
   * thrown while refreshing the previous value will remain.
   *
   * @throws ExecutionException if a checked exception was thrown while refreshing the entry
   * @throws UncheckedExecutionException if an unchecked exception was thrown while refreshing the
   *     entry
   * @throws ExecutionError if an error was thrown while refreshing the entry
   * @since 11.0
   */
  void refresh(K key) throws ExecutionException;
}