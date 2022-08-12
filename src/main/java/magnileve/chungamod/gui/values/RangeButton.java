package magnileve.chungamod.gui.values;

import java.util.function.Function;
import java.util.function.IntSupplier;

import org.lwjgl.input.Mouse;

import magnileve.chungamod.gui.MenuChain;
import magnileve.chungamod.gui.ButtonRendererFactory;
import magnileve.chungamod.gui.ClickGUIButton;
import magnileve.chungamod.gui.MenuButtonBuilder;
import magnileve.chungamod.gui.UpdatableDisplay;
import magnileve.chungamod.util.Permit;
import magnileve.chungamod.util.function.BiIntConsumer;

/**
 * A button with a value from a range.
 * @author Magnileve
 * @param <T> value type
 */
public abstract class RangeButton<T> extends ValueButtonImpl<T> {

private final Permit<BiIntConsumer> mousePermit;
private int highlightWidth;
private BiIntConsumer mouseHandler;

/**
 * Creates a new ranged value button.
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
 */
public RangeButton(int id, int x, int y, int widthIn, int heightIn, String name,
		ButtonRendererFactory<ClickGUIButton> rendererFactory, UpdatableDisplay messageDisplayer,
		T value, ValueProcessor<T> valueProcessor, Function<ValueButton<T>, MenuButtonBuilder> menuButtonBuilder,
		MenuChain menuChain, IntSupplier buttonIDSupplier, String description, Permit<BiIntConsumer> mousePermit) {
	super(id, x, y, widthIn, heightIn, name, rendererFactory, messageDisplayer,
			value, valueProcessor, menuButtonBuilder, menuChain, buttonIDSupplier, description);
	this.mousePermit = mousePermit;
}

/**
 * Converts a width on this button to a value.
 * @param displayWidth width of the highlighted part of this button
 * @return the value for this width
 */
protected abstract T highlightWidthToValue(int displayWidth);

/**
 * Converts a value to a width on this button.
 * @param value a value
 * @return width of the highlighted part of this button for the value
 */
protected abstract int valueToHighlightWidth(T value);

/**
 * Gets the value after this button's current value in the range.
 * @return the value after this button's current value in the range
 */
protected abstract T incrementValue();

/**
 * Gets the value before this button's current value in the range.
 * @return the value before this button's current value in the range
 */
protected abstract T decrementValue();

/**
 * Gets the width of the highlighted part of this button.
 * @return the width of the highlighted part of this button
 */
public int getHighlightWidth() {
	return highlightWidth;
}

/**
 * Sets the width of the highlighted part of this button.
 * @param highlightWidth a width between zero and this button's width
 */
protected void setHighlightWidth(int highlightWidth) {
	this.highlightWidth = highlightWidth;
}

/**
 * Gets this button's handler for mouse activity.
 * @return this button's handler for mouse activity
 */
protected BiIntConsumer mouseHandler() {
	return (mouseX, mouseY) -> {
		if(Mouse.getEventButton() == 0 && !Mouse.getEventButtonState()) {
			releaseMouse();
			T newValue;
			try {
				newValue = processNewValue(highlightWidthToValue(mouseX - x));
			} catch(IllegalArgumentException e) {
				displayMessage(e.getMessage());
				return;
			}
			displayIfChanged(newValue);
		} else displayIfChanged(highlightWidthToValue(mouseX - x));
	};
}

/**
 * Releases the mouse permit.
 */
protected void releaseMouse() {
	mousePermit.release(mouseHandler);
	mouseHandler = null;
}

/**
 * Called when this button has been scrolled.
 * @param up if the scroll direction is up
 */
protected void onScroll(boolean up) {
	T newValue;
	try {
		newValue = processNewValue(up ? incrementValue() : decrementValue());
	} catch(IllegalArgumentException e) {
		displayMessage(e.getMessage());
		return;
	}
	displayIfChanged(newValue);
}

@Override
protected void onClick(int mouseButton, int mouseX, int mouseY) {
	super.onClick(mouseButton, mouseX, mouseY);
	if(mouseButton == 0) mouseHandler = mousePermit.getIfAcquired(() -> mouseHandler());
}

@Override
public boolean mouseScrolled(int mouseX, int mouseY, boolean up, boolean alreadyProcessed) {
	boolean hovered = updateHovered(mouseX, mouseY, alreadyProcessed);
	if(hovered) onScroll(up);
	return hovered;
}

@Override
protected void updateDisplay() {
	setHighlightWidth(valueToHighlightWidth(getValue()));
	setDisplayString(valueToString());
	if(isDisplayStringTrimmed()) displayMessage(valueToString());
	else hideDisplayedMessage();
}

@Override
public void setDisplayString(String displayString) {
	super.setDisplayString(getName() + ": " + displayString);
}

@Override
protected void onClick(int mouseButton) {}

}