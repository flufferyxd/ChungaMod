package magnileve.chungamod.util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * A {@link DoubleKeyMap} backed by instances of {@link HashMap} for mapping secondary keys to primary keys and primary keys to values.
 * @param <K1> primary key type
 * @param <K2> secondary key type
 * @param <V> value type
 * @author Magnileve
 */
public class DoubleKeyHashMap<K1, K2, V> implements DoubleKeyMap<K1, K2, V> {

private final Map<K1, V> map;
private final Map<K2, K1> keyMap;
private final BiFunction<K1, V, K2> keyConverter;

/**
 * Constructs an empty {@code DoubleKeyHashMap} with the specified initial
 * capacity and load factor.
 *
 * @param keyConverter produces a secondary key from a primary key and its associated value
 * @param  initialCapacity the initial capacity
 * @param  loadFactor      the load factor
 * @throws IllegalArgumentException if the initial capacity is negative
 *         or the load factor is not positive
 */
public DoubleKeyHashMap(BiFunction<K1, V, K2> keyConverter, int initialCapacity, float loadFactor) {
	map = new HashMap<>(initialCapacity, loadFactor);
	keyMap = new HashMap<>(initialCapacity, loadFactor);
	this.keyConverter = keyConverter;
}

/**
 * Constructs an empty {@code DoubleKeyHashMap} with the specified initial
 * capacity and the default load factor (0.75).
 *
 * @param keyConverter produces a secondary key from a primary key and its associated value
 * @param  initialCapacity the initial capacity
 * @throws IllegalArgumentException if the initial capacity is negative
 */
public DoubleKeyHashMap(BiFunction<K1, V, K2> keyConverter, int initialCapacity) {
	map = new HashMap<>(initialCapacity);
	keyMap = new HashMap<>(initialCapacity);
	this.keyConverter = keyConverter;
}

/**
 * Constructs an empty {@code DoubleKeyHashMap} with the default initial capacity
 * (16) and the default load factor (0.75).
 * @param keyConverter produces a secondary key from a primary key and its associated value
 */
public DoubleKeyHashMap(BiFunction<K1, V, K2> keyConverter) {
	map = new HashMap<>();
	keyMap = new HashMap<>();
	this.keyConverter = keyConverter;
}
/**
 * Constructs a new {@code DoubleKeyHashMap} with the same mappings as the
 * specified {@code Map}.  The {@code DoubleKeyHashMap} is created with
 * default load factor (0.75) and an initial capacity sufficient to
 * hold the mappings in the specified {@code Map}.
 *
 * @param keyConverter produces a secondary key from a primary key and its associated value
 * @param   m the map whose mappings are to be placed in this map
 * @throws  NullPointerException if the specified map is null
 */
public DoubleKeyHashMap(BiFunction<K1, V, K2> keyConverter, Map<? extends K1, ? extends V> m) {
	map = new HashMap<>(m);
	keyMap = new HashMap<>();
	for(Entry<? extends K1, ? extends V> entry:m.entrySet()) keyMap.put(keyConverter.apply(entry.getKey(), entry.getValue()), entry.getKey());
	this.keyConverter = keyConverter;
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
	return map.containsKey(key);
}

@Override
public boolean containsSecondaryKey(Object key) {
	return keyMap.containsKey(key);
}

@Override
public boolean containsValue(Object value) {
	return map.containsValue(value);
}

@Override
public V get(Object key) {
	return map.get(key);
}

@Override
public K1 getKeyFromSecondaryKey(Object key) {
	return keyMap.get(key);
}

@Override
public V put(K1 key, V value) {
	keyMap.put(keyConverter.apply(key, value), key);
	return map.put(key, value);
}

@SuppressWarnings("unchecked")
@Override
public V remove(Object key) {
	if(map.containsKey(key)) {
		V value = map.remove(key);
		keyMap.remove(keyConverter.apply((K1) key, value));
		return value;
	}
	return null;
}

@Override
public V removeFromSecondaryKey(Object key) {
	if(keyMap.containsKey(key)) return map.remove(keyMap.remove(key));
	return null;
}

@Override
public void putAll(Map<? extends K1, ? extends V> m) {
	for(Entry<? extends K1, ? extends V> entry:m.entrySet()) keyMap.put(keyConverter.apply(entry.getKey(), entry.getValue()), entry.getKey());
	map.putAll(m);
}

@Override
public void clear() {
	keyMap.clear();
	map.clear();
}

@Override
public Set<K1> keySet() {
	return map.keySet();
}

@Override
public Set<K2> secondaryKeySet() {
	return keyMap.keySet();
}

@Override
public Collection<V> values() {
	return map.values();
}

@Override
public Set<Entry<K1, V>> entrySet() {
	return map.entrySet();
}

@Override
public Set<Entry<K2, K1>> keysEntrySet() {
	return keyMap.entrySet();
}

@Override
public Set<Entry<K2, V>> secondaryKeyEntrySet() {
	return new AbstractSet<Entry<K2, V>>() {
		@Override
		public Iterator<Entry<K2, V>> iterator() {
			return new Iterator<Entry<K2, V>>() {
				Iterator<Entry<K2, K1>> iter = keysEntrySet().iterator();
				
				@Override
				public boolean hasNext() {
					return iter.hasNext();
				}
				
				@Override
				public Entry<K2, V> next() {
					Entry<K2, K1> keyEntry = iter.next();
					K2 entryKey = keyEntry.getKey();
					return new Entry<K2, V>(){
						V entryValue = get(keyEntry.getValue());
						
						@Override
						public K2 getKey() {
							return entryKey;
						}
						
						@Override
						public V getValue() {
							return entryValue;
						}
						
						@Override
						public V setValue(V value) {
							entryValue = value;
							return put(getKeyFromSecondaryKey(entryKey), value);
						}
					};
				}
			};
		}
		
		@Override
		public int size() {
			return map.size();
		}
	};
}

@Override
public String toString() {
    Iterator<Entry<K1,V>> i = entrySet().iterator();
    if (! i.hasNext())
        return "{}";

    StringBuilder sb = new StringBuilder();
    sb.append('{');
    for (;;) {
        Entry<K1,V> e = i.next();
        K1 key = e.getKey();
        V value = e.getValue();
        K2 secondaryKey = keyConverter.apply(key, value);
        sb.append(key   == this ? "(this Map)" : key);
        sb.append(":");
        sb.append(secondaryKey == this ? "(this Map)" : secondaryKey);
        sb.append('=');
        sb.append(value == this ? "(this Map)" : value);
        if (! i.hasNext())
            return sb.append('}').toString();
        sb.append(',').append(' ');
    }
}

@Override
public boolean equals(Object obj) {
	if(this == obj) return true;
	if(obj instanceof DoubleKeyMap) {
		DoubleKeyMap<?, ?, ?> other = (DoubleKeyMap<?, ?, ?>) obj;
		if(size() != other.size()) return false;
		
		Iterator<Entry<K1, V>> thisEntries = entrySet().iterator();
		while (thisEntries.hasNext()) {
            Entry<K1,V> e = thisEntries.next();
            K1 key = e.getKey();
            V value = e.getValue();
            if (value == null) {
                if (!(other.get(key)==null && other.containsKey(key))) return false;
            } else {
                if (!value.equals(other.get(key))) return false;
            }
        }
		
		Iterator<Entry<K2, K1>> thisKeys = keysEntrySet().iterator();
		while (thisKeys.hasNext()) {
            Entry<K2,K1> e = thisKeys.next();
            K2 key = e.getKey();
            K1 value = e.getValue();
            if (value == null) {
                if (!(other.getKeyFromSecondaryKey(key)==null && other.containsSecondaryKey(key))) return false;
            } else {
                if (!value.equals(other.getKeyFromSecondaryKey(key))) return false;
            }
        }
		return true;
	}
	if(obj instanceof Map) return map.equals(obj);
	return false;
}

}