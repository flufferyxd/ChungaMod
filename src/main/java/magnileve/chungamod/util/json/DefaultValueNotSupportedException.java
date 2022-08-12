package magnileve.chungamod.util.json;

/**
 * Thrown to indicate that a default value satisfying a {@link ValueLimiter} is unable to be obtained.
 * @author Magnileve
 */
public class DefaultValueNotSupportedException extends UnsupportedOperationException {

private static final long serialVersionUID = 4674807256321254351L;

private final Class<?> forType;
private final String limiterID;

/**
 * Constructs a new {@code DefaultValueNotSupportedException} with the specified value type and value limiter ID.
 * @param forType the value type
 * @param limiterID identifier of value limiter
 */
public DefaultValueNotSupportedException(Class<?> forType, String limiterID) {
	super("Default value not supported for " + forType + " with limiter ID " + limiterID);
	this.forType = forType;
	this.limiterID = limiterID;
}

/**
 * Constructs a new {@code DefaultValueNotSupportedException} with the specified cause, value type, and value limiter ID.
 * @param cause an optional {@code Throwable} cause of this exception
 * @param forType the value type
 * @param limiterID identifier of value limiter
 */
public DefaultValueNotSupportedException(Throwable cause, Class<?> forType, String limiterID) {
	super("Default value not supported for " + forType + (limiterID == null ? "" : " with limiter ID " + limiterID), cause);
	this.forType = forType;
	this.limiterID = limiterID;
}

/**
 * Returns the type of value limited by the value limiter without support for a default value.
 * @return the type of value limited by the value limiter without support for a default value
 */
public Class<?> getValueType() {
	return forType;
}

/**
 * Returns the identifier of the value limiter not supporting a default value.
 * @return the identifier of the value limiter not supporting a default value
 */
public String getLimiterID() {
	return limiterID;
}

}