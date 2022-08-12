package magnileve.chungamod.util;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.RandomAccess;
import java.util.function.Consumer;

/**
 * A {@link java.util.List List} backed by an array.
 * This class differs from {@link java.util.ArrayList ArrayList}
 * in that the backing array is of the element type rather than {@code Object}.
 * Due to the lack of array type safety,
 * {@code ArrayBuildList} is intended to be used privately.
 * This class also contains the method {@link #getArray()}, which returns a direct reference to the backing array for efficiency.
 * @param <E> element type of this list
 * @author Magnileve
 */
public class ArrayBuildList<E> extends AbstractList<E> implements RandomAccess, Cloneable {

private static final int DEFAULT_CAPACITY = 10;

/**
 * The current backing array of this list.
 */
protected E[] array;
/**
 * The current size of this list.
 */
protected int size;

/**
 * Creates a new {@code ArrayBuildList} backed by the given array.
 * If {@code initialSize} is greater than the length of the given array, a copy of the array is made.
 * @param array backing array
 * @param initialSize initial size of this list
 * @throws IllegalArgumentException if {@code initialSize} is negative
 */
public ArrayBuildList(E[] array, int initialSize) {
	if(initialSize < 0) throw new IllegalArgumentException("Length " + initialSize + " must not be negative");
	this.array = initialSize > array.length ? Arrays.copyOf(array, initialSize) : array;
	size = initialSize;
}

/**
 * Creates a new {@code ArrayBuildList} with size {@code 0} backed by the given array.
 * @param array backing array
 */
public ArrayBuildList(E[] array) {
	this(array, 0);
}

/**
 * Creates a new {@code ArrayBuildList} backed by an array of the given type.
 * @param componentType component type of the backing array
 * @param initialCapacity length of the initial backing array
 */
@SuppressWarnings("unchecked")
public ArrayBuildList(Class<?> componentType, int initialCapacity) {
	this((E[]) Array.newInstance(componentType, initialCapacity), 0);
}

/**
 * Creates a new {@code ArrayBuildList} backed by an array of the given type.
 * @param componentType component type of the backing array
 */
@SuppressWarnings("unchecked")
public ArrayBuildList(Class<?> componentType) {
	this((E[]) Array.newInstance(componentType, DEFAULT_CAPACITY), 0);
}

/**
 * Creates a new {@code ArrayBuildList} backed by an array of the given type, filled with the contents of the given collection.
 * @param c a collection
 * @param componentType component type of the backing array
 */
@SuppressWarnings("unchecked")
public ArrayBuildList(Collection<? extends E> c, Class<?> componentType) {
	this(c, (E[]) Array.newInstance(componentType, c.size()));
}

private ArrayBuildList(Collection<? extends E> c, E[] array) {
	this(c.toArray(array), array.length);
}

/**
 * Gets a direct reference to the backing array.
 * The backing array may change at any time.
 * @return the backing array
 */
public E[] getArray() {
	return array;
}

/**
 * Trims the backing array to have the same length as the size of this list.
 * @return this list
 */
public ArrayBuildList<E> trim() {
	if(size != array.length) array = Arrays.copyOf(array, size);
	return this;
}

/**
 * Ensures that the backing array has a length equal to or greater than {@code length}.
 * @param length minimum length of backing array
 */
protected void ensureCapacity(int length) {
	if(length > array.length) {
		int newLength = array.length * 2;
		array = Arrays.copyOf(array, length > newLength ? length : newLength);
	}
}

/**
 * Ensures that the backing array has a length greater than {@code length}.
 * @param length one less than minimum length of backing array
 */
protected void ensureCapacityAdd(int length) {
	if(length >= array.length) {
		int newLength = array.length * 2;
		array = Arrays.copyOf(array, length >= newLength ? length + 1 : newLength);
	}
}

/**
 * Ensures that an index is not too large.
 * @param index an index
 * @throws IndexOutOfBoundsException if {@code index} is equal to or greater than this list's size.
 */
protected void rangeCheck(int index) {
	if(index >= size) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
}

/**
 * Ensures that an index is not negative or too large.
 * @param index an index
 * @throws IndexOutOfBoundsException if {@code index} is negative or greater than this list's size.
 */
protected void rangeCheckForAdd(int index) {
	if(index > size || index < 0) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
}

@Override
public boolean add(E e) {
	ensureCapacityAdd(size);
	array[size++] = e;
	return true;
}

@Override
public int size() {
	return size;
}

@Override
public E[] toArray() {
	return Arrays.copyOf(array, size);
}

@SuppressWarnings("unchecked")
@Override
public <T> T[] toArray(T[] a) {
	if(a.length < size) return (T[]) Arrays.copyOf(array, size, a.getClass());
	System.arraycopy(array, 0, a, 0, size);
	if(a.length > size) a[size] = null;
	return a;
}

@Override
public E get(int index) {
	rangeCheck(index);
	return array[index];
}

@Override
public void add(int index, E element) {
	rangeCheckForAdd(index);
	ensureCapacityAdd(size);
	System.arraycopy(array, index, array, index + 1, size - index);
	array[index] = element;
	size++;
}

@Override
public E remove(int index) {
	rangeCheck(index);
	E value = array[index];
	int move = size - index - 1;
	if(move > 0) System.arraycopy(array, index + 1, array, index, move);
	array[--size] = null;
	return value;
}

@Override
public E set(int index, E element) {
	rangeCheck(index);
	E value = array[index];
	array[index] = element;
	return value;
}

@Override
public boolean addAll(Collection<? extends E> c) {
	int cSize = c.size();
	ensureCapacity(size + cSize);
	for(E e:c) array[size++] = e;
	return cSize != 0;
}

@Override
protected void removeRange(int fromIndex, int toIndex) {
	int move = size - toIndex;
	System.arraycopy(array, toIndex, array, fromIndex, move);
	int newLength = size - (toIndex - fromIndex);
	for(int i = newLength; i < size; i++) array[i] = null;
	size = newLength;
}

@Override
public String toString() {
	if(size == 0) return "[]";
	StringBuilder str = new StringBuilder().append('[').append(Util.toString(array[0]));
	for(int i = 1; i < size; i++) str.append(", ").append(Util.toString(array[i]));
	return str.append(']').toString();
}

@Override
public ArrayBuildList<E> clone() {
	try {
		@SuppressWarnings("unchecked")
		ArrayBuildList<E> clone = (ArrayBuildList<E>) super.clone();
		clone.array = Arrays.copyOf(array, size);
		return clone;
	} catch (CloneNotSupportedException e) {
		throw new InternalError(e);
	}
}

@Override
public void forEach(Consumer<? super E> action) {
	for(int i = 0; i < size; i++) action.accept(array[i]);
}

}