package magnileve.chungamod.gui.values;

import java.util.function.Function;
import java.util.function.IntSupplier;

import org.json.JSONException;
import org.json.JSONTokener;

import magnileve.chungamod.gui.MenuChain;
import magnileve.chungamod.gui.ButtonRendererFactory;
import magnileve.chungamod.gui.ClickGUIButton;
import magnileve.chungamod.gui.MenuButtonBuilder;
import magnileve.chungamod.gui.UpdatableDisplay;
import magnileve.chungamod.util.Permit;
import magnileve.chungamod.util.Util;
import magnileve.chungamod.util.json.JSONManager;

/**
 * A button with a value represented by JSON.
 * @param <T> value type
 * @author Magnileve
 */
public class JSONButton<T> extends StringValueButton<T> {

/**
 * Default name of a {@code JSONButton}.
 */
public static final String DEFAULT_BUTTON_NAME = "JSON";

private final JSONManager json;
private final Class<T> type;
private final boolean allowNull;

/**
 * Creates a new JSON value button.
 * @param id button ID
 * @param x x position
 * @param y y position
 * @param widthIn width
 * @param heightIn height
 * @param rendererFactory factory to build renderer for this button
 * @param messageDisplayer used to display messages
 * @param value initial value
 * @param valueProcessor processes input values
 * @param menuButtonBuilder if not null, is applied to this button and adds buttons for a menu being built
 * @param menuChain menu chain link of this button's menu
 * @param buttonIDSupplier generates button IDs
 * @param description if not null, is displayed when this button is hovered over
 * @param keyboardPermit permit for handling keyboard activity
 * @param type value type
 * @param json this button's {@code JSONManager}
 * @param allowNull if {@code null} values are allowed
 */
public JSONButton(int id, int x, int y, int widthIn, int heightIn, ButtonRendererFactory<ClickGUIButton> rendererFactory,
		UpdatableDisplay messageDisplayer, T value, ValueProcessor<T> valueProcessor,
		Function<ValueButton<T>, MenuButtonBuilder> menuButtonBuilder, MenuChain menuChain, IntSupplier buttonIDSupplier,
		String description, Permit<Runnable> keyboardPermit, Class<T> type, JSONManager json, boolean allowNull) {
	this(id, x, y, widthIn, heightIn, DEFAULT_BUTTON_NAME, rendererFactory, messageDisplayer,
			value, valueProcessor, menuButtonBuilder, menuChain, buttonIDSupplier, description, keyboardPermit, type, json, allowNull);
}

/**
 * Creates a new JSON value button.
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
 * @param type value type
 * @param json this button's {@code JSONManager}
 * @param allowNull if {@code null} values are allowed
 */
public JSONButton(int id, int x, int y, int widthIn, int heightIn, String name, ButtonRendererFactory<ClickGUIButton> rendererFactory,
		UpdatableDisplay messageDisplayer, T value, ValueProcessor<T> valueProcessor,
		Function<ValueButton<T>, MenuButtonBuilder> menuButtonBuilder, MenuChain menuChain, IntSupplier buttonIDSupplier,
		String description, Permit<Runnable> keyboardPermit, Class<T> type, JSONManager json, boolean allowNull) {
	super(id, x, y, widthIn, heightIn, name, rendererFactory, messageDisplayer,
			value, valueProcessor, menuButtonBuilder, menuChain, buttonIDSupplier, description, keyboardPermit);
	this.json = json;
	this.type = type;
	this.allowNull = allowNull;
}

/**
 * Gets this button's value type.
 * @return this button's value type
 */
public Class<T> getType() {
	return type;
}

@Override
public T stringToValue(String valueString) throws IllegalArgumentException {
	try {
		return json.deserialize(new JSONTokener(valueString), type, null, allowNull);
	} catch(JSONException e) {
		throw new IllegalArgumentException(e);
	}
}

@Override
public String valueToString() {
	return json.serializeToString(type, getValue());
}

@Override
public boolean equals(T value1, T value2) {
	ValueConsumer<T> parent = getParent();
	return parent == null ? Util.equals(value1, value2) : parent.equals(value1, value2);
}

@Override
public boolean equals(Object obj) {
	if(super.equals(obj)) return obj instanceof JSONButton ? getType().equals(((JSONButton<?>) obj).getType()) : true;
	return false;
}

}