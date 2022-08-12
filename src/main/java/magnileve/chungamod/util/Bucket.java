package magnileve.chungamod.util;

import java.util.Objects;

/**
 * A {@code Bucket} contains two elements, which may be {@code null}, and are not able to be changed.
 * If the elements contained by a {@code Bucket} are immutable, then the {@code Bucket} is immutable.
 * @author Magnileve
 * @param <E1> type of first element
 * @param <E2> type of second element
 */
public final class Bucket<E1, E2> {

/**
 * A {@code Bucket} containing {@code null} for both of its elements.
 * @see #empty()
 */
public static final Bucket<?, ?> EMPTY = new Bucket<>(null, null);

private final E1 e1;
private final E2 e2;

private Bucket(E1 e1, E2 e2) {
	this.e1 = e1;
	this.e2 = e2;
}

/**
 * Creates a {@code Bucket} of the given elements, which may be {@code null}.
 * @param <E1> type of first element
 * @param <E2> type of second element
 * @param e1 first element
 * @param e2 second element
 * @return a {@code Bucket} containing the given elements
 */
public static <E1, E2> Bucket<E1, E2> of(E1 e1, E2 e2) {
	return new Bucket<>(e1, e2);
}

/**
 * Returns a {@code Bucket} with both elements being {@code null}.
 * @param <E1> type of first element
 * @param <E2> type of second element
 * @return a {@code Bucket} containing {@code null} for both of its elements
 * @see #EMPTY
 */
@SuppressWarnings("unchecked")
public static <E1, E2> Bucket<E1, E2> empty() {
	return (Bucket<E1, E2>) EMPTY;
}

/**
 * Gets the first element of this bucket.
 * @return the first element of this bucket
 */
public E1 getE1() {
	return e1;
}

/**
 * Gets the second element of this bucket.
 * @return the second element of this bucket
 */
public E2 getE2() {
	return e2;
}

@Override
public String toString() {
	return '[' + Util.toString(e1) + ", " + Util.toString(e2) + ']';
}

@Override
public boolean equals(Object obj) {
	if(obj == null) return false;
	if(obj instanceof Bucket) {
		Bucket<?, ?> b = (Bucket<?, ?>) obj;
		return Objects.equals(e1, b.e1) && Objects.equals(e2, b.e2);
	}
	return false;
}

@Override
public int hashCode() {
	if(e1 == null) return e2 == null ? 0 : e2.hashCode();
	return e2 == null ? e1.hashCode() : e1.hashCode() * 31 + e2.hashCode();
}

}