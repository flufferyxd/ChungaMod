package magnileve.chungamod.gui;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.ToIntFunction;

import magnileve.chungamod.Tick;
import magnileve.chungamod.TickListener;
import magnileve.chungamod.util.Corner;
import magnileve.chungamod.util.function.ObjIntFunction;

/**
 * An {@link UpdatableDisplayButton} that displays messages in a corner of a GUI.
 * @author Magnileve
 */
public class CornerDisplayButton extends ResizableDisplayButton implements TickListener {

/**
 * The maximum y position this button can be displayed at.
 */
protected final int maxY;
private final Corner corner;
private final int displayTimeLength;
private final int fadeOutTime;

/**
 * Given this button's y position every time it changes while this button is fading out.
 */
protected IntConsumer renderer;
private int displayY;
private int displayTime;

/**
 * Creates a new updatable display button.
 * @param id button ID
 * @param x x position
 * @param maxY the maximum y position this button can be displayed at
 * @param maxWidth maximum width of this button
 * @param lineHeight height of one line of text
 * @param displayString initial string to be displayed
 * @param rendererFactory factory to build renderer for this button
 * @param dividerSize standard space between buttons
 * @param startVisible if this button should initially display the given {@code displayString}
 * @param changeNotifier if not null, runs the next time {@code display} or {@code hide} is called.
 * @param stringSplitter splits strings into strings no longer than a given length
 * @param stringToLength gets the length of a string
 * @param corner the corner this button should be displayed in
 * @param displayTimeLength the amount of ticks this button should be displayed
 * @param fadeOutTime the amount of ticks this button should take to fade out
 */
public CornerDisplayButton(int id, int x, int maxY, int maxWidth, int lineHeight, String displayString,
		ButtonRendererFactory<ClickGUIButton> rendererFactory, int dividerSize, boolean startVisible, Runnable changeNotifier,
		ObjIntFunction<String, List<String>> stringSplitter, ToIntFunction<String> stringToLength,
		Corner corner, int displayTimeLength, int fadeOutTime) {
	super(id, x, maxY, maxWidth, lineHeight, displayString, rendererFactory, dividerSize, startVisible, changeNotifier,
			stringSplitter, stringToLength);
	this.maxY = maxY;
	this.corner = corner;
	this.displayTimeLength = displayTimeLength;
	this.fadeOutTime = fadeOutTime;
}

@Override
public int onTick() {
	if(!hovered) {
		displayTime++;
		if(displayTime > displayTimeLength) {
			y = (int) (displayY +
					(double) (corner.isTop() ? displayTimeLength - displayTime : displayTime - displayTimeLength)
					/ fadeOutTime * height);
			if(displayTime > displayTimeLength + fadeOutTime) {
				displayTime = 0;
				visible = false;
				updateRenderer();
				update(null);
				return -1;
			}
			renderer.accept(y);
		}
	}
	return 1;
}

/**
 * Sets the consumer for y position changes while this button is fading out.
 * @param consumer the consumer
 */
public void setYValueConsumer(IntConsumer consumer) {
	renderer = consumer;
}

@Override
public void setDisplayString(String displayString) {
	y = -1;
	super.setDisplayString(displayString);
	displayY = corner.isTop() ? 0 : maxY - height;
	x = corner.isLeft() ? 0 : maxWidth - width;
	y = displayY;
	updateRenderer();
}

@Override
public void display(String message, int x, int y, Runnable changeNotifier) {
	if(message != null && !message.isEmpty()) {
		if(displayTime > 0) {
			displayTime = 0;
			Tick.MAIN.remove(this);
		}
		visible = true;
		setDisplayString(message);
	}
	update(changeNotifier);
}

@Override
public void display(String message, ClickGUIButton button, Runnable changeNotifier) {
	display(message, 0, 0, changeNotifier);
}

@Override
public void hide(Runnable changeNotifier) {
	if(isVisible() && displayTime == 0) {
		displayTime++;
		Tick.MAIN.add(this, 1);
	}
	update(changeNotifier);
}

/**
 * Gets the x positions of each visible corner of this button when displayed in a given corner.
 * @param corner the corner for this button to be displayed in
 * @param x1 the x position of the left side of this button
 * @param x2 the x position of the right side of this button
 * @return an {@code int} array of length {@code 3}
 */
public static int[] getVisibleCornersX(Corner corner, int x1, int x2) {
	switch(corner) {
	case TOP_LEFT:
		return new int[] {x1, x2, x2};
	case TOP_RIGHT:
		return new int[] {x1, x1, x2};
	case BOTTOM_LEFT:
		return new int[] {x2, x2, x1};
	case BOTTOM_RIGHT:
		return new int[] {x2, x1, x1};
	default:
		return null;
	}
}

/**
 * Gets the y positions of each visible corner of this button when displayed in a given corner.
 * @param corner the corner for this button to be displayed in
 * @param y1 the y position of the top side of this button
 * @param y2 the y position of the bottom side of this button
 * @return an {@code int} array of length {@code 3}
 */
public static int[] getVisibleCornersY(Corner corner, int y1, int y2) {
	switch(corner) {
	case TOP_LEFT:
		return new int[] {y2, y2, y1};
	case TOP_RIGHT:
		return new int[] {y1, y2, y2};
	case BOTTOM_LEFT:
		return new int[] {y2, y1, y1};
	case BOTTOM_RIGHT:
		return new int[] {y1, y1, y2};
	default:
		return null;
	}
}

/**
 * Gets the corner this button should be displayed in.
 * @return the corner this button should be displayed in
 */
public Corner getCorner() {
	return corner;
}

}