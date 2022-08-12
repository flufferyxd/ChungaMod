package magnileve.chungamod.modules;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a type as a Chungamod module.
 * @author Magnileve
 * @see magnileve.chungamod.Chung Chung
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface ModuleInfo {
	/**
	 * @return the name of this module.  If left blank, {@link Class#getSimpleName()} for this module's type should be used.
	 */
	String name() default "";
	/**
	 * @return The name of this module's category.
	 */
	String category();
	/**
	 * @return A description of this module.
	 */
	String description();
	/**
	 * @return {@code true} if a singleton instance should be created for this module;
	 * {@code false} if new instances should be created for each {@link magnileve.chungamod.events.ConnectionEvent ConnectionEvent}.
	 */
	boolean alwaysInstantiate() default false;
}