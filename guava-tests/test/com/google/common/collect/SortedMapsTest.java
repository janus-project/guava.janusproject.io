/*
 * Copyright (C) 2010 The Guava Authors
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

package com.google.common.collect;

import static org.junit.contrib.truth.Truth.ASSERT;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps.EntryTransformer;
import com.google.common.collect.Maps.ValueDifferenceImpl;
import com.google.common.collect.testing.SortedMapInterfaceTest;
import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;

import junit.framework.TestCase;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

/**
 * Tests for SortedMaps.
 *
 * @author Louis Wasserman
 */
@GwtCompatible(emulated = true)
public class SortedMapsTest extends TestCase {

  private static final EntryTransformer<Object, Object, Object> ALWAYS_NULL =
      new EntryTransformer<Object, Object, Object>() {
        @Override
        public Object transformEntry(Object k, Object v1) {
          return null;
        }
      };

  @GwtIncompatible("NullPointerTester")
  public void testNullPointer() throws Exception {
    NullPointerTester nullPointerTester = new NullPointerTester();
    nullPointerTester.setDefault(EntryTransformer.class, ALWAYS_NULL);
    nullPointerTester.setDefault(
        SortedMap.class, Maps.<String, String>newTreeMap());
    nullPointerTester.testAllPublicStaticMethods(SortedMaps.class);
  }

  public void testTransformSortedValues() {
    SortedMap<String, Integer> map = ImmutableSortedMap.of("a", 4, "b", 9);
    Function<Integer, Double> sqrt = new Function<Integer, Double>() {
      @Override
      public Double apply(Integer in) {
        return Math.sqrt(in);
      }
    };
    SortedMap<String, Double> transformed =
        SortedMaps.transformValues(map, sqrt);

    assertEquals(ImmutableSortedMap.of("a", 2.0, "b", 3.0), transformed);
  }

  public void testTransformSortedEntries() {
    SortedMap<String, String> map = ImmutableSortedMap.of("a", "4", "b", "9");
    EntryTransformer<String, String, String> concat =
        new EntryTransformer<String, String, String>() {
          @Override
          public String transformEntry(String key, String value) {
            return key + value;
          }
        };
    SortedMap<String, String> transformed =
        SortedMaps.transformEntries(map, concat);

    assertEquals(ImmutableSortedMap.of("a", "a4", "b", "b9"), transformed);
  }

  private static final SortedMap<Integer, Integer> EMPTY = Maps.newTreeMap();
  private static final SortedMap<Integer, Integer> SINGLETON =
      ImmutableSortedMap.of(1, 2);

  public void testMapDifferenceEmptyEmpty() {
    SortedMapDifference<Integer, Integer> diff =
        SortedMaps.difference(EMPTY, EMPTY);
    assertTrue(diff.areEqual());
    assertEquals(EMPTY, diff.entriesOnlyOnLeft());
    assertEquals(EMPTY, diff.entriesOnlyOnRight());
    assertEquals(EMPTY, diff.entriesInCommon());
    assertEquals(EMPTY, diff.entriesDiffering());
    assertEquals("equal", diff.toString());
  }

  public void testMapDifferenceEmptySingleton() {
    SortedMapDifference<Integer, Integer> diff =
        SortedMaps.difference(EMPTY, SINGLETON);
    assertFalse(diff.areEqual());
    assertEquals(EMPTY, diff.entriesOnlyOnLeft());
    assertEquals(SINGLETON, diff.entriesOnlyOnRight());
    assertEquals(EMPTY, diff.entriesInCommon());
    assertEquals(EMPTY, diff.entriesDiffering());
    assertEquals("not equal: only on right={1=2}", diff.toString());
  }

  public void testMapDifferenceSingletonEmpty() {
    SortedMapDifference<Integer, Integer> diff =
        SortedMaps.difference(SINGLETON, EMPTY);
    assertFalse(diff.areEqual());
    assertEquals(SINGLETON, diff.entriesOnlyOnLeft());
    assertEquals(EMPTY, diff.entriesOnlyOnRight());
    assertEquals(EMPTY, diff.entriesInCommon());
    assertEquals(EMPTY, diff.entriesDiffering());
    assertEquals("not equal: only on left={1=2}", diff.toString());
  }

