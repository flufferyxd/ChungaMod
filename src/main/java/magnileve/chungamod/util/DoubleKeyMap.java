package magnileve.chungamod.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Extends {@link Map} with support for a secondary key.
 * When a key-value pair is added to this map,
 * the key is converted to a secondary key by a constant process, and the secondary key is mapped back to the primary key.
 * Thus, secondary keys can be used to access primary keys, and primary keys can be used to access values.
 * @param <K1> primary key type
 * @param <K2> secondary key type
 * @param <V> value type
 * @author Magnileve
 */
public interface DoubleKeyMap<K1, K2, V> extends Map<K1, V> {

/**
 * Returns {@code true} if this map contains a mapping for the specified secondary
 * key.  More formally, returns {@code true} if and only if
 * this map contains a mapping for a secondary key {@code k} such that
 * {@code (key==null ? k==null : key.equals(k))}.  (There can be
 * at most one such mapping.)
 * @param key secondary key whose presence in this map is to be tested
 * @return {@code true} if this map contains a mapping for the specified key
 */
public boolean containsSecondaryKey(Object key);

/**
 * <p>Returns the value to which the specified secondary key is mapped,
 * or {@code null} if this map contains no mapping for the secondary key.</p>
 *
 * <p>More formally, if this map contains a mapping from a secondary key
 * {@code k} to a value {@code v} such that {@code (key==null ? k==null :
 * key.equals(k))}, then this method returns {@code v}; otherwise
 * it returns {@code null}.  (There can be at most one such mapping.)</p>
 *
 * <p>If this map permits null values, then a return value of
 * {@code null} does not <i>necessarily</i> indicate that the map
 * contains no mapping for the secondary key; it's also possible that the map
 * explicitly maps the secondary key to {@code null}.  The {@link #containsSecondaryKey(Object)
 * containsSecondaryKey} operation may be used to distinguish these two cases.</p>
 *
 * @param key the secondary key whose associated value is to be returned
 * @return the value to which the specified secondary key is mapped, or
 *         {@code null} if this map contains no mapping for the secondary key
 * @throws ClassCastException if the secondary key is of an inappropriate type for
 *         this map (optional)
 * @throws NullPointerException if the specified secondary key is null and this map
 *         does not permit null keys (optional)
 */
public default V getFromSecondaryKey(Object key) {
	K1 primary = getKeyFromSecondaryKey(key);
	return primary != null || containsSecondaryKey(key) ? get(primary) : null;
}

/**
 * <p>Returns the primary key to which the specified secondary key is mapped,
 * or {@code null} if this map contains no mapping for the secondary key.</p>
 *
 * <p>More formally, if this map contains a mapping from a secondary key
 * {@code k2} to a primary key {@code k1} such that {@code (key==null ? k2==null :
 * key.equals(k2))}, then this method returns {@code k1}; otherwise
 * it returns {@code null}.  (There can be at most one such mapping.)</p>
 *
 * <p>If this map permits null keys, then a return value of
 * {@code null} does not <i>necessarily</i> indicate that the map
 * contains no mapping for the secondary key; it's also possible that the map
 * explicitly maps the secondary key to the primary key {@code null}.  The {@link #containsSecondaryKey(Object)
 * containsSecondaryKey} operation may be used to distinguish these two cases.</p>
 *
 * @param key the secondary key whose associated primary key is to be returned
 * @return the primary key to which the specified secondary key is mapped, or
 *         {@code null} if this map contains no mapping for the secondary key
 * @throws ClassCastException if the secondary key is of an inappropriate type for
 *         this map (optional)
 * @throws NullPointerException if the specified secondary key is null and this map
 *         does not permit null keys (optional)
 */
public K1 getKeyFromSecondaryKey(Object key);

public default V putSecondaryKey(K2 key, V value) {
	K1 primary = getKeyFromSecondaryKey(key);
	return primary != null || containsSecondaryKey(key) ? put(primary, value) : null;
}

/**
 * <p>Removes the mapping for a secondary key and its associated primary key from this map if it is present
 * (optional operation).   More formally, if this map contains a mapping
 * from secondary key {@code k2} to primary key {@code k1} to value {@code v} such that
 * {@code (key==null ?  k2==null : key.equals(k2))}, that mapping
 * is removed.  (The map can contain at most one such mapping.)</p>
 *
 * <p>Returns the value to which this map previously associated the secondary key,
 * or {@code null} if the map contained no mapping for the secondary key.</p>
 *
 * <p>If this map permits null values, then a return value of
 * {@code null} does not <i>necessarily</i> indicate that the map
 * contained no mapping for the secondary key; it's also possible that the map
 * explicitly mapped the secondary key to {@code null}.</p>
 *
 * <p>The map will not contain a mapping for the specified secondary key once the
 * call returns.</p>
 *
 * @param key secondary key whose mapping is to be removed from the map
 * @return the previous value associated with {@code key}, or
 *         {@code null} if there was no mapping for {@code key}.
 * @throws UnsupportedOperationException if the {@code remove} operation
 *         is not supported by this map
 * @throws ClassCastException if the secondary key is of an inappropriate type for
 *         this map (optional)
 * @throws NullPointerException if the specified secondary key is null and this
 *         map does not permit null secondary keys (optional)
 */
public V removeFromSecondaryKey(Object key);

/**
 * Returns a {@link Set} view of the secondary keys contained in this map.
 * The set is backed by the map, so changes to the map are
 * reflected in the set, and vice-versa.  If the map is modified
 * while an iteration over the set is in progress (except through
 * the iterator's own {@code remove} operation), the results of
 * the iteration are undefined.  The set supports element removal,
 * which removes the corresponding mapping from the map, via the
 * {@code Iterator.remove}, {@code Set.remove},
 * {@code removeAll}, {@code retainAll}, and {@code clear}
 * operations.  It does not support the {@code add} or {@code addAll}
 * operations.
 *
 * @return a set view of the secondary keys contained in this map
 */
public Set<K2> secondaryKeySet();

/**
 * Returns a {@link Set} view of the mappings of secondary keys to primary keys contained in this map.
 * The set is backed by the map, so changes to the map are
 * reflected in the set, and vice-versa.  If the map is modified
 * while an iteration over the set is in progress (except through
 * the iterator's own {@code remove} operation, or through the
 * {@code setValue} operation on a map entry returned by the
 * iterator) the results of the iteration are undefined.  The set
 * supports element removal, which removes the corresponding
 * mapping from the map, via the {@code Iterator.remove},
 * {@code Set.remove}, {@code removeAll}, {@code retainAll}, and
 * {@code clear} operations.  It does not support the
 * {@code add} or {@code addAll} operations.
 *
 * @return a set view of the mappings of secondary keys to primary keys contained in this map
 */
public Set<Entry<K2, K1>> keysEntrySet();

/**
 * Returns a {@link Set} view of the mappings of secondary keys to values contained in this map.
 * The set is backed by the map, so changes to the map are
 * reflected in the set, and vice-versa.  If the map is modified
 * while an iteration over the set is in progress (except through
 * the iterator's own {@code remove} operation, or through the
 * {@code setValue} operation on a map entry returned by the
 * iterator) the results of the iteration are undefined.  The set
 * supports element removal, which removes the corresponding
 * mapping from the map, via the {@code Iterator.remove},
 * {@code Set.remove}, {@code removeAll}, {@code retainAll}, and
 * {@code clear} operations.  It does not support the
 * {@code add} or {@code addAll} operations.
 *
 * @return a set view of the mappings of secondary keys to values contained in this map
 */
public Set<Entry<K2, V>> secondaryKeyEntrySet();

/**
 * Returns a {@link Map} of secondary keys to values as contained in this map.  
 * The returned map is backed by the map, so changes to the this map are
 * reflected in the returned map, and vice-versa.
 * The returned map does not support {@link #put(K, V)} or {@link #putAll(Map)}.
 * All removal methods are supported by the returned map.
 * @return a {@code Map} of secondary keys to values as contained in this map
 */
public default Map<K2, V> secondaryKeyOnlyMap() {
	return new Map<K2, V>() {
		@Override
		public void clear() {
			DoubleKeyMap.this.clear();
		}
		
		@Override
		public boolean containsKey(Object key) {
			return containsSecondaryKey(key);
		}
		
		@Override
		public boolean containsValue(Object value) {
			return DoubleKeyMap.this.containsValue(value);
		}
		
		@Override
		public Set<Entry<K2, V>> entrySet() {
			return secondaryKeyEntrySet();
		}
		
		@Override
		public V get(Object key) {
			return getFromSecondaryKey(key);
		}
		
		@Override
		public boolean isEmpty() {
			return DoubleKeyMap.this.isEmpty();
		}
		
		@Override
		public Set<K2> keySet() {
			return secondaryKeySet();
		}
		
		@Override
		public V put(K2 key, V value) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void putAll(Map<? extends K2, ? extends V> m) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public V remove(Object key) {
			return removeFromSecondaryKey(key);
		}
		
		@Override
		public int size() {
			return DoubleKeyMap.this.size();
		}
		
		@Override
		public Collection<V> values() {
			return DoubleKeyMap.this.values();
		}
	};
}

}