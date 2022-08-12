package magnileve.chungamod.modules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a method or constructor is a factory for a type.
 * @author Magnileve
 * @see magnileve.chungamod.Chung#getCallableFactory(Class, ModuleManager) Chung.getCallableFactory(Class, ModuleManager)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface Factory {}