package magnileve.chungamod.gui.values;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntSupplier;

import magnileve.chungamod.gui.MenuChain;
import magnileve.chungamod.gui.MenuChainImpl;
import magnileve.chungamod.gui.ClickGUIButtonImpl;
import magnileve.chungamod.gui.Menu;
import magnileve.chungamod.gui.MenuImpl;
import magnileve.chungamod.gui.MenuProperties;
import magnileve.chungamod.gui.MenuButtonBuilder;
import magnileve.chungamod.gui.ButtonRendererFactory;
import magnileve.chungamod.gui.ClickGUI;
import magnileve.chungamod.gui.ClickGUIButton;
import magnileve.chungamod.gui.UpdatableDisplay;
import magnileve.chungamod.util.ArrayBuildList;
import magnileve.chungamod.util.Util;
import magnileve.chungamod.util.json.DefaultValueNotSupportedException;
import magnileve.chungamod.util.json.JSONManager;

/**
 * A button with an array value.
 * @author Magnileve
 * @param <C> component type of value type
 */
public class ArrayButton<C> extends ValueButtonImpl<C[]> {

/**
 * Component type of this button's value type.
 */
protected final Class<C> componentType;
/**
 * This button's GUI.
 */
protected final ClickGUI clickGUI;
/**
 * This button's {@code JSONManager}.
 */
protected final JSONManager json;
/**
 * This button's value's limiter string.
 */
protected final String valueLimits;
/**
 * Factory for component type buttons.
 */
protected final ValueButtonFactory factory;

private int previousLength;
private boolean buttonInput;

/**
 * Creates a new array value button.
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
 * @param componentType component type of value type
 * @param clickGUI this button's GUI
 * @param json this button's {@code JSONManager}
 * @param valueLimits value limiter string
 * @param factory factory for component type buttons
 */
public ArrayButton(int id, int x, int y, int widthIn, int heightIn, String name, ButtonRendererFactory<ClickGUIButton> rendererFactory,
		UpdatableDisplay messageDisplayer, C[] value, ValueProcessor<C[]> valueProcessor,
		Function<ValueButton<C[]>, MenuButtonBuilder> menuButtonBuilder, MenuChain menuChain, IntSupplier buttonIDSupplier, String description,
		Class<C> componentType, ClickGUI clickGUI, JSONManager json, String valueLimits, ValueButtonFactory factory) {
	super(id, x, y, widthIn, heightIn, name, rendererFactory, messageDisplayer,
			value, valueProcessor, menuButtonBuilder, menuChain, buttonIDSupplier, description);
	this.componentType = componentType;
	this.clickGUI = clickGUI;
	this.json = json;
	this.valueLimits = valueLimits;
	this.factory = factory;
	previousLength = value.length;
}

@Override
public ArrayButton<C> init() {
	super.init();
	setDisplayString(getName() + "[]");
	return this;
}

/**
 * Sets a component value at an index in this button's array.
 * @param index index in the array
 * @param newValue a value
 * @return the value at {@code index} after processing the input value
 * @throws ArrayIndexOutOfBoundsException if {@code index} is less than zero or equal to or greater than {@code value.length}
 * @throws IllegalArgumentException if the array is invalid after modification
 */
protected C set(int index, C newValue) {
	C[] newArray = getValue().clone();
	C oldValue = (C) newArray[index];
	newArray[index] = (C) newValue;
	return displayIfChanged(processNewValue(newArray), true) ? newValue : oldValue;
}

/**
 * Adds a component value to this button's array.
 * @param newValue a value
 * @return {@code true} if the array has changed as a result of this call
 * @throws IllegalArgumentException if the array is invalid after modification
 */
protected boolean add(C newValue) {
	return add(getValue().length, newValue);
}

/**
 * Adds a component value at an index to this button's array.
 * @param index index in the array
 * @param newValue a value
 * @return {@code true} if the array has changed as a result of this call
 * @throws IndexOutOfBoundsException if {@code index} is less than zero or greater than {@code value.length}
 * @throws IllegalArgumentException if the array is invalid after modification
 */
protected boolean add(int index, C newValue) {
	C[] value = getValue();
	int move = value.length - index;
	if(index < 0 || move < 0) throw new IndexOutOfBoundsException(Integer.toString(index));
	@SuppressWarnings("unchecked")
	C[] newArray = (C[]) Array.newInstance(componentType, value.length + 1);
	System.arraycopy(value, 0, newArray, 0, index);
	if(move != 0) System.arraycopy(value, index, newArray, index + 1, move);
	newArray[index] = newValue;
	return displayIfChanged(processNewValue(newArray), true);
}

/**
 * Removes the last component value from this button's array.
 * @return {@code true} if the array has changed as a result of this call
 * @throws IllegalArgumentException if the array is invalid after modification
 */
protected boolean remove() {
	return remove(getValue().length - 1);
}

/**
 * Removes a component value at an index from this button's array if the index is valid.
 * @param index index in the array
 * @return {@code true} if the array has changed as a result of this call
 * @throws IllegalArgumentException if the array is invalid after modification
 */
protected boolean remove(int index) {
	C[] value = getValue();
	int move = value.length - index - 1;
	if(index < 0 || move < 0) return false;
	@SuppressWarnings("unchecked")
	C[] newArray = (C[]) Array.newInstance(componentType, value.length - 1);
	System.arraycopy(value, 0, newArray, 0, index);
	if(move != 0) System.arraycopy(value, index + 1, newArray, index, move);
	return displayIfChanged(processNewValue(newArray), true);
}

@Override
public void addMenuEntries(ArrayBuildList<ClickGUIButton> builder, Menu menu, MenuChain menuChain) {
	C[] value = getValue();
	builder.add(menu.newHeader("Length: " + value.length));
	int i = 0;
	for(; i < value.length; i++) builder.add(getComponentTypeButton(value[i], i, menuChain));
	builder.add(getAddButton());
	builder.add(getRemoveButton());
	super.addMenuEntries(builder, menu, menuChain);
}

@Override
protected void updateDisplay() {
	updateMenuButtons();
}

/**
 * Calls {@link ValueConsumer#displayIfChanged(C[]) displayIfChanged(C[])},
 * but if {@code fromButtonInput} is true, marks a flag to prevent component buttons from being updated during the call.
 * @param newValue the value
 * @param fromButtonInput if this method is called from a component button
 * @return {@code true} if the new value has been displayed; {@code false} if the new and old values are equal
 */
protected boolean displayIfChanged(C[] newValue, boolean fromButtonInput) {
	if(fromButtonInput) {
		buttonInput = true;
		boolean changed = displayIfChanged(newValue);
		buttonInput = false;
		return changed;
	} else return displayIfChanged(newValue);
}

@Override
public String valueToString() {
	return Util.toString(getValue());
}

@Override
public boolean equals(C[] value1, C[] value2) {
	return Arrays.deepEquals(value1, value2);
}

/**
 * Builds a component button.
 * @param value component value
 * @param index index in array
 * @param menuChain component button's menu chain
 * @return a new component button
 */
protected ValueButton<C> getComponentTypeButton(C value, int index, MenuChain menuChain) {
	return (componentType.isArray() ? componentArrayButton(value, index, menuChain, factory) :
			factory.build(buttonIDSupplier.getAsInt(), 0, 0, getWidth(), getHeight(), Integer.toString(index),
					value, valueProcessor(index), menuBuilder(componentType, index),
					menuChain, null, componentType, false, valueLimits)).init();
}

/**
 * Creates a {@link ValueProcessor} for the component at a given index in the array.
 * @param index index of the component
 * @return a new {@code ValueProcessor} for the component, not supporting default values
 */
private ValueProcessor<C> valueProcessor(int index) {
	return new ValueProcessor<C>() {
		@Override
		public C processNewValue(C newValue) {
			return set(index, newValue);
		}
		
		@Override
		public C processDefaultValue() {
			return null;
		}
	};
}

/**
 * Creates a {@link MenuButtonBuilder} factory for a component at a given index.
 * @param type component type
 * @param index index of the component
 * @return a {@link Function} that accepts the component button and produces a {@code MenuButtonBuilder}
 */
private Function<ValueButton<C>, MenuButtonBuilder> menuBuilder(Class<C> type, int index) {
	return button -> button instanceof JSONButton ? null :
			(builder, menu, menuChain) -> builder.add(new JSONButton<C>(buttonIDSupplier.getAsInt(),
					0, 0, getWidth(), getHeight(), rendererFactory, messageDisplayer,
					button.getValue(), button, null, menuChain, buttonIDSupplier, null, clickGUI.getKeyboardPermit(), type, json, false).init());
}

/**
 * Creates a new component array button.
 * @param value component button's array
 * @param index component button's index
 * @param menuChain component button's menu chain
 * @param factory factory for component type buttons of component button
 * @return a new component array button
 */
@SuppressWarnings("unchecked")
protected ValueButton<C> componentArrayButton(C value, int index, MenuChain menuChain, ValueButtonFactory factory) {
	return (ValueButton<C>) newComponentArrayButton(value, index, menuChain, factory);
}

/**
 * Creates a new component array button.
 * @param <CC> component type of component type
 * @param value component button's array
 * @param index component button's index
 * @param menuChain component button's menu chain
 * @param factory factory for component type buttons of component button
 * @return a new component array button
 */
@SuppressWarnings("unchecked")
protected <CC> ComponentArrayButton<CC> newComponentArrayButton(C value, int index, MenuChain menuChain, ValueButtonFactory factory) {
	return new ComponentArrayButton<>((ArrayButton<CC[]>) this, (CC[]) value, factory,
			(Class<CC>) componentType.getComponentType(), clickGUI, index, menuChain);
}

/**
 * A value button of a component array of this array.
 * @param <C> component type of this component button's type
 * @author Magnileve
 */
protected static class ComponentArrayButton<C> extends ArrayButton<C> {
	private final ArrayButton<C[]> arrayButton;
	
