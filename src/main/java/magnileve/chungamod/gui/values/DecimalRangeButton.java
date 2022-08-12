package magnileve.chungamod.gui.values;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;
import java.util.function.IntSupplier;

import magnileve.chungamod.gui.ButtonRendererFactory;
import magnileve.chungamod.gui.ClickGUIButton;
import magnileve.chungamod.gui.MenuButtonBuilder;
import magnileve.chungamod.gui.MenuChain;
import magnileve.chungamod.gui.UpdatableDisplay;
import magnileve.chungamod.util.Bucket;
import magnileve.chungamod.util.Permit;
import magnileve.chungamod.util.function.BiIntConsumer;
import magnileve.chungamod.util.json.JSONUtil;

/**
 * A button with a {@code BigDecimal} value.
 * @author Magnileve
 */
public class DecimalRangeButton extends RangeButton<BigDecimal> {

private final BigDecimal min;
private final BigDecimal max;
private final int scale;

/**
 * Creates a new decimal value button.
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
public DecimalRangeButton(int id, int x, int y, int widthIn, int heightIn, String name,
		ButtonRendererFactory<ClickGUIButton> rendererFactory, UpdatableDisplay messageDisplayer, BigDecimal value,
		ValueProcessor<BigDecimal> valueProcessor, Function<ValueButton<BigDecimal>, MenuButtonBuilder> menuButtonBuilder,
		MenuChain menuChain, IntSupplier buttonIDSupplier, String description,
		Permit<BiIntConsumer> mousePermit, BigDecimal min, BigDecimal max) {
	this(id, x, y, widthIn, heightIn, name, rendererFactory, messageDisplayer, value, valueProcessor, menuButtonBuilder,
			menuChain, buttonIDSupplier, description, mousePermit, min, max, BigDecimal.TEN);
}

/**
 * Creates a new decimal value button.
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
 * @param maxValueWidth maximum width between values on this button
 */
public DecimalRangeButton(int id, int x, int y, int widthIn, int heightIn, String name,
		ButtonRendererFactory<ClickGUIButton> rendererFactory, UpdatableDisplay messageDisplayer, BigDecimal value,
		ValueProcessor<BigDecimal> valueProcessor, Function<ValueButton<BigDecimal>, MenuButtonBuilder> menuButtonBuilder,
		MenuChain menuChain, IntSupplier buttonIDSupplier, String description,
		Permit<BiIntConsumer> mousePermit, BigDecimal min, BigDecimal max, BigDecimal maxValueWidth) {
	super(id, x, y, widthIn, heightIn, name, rendererFactory, messageDisplayer,
			value, valueProcessor, menuButtonBuilder, menuChain, buttonIDSupplier, description, mousePermit);
	this.min = min;
	this.max = max;
	scale = getButtonScale(min, max, new BigDecimal(widthIn), maxValueWidth);
	setHighlightWidth(valueToHighlightWidth(value));
}

/**
 * Parses a range from a value limiter string.
 * @param limits value limiter string
 * @return a {@code Bucket} containing the minimum and maximum values, or {@code null} if the given string could not be parsed
 */
public static Bucket<BigDecimal, BigDecimal> parseRange(String limits) {
	String[] limitArray = JSONUtil.parseLimits(limits);
	if(limitArray[0].equals("range")) {
		String[] values = limitArray[1].split(",");
		if(values.length == 2) try {
			return Bucket.of(new BigDecimal(values[0]), new BigDecimal(values[1]));
		} catch(NumberFormatException e) {}
	}
	return null;
}

/**
 * Gets a {@code BigDecimal} scale to be used for values selected from display width.
 * @param min minimum value
 * @param max maximum value
 * @param buttonWidth width of button
 * @param maxValueWidth maximum width between values on the button
 * @return a {@code BigDecimal} scale to be used for values selected from display width
 * @see BigDecimal#scale()
 */
public static int getButtonScale(BigDecimal min, BigDecimal max, BigDecimal buttonWidth, BigDecimal maxValueWidth) {
	BigDecimal range = max.subtract(min);
	int getScale = range.scale();
	range = new BigDecimal(range.unscaledValue());
	while(buttonWidth.divide(range, RoundingMode.DOWN).compareTo(maxValueWidth) > 0) {
		range = range.scaleByPowerOfTen(1);
		getScale++;
	}
	return getScale;
}

@Override
protected BigDecimal highlightWidthToValue(int displayWidth) {
	if(displayWidth <= 0) return min;
	if(displayWidth >= width) return max;
	return new BigDecimal(displayWidth)
			.multiply(max.subtract(min))
			.divide(new BigDecimal(width), scale, RoundingMode.HALF_UP)
			.add(min);
}

@Override
protected int valueToHighlightWidth(BigDecimal value) {
	return value.subtract(min)
			.multiply(new BigDecimal(width))
			.divide(max.subtract(min), RoundingMode.HALF_UP)
			.intValue();
}

@Override
protected BigDecimal incrementValue() {
	BigDecimal value = getValue(),
			newValue = value.add(BigDecimal.ONE);
	return newValue.compareTo(max) > 0 ? value.equals(max) ? min : max : newValue;
}

@Override
protected BigDecimal decrementValue() {
	BigDecimal value = getValue(),
			newValue = value.subtract(BigDecimal.ONE);
	return newValue.compareTo(min) < 0 ? value.equals(min) ? max : min : newValue;
}

}