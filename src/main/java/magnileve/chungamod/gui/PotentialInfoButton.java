package magnileve.chungamod.gui;

/**
 * A button that can display a message.
 * @author Magnileve
 */
public abstract class PotentialInfoButton extends HoverInfoButton {

/**
 * Creates a new button.
 * @param id button ID
 * @param x x position
 * @param y y position
 * @param widthIn width
 * @param heightIn height
 * @param name name of button; also the display string unless used differently by a subclass
 * @param rendererFactory factory to build renderer for this button
 * @param messageDisplayer used to display messages
 */
public PotentialInfoButton(int id, int x, int y, int widthIn, int heightIn, String displayString,
		ButtonRendererFactory<ClickGUIButton> rendererFactory, UpdatableDisplay messageDisplayer) {
	super(id, x, y, widthIn, heightIn, displayString, rendererFactory, messageDisplayer);
}

@Override
protected String getHoverMessage() {
	return null;
}

@Override
protected void onHover() {
	if(!hovered) hideDisplayedMessage();
}

}