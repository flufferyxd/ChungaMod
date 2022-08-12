package magnileve.chungamod.gui.values;

import java.awt.Color;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;

import org.lwjgl.input.Mouse;

import magnileve.chungamod.gui.ButtonRendererFactory;
import magnileve.chungamod.gui.ClickGUIButton;
import magnileve.chungamod.gui.MenuButtonBuilder;
import magnileve.chungamod.gui.MenuChain;
import magnileve.chungamod.gui.UpdatableDisplay;
import magnileve.chungamod.util.Permit;
import magnileve.chungamod.util.function.BiIntConsumer;
import magnileve.chungamod.util.function.ObjIntFunction;

/**
 * A button containing a color component.
 * @author Magnileve
 */
public class ColorComponentButton extends IntRangeButton<Color> {

/**
 * The minimum value of a color component.
 */
public static final int MIN_INT = 0;
/**
 * The maximum value of a color component.
 */
public static final int MAX_INT = 255;

private final ObjIntFunction<Color, Color> intToValue;
private final ToIntFunction<Color> valueToInt;
private final boolean renderAlpha;

private Color minColor;
private Color maxColor;

/**
 * Creates a new color component button.
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
 * @param intToValue takes in a color and an int value,
 * and produces a similar color with this button's color component at that {@code int} value
 * @param valueToInt gets this button's color component from a color
 * @param renderAlpha {@code true} if this button's color's alpha should be rendered;
 * {@code false} if alpha should always be {@code 255} when rendered
 */
public ColorComponentButton(int id, int x, int y, int widthIn, int heightIn, String name,
		ButtonRendererFactory<ClickGUIButton> rendererFactory, UpdatableDisplay messageDisplayer, Color value,
		ValueProcessor<Color> valueProcessor, Function<ValueButton<Color>, MenuButtonBuilder> menuButtonBuilder,
		MenuChain menuChain, IntSupplier buttonIDSupplier, String description, Permit<BiIntConsumer> mousePermit,
		ObjIntFunction<Color, Color> intToValue, ToIntFunction<Color> valueToInt, boolean renderAlpha) {
	super(id, x, y, widthIn, heightIn, name, rendererFactory, messageDisplayer, value, valueProcessor, menuButtonBuilder,
			menuChain, buttonIDSupplier, description, mousePermit, MIN_INT, MAX_INT);
	this.intToValue = intToValue;
	this.valueToInt = valueToInt;
	this.renderAlpha = renderAlpha;
}

/**
 * Gets the color with the minimum value for this button's color component.
 * @return the color with the minimum value for this button's color component
 */
public Color getMinColor() {
	return minColor;
}

/**
 * Gets the color with the maximum value for this button's color component.
 * @return the color with the maximum value for this button's color component
 */
public Color getMaxColor() {
	return maxColor;
}

/**
 * Indicates if alpha should be rendered for this button's color.
 * @return {@code true} if this button's color's alpha should be rendered;
 * {@code false} if alpha should always be {@code 255} when rendered
 */
public boolean renderAlpha() {
	return renderAlpha;
}

@Override
protected Color intToValue(int value) {
	return intToValue.apply(getValue(), value);
}

@Override
protected int valueToInt(Color value) {
	return valueToInt.applyAsInt(value);
}

@Override
protected void updateDisplay() {
	minColor = intToValue(MIN_INT);
	maxColor = intToValue(MAX_INT);
	super.updateDisplay();
}

@Override
public String valueToString() {
	return Integer.toString(valueToInt(getValue()));
}

/**
 * A button containing a color component of a format other than RGB.
 * The difference between this class and {@code ColorComponentButton} is that when incrementing or decrementing values,
 * this class tries until it finds a color component value that produces a different color.
 * @author Magnileve
 */
public static class AltColorComponentButton extends ColorComponentButton {
	/**
	 * Creates a new color component button.
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
	 * @param intToValue takes in a color and an int value,
	 * and produces a similar color with this button's color component at that {@code int} value
	 * @param valueToInt gets this button's color component from a color
	 * @param renderAlpha {@code true} if this button's color's alpha should be rendered;
	 * {@code false} if alpha should always be {@code 255} when rendered
	 */
	public AltColorComponentButton(int id, int x, int y, int widthIn, int heightIn, String name,
			ButtonRendererFactory<ClickGUIButton> rendererFactory, UpdatableDisplay messageDisplayer, Color value,
			ValueProcessor<Color> valueProcessor, Function<ValueButton<Color>, MenuButtonBuilder> menuButtonBuilder,
			MenuChain menuChain, IntSupplier buttonIDSupplier, String description, Permit<BiIntConsumer> mousePermit,
			ObjIntFunction<Color, Color> intToValue, ToIntFunction<Color> valueToInt, boolean renderAlpha) {
		super(id, x, y, widthIn, heightIn, name, rendererFactory, messageDisplayer, value, valueProcessor, menuButtonBuilder,
				menuChain, buttonIDSupplier, description, mousePermit, intToValue, valueToInt, renderAlpha);
	}
	
