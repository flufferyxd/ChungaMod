package magnileve.chungamod.gui.values;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Operates on and verifies values.
 * @author Magnileve
 * @param <T> value type
 */
public interface ValueProcessor<T> {

/**
 * Accepts an input value.
 * @param newValue a value
 * @return the value after operated on
 * @throws IllegalArgumentException if the given value is invalid
 */
public T processNewValue(T newValue) throws IllegalArgumentException;

/**
 * Accepts the default value.
 * @return the value after operated on
 * @throws IllegalArgumentException if the default value is invalid or cannot be obtained
 */
public T processDefaultValue() throws IllegalArgumentException;

/**
 * A {@code ValueProcessor} backed by functional interfaces.
 * @author Magnileve
 * @param <T> value type
 */
public class Of<T> implements ValueProcessor<T> {
	private final Function<T, T> processValue;
	private final Supplier<T> resetValue;

	public Of(Function<T, T> processValue, Supplier<T> processDefaultValue) {
		this.processValue = processValue;
		this.resetValue = processDefaultValue;
		
	}
	
	@Override
	public T processNewValue(T newValue) throws IllegalArgumentException {
		return processValue.apply(newValue);
	}
	
	@Override
	public T processDefaultValue() throws IllegalArgumentException {
		return resetValue.get();
	}
}

}