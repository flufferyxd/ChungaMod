package magnileve.chungamod.gui;

/**
 * A button that displays a message when hovered over.
 * @author Magnileve
 */
public abstract class HoverInfoButton extends ClickGUIButtonImpl implements DisplayMessageSender {

/**
 * The {@link UpdatableDisplay} used by this button to display messages.
 */
protected final UpdatableDisplay messageDisplayer;

private boolean isNameTrimmed;
private boolean displayingMessage;

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
public HoverInfoButton(int id, int x, int y, int widthIn, int heightIn, String name, ButtonRendererFactory<ClickGUIButton> rendererFactory,
		UpdatableDisplay messageDisplayer) {
	super(id, x, y, widthIn, heightIn, name, rendererFactory);
	this.messageDisplayer = messageDisplayer;
}

@Override
public void displayMessage(String message) {
	messageDisplayer.display(message, this, () -> displayingMessage = false);
	displayingMessage = true;
}

@Override
public void hideDisplayedMessage() {
	if(displayingMessage) messageDisplayer.hide();
}

@Override
protected boolean isNameTrimmed() {
	return isNameTrimmed;
}

/**
 * Gets a message to be displayed when this button is hovered.
 * @return if this button's name is trimmed, the name and a newline; if not, an empty string
 */
protected String getHoverMessage() {
	return isNameTrimmed() ? getName() + '\n' : "";
}

/**
 * Called when the mouse moves over or off of this button.
 */
protected void onHover() {
	if(isHovered()) displayMessage(getHoverMessage());
	else hideDisplayedMessage();
}

@Override
public boolean updateHovered(int mouseX, int mouseY, boolean alreadyProcessed) {
	boolean oldHovered = hovered, hovered = super.updateHovered(mouseX, mouseY, alreadyProcessed);
	if(oldHovered != hovered) onHover();
	return hovered;
}

@Override
public void setDisplayString(String displayString) {
	super.setDisplayString(displayString);
	setIsNameTrimmed(super.isNameTrimmed());
}

/**
 * Sets the cached value to be returned by {@link #isNameTrimmed()}.
 * @param value whether or not the button name is trimmed
 */
protected void setIsNameTrimmed(boolean value) {
	isNameTrimmed = value;
}

}