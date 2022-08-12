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
import magnileve.chungamod.util.json.JSONUtil;
import magnileve.chungamod.util.math.Vec2i;

/**
 * A button with a value represented by an integer from a range.
 * @author Magnileve
 * @param <T> value type
 */
public abstract class IntRangeButton<T> extends RangeButton<T> {

/**
 * Minimum value as an {@code int}.
 */
protected final int min;
/**
 * Maximum value as an {@code int}.
 */
protected final int max;

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
 * @param min minimum value
 * @param max maximum value
 */
public IntRangeButton(int id, int x, int y, int widthIn, int heightIn, String name,
		ButtonRendererFactory<ClickGUIButton> rendererFactory, UpdatableDisplay messageDisplayer, T value,
		ValueProcessor<T> valueProcessor, Function<ValueButton<T>, MenuButtonBuilder> menuButtonBuilder,
		MenuChain menuChain, IntSupplier buttonIDSupplier, String description,
		Permit<BiIntConsumer> mousePermit, int min, int max) {
	super(id, x, y, widthIn, heightIn, name, rendererFactory, messageDisplayer,
			value, valueProcessor, menuButtonBuilder, menuChain, buttonIDSupplier, description, mousePermit);
	this.min = min;
	this.max = max;
}

/**
 * Converts an {@code int} to a value.
 * @param value an {@code int}
 * @return the value represented by the given {@code int}
 */
protected abstract T intToValue(int value);

/**
 * Converts a value to an {@code int}.
 * @param value a value
 * @return the {@code int} representing the given value
 */
protected abstract int valueToInt(T value);

/**
 * Parses a range from a value limiter string.
 * @param limits value limiter string
 * @return a {@code Vec2i} containing the minimum and maximum values, or {@code null} if the given string could not be parsed
 */
public static Vec2i parseRange(String limits) {
	String[] limitArray = JSONUtil.parseLimits(limits);
	if(limitArray[0].equals("range")) {
		String[] values = limitArray[1].split(",");
		if(values.length == 2) try {
			return new Vec2i(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
		} catch(NumberFormatException e) {}
	}
	return null;
}

@Override
protected T highlightWidthToValue(int displayWidth) {
	if(displayWidth <= 0) return intToValue(min);
	if(displayWidth >= width) return intToValue(max);
	return intToValue(Math.round((float) displayWidth * (max - min) / width) + min);
}

@Override
protected int valueToHighlightWidth(T value) {
	return Math.round((float) (valueToInt(value) - min) * width / (max - min));
}

@Override
protected T incrementValue() {
	int newIndex = valueToInt(getValue()) + 1;
	if(newIndex > max) newIndex = min;
	return intToValue(newIndex);
}

@Override
protected T decrementValue() {
	int newIndex = valueToInt(getValue()) - 1;
	if(newIndex < min) newIndex = max;
	return intToValue(newIndex);
}

/**
 * A button with an {@code int} value.
 * @author Magnileve
 */
public static class IntegerButton extends IntRangeButton<Integer> {
	/**
	 * Creates a new {@code int} value button.
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
	 * @param min minimum value
	 * @param max maximum value
	 */
	public IntegerButton(int id, int x, int y, int widthIn, int heightIn, String name,
			ButtonRendererFactory<ClickGUIButton> rendererFactory, UpdatableDisplay messageDisplayer, int value,
			ValueProcessor<Integer> valueProcessor, Function<ValueButton<Integer>, MenuButtonBuilder> menuButtonBuilder,
			MenuChain menuChain, IntSupplier buttonIDSupplier, String description,
			Permit<BiIntConsumer> mousePermit, int min, int max) {
		super(id, x, y, widthIn, heightIn, name, rendererFactory, messageDisplayer,
				value, valueProcessor, menuButtonBuilder, menuChain, buttonIDSupplier, description, mousePermit, min, max);
	}

	@Override
	protected Integer intToValue(int value) {
		return value;
	}

	@Override
	protected int valueToInt(Integer value) {
		return value;
	}
}

/**
 * A button with a {@code byte} value.
 * @author Magnileve
 */
public static class ByteButton extends IntRangeButton<Byte> {
	/**
	 * Creates a new {@code byte} value button.
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
	 * @param min minimum value
	 * @param max maximum value
	 */
	public ByteButton(int id, int x, int y, int widthIn, int heightIn, String name,
			ButtonRendererFactory<ClickGUIButton> rendererFactory, UpdatableDisplay messageDisplayer, byte value,
			ValueProcessor<Byte> valueProcessor, Function<ValueButton<Byte>, MenuButtonBuilder> menuButtonBuilder,
			MenuChain menuChain, IntSupplier buttonIDSupplier, String description,
			Permit<BiIntConsumer> mousePermit, int min, int max) {
		super(id, x, y, widthIn, heightIn, name, rendererFactory, messageDisplayer, value, valueProcessor, menuButtonBuilder,
				menuChain, buttonIDSupplier, description, mousePermit, Util.roundToByte(min), Util.roundToByte(max));
	}

	@Override
	protected Byte intToValue(int value) {
		return (byte) value;
	}

	@Override
	protected int valueToInt(Byte value) {
		return value;
	}
}

/**
 * A button with a {@code short} value.
 * @author Magnileve
 */
public static class ShortButton extends IntRangeButton<Short> {
	/**
	 * Creates a new {@code short} value button.
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
	 * @param min minimum value
	 * @param max maximum value
	 */
	public ShortButton(int id, int x, int y, int widthIn, int heightIn, String name,
			ButtonRendererFactory<ClickGUIButton> rendererFactory, UpdatableDisplay messageDisplayer, short value,
			ValueProcessor<Short> valueProcessor, Function<ValueButton<Short>, MenuButtonBuilder> menuButtonBuilder,
			MenuChain menuChain, IntSupplier buttonIDSupplier, String description,
			Permit<BiIntConsumer> mousePermit, int min, int max) {
		super(id, x, y, widthIn, heightIn, name, rendererFactory, messageDisplayer, value, valueProcessor, menuButtonBuilder,
				menuChain, buttonIDSupplier, description, mousePermit, Util.roundToShort(min), Util.roundToShort(max));
	}

	@Override
	protected Short intToValue(int value) {
		return (short) value;
	}

	@Override
	protected int valueToInt(Short value) {
		return value;
	}
}

}