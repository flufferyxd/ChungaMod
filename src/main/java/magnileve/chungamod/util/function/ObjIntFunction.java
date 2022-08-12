package magnileve.chungamod.util.function;

/**
 * Represents an operation that accepts an object-valued and an
 * {@code int}-valued argument, and produces a result.  This is the
 * {@code (reference, int)} specialization of {@link java.util.function.BiFunction BiFunction}.
 * @param <T1> the type of the object argument to the function
 * @param <T2> the type of the result of the function
 */
@FunctionalInterface
public interface ObjIntFunction<T1, T2> {

/**
 * Applies this function to the given arguments.
 *
 * @param t the first function argument
 * @param value the second function argument
 * @return the function result
 */
public T2 apply(T1 t, int value);

}