package magnileve.chungamod.modules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that classes of a certain group are part of a Chungamod plugin.
 * @author Magnileve
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ChungamodPlugin {

/**
 * @return the plugin ID
 */
String id();
/**
 * @return where this annotation should be applied.
 * If multiple results of this annotation are found for a module,
 * the annotation with the most specific level is used.
 * @see Level
 */
ChungamodPlugin.Level level();

/**
 * Contains different scopes for an annotation to be applied.
 * @author Magnileve
 */
public static enum Level {
	/**
	 * Only the declaring type.
	 */
	TYPE,
	/**
	 * All types within the package of the declaring type.
	 */
	PACKAGE,
	/**
	 * All types within the loading group of the declaring type.
	 * When loaded directly by Chungamod, the loading group is always the jar file.
	 */
	LOADING_GROUP;
}

}