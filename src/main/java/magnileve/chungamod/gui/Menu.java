package magnileve.chungamod.gui;

import java.util.List;

/**
 * Manages a list of buttons intended to be displayed within a certain area.
 * @author Magnileve
 * @see MenuChain
 */
public interface Menu extends ClickGUIButton, MenuProperties {

/**
 * Gets a list of the menu's buttons that, when updated, will appropriately update each button's position on the menu.
 * @return a list of buttons
 */
public List<ClickGUIButton> buttons();

/**
 * Called by the list returned by {@link #buttons()} when modified
 * to appropriately update the menu size and positions of buttons starting at a given index.
 * @param index index in this menu's list of buttons
 * @throws IndexOutOfBoundsException if {@code index} is less than zero
 * or greater than (but not equal to) the size of this menu's list of buttons
 */
public void updateButtonPositions(int index);

/**
 * Creates a new header represented buy a {@link ClickGUIButton}.
 * @param displayString string to be displayed by the header
 * @return a new header displaying the given string
 */
public ClickGUIButton newHeader(String displayString);

/**
 * Gets the remaining width that additional buttons can occupy in a row of buttons.
 * Keep in mind that if multiple buttons are inserted into the row, divider space may need to be added between them.
 * @param buttons current buttons in the row
 * @return the remaining width additional buttons can occupy in the row
 */
public int getRemainingWidth(ClickGUIButton... buttons);

/**
 * Gets the height that this menu is currently scrolled to.
 * This value is no less than {@code 0} and no more than {@code getScrollableHeight() - getHeight()}.
 * @return the height that this menu is currently scrolled to
 */
public int getScrollHeight();

/**
 * Gets the distance from the top of the first button to the bottom of the last button, including dividers on each end.
 * @return the scrollable height of this menu
 */
public int getScrollableHeight();

/**
 * Determines if this menu is being scrolled.
 * @return {@code true} if this menu is being or has very recently been scrolled; {@code false} otherwise
 */
public boolean isBeingScrolled();

}