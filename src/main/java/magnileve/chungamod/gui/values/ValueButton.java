package magnileve.chungamod.gui.values;

import magnileve.chungamod.gui.ClickGUIButton;
import magnileve.chungamod.gui.DisplayMessageSender;

/**
 * A button containing a value that can be set.
 * @author Magnileve
 * @param <T> value type
 */
public interface ValueButton<T> extends ClickGUIButton, ValueConsumer<T>, DisplayMessageSender {

/**
 * Initializes this button.
 * @return this button
 */
public ValueButton<T> init();

/**
 * Returns a {@code String} representation of this button's current value.
 * @return a {@code String} representation of this button's current value
 */
public String valueToString();

/**
 * Gets this button's current value.
 * @return this button's current value
 */
public T getValue();

}