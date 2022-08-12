package magnileve.chungamod.gui.values;

import java.util.function.Function;
import java.util.function.IntSupplier;

import magnileve.chungamod.gui.ButtonRendererFactory;
import magnileve.chungamod.gui.ClickGUI;
import magnileve.chungamod.gui.ClickGUIButton;
import magnileve.chungamod.gui.MenuButtonBuilder;
import magnileve.chungamod.gui.MenuChain;
import magnileve.chungamod.util.json.JSONManager;

/**
 * Builds value buttons.
 * @author Magnileve
 * @see ValueButton
 * @see ValueButtonFactoryLink
 */
public interface ValueButtonFactory extends ValueButtonFactoryLink {

/**
 * Builds a new value button.
 * @param <T> value type
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
public default <T> ValueButton<T> build(int id, int x, int y, int widthIn, int heightIn, String name,
		T value, ValueProcessor<T> valueProcessor, Function<ValueButton<T>, MenuButtonBuilder> menuButtonBuilder,
		MenuChain menuChain, String description, Class<T> type, boolean allowNull, String limits) {
	return build(this, id, x, y, widthIn, heightIn, name, value, valueProcessor, menuButtonBuilder,
			menuChain, description, type, allowNull, limits);
}

/**
 * Gets this factory's {@code ClickGUI}.
 * @return this factory's {@code ClickGUI}
 */
public ClickGUI getClickGUI();

/**
 * Gets this factory's button ID supplier.
 * @return this factory's button ID supplier
 */
public IntSupplier getButtonIDSupplier();

/**
 * Gets this factory's JSON manager.
 * @return this factory's JSON manager
 */
public JSONManager getJSONManager();

/**
 * Gets this factory's button renderer factory.
 * @return this factory's button renderer factory
 */
public ButtonRendererFactory<ClickGUIButton> getRendererFactory();

}