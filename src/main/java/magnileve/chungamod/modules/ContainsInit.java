package magnileve.chungamod.modules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for types containing methods annotated with
 * {@link Init}, {@link Init.PreInit1 PreInit1}, or {@link Init.PreInit2 PreInit2}.
 * @author Magnileve
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ContainsInit {}