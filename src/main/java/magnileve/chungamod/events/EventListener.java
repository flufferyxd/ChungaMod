package magnileve.chungamod.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a type as an event listener.
 * This annotation is intended to be used for automatic registration of event listeners to an {@link EventManager}
 * and is not used by the {@code EventManager} itself.
 * {@link magnileve.chungamod.modules.ModuleLoader ModuleLoader} uses this annotation.
 * @author Magnileve
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EventListener {

/**
 * Declares the scope of the listener.
 * @return the scope of the listener
 * @see magnileve.chungamod.modules.ModuleLoader#SCOPE_SESSION ModuleLoader.SCOPE_SESSION
 * @see magnileve.chungamod.modules.ModuleLoader#SCOPE_SINGLETON ModuleLoader.SCOPE_SINGLETON
 */
String[] value() default "";

}