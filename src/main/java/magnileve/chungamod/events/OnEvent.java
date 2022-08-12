package magnileve.chungamod.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an event listener method to be registered by an {@link EventManager}.
 * This method must be {@code public}
 * and have a single parameter that is assignable from all types returned by {@link #value()}.
 * @author Magnileve
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnEvent {

/**
 * Gets the event types that the annotated method can listen for.
 * Each type returned must be assignable to the method's parameter type.
 * If the returned array is empty, it is assumed that the method can listen for any event assignable to the method's parameter type.
 * @return the event types that the annotated method can listen for.
 */
Class<?>[] value() default {};

}