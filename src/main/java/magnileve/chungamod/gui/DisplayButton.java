package magnileve.chungamod.gui;

/**
 * A button only for displaying.
 * @author Magnileve
 */
public class DisplayButton extends ClickGUIButtonImpl {

/**
 * Creates a new display button.
 * @param id button ID
 * @param x x position
 * @param y y position
 * @param widthIn width
 * @param heightIn height
 * @param name name of button; also the display string unless used differently by a subclass
 * @param rendererFactory factory to build renderer for this button
 */
public DisplayButton(int id, int x, int y, int widthIn, int heightIn, String name, ButtonRendererFactory<ClickGUIButton> rendererFactory) {
	super(id, x, y, widthIn, heightIn, name, rendererFactory);
}

@Override
protected void onClick(int mouseButton) {}

}