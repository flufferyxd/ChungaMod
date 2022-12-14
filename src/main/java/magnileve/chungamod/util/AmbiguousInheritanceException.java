package magnileve.chungamod.util;

/**
 * A runtime exception thrown by {@link InheritanceMap} to indicate that a key has inherited multiple values, and no value takes priority.
 * @author Magnileve
 */
public class AmbiguousInheritanceException extends RuntimeException {

private static final long serialVersionUID = -668381061305684953L;

/** Constructs a new ambiguous inheritance exception with {@code null} as its
 * detail message.  The cause is not initialized, and may subsequently be
 * initialized by a call to {@link #initCause}.
 */
public AmbiguousInheritanceException() {
    super();
}

/** Constructs a new ambiguous inheritance exception with the specified detail message.
 * The cause is not initialized, and may subsequently be initialized by a
 * call to {@link #initCause}.
 *
 * @param   message   the detail message. The detail message is saved for
 *          later retrieval by the {@link #getMessage()} method.
 */
public AmbiguousInheritanceException(String message) {
    super(message);
}

/**
 * Constructs a new ambiguous inheritance exception with the specified detail message and
 * cause.  <p>Note that the detail message associated with
 * {@code cause} is <i>not</i> automatically incorporated in
 * this ambiguous inheritance exception's detail message.
 *
 * @param  message the detail message (which is saved for later retrieval
 *         by the {@link #getMessage()} method).
 * @param  cause the cause (which is saved for later retrieval by the
 *         {@link #getCause()} method).  (A {@code null} value is
 *         permitted, and indicates that the cause is nonexistent or
 *         unknown.)
 */
public AmbiguousInheritanceException(String message, Throwable cause) {
    super(message, cause);
}

/** Constructs a new ambiguous inheritance exception with the specified cause and a
 * detail message of {@code (cause==null ? null : cause.toString())}
 * (which typically contains the class and detail message of
 * {@code cause}).  This constructor is useful for ambiguous inheritance exceptions
 * that are little more than wrappers for other throwables.
 *
 * @param  cause the cause (which is saved for later retrieval by the
 *         {@link #getCause()} method).  (A {@code null} value is
 *         permitted, and indicates that the cause is nonexistent or
 *         unknown.)
 */
public AmbiguousInheritanceException(Throwable cause) {
    super(cause);
}

}