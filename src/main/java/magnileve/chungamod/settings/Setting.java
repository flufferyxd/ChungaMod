package magnileve.chungamod.settings;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a setting of a module.
 * @author Magnileve
 */
@Repeatable(Setting.SettingsArray.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Setting {

/**
 * Name of the setting that determines if a module is enabled or disabled.
 * All modules implicitly have this as a setting, but declarations can override it as long as it is of type {@link Boolean}.
 */
public static final String ON = "On";
/**
 * A value limiter allowing any positive number, including zero.
 */
public static final String POSITIVE_INT = "range:0," + Integer.MAX_VALUE;
/**
 * A value limiter allowing any positive number, excluding zero.
 */
public static final String POSITIVE_INT_NONZERO = "range:1," + Integer.MAX_VALUE;

/**
 * @return the name of this setting
 */
String name();
/**
 * @return the value type of this setting
 */
Class<?> type();
/**
 * @return the value limiter string of this setting, or {@code ""} for no value limiter
 */
String limits() default "";
/**
 * @return the default value of this setting in JSON.
 * If a {@code String} type is expected, and this value does not start with a quote or equal "null",
 * a JSON String is attempted to be parsed.
 */
String value() default "null";
/**
 * @return a description of this setting
 */
String description() default "";

/**
 * Contains repeatable {@link Setting} annotations.
 * @author Magnileve
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public static @interface SettingsArray {
	/**
	 * @return declared {@link Setting} annotations
	 */
	Setting[] value();
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public static @interface SubSettingsArray {
	Sub[] value();
}

/**
 * Declares a subsettings branch in the first string of a setting path.
 * @author Magnileve
 */
@Repeatable(SubSettingsArray.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public static @interface Sub {
	/**
	 * @return the index of this branch in the list of settings
	 */
	int index();
	/**
	 * @return the name of this subsetting branch
	 */
	String name();
	/**
	 * @return settings in this branch
	 */
	Setting[] value();
	/**
	 * @return subsettings branches in this branch
	 */
	SubSub[] subSettings() default {};
}

/**
 * Declares a subsetting branch in the second string of a setting path.
 * @author Magnileve
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public static @interface SubSub {
	/**
	 * @return the index of this branch in the list of subsettings
	 */
	int index();
	/**
	 * @return the name of this subsetting branch
	 */
	String name();
	/**
	 * @return subsettings in this branch
	 */
	Setting[] value();
	/**
	 * @return subsetting branches in this branch
	 */
	SubSubSub[] subSettings() default {};
}

/**
 * Declares a subsetting branch in the third string of a setting path.
 * @author Magnileve
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public static @interface SubSubSub {
	/**
	 * @return the index of this branch in the list of subsettings
	 */
	int index();
	/**
	 * @return the name of this subsetting branch
	 */
	String name();
	/**
	 * @return subsettings in this branch
	 */
	Setting[] value();
	/**
	 * @return subsetting branches in this branch
	 */
	SubSubSubSub[] subSettings() default {};
}

/**
 * Declares a subsetting branch in the fourth string of a setting path.
 * @author Magnileve
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public static @interface SubSubSubSub {
	/**
	 * @return the index of this branch in the list of subsettings
	 */
	int index();
	/**
	 * @return the name of this subsetting branch
	 */
	String name();
	/**
	 * @return subsettings in this branch
	 */
	Setting[] value();
}

}