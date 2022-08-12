package magnileve.chungamod.gui;

import java.util.Collections;
import java.util.List;
import java.util.function.ToIntFunction;

import magnileve.chungamod.util.function.ObjIntFunction;

/**
 * A resizable {@link UpdatableDisplayButton} that displays messages on multiple lines.
 * @author Magnileve
 */
public class ResizableDisplayButton extends UpdatableDisplayButtonImpl {

/**
 * Maximum width of this button.
 */
protected final int maxWidth;
/**
 * Splits strings into strings no longer than a given length.
 */
protected final ObjIntFunction<String, List<String>> stringSplitter;
/**
 * Gets the length of a string.
 */
protected final ToIntFunction<String> stringToLength;

private final int lineHeight;

private List<String> displayLines;

/**
 * Creates a new updatable display button.
 * @param id button ID
 * @param x x position
 * @param y y position
 * @param maxWidth maximum width of this button
 * @param lineHeight height of one line of text
 * @param displayString initial string to be displayed
 * @param rendererFactory factory to build renderer for this button
 * @param dividerSize standard space between buttons
 * @param startVisible if this button should initially display the given {@code displayString}
 * @param changeNotifier if not null, runs the next time {@code display} or {@code hide} is called.
 * @param stringSplitter splits strings into strings no longer than a given length
 * @param stringToLength gets the length of a string
 */
public ResizableDisplayButton(int id, int x, int y, int maxWidth, int lineHeight, String displayString,
		ButtonRendererFactory<ClickGUIButton> rendererFactory, int dividerSize, boolean startVisible, Runnable changeNotifier,
		ObjIntFunction<String, List<String>> stringSplitter, ToIntFunction<String> stringToLength) {
	super(id, x, y, maxWidth, lineHeight, displayString, rendererFactory, dividerSize, startVisible, changeNotifier);
	this.maxWidth = maxWidth;
	this.lineHeight = lineHeight;
	this.stringSplitter = stringSplitter;
	this.stringToLength = stringToLength;
}

@Override
public void setDisplayString(String displayString) {
	displayLines = Collections.unmodifiableList(stringSplitter.apply(displayString, maxWidth));
	width = 0;
	for(String line:displayLines) {
		int checkWidth = stringToLength.applyAsInt(line);
		if(checkWidth > width) width = checkWidth;
	}
	height = getLineHeight() * displayLines.size();
	updateRenderer();
}

/**
 * Gets the height of one line of text.
 * @return the height of one line of text
 */
public int getLineHeight() {
	return lineHeight;
}

/**
 * Gets the list of lines of text displayed by this button.
 * @return the list of lines of text displayed by this button
 */
public List<String> getDisplayLines() {
	return displayLines;
}

}