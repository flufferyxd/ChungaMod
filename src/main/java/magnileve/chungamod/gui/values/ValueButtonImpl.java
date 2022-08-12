package magnileve.chungamod.gui.values;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.IntSupplier;

import magnileve.chungamod.gui.MenuChain;
import magnileve.chungamod.gui.MenuChainImpl;
import magnileve.chungamod.gui.ButtonRendererFactory;
import magnileve.chungamod.gui.ClickGUIButton;
import magnileve.chungamod.gui.HoverInfoButton;
import magnileve.chungamod.gui.Menu;
import magnileve.chungamod.gui.MenuImpl;
import magnileve.chungamod.gui.MenuProperties;
import magnileve.chungamod.gui.MenuButtonBuilder;
import magnileve.chungamod.gui.UpdatableDisplay;
import magnileve.chungamod.util.ArrayBuildList;
import magnileve.chungamod.util.Util;

/**
 * Basic implementation of {@link ValueButton}.
 * @author Magnileve
 * @param <T> value type
 */
public abstract class ValueButtonImpl<T> extends HoverInfoButton implements ValueButton<T>, MenuButtonBuilder {

/**
 * Generates button IDs.
 */
protected final IntSupplier buttonIDSupplier;
/**
 * Contains a description followed by a newline if a description exists, and always ends in "Value: ".
 */
protected final String descString;

private final MenuChain menuChain;
private final ValueConsumer<T> parentInteractableValue;
private final ValueProcessor<T> valueProcessor;
private final MenuButtonBuilder menuButtonBuilder;

private T value;
private boolean isDisplayStringTrimmed;

/**
 * Creates a new value button.
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
 */
public ValueButtonImpl(int id, int x, int y, int widthIn, int heightIn, String name,
		ButtonRendererFactory<ClickGUIButton> rendererFactory, UpdatableDisplay messageDisplayer,
		T value, ValueProcessor<T> valueProcessor, Function<ValueButton<T>, MenuButtonBuilder> menuButtonBuilder,
		MenuChain menuChain, IntSupplier buttonIDSupplier, String description) {
	super(id, x, y, widthIn, heightIn, name, rendererFactory, messageDisplayer);
	this.buttonIDSupplier = buttonIDSupplier;
	this.descString = description == null || description.isEmpty() ? "Value: " : description + "\nValue: ";
	this.menuChain = menuChain;
	this.parentInteractableValue = valueProcessor instanceof ValueConsumer ? (ValueConsumer<T>) valueProcessor : null;
	this.valueProcessor = this.parentInteractableValue == null ? valueProcessor : null;
	this.menuButtonBuilder = menuButtonBuilder == null ? null : menuButtonBuilder.apply(this);
	this.value = value;
	isDisplayStringTrimmed = name == null ? false : !name.equals(displayString);
}

@Override
public ValueButtonImpl<T> init() {
	display(getValue(), null);
	hideDisplayedMessage();
	return this;
}

@Override
public void display(T newValue, ValueConsumer<T> caller) {
	setValue(newValue);
	MenuChain subMenu = getNextMenuChain();
	if(subMenu != null) for(ClickGUIButton button:subMenu.getMenu().buttons()) if(button instanceof ValueConsumer<?>) {
		@SuppressWarnings("unchecked")
		ValueConsumer<T> listener = (ValueConsumer<T>) button;
		if(equals(listener.getParent()) && listener != caller) listener.displayIfChanged(newValue, null);
	}
	updateDisplay();
	updateRenderer();
}

/**
 * Called when this button's value has been updated.
 * A call to this method is followed by a call to {@link #updateRenderer()}.
 */
protected void updateDisplay() {
	displayMessage("Set value: " + valueToString());
}

@Override
public boolean displayIfChanged(T newValue, ValueConsumer<T> caller) {
	boolean changed = !equals(getValue(), newValue);
	if(changed) display(newValue, caller);
	return changed;
}

@Override
public T processNewValue(T newValue) throws IllegalArgumentException {
	if(parentInteractableValue != null) {
		newValue = parentInteractableValue.processNewValue(newValue);
		parentInteractableValue.displayIfChanged(newValue, this);
		return newValue;
	}
	return valueProcessor.processNewValue(newValue);
}

@Override
public T processDefaultValue() throws IllegalArgumentException {
	return parentInteractableValue == null ? valueProcessor.processDefaultValue() : parentInteractableValue.processDefaultValue();
}

@Override
public T getValue() {
	return value;
}

/**
 * Sets this button's current value.  This method is called by {@link #display(Object, ValueConsumer)}.
 * @param newValue a value
 * @see #processNewValue(Object)
 * @see #displayIfChanged(Object, ValueConsumer)
 */
protected void setValue(T newValue) {
	value = newValue;
}

@Override
public String valueToString() {
	return getValue().toString();
}

@Override
public void setDisplayString(String displayString) {
	this.displayString = rendererFactory.trim(this, displayString);
	isDisplayStringTrimmed = !this.displayString.equals(displayString);
	setIsNameTrimmed(!this.displayString.startsWith(getName()));
	updateRenderer();
}

@Override
protected String getHoverMessage() {
	return Util.concat(super.getHoverMessage(), descString + valueToString());
}

@Override
public ValueConsumer<T> getParent() {
	return parentInteractableValue;
}

@Override
public boolean equals(T value1, T value2) {
	return value1.equals(value2);
}

@Override
public boolean mouseClicked(int mouseX, int mouseY, int mouseButton, boolean alreadyProcessed) {
	boolean hovered = super.mouseClicked(mouseX, mouseY, mouseButton, alreadyProcessed);
	if(hovered) onClick(mouseButton, mouseX, mouseY);
	else if(alreadyProcessed) hideDisplayedMessage();
	return hovered;
}

/**
 * Called when this button has been clicked.
 * If this button has been right clicked, a menu is created by calling {@link #rightClickMenu(int, int)}.
 * @param mouseButton the mouse button clicked
 * @param mouseX x position of mouse
 * @param mouseY y position of mouse
 */
protected void onClick(int mouseButton, int mouseX, int mouseY) {
	if(mouseButton == 1) setNextMenuChain(rightClickMenu(mouseX, mouseY));
}

/**
 * Creates a menu when this button is right clicked.
 * @param mouseX x position of mouse
 * @param mouseY y position of mouse
 * @return a {@code MenuChain} link containing the new menu
 */
protected MenuChain rightClickMenu(int mouseX, int mouseY) {
	MenuChain newLink = new MenuChainImpl();
	Menu menu = new MenuImpl(buttonIDSupplier.getAsInt(), mouseX, mouseY, width, height, rendererFactory,
			newLink, new ArrayList<>(0), getMenuProperties()).init();
	newLink.setMenu(menu);
	ArrayBuildList<ClickGUIButton> builder = new ArrayBuildList<>(new ClickGUIButton[2]);
	addMenuEntries(builder, menu, newLink);
	if(builder.isEmpty()) return null;
	menu.buttons().addAll(builder);
	return newLink;
}

@Override
public void addMenuEntries(ArrayBuildList<ClickGUIButton> builder, Menu menu, MenuChain menuChain) {
	if(menuButtonBuilder != null) menuButtonBuilder.addMenuEntries(builder, menu, menuChain);
}

/**
 * Gets the next link in the menu chain relative to this button's link.
 * @return the next link, or {@code null} one does not exist
 */
protected MenuChain getNextMenuChain() {
	return menuChain.getNext();
}

/**
 * Sets the next link in the menu chain relative to this button's link.
 * @param menuChain the next link, or {@code null} for no link
 */
protected void setNextMenuChain(MenuChain menuChain) {
	this.menuChain.setNext(menuChain);
}

/**
 * Gets the menu properties of this button's menu chain.
 * @return menu properties
 */
protected MenuProperties getMenuProperties() {
	return menuChain.getMenuProperties();
}

/**
 * Indicates if the display string of this button has been trimmed.
 * @return {@code true} if this button's display string was trimmed upon being set; {@code false} otherwise 
 */
protected boolean isDisplayStringTrimmed() {
	return isDisplayStringTrimmed;
}

}