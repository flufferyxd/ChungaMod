package magnileve.chungamod.util.json;

/**
 * Ensures that a value satisfies a set of limits.
 * @param <T> value type
 * @author Magnileve
 * @see JSONManager#testLimits(Object, Class, String)
 * @see JSONUtil#parseLimits(String)
 */
@FunctionalInterface
public interface ValueLimiter<T> {

/**
 * Ensures that a given value satisfies a set of limits.
 * @param value a value
 * @param limits limits of the given value
 * @throws IllegalArgumentException if the given value does not satisfy the given limits
 * @throws ValueLimiterSyntaxException if {@code limits} is of an improper syntax for this {@code ValueLimiter}
 */
public void verify(T value, String limits) throws IllegalArgumentException;

/**
 * Creates a new syntax error.
 * @param limits given limits
 * @param expectedSyntax expected syntax of {@code limits}
 * @return a new syntax error to be thrown.
 */
public static ValueLimiterSyntaxException syntaxError(String limits, String expectedSyntax) {
	return new ValueLimiterSyntaxException("Invalid syntax: \"" + limits + "\", expected syntax: " + expectedSyntax);
}

/**
 * Creates a new syntax error.
 * @param limits given limits
 * @return a new syntax error to be thrown.
 */
public static ValueLimiterSyntaxException syntaxError(String limits) {
	return new ValueLimiterSyntaxException("Invalid syntax: \"" + limits + "\"");
}

}