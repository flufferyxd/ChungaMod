package magnileve.chungamod.gui;

/**
 * Builds renderers and trims strings for buttons.
 * @param <T> common type extended by buttons
 * @author Magnileve
 */
public interface ButtonRendererFactory<T> {

/**
 * Builds a {@link ButtonRenderer} for a given button.
 * @param b the button
 * @return a renderer for the button in its current state
 */
public ButtonRenderer buildRenderer(T b);

/**
 * Trims a string to fit within a button.
 * @param b the button
 * @param displayString string to be displayed on the button
 * @return {@code displayString} or a shortened version of it
 */
public String trim(T b, String displayString);

}