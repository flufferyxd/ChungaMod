package magnileve.chungamod.gui;

/**
 * Contains simple methods for rendering and passing mouse input through a button.
 * @author Magnileve
 * @see ClickGUIButton
 */
public interface ClickGUIButtonBase {

/**
 * Renders this button.
 */
public void draw();

/**
 * Checks if this button is currently hovered over and appropriately updates its hovered state.
 * @param mouseX x position of mouse
 * @param mouseY y position of mouse
 * @param alreadyProcessed if another button is already hovered over.
 * If this parameter is {@code true}, this method should return {@code false}.
 * @return {@code true} if this button is currently hovered over; {@code false} otherwise
 */
public boolean updateHovered(int mouseX, int mouseY, boolean alreadyProcessed);

/**
 * Called when the mouse is clicked.  Does the same as {@link #updateHovered(int, int, boolean)},
 * and optionally additionally does an action as a result of the click.
 * @param mouseX x position of mouse
 * @param mouseY y position of mouse
 * @param mouseButton ID of the button clicked
 * @param alreadyProcessed if another button is already hovered over.
 * If this parameter is {@code true}, this method should return {@code false}.
 * @return {@code true} if this button is currently hovered over; {@code false} otherwise
 */
public boolean mouseClicked(int mouseX, int mouseY, int mouseButton, boolean alreadyProcessed);

/**
 * Called when the mouse is scrolled.  Unlike {@link #mouseClicked(int, int, int, boolean)},
 * this method does not have to return {@code true} if this button is currently hovered over;
 * instead, it can return {@code false} to pass on the scroll action to the next button beneath it.
 * This can be useful if this button is displayed on a {@link Menu}.
 * @param mouseX x position of mouse
 * @param mouseY y position of mouse
 * @param up if the scroll direction is up
 * @param alreadyProcessed if another button is already scrolled.
 * If this parameter is {@code true}, this method should return {@code false}.
 * @return {@code true} if this button has been scrolled; {@code false} otherwise
 */
public boolean mouseScrolled(int mouseX, int mouseY, boolean up, boolean alreadyProcessed);

}