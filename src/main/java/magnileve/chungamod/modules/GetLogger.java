package magnileve.chungamod.modules;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.logging.log4j.spi.StandardLevel;

/**
 * Specifies properties for a module logger to be provided by Chungamod.
 * @author Magnileve
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface GetLogger {

/**
 * @return the least specific level for logging to chat
 */
StandardLevel chatLevel() default StandardLevel.INFO;
/**
 * @return the least specific level for logging to a file
 */
StandardLevel fileLevel() default StandardLevel.OFF;
/**
 * @return the {@link ModuleLogger} format for logging
 */
String format() default "";
/**
 * @return the {@link ModuleLogger} format for logging to chat
 */
String chatFormat() default "";
/**
 * @return the {@link ModuleLogger} format for logging to a file
 */
String fileFormat() default "";
/**
 * @return the directory of log files
 */
String directory() default "";
/**
 * Alternative to {@link #chatLevel()}.
 * @return a custom least specific level for logging to chat
 */
int customChatLevel() default 0;
/**
 * Alternative to {@link #fileLevel()}.
 * @return a custom least specific level for logging to a file
 */
int customFileLevel() default 0;

/**
 * A {@code GetLogger} annotation containing all default values.
 */
public static final GetLogger DEFAULT_VALUES = new GetLogger() {
	@Override public Class<? extends Annotation> annotationType() {return GetLogger.class;}
	@Override public StandardLevel chatLevel() {return StandardLevel.INFO;}
	@Override public StandardLevel fileLevel() {return StandardLevel.OFF;}
	@Override public String format() {return "";}
	@Override public String chatFormat() {return "";}
	@Override public String fileFormat() {return "";}
	@Override public String directory() {return "";}
	@Override public int customChatLevel() {return 0;}
	@Override public int customFileLevel() {return 0;}
};

}