  public void testMapDifferenceTypical() {
    SortedMap<Integer, String> left =
        ImmutableSortedMap.<Integer, String>reverseOrder()
        .put(1, "a").put(2, "b").put(3, "c").put(4, "d").put(5, "e")
        .build();

    SortedMap<Integer, String> right =
        ImmutableSortedMap.of(1, "a", 3, "f", 5, "g", 6, "z");

    SortedMapDifference<Integer, String> diff1 =
        SortedMaps.difference(left, right);
    assertFalse(diff1.areEqual());
    ASSERT.that(diff1.entriesOnlyOnLeft().entrySet()).hasContentsInOrder(
        Maps.immutableEntry(4, "d"), Maps.immutableEntry(2, "b"));
    ASSERT.that(diff1.entriesOnlyOnRight().entrySet()).hasContentsInOrder(
        Maps.immutableEntry(6, "z"));
    ASSERT.that(diff1.entriesInCommon().entrySet()).hasContentsInOrder(
        Maps.immutableEntry(1, "a"));
    ASSERT.that(diff1.entriesDiffering().entrySet()).hasContentsInOrder(
        Maps.immutableEntry(5, ValueDifferenceImpl.create("e", "g")),
        Maps.immutableEntry(3, ValueDifferenceImpl.create("c", "f")));
    assertEquals("not equal: only on left={4=d, 2=b}: only on right={6=z}: "
        + "value differences={5=(e, g), 3=(c, f)}", diff1.toString());

    SortedMapDifference<Integer, String> diff2 =
        SortedMaps.difference(right, left);
    assertFalse(diff2.areEqual());
    ASSERT.that(diff2.entriesOnlyOnLeft().entrySet()).hasContentsInOrder(
        Maps.immutableEntry(6, "z"));
    ASSERT.that(diff2.entriesOnlyOnRight().entrySet()).hasContentsInOrder(
        Maps.immutableEntry(2, "b"), Maps.immutableEntry(4, "d"));
    ASSERT.that(diff1.entriesInCommon().entrySet()).hasContentsInOrder(
        Maps.immutableEntry(1, "a"));
    assertEquals(ImmutableMap.of(
            3, ValueDifferenceImpl.create("f", "c"),
            5, ValueDifferenceImpl.create("g", "e")),
        diff2.entriesDiffering());
    assertEquals("not equal: only on left={6=z}: only on right={2=b, 4=d}: "
        + "value differences={3=(f, c), 5=(g, e)}", diff2.toString());
  }

  public void testMapDifferenceImmutable() {
    SortedMap<Integer, String> left = Maps.newTreeMap(
        ImmutableSortedMap.of(1, "a", 2, "b", 3, "c", 4, "d", 5, "e"));
    SortedMap<Integer, String> right =
        Maps.newTreeMap(ImmutableSortedMap.of(1, "a", 3, "f", 5, "g", 6, "z"));

    SortedMapDifference<Integer, String> diff1 =
        SortedMaps.difference(left, right);
    left.put(6, "z");
    assertFalse(diff1.areEqual());
    ASSERT.that(diff1.entriesOnlyOnLeft().entrySet()).hasContentsInOrder(
        Maps.immutableEntry(2, "b"), Maps.immutableEntry(4, "d"));
    ASSERT.that(diff1.entriesOnlyOnRight().entrySet()).hasContentsInOrder(
        Maps.immutableEntry(6, "z"));
    ASSERT.that(diff1.entriesInCommon().entrySet()).hasContentsInOrder(
        Maps.immutableEntry(1, "a"));
    ASSERT.that(diff1.entriesDiffering().entrySet()).hasContentsInOrder(
        Maps.immutableEntry(3, ValueDifferenceImpl.create("c", "f")),
        Maps.immutableEntry(5, ValueDifferenceImpl.create("e", "g")));
    try {
      diff1.entriesInCommon().put(7, "x");
      fail();
    } catch (UnsupportedOperationException expected) {
    }
    try {
      diff1.entriesOnlyOnLeft().put(7, "x");
      fail();
    } catch (UnsupportedOperationException expected) {
    }
    try {
      diff1.entriesOnlyOnRight().put(7, "x");
      fail();
    } catch (UnsupportedOperationException expected) {
    }
  }

  public void testMapDifferenceEquals() {
    SortedMap<Integer, String> left =
        ImmutableSortedMap.of(1, "a", 2, "b", 3, "c", 4, "d", 5, "e");
    SortedMap<Integer, String> right =
        ImmutableSortedMap.of(1, "a", 3, "f", 5, "g", 6, "z");
    SortedMap<Integer, String> right2 =
        ImmutableSortedMap.of(1, "a", 3, "h", 5, "g", 6, "z");
    SortedMapDifference<Integer, String> original =
        SortedMaps.difference(left, right);
    SortedMapDifference<Integer, String> same =
        SortedMaps.difference(left, right);
    SortedMapDifference<Integer, String> reverse =
        SortedMaps.difference(right, left);
    SortedMapDifference<Integer, String> diff2 =
        SortedMaps.difference(left, right2);

    new EqualsTester()
        .addEqualityGroup(original, same)
        .addEqualityGroup(reverse)
        .addEqualityGroup(diff2)
        .testEquals();
  }

  // Not testing Map methods of SortedMaps.filter*, since the implementation
  // doesn't override Maps.FilteredEntryMap, which is already tested.
  
  private static final Predicate<Integer> EVEN =
      new Predicate<Integer>() {
        @Override
        public boolean apply(Integer input) {
          return input % 2 == 0;
        }
      };
  