	/**
	 * Creates a new component array button.
	 * @param b the array button this button is a component of
	 * @param value this button's array
	 * @param factory factory for component type buttons
	 * @param componentType component type of this button's type
	 * @param clickGUI this button's GUI
	 * @param index this button's index in its array button
	 * @param menuChain this button's menu chain
	 */
	@SuppressWarnings("unchecked")
	public ComponentArrayButton(ArrayButton<C[]> b, C[] value, ValueButtonFactory factory,
			Class<C> componentType, ClickGUI clickGUI, int index, MenuChain menuChain) {
		super(b.buttonIDSupplier.getAsInt(), 0, 0, b.getWidth(), b.getHeight(), String.valueOf(index), b.rendererFactory,
				b.messageDisplayer, (C[]) value, new ValueProcessor.Of<>(newValue -> b.set(index, newValue),
				() -> b.set(index, (C[]) Array.newInstance(componentType, 0))),
				null, menuChain, b.buttonIDSupplier, null, componentType, clickGUI, b.json, b.valueLimits, factory);
		arrayButton = b;
	}
	
	@Override
	public void addMenuEntries(ArrayBuildList<ClickGUIButton> builder, Menu menu, MenuChain menuChain) {
		super.addMenuEntries(builder, menu, menuChain);
		builder.add(new SetToDefaultButton<>(buttonIDSupplier.getAsInt(), rendererFactory, messageDisplayer, arrayButton));
		builder.add(new JSONButton<>(buttonIDSupplier.getAsInt(), 0, 0, getWidth(), getHeight(), rendererFactory,
				messageDisplayer, null, this, null, menuChain, buttonIDSupplier, null, clickGUI.getKeyboardPermit(), arrayButton.componentType, json, false).init());
	}
	
