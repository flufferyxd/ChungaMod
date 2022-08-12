package magnileve.chungamod.modules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a static method should be called by Chungamod's {@code ModuleLoader} during initialization.
 * @author Magnileve
 * @see PreInit1
 * @see PreInit2
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Init {

/**
 * Indicates that a static method should be called by Chungamod's {@code ModuleLoader} before processing its declaring type.
 * @author Magnileve
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public static @interface PreInit1 {}

/**
 * Indicates that a static method should be called by Chungamod's {@code ModuleLoader} after processing its declaring type.
 * @author Magnileve
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public static @interface PreInit2 {}

}