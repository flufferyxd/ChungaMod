package magnileve.chungamod.gui.values;

import java.util.function.Function;

import magnileve.chungamod.gui.MenuChain;
import magnileve.chungamod.gui.MenuButtonBuilder;

/**
 * A link to be used internally in a {@link ValueButtonFactory}.
 * For a button to be built, some resources are stored only in a {@code ValueButtonFactory},
 * so an instance is passed through each link's {@code build} method.
 * @author Magnileve
 */
@FunctionalInterface
public interface ValueButtonFactoryLink {

/**
 * Builds a new value button.
 * @param <T> value type
 * @param factory instance of {@code ValueButtonFactory}
 * @param id button ID
 * @param x x position
 * @param y y position
 * @param widthIn width
 * @param heightIn height
 * @param name key for this button's value
 * @param value initial value
 * @param valueProcessor processes input values
 * @param menuButtonBuilder if not null, is applied to this button and adds buttons for a menu being built
 * @param menuChain menu chain link of this button's menu
 * @param description if not null, is displayed when this button is hovered over
 * @param type value type
 * @param allowNull if {@code null} values are allowed
 * @param limits value limiter string
 * @return a new value button
 */
public <T> ValueButton<T> build(ValueButtonFactory factory, int id, int x, int y, int widthIn, int heightIn, String name,
		T value, ValueProcessor<T> valueProcessor, Function<ValueButton<T>, MenuButtonBuilder> menuButtonBuilder,
		MenuChain menuChain, String description, Class<T> type, boolean allowNull, String limits);

}