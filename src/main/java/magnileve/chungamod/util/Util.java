package magnileve.chungamod.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.IntSupplier;

/**
 * Contains static utility methods to use with the Java API.
 * @author Magnileve
 */
public class Util {

/**
 * An empty {@code String} array.
 */
public static final String[] STRING_ARRAY_0 = new String[0];
/**
 * An empty {@code Class} array.
 */
public static final Class<?>[] CLASS_ARRAY_0 = new Class<?>[0];
/**
 * Compares map entries with {@code String} keys lexicographically.
 */
public static final Comparator<Map.Entry<String, ?>> STRING_KEY_COMPARATOR = (o1, o2) -> o1.getKey().compareTo(o2.getKey());
/**
 * An {@link Iterable} that always returns empty iterators.
 * @see #emptyIterable()
 */
public static final Iterable<?> EMPTY_ITERABLE = new Iterable<Object>() {
	private final Iterator<Object> emptyIterator = new Iterator<Object>() {
		@Override public boolean hasNext() {return false;}
		@Override public Object next() {throw new NoSuchElementException();}
	};
	
	@Override
	public Iterator<Object> iterator() {
		return emptyIterator;
	}
};

private Util() {}

/**
 * Returns a {@code String} representation of an object, including support for {@code null} and arrays.
 * @param o an object
 * @return a {@code String} representation of the object
 * @see String#valueOf(Object)
 * @see Object#toString()
 */
public static String toString(Object o) {
	if(o == null) return "null";
	if(o instanceof Object[]) {
		Object[] a = (Object[]) o;
		if(a.length == 0) return "[]";
		StringBuilder str = new StringBuilder().append('[').append(toString(a[0]));
		for(int i = 1; i < a.length; i++) str.append(", ").append(toString(a[i]));
		return str.append(']').toString();
	}
	return o.toString();
}

/**
 * Returns {@code true} if the arguments are equal to each other.
 * This method also considers {@code null} equal to {@code null}
 * and object arrays of the same length, with equal elements, equal to each other.
 * @param a an object
 * @param b an object to be compared with {@code a} for equality
 * @return {@code true} if the arguments are equal to each other; {@code false} otherwise
 * @see java.util.Objects#equals(Object, Object) Objects.equals(Object, Object)
 * @see Object#equals(Object)
 */
public static boolean equals(Object a, Object b) {
	if(a == b) return true;
	if(a == null) return false;
	if(a instanceof Object[] && b != null && b instanceof Object[]) {
		Object[] a1 = (Object[]) a;
		Object[] b1 = (Object[]) b;
		int length = a1.length;
		if(length != b1.length) return false;
		for(int i = 0; i < length; i++) if(!equals(a1[i], b1[i])) return false;
		return true;
	}
    return a.equals(b);
}

/**
 * Returns {@code true} if each two elements in each index of the given range in the given arrays are equal to each other.
 * This method uses {@link #equals(Object, Object)} to check for equality.
 * @param a an array
 * @param b an array to be partially compared with {@code a} for equality
 * @param start beginning index of comparison, inclusive
 * @param end ending index of comparison, exclusive
 * @return {@code true} if the arguments are equal to each other; {@code false} otherwise
 * @throws ArrayIndexOutOfBoundsException if {@code start} or {@code end} is less than zero or greater than the size of either of the arrays
 */
public static boolean arraySectionEquals(Object[] a, Object[] b, int start, int end) {
	for(int i = start; i < end; i++) if(!equals(a[i], b[i])) return false;
	return true;
}

/**
 * Formats milliseconds into a number of seconds followed by {@code 's'}.
 * @param milliseconds an amount of milliseconds
 * @return the {@code String} representation of {@code milliseconds} divided by {@code 1000} followed by {@code s}
 */
public static String formatSeconds(long milliseconds) {
	String input = String.valueOf(milliseconds);
	int l = input.length();
	StringBuilder build = new StringBuilder(l < 4 ? 6 : l + 2);
	int i = 0;
	while(i < l - 3) {
		build.append(input.charAt(i));
		i++;
	}
	if(l < 4) {
		build.append('0').append('.');
		i = l - 3;
		while(i < 0) {
			build.append('0');
			i++;
		}
	} else build.append('.');
	while(i < l) {
		build.append(input.charAt(i));
		i++;
	}
	return build.append('s').toString();
}

/**
 * Inverse of {@link String#split(String)}.
 * @param strings array of split strings to be converted back into one string
 * @param insert inserted between each split string
 */
public static String inverseSplit(String[] strings, String insert) {
	if (strings.length == 0) return "";
	StringBuilder str = new StringBuilder(strings[0]);
	for (int i = 1; i < strings.length; i++) str.append(insert).append(strings[i]);
	return str.toString();
}

/**
 * Concatenates one {@code String} to another.
 * @param str1 a string
 * @param str2 a string to be concatenated to the end of {@code str1}
 * @return a string that represents the concatenation of {@code str1}'s characters followed by {@code str2}'s characters
 * @see String#concat(String)
 */
public static String concat(String str1, String str2) {
	return str1.isEmpty() ? str2 : str1.concat(str2);
}

/**
 * Sorts this collection into a new list according to the order induced by the specified {@link Comparator}.
 * @param <T> element type to be sorted
 * @param collection contains the elements to be sorted
 * @param comparator the {@code Comparator} used to compare list elements.
 * A {@code null} value indicates that the elements'
 * {@linkplain Comparable natural ordering} should be used
 * @return a {@link List} containing the elements of the given collection in order
 * @throws ClassCastException if the collection contains elements that are not mutually comparable using the specified comparator
 * @see List#sort(Comparator)
 */
public static <T> List<T> sort(Collection<T> collection, Comparator<T> comparator) {
	List<T> list = new ArrayList<>(collection);
	list.sort(comparator);
	return list;
}

/**
 * Sorts this collection into a new list according to the natural order of the elements.
 * @param <T> element type to be sorted
 * @param collection contains the elements to be sorted
 * @return a {@link List} containing the elements of the given collection in order
 * @throws ClassCastException if the collection contains elements that do not implement {@link Comparable}
 * @see List#sort(Comparator)
 */
public static <T> List<T> sort(Collection<T> collection) {
	return sort(collection, null);
}

/**
 * Sorts entries of a map with {@code String} keys lexicographically.
 * @param <T> entry value type
 * @param map map of entries to be sorted
 * @return a {@link List} of entries in the given map sorted lexicographically by their keys
 */
@SuppressWarnings("unchecked")
public static <T> List<Map.Entry<String, T>> sortEntries(Map<String, T> map) {
	return (List<Map.Entry<String, T>>) (List<?>) sort((Collection<Map.Entry<String, ?>>) (Collection<?>)
			map.entrySet(), STRING_KEY_COMPARATOR);
}

/**
 * Adds an element to a collection if it is not null.
 * @param <E> element type
 * @param c a collection
 * @param e an element
 * @return {@code true} if the collection has changed as a result of this call; {@code false} otherwise
 */
public static <E> boolean addIfNotNull(Collection<E> c, E e) {
	return e == null ? false : c.add(e);
}

/**
 * Compares the size of an {@link Iterable} to a value by iterating until either there are no more elements remaining or the value has been passed.
 * @param iterable an iterable
 * @param value a value
 * @return 1 if the iterator returned by the iterable has more elements than {@code value},
 * 0 if the amount of elements is equal to {@code value}, or -1 if the amount of elements is less than {@code value}
 */
public static int compareSizeTo(Iterable<?> iterable, int value) {
	Iterator<?> iter = iterable.iterator();
	for(int i = 0; i < value; i++) {
		if(!iter.hasNext()) return -1;
		iter.next();
	}
	return iter.hasNext() ? 1 : 0;
}

/**
 * Returns an {@link IntSupplier} that returns an {@code int} {@code 1} higher than the previous result, starting with {@code 0}.
 * @return an {@link IntSupplier} that returns an {@code int} {@code 1} higher than the previous result, starting with {@code 0}
 */
public static IntSupplier newIDSupplier() {
	return new IntSupplier() {
		private int id;
		
		@Override
		public int getAsInt() {
			return id++;
		}
	};
}

/**
 * Returns the closest {@code byte} value to an {@code int} value.
 * @param value an {@code int}
 * @return a {@code byte} with a value closest to {@code value}
 */
public static byte roundToByte(int value) {
	if(value < Byte.MIN_VALUE) return Byte.MIN_VALUE;
	if(value > Byte.MAX_VALUE) return Byte.MAX_VALUE;
	return (byte) value;
}

/**
 * Returns the closest {@code short} value to an {@code int} value.
 * @param value an {@code int}
 * @return a {@code short} with a value closest to {@code value}
 */
public static short roundToShort(int value) {
	if(value < Short.MIN_VALUE) return Short.MIN_VALUE;
	if(value > Short.MAX_VALUE) return Short.MAX_VALUE;
	return (short) value;
}

/**
 * Finds the first index in {@code array} of any value in {@code values} using {@link #equals(Object, Object)} to check equality.
 * @param array an array to search
 * @param values values to search the array for
 * @return the first index in {@code array} of any value in {@code values}, or {@code -1} if no values are found
 */
public static int indexOfAny(Object[] array, Object[] values) {
	for(int i = 0; i < array.length; i++) for(int h = 0; h < values.length; h++) if(equals(array[i], array[h])) return i;
	return -1;
}

/**
 * Finds the first index in an array of a value using {@link #equals(Object, Object)} to check equality.
 * @param array an array to search
 * @param value a value to search the array for
 * @return the first index in {@code array} of {@code value}, or {@code -1} if no values are found
 */
public static int indexOf(Object[] array, Object value) {
	if(value == null) for(int i = 0; i < array.length; i++) {
		if(array[i] == null) return i;
	} else if(value instanceof Object[]) for(int i = 0; i < array.length; i++) {
		if(equals(value, array[i])) return i;
	} else for(int i = 0; i < array.length; i++) {
		if(value.equals(array[i])) return i;
	}
	return -1;
}

/**
 * If an object is an object array, clones the object array and any object arrays contained by it recursively.
 * @param <T> type of object
 * @param obj an object
 * @return if {@code obj} is an object array, an array with all of its dimensions cloned; otherwise, {@code obj} unaffected
 */
@SuppressWarnings("unchecked")
public static <T> T recursiveArrayClone(T obj) {
	if(obj instanceof Object[]) {
		Object[] array = (Object[]) obj;
		int l = array.length;
		Object[] clone = (Object[]) Array.newInstance(array.getClass().getComponentType(), l);
		for(int i = 0; i < l; i++) clone[i] = recursiveArrayClone(array[i]);
		return (T) clone;
	}
	return obj;
}

/**
 * Returns an {@link Iterable} that always returns empty iterators.
 * @param <T> element type
 * @return an {@link Iterable} that always returns empty iterators
 * @see #EMPTY_ITERABLE
 */
@SuppressWarnings("unchecked")
public static <T> Iterable<T> emptyIterable() {
	return (Iterable<T>) EMPTY_ITERABLE;
}

}