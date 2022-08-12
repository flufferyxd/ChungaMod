package magnileve.chungamod.gui;

/**
 * A button that contains dimensions, a display string, and a name.
 * @author Magnileve
 * @see ClickGUIButtonImpl
 */
public interface ClickGUIButton extends ClickGUIButtonBase, ButtonRenderer {

/**
 * Sets the display string of this button.
 * @param displayString the string for this button to display
 */
public void setDisplayString(String displayString);

/**
 * Gets the current string displayed by this button.
 * @return the string displayed by this button
 * @see #getName()
 */
public String getDisplayString();

/**
 * Gets the name of this button.
 * The name can be used for identification or helping to build the display string.
 * This may or may not return the same result as {@link #getDisplayString()}.
 * @return the name of this button
 */
public String getName();

/**
 * Gets this button's x position.
 * @return this button's x position
 */
public int getX();

/**
 * Gets this button's y position.
 * @return this button's y position
 */
public int getY();

/**
 * Sets this button's x position.
 * @param x a new x position
 * @see #setPos(int, int)
 */
public void setX(int x);

/**
 * Sets this button's y position.
 * @param y a new y position
 * @see #setPos(int, int)
 */
public void setY(int y);

/**
 * Sets this button's position.
 * @param x a new x position
 * @param y a new y position
 * @see #setX(int)
 * @see #setY(int)
 */
public default void setPos(int x, int y) {
	setX(x);
	setY(y);
}

/**
 * Gets this button's width.
 * @return this button's width
 */
public int getWidth();

/**
 * Gets this button's height.
 * @return this button's height
 */
public int getHeight();

/**
 * Gets whether or not this button is hovered over.
 * @return {@code true} if this button is hovered over; {@code false} otherwise
 */
public boolean isHovered();

}