package magnileve.chungamod.gui.values;

import java.util.function.Function;
import java.util.function.IntSupplier;

import magnileve.chungamod.gui.MenuChain;
import magnileve.chungamod.gui.ButtonRendererFactory;
import magnileve.chungamod.gui.ClickGUIButton;
import magnileve.chungamod.gui.MenuButtonBuilder;
import magnileve.chungamod.gui.UpdatableDisplay;
import magnileve.chungamod.util.Permit;

/**
 * A button with a {@code String} value.
 * @author Magnileve
 */
public class StringButton extends StringValueButton<String> {

/**
 * Creates a new {@code String} value button.
 * @param id button ID
 * @param x x position
 * @param y y position
 * @param widthIn width
 * @param heightIn height
 * @param name key for this button's value
 * @param rendererFactory factory to build renderer for this button
 * @param messageDisplayer used to display messages
 * @param value initial value
 * @param valueProcessor processes input values
 * @param menuButtonBuilder if not null, is applied to this button and adds buttons for a menu being built
 * @param menuChain menu chain link of this button's menu
 * @param buttonIDSupplier generates button IDs
 * @param description if not null, is displayed when this button is hovered over
 * @param keyboardPermit permit for handling keyboard activity
 */
public StringButton(int id, int x, int y, int widthIn, int heightIn, String name, ButtonRendererFactory<ClickGUIButton> rendererFactory,
		UpdatableDisplay messageDisplayer, String value, ValueProcessor<String> valueProcessor,
		Function<ValueButton<String>, MenuButtonBuilder> menuButtonBuilder, MenuChain menuChain, IntSupplier buttonIDSupplier,
		String description, Permit<Runnable> keyboardPermit) {
	super(id, x, y, widthIn, heightIn, name, rendererFactory, messageDisplayer,
			value, valueProcessor, menuButtonBuilder, menuChain, buttonIDSupplier, description, keyboardPermit);
}

@Override
public String stringToValue(String valueString) {
	return valueString;
}

@Override
public String valueToString() {
	return getValue();
}

}