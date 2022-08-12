package magnileve.chungamod.gui.values;

import java.awt.Color;
import java.util.function.Function;
import java.util.function.IntSupplier;

import magnileve.chungamod.gui.ButtonRendererFactory;
import magnileve.chungamod.gui.ClickGUIButton;
import magnileve.chungamod.gui.Menu;
import magnileve.chungamod.gui.MenuButtonBuilder;
import magnileve.chungamod.gui.MenuChain;
import magnileve.chungamod.gui.UpdatableDisplay;
import magnileve.chungamod.util.ArrayBuildList;
import magnileve.chungamod.util.Colors;
import magnileve.chungamod.util.Permit;
import magnileve.chungamod.util.function.BiIntConsumer;
import magnileve.chungamod.util.json.JSONManager;
import magnileve.chungamod.util.math.Vec3f;

/**
 * A button with a color value.
 * @author Magnileve
 */
public class ColorButton extends StringValueButton<Color> {

/**
 * Parses number values of color components.
 */
protected final JSONManager json;
/**
 * Permit for handling mouse activity.
 */
protected final Permit<BiIntConsumer> mousePermit;

/**
 * Creates a new color value button.
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
 * @param json parses number values of color components
 * @param mousePermit permit for handling mouse activity
 */
public ColorButton(int id, int x, int y, int widthIn, int heightIn, String name, ButtonRendererFactory<ClickGUIButton> rendererFactory,
		UpdatableDisplay messageDisplayer, Color value, ValueProcessor<Color> valueProcessor,
		Function<ValueButton<Color>, MenuButtonBuilder> menuButtonBuilder, MenuChain menuChain, IntSupplier buttonIDSupplier,
		String description, Permit<Runnable> keyboardPermit, JSONManager json, Permit<BiIntConsumer> mousePermit) {
	super(id, x, y, widthIn, heightIn, name, rendererFactory, messageDisplayer,
			value, valueProcessor, menuButtonBuilder, menuChain, buttonIDSupplier, description, keyboardPermit);
	this.json = json;
	this.mousePermit = mousePermit;
}

/**
 * Gets the maximum text width of a {@code ColorButton}.
 * @param buttonWidth width of a {@code ColorButton}
 * @return maximum text width for that button
 */
public static int getTextWidth(int buttonWidth) {
	return buttonWidth * 8 / 9;
}

@Override
public void addMenuEntries(ArrayBuildList<ClickGUIButton> builder, Menu menu, MenuChain menuChain) {
	Color value = getValue();
	builder.add(new ColorPicker(buttonIDSupplier.getAsInt(), 0, 0,
			getWidth(), (int) (getWidth() * ColorPicker.TRIANGLE_HEIGHT_WIDTH_RATIO) + getHeight(), "Pick color", rendererFactory,
			messageDisplayer, value, this, null, menuChain, buttonIDSupplier, mousePermit).init());
	
	Function<ValueButton<Color>, MenuButtonBuilder> buildJSONButton = button -> {
		IntRangeButton<Color> b = (IntRangeButton<Color>) button;
		return (builder1, menu1, menuChain1) -> {
			builder1.add(new JSONButton<Integer>(buttonIDSupplier.getAsInt(), 0, 0, getWidth(), getHeight(), rendererFactory,
					messageDisplayer, b.valueToInt(b.getValue()), new ValueProcessor.Of<>(newValue -> {
				Color color = b.processNewValue(b.intToValue(newValue));
				b.displayIfChanged(color);
				b.hideDisplayedMessage();
				return b.valueToInt(color);
			}, () -> null), null, menuChain1, buttonIDSupplier, null, keyboardPermit, Integer.class, json, false).init());
		};
	};
	
	builder.add(new ColorComponentButton.HueButton(buttonIDSupplier.getAsInt(), 0, 0, getWidth(), getHeight(), "Hue",
			rendererFactory, messageDisplayer, value, this, buildJSONButton, menuChain, buttonIDSupplier, null, mousePermit, (color, value1) -> {
				Vec3f hsv = Colors.rgbTohsv(Colors.getComponents(color));
				return Colors.newColor(Colors.hsvTorgb(new Vec3f(Colors.iToF(value1), hsv.getY(), hsv.getZ())), color.getAlpha());
			}, color -> Colors.fToI(Colors.rgbTohsv(Colors.getComponents(color)).getX()), false).init());
	builder.add(new ColorComponentButton.AltColorComponentButton(buttonIDSupplier.getAsInt(), 0, 0, getWidth(), getHeight(), "Saturation",
			rendererFactory, messageDisplayer, value, this, buildJSONButton, menuChain, buttonIDSupplier, null, mousePermit, (color, value1) -> {
		Vec3f hsv = Colors.rgbTohsv(Colors.getComponents(color));
		return Colors.newColor(Colors.hsvTorgb(new Vec3f(hsv.getX(), Colors.iToF(value1), hsv.getZ())), color.getAlpha());
	}, color -> Colors.fToI(Colors.rgbTohsv(Colors.getComponents(color)).getY()), false).init());
	builder.add(new ColorComponentButton.AltColorComponentButton(buttonIDSupplier.getAsInt(), 0, 0, getWidth(), getHeight(), "Value",
			rendererFactory, messageDisplayer, value, this, buildJSONButton, menuChain, buttonIDSupplier, null, mousePermit, (color, value1) -> {
		Vec3f hsv = Colors.rgbTohsv(Colors.getComponents(color));
		return Colors.newColor(Colors.hsvTorgb(new Vec3f(hsv.getX(), hsv.getY(), Colors.iToF(value1))), color.getAlpha());
	}, color -> Colors.fToI(Colors.rgbTohsv(Colors.getComponents(color)).getZ()), false).init());
	
	builder.add(new ColorComponentButton(buttonIDSupplier.getAsInt(), 0, 0, getWidth(), getHeight(), "Red", rendererFactory,
			messageDisplayer, value, this, buildJSONButton, menuChain, buttonIDSupplier, null, mousePermit,
			(color, value1) -> new Color(color.getRGB() & 0xFF00FFFF | value1 << 16, true), color -> color.getRed(), false).init());
	builder.add(new ColorComponentButton(buttonIDSupplier.getAsInt(), 0, 0, getWidth(), getHeight(), "Green", rendererFactory,
			messageDisplayer, value, this, buildJSONButton, menuChain, buttonIDSupplier, null, mousePermit,
			(color, value1) -> new Color(color.getRGB() & 0xFFFF00FF | value1 << 8, true), color -> color.getGreen(), false).init());
	builder.add(new ColorComponentButton(buttonIDSupplier.getAsInt(), 0, 0, getWidth(), getHeight(), "Blue", rendererFactory,
			messageDisplayer, value, this, buildJSONButton, menuChain, buttonIDSupplier, null, mousePermit,
			(color, value1) -> new Color(color.getRGB() & 0xFFFFFF00 | value1, true), color -> color.getBlue(), false).init());
	builder.add(new ColorComponentButton(buttonIDSupplier.getAsInt(), 0, 0, getWidth(), getHeight(), "Alpha", rendererFactory,
			messageDisplayer, value, this, buildJSONButton, menuChain, buttonIDSupplier, null, mousePermit,
			(color, value1) -> new Color(color.getRGB() & 0x00FFFFFF | value1 << 24, true), color -> color.getAlpha(), true).init());
	super.addMenuEntries(builder, menu, menuChain);
}

@Override
public String valueToString() {
	return Colors.toString(getValue());
}

@Override
public Color stringToValue(String valueString) throws IllegalArgumentException {
	try {
		return Colors.parseColor(valueString);
	} catch(NumberFormatException e) {
		throw new IllegalArgumentException("Format: [#]<hex code>");
	}
}

}