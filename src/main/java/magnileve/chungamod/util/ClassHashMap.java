package magnileve.chungamod.util;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Maps instances of {@link Class} to values using an internal
 * {@link HashMap} mapping the names returned by {@link Class#getName()} to values.
 * @param <T> common extended type by classes in this map
 * @param <V> value type
 * @author Magnileve
 */
public class ClassHashMap<T, V> implements Map<Class<? extends T>, V>, Serializable {

private static final long serialVersionUID = -7455083605921161495L;

private final Map<String, V> map;

/**
 * Constructs an empty {@code ClassHashMap} with the specified initial
 * capacity and load factor.
 *
 * @param  initialCapacity the initial capacity
 * @param  loadFactor      the load factor
 * @throws IllegalArgumentException if the initial capacity is negative
 *         or the load factor is not positive
 */
public ClassHashMap(int initialCapacity, float loadFactor) {
	map = new HashMap<>(initialCapacity, loadFactor);
}

/**
 * Constructs an empty {@code ClassHashMap} with the specified initial
 * capacity and the default load factor (0.75).
 *
 * @param  initialCapacity the initial capacity
 * @throws IllegalArgumentException if the initial capacity is negative
 */
public ClassHashMap(int initialCapacity) {
	map = new HashMap<>(initialCapacity);
}

/**
 * Constructs an empty {@code ClassHashMap} with the default initial capacity
 * (16) and the default load factor (0.75).
 */
public ClassHashMap() {
	map = new HashMap<>();
}

/**
 * Constructs a new {@code ClassHashMap} with the same mappings as the
 * specified {@code Map}.  The {@code ClassHashMap} is created with
 * default load factor (0.75) and an initial capacity sufficient to
 * hold the mappings in the specified {@code Map}.
 *
 * @param   m the map whose mappings are to be placed in this map
 * @throws  NullPointerException if the specified map is null
 */
public ClassHashMap(Map<? extends Class<? extends T>, ? extends V> m) {
	map = new HashMap<>();
	putMapEntries(m);
}

/**
 * Returns a {@link Set} view of the class name strings used internally as keys in this map.
 * This set is backed by the internal map.
 * @return a {@link Set} view of the class name strings used internally as keys in this map
 */
public Set<String> containedKeySet() {
	return map.keySet();
}

/**
 * Returns a {@link Set} view of the internal string-to-value mappings in this map.
 * This set is backed by the internal map.
 * @return a {@link Set} view of the internal string-to-value mappings in this map
 */
public Set<Entry<String, V>> containedEntrySet() {
	return map.entrySet();
}

private void putMapEntries(Map<? extends Class<? extends T>, ? extends V> map) {
	this.map.putAll(new AbstractMap<String, V>() {
		@Override
		public Set<Entry<String, V>> entrySet() {
			return new AbstractSet<Entry<String,V>>() {
				@Override
				public Iterator<Entry<String, V>> iterator() {
					return map.entrySet().stream().<Entry<String, V>>map(entry -> new Entry<String, V>() {
						@Override
						public String getKey() {
							// TODO Auto-generated method stub
							return null;
						}
						
						@Override
						public V getValue() {
							// TODO Auto-generated method stub
							return null;
						}
						
						@Override
						public V setValue(V value) {
							// TODO Auto-generated method stub
							return null;
						}
					}).iterator();
				}

				@Override
				public int size() {
					return map.size();
				}
			};
		}
	});
}

@Override
public int size() {
	return map.size();
}

@Override
public boolean isEmpty() {
	return map.isEmpty();
}

@Override
public boolean containsKey(Object key) {
	if(key instanceof Class) return map.containsKey(((Class<?>) key).getName());
	return false;
}

@Override
public boolean containsValue(Object value) {
	return map.containsValue(value);
}

@Override
public V get(Object key) {
	if(key instanceof Class) return map.get(((Class<?>) key).getName());
	return null;
}

@Override
public V put(Class<? extends T> key, V value) {
	return map.put(key.getName(), value);
}

@Override
public V remove(Object key) {
	if(key instanceof Class) return map.remove(((Class<?>) key).getName());
	return null;
}

@Override
public void putAll(Map<? extends Class<? extends T>, ? extends V> m) {
	putMapEntries(m);
}

@Override
public void clear() {
	map.clear();
}

@Override
public Set<Class<? extends T>> keySet() {
	throw new UnsupportedOperationException();
}

@Override
public Collection<V> values() {
	return map.values();
}

@Override
public Set<Entry<Class<? extends T>, V>> entrySet() {
	throw new UnsupportedOperationException();
}

}