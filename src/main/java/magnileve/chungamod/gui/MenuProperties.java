package magnileve.chungamod.gui;

import net.minecraft.util.math.Vec3i;

/**
 * Provides properties used by a {@link Menu}.
 * @author Magnileve
 */
public interface MenuProperties {

/**
 * Gets the size of the space intended to be between borders of buttons and the menu.
 * @return the divider size
 */
public int getDividerSize();

/**
 * Gets a {@code Vec3i} containing values of properties for scrolling.  These values are:
 * <p style="margin-left:40px">
 * x - the maximum display height of the menu<br>
 * y - the speed of scrolling<br>
 * z - how long to override scrolls on buttons when scrolled
 * </p>
 * @return the scroll properties
 */
public Vec3i getScrollProperties();

}