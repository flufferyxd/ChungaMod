package magnileve.chungamod.util.function;

import java.util.Objects;

/**
 * Represents a function that accepts an two int-valued arguments and returns no
 * result.  This is the {@code int}-consuming primitive specialization for
 * {@link java.util.function.BiConsumer BiConsumer}.
 */
@FunctionalInterface
public interface BiIntConsumer {

/**
 * Performs this operation on the given arguments.
 * @param value1 the first input argument
 * @param value2 the second input argument
 */
public void accept(int value1, int value2);

/**
 * Returns a composed {@code BiIntConsumer} that performs, in sequence, this
 * operation followed by the {@code after} operation. If performing either
 * operation throws an exception, it is relayed to the caller of the
 * composed operation.  If performing this operation throws an exception,
 * the {@code after} operation will not be performed.
 *
 * @param after the operation to perform after this operation
 * @return a composed {@code BiIntConsumer} that performs in sequence this
 * operation followed by the {@code after} operation
 * @throws NullPointerException if {@code after} is null
 */
default BiIntConsumer andThen(BiIntConsumer after) {
    Objects.requireNonNull(after);
    return (v1, v2) -> {
    	accept(v1, v2);
    	after.accept(v1, v2);
    };
}

}