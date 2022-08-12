package magnileve.chungamod.modules;

/**
 * A runtime exception thrown to indicate that a module cannot be enabled.
 * @author Magnileve
 */
public class ModuleConstructionException extends RuntimeException {

private static final long serialVersionUID = 1955659215498535939L;

/** Constructs a new module construction exception with {@code null} as its
 * detail message.  The cause is not initialized, and may subsequently be
 * initialized by a call to {@link #initCause}.
 */
public ModuleConstructionException() {
	super();
}

/** Constructs a new module construction exception with the specified detail message.
 * The cause is not initialized, and may subsequently be initialized by a
 * call to {@link #initCause}.
 *
 * @param   message   the detail message. The detail message is saved for
 *          later retrieval by the {@link #getMessage()} method.
 */
public ModuleConstructionException(String message) {
	super(message);
}

/**
 * Constructs a new module construction exception with the specified detail message and
 * cause.  <p>Note that the detail message associated with
 * {@code cause} is <i>not</i> automatically incorporated in
 * this module construction exception's detail message.
 *
 * @param  message the detail message (which is saved for later retrieval
 *         by the {@link #getMessage()} method).
 * @param  cause the cause (which is saved for later retrieval by the
 *         {@link #getCause()} method).  (A {@code null} value is
 *         permitted, and indicates that the cause is nonexistent or
 *         unknown.)
 */
public ModuleConstructionException(String message, Throwable cause) {
	super(message, cause);
}

/** Constructs a new module construction exception with the specified cause and a
 * detail message of {@code (cause==null ? null : cause.toString())}
 * (which typically contains the class and detail message of
 * {@code cause}).  This constructor is useful for module construction exceptions
 * that are little more than wrappers for other throwables.
 *
 * @param  cause the cause (which is saved for later retrieval by the
 *         {@link #getCause()} method).  (A {@code null} value is
 *         permitted, and indicates that the cause is nonexistent or
 *         unknown.)
 */
public ModuleConstructionException(Throwable cause) {
	super(cause);
}

}