	@Override
	protected void updateDisplay() {
		super.updateDisplay();
		setDisplayString(Arrays.deepToString(getValue()));
	}
	
	@Override
	public void setDisplayString(String displayString) {
		super.setDisplayString(getName() + ": " + displayString);
	}
}

/**
 * Creates a button that adds a new element to the array when clicked.
 * @return a button that adds a new element to the array when clicked
 */
protected ClickGUIButton getAddButton() {
	return new ClickGUIButtonImpl(buttonIDSupplier.getAsInt(), 0, 0, getWidth(), getHeight(), "+", rendererFactory) {
		@Override
		protected void onClick(int mouseButton) {
			if(mouseButton == 0) {
				C addValue;
				try {
					addValue = json.getDefault(componentType, valueLimits);
				} catch(DefaultValueNotSupportedException e) {
					addMenuNoDefault(getNextMenuChain(), getX() + getWidth(), getY());
					return;
				}
				try {
					add(addValue);
				} catch(IllegalArgumentException e) {
					displayMessage(e.getMessage());
				}
			}
		}
	};
}

/**
 * Creates a menu with a single button that accepts JSON input of a value
 * and validates the input of its limits before adding it to the array.
 * @param menuChain the menu chain link before the link to be created
 * @param x x position of menu
 * @param y y position of menu
 */
protected void addMenuNoDefault(MenuChain menuChain, int x, int y) {
	MenuChain subMenuChain = new MenuChainImpl();
	MenuProperties menuProperties = getMenuProperties();
	MenuImpl subMenu = new MenuImpl(buttonIDSupplier.getAsInt(), x, y, getWidth(), getHeight(), rendererFactory,
			subMenuChain, new ArrayList<>(1), menuProperties).init();
	subMenuChain.setMenu(subMenu);
	ValueButtonImpl<C> button = new JSONButton<C>(buttonIDSupplier.getAsInt(), 0, 0, getWidth(), getHeight(), rendererFactory,
			messageDisplayer, null, new ValueProcessor.Of<>(newValue -> {
				if(add(newValue)) menuChain.closeNext();
				return newValue;
			}, () -> null), null, subMenuChain, buttonIDSupplier, null, clickGUI.getKeyboardPermit(), componentType, json, false) {
		@Override protected void updateDisplay() {}
		@Override public String getHoverMessage() {return "";}
	}.init();
	button.displayString = "Insert JSON..."; //intentionally bypass setDisplayString(String)
	subMenu.buttons().add(button);
	menuChain.setNext(subMenuChain);
}

/**
 * Creates a button that removes the last element from the array when clicked.
 * @return a button that removes the last element from the array when clicked
 */
protected ClickGUIButton getRemoveButton() {
	return new ClickGUIButtonImpl(buttonIDSupplier.getAsInt(), 0, 0, getWidth(), getHeight(), "-", rendererFactory) {
		@Override
		protected void onClick(int mouseButton) {
			if(mouseButton == 0) try {
				remove();
			} catch(IllegalArgumentException e) {
				displayMessage(e.getMessage());
			}
		}
	};
}

/**
 * Updates the component buttons if they are currently being displayed.
 */
@SuppressWarnings("unchecked")
protected void updateMenuButtons() {
	MenuChain menuChain = getNextMenuChain();
	if(menuChain == null) return;
	
	List<ClickGUIButton> buttons = menuChain.getMenu().buttons();
	Iterator<ClickGUIButton> iter = buttons.iterator();
	C[] value = getValue();
	ClickGUIButton button;
	int i = 0, h = 0;
	while(!(button = iter.next()).getName().startsWith("Length: ")) h++;
	button.setDisplayString("Length: " + value.length);
	if(previousLength != 0 && value.length != 0) {
		button = iter.next();
		h++;
		if(buttonInput) {
			i = Math.min(previousLength, value.length);
			h += i - 1;
			for(int j = 1; j < i; j++) iter.next();
		} else {
			((ValueButton<C>) button).displayIfChanged(value[i++]);
			for(; i < previousLength && i < value.length; h++) ((ValueButton<C>) iter.next()).displayIfChanged(value[i++]);
		}
	}
	
	if(i < previousLength) {
		messageDisplayer.hide();
		for(; i < previousLength; i++) {
			iter.next();
			iter.remove();
		}
	} else if(i < value.length) {
		messageDisplayer.hide();
		ValueButton<?>[] newButtons = new ValueButton<?>[value.length - i];
		for(int j = 0; i < value.length; i++) {
			newButtons[j++] = getComponentTypeButton(value[i], i, menuChain);
		}
		buttons.addAll(h + 1, Arrays.asList(newButtons));
	}
	previousLength = value.length;
}

@Override
protected void onClick(int mouseButton) {}

}