  public void testFilterKeys() {
    Comparator<Integer> comparator = Ordering.natural();
    SortedMap<Integer, String> unfiltered = Maps.newTreeMap(comparator);
    unfiltered.put(1, "one");
    unfiltered.put(2, "two");
    unfiltered.put(3, "three");
    unfiltered.put(4, "four");
    unfiltered.put(5, "five");
    unfiltered.put(6, "six");
    unfiltered.put(7, "seven");
    SortedMap<Integer, String> filtered 
        = SortedMaps.filterKeys(unfiltered, EVEN);
    ASSERT.that(filtered.keySet()).hasContentsInOrder(2, 4, 6);
    assertSame(comparator, filtered.comparator());
    assertEquals((Integer) 2, filtered.firstKey());
    assertEquals((Integer) 6, filtered.lastKey());
    ASSERT.that(filtered.headMap(5).keySet()).hasContentsInOrder(2, 4);
    ASSERT.that(filtered.tailMap(3).keySet()).hasContentsInOrder(4, 6);
    ASSERT.that(filtered.subMap(3, 5).keySet()).hasContentsInOrder(4);
  }
  
  private static final Predicate<String> NOT_LENGTH_3 =
      new Predicate<String>() {
        @Override
        public boolean apply(String input) {
          return input == null || input.length() != 3;
        }
      };

  public void testFilterValues() {
    Comparator<Integer> comparator = Ordering.natural();
    SortedMap<Integer, String> unfiltered = Maps.newTreeMap(comparator);
    unfiltered.put(1, "one");
    unfiltered.put(2, "two");
    unfiltered.put(3, "three");
    unfiltered.put(4, "four");
    unfiltered.put(5, "five");
    unfiltered.put(6, "six");
    unfiltered.put(7, "seven");
    SortedMap<Integer, String> filtered 
        = SortedMaps.filterValues(unfiltered, NOT_LENGTH_3);
    ASSERT.that(filtered.keySet()).hasContentsInOrder(3, 4, 5, 7);
    assertSame(comparator, filtered.comparator());
    assertEquals((Integer) 3, filtered.firstKey());
    assertEquals((Integer) 7, filtered.lastKey());
    ASSERT.that(filtered.headMap(5).keySet()).hasContentsInOrder(3, 4);
    ASSERT.that(filtered.tailMap(4).keySet()).hasContentsInOrder(4, 5, 7);
    ASSERT.that(filtered.subMap(4, 6).keySet()).hasContentsInOrder(4, 5);
  }

  private static final Predicate<Map.Entry<Integer, String>>
      EVEN_AND_LENGTH_3 = new Predicate<Map.Entry<Integer, String>>() {
        @Override public boolean apply(Entry<Integer, String> entry) {
          return (entry.getKey() == null || entry.getKey() % 2 == 0) 
              && (entry.getValue() == null || entry.getValue().length() == 3);
        }   
    };
    
  private static class ContainsKeySafeSortedMap 
      extends ForwardingSortedMap<Integer, String> {
    SortedMap<Integer, String> delegate 
        = Maps.newTreeMap(Ordering.natural().nullsFirst());
    
    @Override protected SortedMap<Integer, String> delegate() {
      return delegate;
    }
    
    // Needed by MapInterfaceTest.testContainsKey()
    @Override public boolean containsKey(Object key) {
      try {
        return super.containsKey(key);
      } catch (ClassCastException e) {
        return false;
      }
    }
  }
  
  public static class FilteredEntriesSortedMapInterfaceTest 
      extends SortedMapInterfaceTest<Integer, String> {
    public FilteredEntriesSortedMapInterfaceTest() {
      super(true, true, true, true, true);      
    }

    @Override protected SortedMap<Integer, String> makeEmptyMap() {
      SortedMap<Integer, String> unfiltered = new ContainsKeySafeSortedMap();
      unfiltered.put(1, "one");
      unfiltered.put(3, "three");
      unfiltered.put(4, "four");         
      unfiltered.put(5, "five");
      return SortedMaps.filterEntries(unfiltered, EVEN_AND_LENGTH_3);
    }

    @Override protected SortedMap<Integer, String> makePopulatedMap() {
      SortedMap<Integer, String> unfiltered = new ContainsKeySafeSortedMap();
      unfiltered.put(1, "one");
      unfiltered.put(2, "two");
      unfiltered.put(3, "three");
      unfiltered.put(4, "four");
      unfiltered.put(5, "five");
      unfiltered.put(6, "six");
      return SortedMaps.filterEntries(unfiltered, EVEN_AND_LENGTH_3);
    }

    @Override protected Integer getKeyNotInPopulatedMap() {
      return 10;
    }

    @Override protected String getValueNotInPopulatedMap() {
      return "ten";
    }
    
    // Iterators don't support remove.
    @Override public void testEntrySetIteratorRemove() {}
    @Override public void testValuesIteratorRemove() {}
    
    // These tests fail on GWT.
    // TODO: Investigate why.
    @Override public void testEntrySetRemoveAll() {}
    @Override public void testEntrySetRetainAll() {}
  }
}