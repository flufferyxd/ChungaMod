package magnileve.chungamod.settings;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import magnileve.chungamod.modules.Module;

/**
 * Identifies a setting that a parameter should be given the value of.
 * @author Magnileve
 * @see magnileve.chungamod.Chung#getCallableFactory(Class, magnileve.chungamod.modules.ModuleManager)
 * Chung.getCallableFactory(Class, ModuleManager)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface GetSetting {

/**
 * Gets the path of the target setting.  If the path is empty or points to subsettings,
 * the name of the parameter is used an additional {@code String} in the path
 * @return path of the target setting
 */
String[] value() default {};
/**
 * Gets the type of the module the target setting belongs to.  If {@code Module.class} is returned, the default module is used.
 * @return the type of the module the target setting belongs to
 */
Class<? extends Module> moduleType() default Module.class;
/**
 * Indicates if {@code null} values should be given to this parameter,
 * or if {@link UnsetSettingException} should be thrown in such a case.
 * If the parameter type is wrapped in {@link java.util.Optional Optional}, this method has no affect.
 * @return {@code true} if this parameter is allowed to be set to {@code null}
 */
boolean allowNull() default false;

}