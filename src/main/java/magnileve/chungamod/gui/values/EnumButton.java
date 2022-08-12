package magnileve.chungamod.gui.values;

import java.util.function.Function;
import java.util.function.IntSupplier;

import magnileve.chungamod.gui.ButtonRendererFactory;
import magnileve.chungamod.gui.ClickGUIButton;
import magnileve.chungamod.gui.MenuButtonBuilder;
import magnileve.chungamod.gui.MenuChain;
import magnileve.chungamod.gui.UpdatableDisplay;
import magnileve.chungamod.util.Permit;
import magnileve.chungamod.util.Util;
import magnileve.chungamod.util.function.BiIntConsumer;

/**
 * A button with a value from a set of constants.
 * @author Magnileve
 * @param <T> value type
 */
public class EnumButton<T> extends IntRangeButton<T> {

private final T[] constants;

/**
 * Creates a new enum value button.
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
 * @param mousePermit permit for handling mouse activity
 * @param constants array of value constants
 * @throws IllegalArgumentException if {@code value} is not in {@code constants}
 */
public EnumButton(int id, int x, int y, int widthIn, int heightIn, String name,
		ButtonRendererFactory<ClickGUIButton> rendererFactory, UpdatableDisplay messageDisplayer, T value,
		ValueProcessor<T> valueProcessor, Function<ValueButton<T>, MenuButtonBuilder> menuButtonBuilder,
		MenuChain menuChain, IntSupplier buttonIDSupplier, String description,
		Permit<BiIntConsumer> mousePermit, T[] constants) {
	super(id, x, y, widthIn, heightIn, name, rendererFactory, messageDisplayer, value, valueProcessor, menuButtonBuilder,
			menuChain, buttonIDSupplier, description, mousePermit, 0, constants.length - 1);
	if(Util.indexOf(constants, value) < 0) throw new IllegalArgumentException("Provided constant " +
			Util.toString(value) + " is not in array of constants " + Util.toString(constants));
	this.constants = constants;
}

@Override
protected T intToValue(int value) {
	return constants[value];
}

@Override
protected int valueToInt(T value) {
	return Util.indexOf(constants, value);
}

}