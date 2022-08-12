package magnileve.chungamod.gui.values;

import java.awt.Color;
import java.util.function.Function;
import java.util.function.IntSupplier;

import org.lwjgl.input.Mouse;

import magnileve.chungamod.gui.ButtonRendererFactory;
import magnileve.chungamod.gui.ClickGUIButton;
import magnileve.chungamod.gui.MenuButtonBuilder;
import magnileve.chungamod.gui.MenuChain;
import magnileve.chungamod.gui.UpdatableDisplay;
import magnileve.chungamod.util.Colors;
import magnileve.chungamod.util.Permit;
import magnileve.chungamod.util.function.BiIntConsumer;
import magnileve.chungamod.util.math.MathUtil;
import net.minecraft.util.math.Vec3i;

/**
 * A button containing a triangle for selecting saturation and value of a color.
 * @author Magnileve
 */
public class ColorPicker extends ValueButtonImpl<Color> {

/**
 * The ratio of width to height for a color triangle.
 * This value is equal to {@code sqrt(3) / 2}.
 */
public static final double TRIANGLE_HEIGHT_WIDTH_RATIO = 0.8660254037844386D;

/**
 * Permit for handling mouse activity.
 */
protected final Permit<BiIntConsumer> mousePermit;

private final int triStartHeight, triWidth, triHeight;

/**
 * This button's color's hue.
 */
protected float hue;
/**
 * This button's color's alpha.  Alpha may be removed from {@code value}
 */
protected int alpha;

private Vec3i saturatedRGB;
private BiIntConsumer mouseHandler;
private int triX, triY;

/**
 * Creates a new color picker.
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
 * @param mousePermit permit for handling mouse activity
 */
public ColorPicker(int id, int x, int y, int widthIn, int heightIn, String name,
		ButtonRendererFactory<ClickGUIButton> rendererFactory, UpdatableDisplay messageDisplayer,
		Color value, ValueProcessor<Color> valueProcessor, Function<ValueButton<Color>, MenuButtonBuilder> menuButtonBuilder,
		MenuChain menuChain, IntSupplier buttonIDSupplier, Permit<BiIntConsumer> mousePermit) {
	super(id, x, y, widthIn, heightIn, name, rendererFactory, messageDisplayer,
			value, valueProcessor, menuButtonBuilder, menuChain, buttonIDSupplier, null);
	this.mousePermit = mousePermit;
	
	ValueConsumer<Color> parentButton = getParent();
	while(parentButton != null && (!(parentButton instanceof ValueButton) || parentButton instanceof ColorPicker))
		parentButton = parentButton.getParent();
	
	int dividerSize = menuChain.getMenuProperties().getDividerSize();
	triStartHeight = parentButton == null ? Math.min(9 + dividerSize * 2, height) :
		((ValueButton<Color>) parentButton).getHeight();
	
	int maxHeight = height - triStartHeight - dividerSize * 2,
			getWidth = width - dividerSize * 2,
			getHeight = (int) (width * TRIANGLE_HEIGHT_WIDTH_RATIO);
	if(getHeight > maxHeight) {
		double maxToCurrentRatio = (double) maxHeight / getHeight;
		getWidth *= maxToCurrentRatio;
		getHeight *= maxToCurrentRatio;
	}
	
	triWidth = getWidth;
	triHeight = getHeight;
	triX = width == triWidth ? x : x + (width - triWidth) / 2;
	triY = y + triStartHeight;
}

/**
 * Gets the color at a position in the color triangle.
 * If the given coordinates are outside of the color triangle, the currently set color is returned.
 * @param mouseX x position of mouse
 * @param mouseY y position of mouse
 * @return the color at a position in the color triangle
 */
protected Color getColor(int mouseX, int mouseY) {
	double light, s, darkS;
	
	try {
		light = MathUtil.roundBetween(MathUtil.relativePointBetween(mouseX, mouseY,
				triX + getTriWidth() / 2, triY, triX, triY + getTriHeight(), triX + getTriWidth(), triY + getTriHeight(), true), 0D, 1D);
		s = MathUtil.roundBetween(MathUtil.relativePointBetween(mouseX, mouseY,
				triX, triY + getTriHeight(), triX + getTriWidth(), triY + getTriHeight(), triX + getTriWidth() / 2, triY, true), 0D, 1D);
		darkS = MathUtil.roundBetween(MathUtil.relativePointBetween(mouseX, mouseY,
				triX + getTriWidth() / 2, triY, triX + getTriWidth(), triY + getTriHeight(), triX, triY + getTriHeight(), true), 0D, 1D);
	} catch(IllegalArgumentException e) {
		return getValue();
	}
	
	Vec3i rgb = Colors.fToI(Colors.hsvTorgb(hue, (float) s, (float) (darkS * s + light * (1 - s))));
	return new Color(rgb.getX(), rgb.getY(), rgb.getZ(), alpha);
}

/**
 * Gets the x position of the triangle at a given button location.
 * @param x x position of this button
 * @return x position of the triangle
 */
protected int getTriX(int x) {
	int width = getWidth(), triWidth = getTriWidth();
	return width == triWidth ? x : x + (width - triWidth) / 2;
}

/**
 * Gets the y position of the triangle at a given button location.
 * @param y y position of this button
 * @return y position of the triangle
 */
protected int getTriY(int y) {
	return y + getTriStartHeight();
}

/**
 * Gets the x position of the triangle.
 * @return x position of the triangle
 */
public int getTriX() {
	return triX;
}

/**
 * Gets the y position of the triangle.
 * @return y position of the triangle
 */
public int getTriY() {
	return triY;
}

/**
 * Gets the distance between the top of the button and the top of the triangle.
 * @return distance between the top of the button and the top of the triangle
 */
public int getTriStartHeight() {
	return triStartHeight;
}

/**
 * Gets the width of the triangle.
 * @return width of the triangle
 */
public int getTriWidth() {
	return triWidth;
}

/**
 * Gets the height of the triangle.
 * @return height of the triangle
 */
public int getTriHeight() {
	return triHeight;
}

/**
 * Gets the RGB values of the current value but with full saturation.
 * @return the RGB values of the current value but with full saturation
 */
public Vec3i getSaturatedRGB() {
	return saturatedRGB;
}

@Override
protected void onClick(int mouseButton, int mouseX, int mouseY) {
	super.onClick(mouseButton, mouseX, mouseY);
	if(mouseButton == 0) {
		if(mouseY <= triY + getTriHeight() &&
				MathUtil.compareToLine(mouseX, mouseY, triX, triY + getTriHeight(), triX + getTriWidth() / 2, triY) >= 0 &&
				MathUtil.compareToLine(mouseX, mouseY, triX + getTriWidth() / 2, triY, triX + getTriWidth(), triY + getTriHeight()) >= 0)
			mouseHandler = mousePermit.getIfAcquired(() -> (mouseX1, mouseY1) -> {
				if(Mouse.getEventButton() == 0 && !Mouse.getEventButtonState()) {
					mousePermit.release(mouseHandler);
					mouseHandler = null;
					displayIfChanged(processNewValue(getColor(mouseX1, mouseY1)));
					super.updateDisplay();
				} displayIfChanged(getColor(mouseX1, mouseY1));
			});
		else hideDisplayedMessage();
	}
	
}

@Override
protected void updateDisplay() {
	if(mouseHandler == null) {
		Color value = getValue();
		hue = Colors.rgbTohsv(Colors.getComponents(value)).getX();
		alpha = value.getAlpha();
		saturatedRGB = Colors.fToI(Colors.hsvTorgb(hue, 1.0F, 1.0F));
	}
}

@Override
public String valueToString() {
	return Colors.toString(getValue());
}

@Override
public void setX(int x) {
	triX = getTriX(x);
	super.setX(x);
}

@Override
public void setY(int y) {
	triY = getTriY(y);
	super.setY(y);
}

@Override
public void setPos(int x, int y) {
	triX = getTriX(x);
	triY = getTriY(y);
	super.setPos(x, y);
}

@Override
protected String getHoverMessage() {
	return null;
}

@Override
protected void onClick(int mouseButton) {}

}