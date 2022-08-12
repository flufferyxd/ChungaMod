package magnileve.chungamod.gui.values;

/**
 * Processes and displays values.
 * @author Magnileve
 * @param <T> value type
 */
public interface ValueConsumer<T> extends ValueProcessor<T> {

/**
 * Displays a new value.
 * This method should typically not be called outside of the implementing instance;
 * {@link #displayIfChanged(Object, ValueConsumer)} should be preferred instead.
 * @param newValue the value
 * @param caller caller of this action, or {@code null} if one does not exist
 * @see #displayIfChanged(Object, ValueConsumer)
 */
public void display(T newValue, ValueConsumer<T> caller);

/**
 * Displays a new value if it is different than the currently displayed value.
 * Implementations of this method should invoke {@link #equals(Object, Object)} and {@link #display(Object, ValueConsumer)}.
 * @param newValue the value
 * @param caller caller of this action, or {@code null} if one does not exist
 * @return {@code true} if the new value has been displayed; {@code false} if the new and old values are equal
 */
public boolean displayIfChanged(T newValue, ValueConsumer<T> caller);

/**
 * Gets the parent {@code ValueConsumer} if one exists.
 * A parent {@code ValueConsumer} can be used for value processing or other tasks.
 * @return the parent {@code ValueConsumer}, or {@code null} if one does not exist
 */
public ValueConsumer<T> getParent();

/**
 * Returns {@code true} if two values are equal to each other; {@code false} otherwise.
 * @param value1 a value
 * @param value2 another value
 * @return {@code true} if two values are equal to each other; {@code false} otherwise
 */
public boolean equals(T value1, T value2);

/**
 * Displays a new value with no caller.
 * This method is equivalent to the following:
 * <blockquote>
 * {@code display(newValue, null)}
 * </blockquote>
 * @param newValue the value
 * @see #display(Object, ValueConsumer)
 */
public default void display(T newValue) {
	display(newValue, null);
}

/**
 * Displays a new value with no caller if it is different than the currently displayed value.
 * This method is equivalent to the following:
 * <blockquote>
 * {@code displayIfChanged(newValue, null)}
 * </blockquote>
 * @param newValue the value
 * @return {@code true} if the new value has been displayed; {@code false} if the new and old values are equal
 * @see #displayIfChanged(Object, ValueConsumer)
 */
public default boolean displayIfChanged(T newValue) {
	return displayIfChanged(newValue, null);
}

}