	@Override
	protected BiIntConsumer mouseHandler() {
		Color oldValue = getValue();
		return (mouseX, mouseY) -> {
			if(Mouse.getEventButton() == 0 && !Mouse.getEventButtonState()) {
				setValue(oldValue);
				releaseMouse();
				Color newValue;
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
	
	@Override
	protected Color incrementValue() {
		Color value = getValue();
		int startIndex = valueToInt(value), newIndex = startIndex;
		Color newValue;
		do {
			if(++newIndex > max) newIndex = min;
			newValue = intToValue(newIndex);
		} while(value.equals(newValue) && newIndex != startIndex);
		return newValue;
	}
	
	@Override
	protected Color decrementValue() {
		Color value = getValue();
		int startIndex = valueToInt(value), newIndex = startIndex;
		Color newValue;
		do {
			if(--newIndex < min) newIndex = max;
			newValue = intToValue(newIndex);
		} while(value.equals(newValue) && newIndex != startIndex);
		return newValue;
	}
}

/**
 * A button containing a hue.
 * @author Magnileve
 */
public static class HueButton extends AltColorComponentButton {
	/**
	 * One third from {@value ColorComponentButton#MIN_INT} to {@value ColorComponentButton#MAX_INT}.
	 */
	public static final int ONE_THIRD_INT = (MAX_INT - MIN_INT) / 3 + MIN_INT;
	/**
	 * Two thirds from {@value ColorComponentButton#MIN_INT} to {@value ColorComponentButton#MAX_INT}.
	 */
	public static final int TWO_THIRDS_INT = (MAX_INT - MIN_INT) * 2 / 3 + MIN_INT;
	
	private Color oneThirdColor;
	private Color twoThirdsColor;
	
	/**
	 * Creates a new hue button.
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
	 * @param intToValue takes in a color and an int value,
	 * and produces a similar color with this button's color component at that {@code int} value
	 * @param valueToInt gets this button's color component from a color
	 * @param renderAlpha {@code true} if this button's color's alpha should be rendered;
	 * {@code false} if alpha should always be {@code 255} when rendered
	 */
	public HueButton(int id, int x, int y, int widthIn, int heightIn, String name,
			ButtonRendererFactory<ClickGUIButton> rendererFactory, UpdatableDisplay messageDisplayer, Color value,
			ValueProcessor<Color> valueProcessor, Function<ValueButton<Color>, MenuButtonBuilder> menuButtonBuilder,
			MenuChain menuChain, IntSupplier buttonIDSupplier, String description, Permit<BiIntConsumer> mousePermit,
			ObjIntFunction<Color, Color> intToValue, ToIntFunction<Color> valueToInt, boolean renderAlpha) {
		super(id, x, y, widthIn, heightIn, name, rendererFactory, messageDisplayer, value, valueProcessor, menuButtonBuilder,
				menuChain, buttonIDSupplier, description, mousePermit, intToValue, valueToInt, renderAlpha);
	}
	
	/**
	 * Gets this button's color with a hue of {@value #ONE_THIRD_INT}.
	 * @return this button's color with a hue of {@value #ONE_THIRD_INT}
	 */
	public Color getOneThirdColor() {
		return oneThirdColor;
	}
	
	/**
	 * Gets this button's color with a hue of {@value #TWO_THIRDS_INT}.
	 * @return this button's color with a hue of {@value #TWO_THIRDS_INT}
	 */
	public Color getTwoThirdsColor() {
		return twoThirdsColor;
	}
	
	@Override
	protected void updateDisplay() {
		oneThirdColor = intToValue(ONE_THIRD_INT);
		twoThirdsColor = intToValue(TWO_THIRDS_INT);
		super.updateDisplay();
	}
}

}