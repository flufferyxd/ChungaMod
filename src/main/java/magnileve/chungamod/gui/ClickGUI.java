package magnileve.chungamod.gui;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import magnileve.chungamod.util.Permit;
import magnileve.chungamod.util.function.BiIntConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;

/**
 * <p>
 * A {@code GuiScreen} composed of instances of core interfaces of the Chungamod GUI API.
 * </p>
 * <p>
 * Most notably, a {@code ClickGUI} contains
 * a {@link ClickGUIButtonBase} list, a {@link MenuChain}, and an {@link UpdatableDisplayButton}.
 * The {@code ClickGUI} manages calls to the buttons for rendering and mouse handling.
 * A {@link Permit} for keyboard and for mouse are also contained and can override default calls on activity.
 * </p>
 * <p>
 * The process for handling mouse activity is as follows:<br>
 * 1. If there is a subGUI as returned by {@link #getSubGUI()}, calls are forwarded to the subGUI.<br>
 * 2. If there is no subGUI, and the mouse permit as returned by {@link #getMousePermit()} is being controlled by an object,
 * calls are forwarded to that object.<br>
 * 3. If there is no subGUI, and the mouse permit is not being controlled by an object,
 * the menu chain as returned by {@link #getSubMenus()} is first notified,
 * and then the buttons in the list returned by {@link #getButtons()} are notified.
 * A copy of the list is made for iteration except for calls to {@link ClickGUIButtonBase#updateHovered(int, int, boolean)}.
 * </p>
 * <p>
 * The process for rendering is the same as above, excluding the mouse permit,
 * except that step 3 is in reverse order, and the display button as returned by {@link #getDisplayer()} is rendered after the rest of step 3.
 * The list of buttons is also iterated directly.
 * </p>
 * @author Magnileve
 * @see ClickGUIButton
 * @see Menu
 */
public class ClickGUI extends GuiScreen {

private final List<ClickGUIButtonBase> buttons;
private final MenuChain subMenus;
private final UpdatableDisplayButton displayer;
private final Permit<BiIntConsumer> mousePermit;
private final Permit<Runnable> keyboardPermit;
private final Runnable onEscape;
private final double sizeMultiplier;

private ClickGUI subGUI;

/**
 * Constructs a new {@code ClickGUI}.
 * @param buttons an optionally modifiable list of buttons
 * @param subMenus the start of a menu chain
 * @param displayer an updatable display button
 * @param mousePermit permit to handle mouse activity
 * @param keyboardPermit permit to handle keyboard activity
 * @param closeGUI if not null, runs when the escape key is pressed
 * @param sizeMultiplier constant scale for GUI size
 */
public ClickGUI(List<ClickGUIButtonBase> buttons, MenuChain subMenus, UpdatableDisplayButton displayer,
		Permit<BiIntConsumer> mousePermit, Permit<Runnable> keyboardPermit, Runnable closeGUI, double sizeMultiplier) {
	this.buttons = buttons;
	this.subMenus = subMenus;
	this.displayer = displayer;
	this.mousePermit = mousePermit;
	this.keyboardPermit = keyboardPermit;
	this.onEscape = closeGUI;
	this.sizeMultiplier = sizeMultiplier;
}

/**
 * Renders the components of the GUI.
 * @param mouseX x position of mouse
 * @param mouseY y position of mouse
 * @param partialTicks amount of time passed since the last tick
 */
@Override
public void drawScreen(int mouseX, int mouseY, float partialTicks) {
	if(subGUI != null) {
		subGUI.drawScreen(mouseX, mouseY, partialTicks);
		return;
	}
	if(!mousePermit.isAvailable()) try {
		handleInput();
	} catch (IOException e) {
		throw new RuntimeException(e);
	}
	mouseX /= sizeMultiplier;
	mouseY /= sizeMultiplier;
	boolean alreadyHovered = subMenus.updateHovered(mouseX, mouseY, false);
	for(ClickGUIButtonBase button:buttons)
		alreadyHovered = button.updateHovered(mouseX, mouseY, alreadyHovered) || alreadyHovered;
	GlStateManager.scale(sizeMultiplier, sizeMultiplier, 1.0D);
	ListIterator<ClickGUIButtonBase> iter = buttons.listIterator(buttons.size());
	while(iter.hasPrevious()) iter.previous().draw();
	subMenus.draw();
	displayer.draw();
	GlStateManager.scale(1.0D, 1.0D, 1.0D);
}

/**
 * Handles mouse activity.
 * @see #mouseClicked(int, int, int)
 * @see #mouseScrolled(int, int, boolean)
 * @see #getMousePermit()
 */
@Override
public void handleMouseInput() throws IOException {
	if(subGUI != null) subGUI.handleMouseInput();
	else if(mousePermit.isAvailable()) {
		int wheelMovement = Mouse.getEventDWheel();
		if(wheelMovement != 0) mouseScrolled(scaleMouseX(Mouse.getEventX()), scaleMouseY(Mouse.getEventY()), wheelMovement >= 0);
		super.handleMouseInput();
	} else mousePermit.getController().accept(scaleMouseX(Mouse.getEventX()), scaleMouseY(Mouse.getEventY()));
}

/**
 * Handles keyboard activity.
 * @see #keyTyped(char, int)
 * @see #getKeyboardPermit()
 */
@Override
public void handleKeyboardInput() throws IOException {
	if(subGUI != null) subGUI.handleKeyboardInput();
	else if(keyboardPermit.isAvailable()) super.handleKeyboardInput();
	else keyboardPermit.getController().run();
}

/**
 * Called when a key is typed.
 * If the escape key is pressed, the GUI is closed, or {@code closeGUI} is ran if it was provided during construction.
 * @param typedChar the character of the key pressed
 * @param keyCode the code of the key pressed
 */
@Override
protected void keyTyped(char typedChar, int keyCode) throws IOException {
	if(keyCode == Keyboard.KEY_ESCAPE) {
		if(onEscape == null) super.keyTyped(typedChar, keyCode);
		else onEscape.run();
	}
}

/**
 * Scales LWJGL mouse position to Mincerfat mouse position on the x axis.
 * @param mouseX a position on the x axis
 * @return the position scaled to Mincerfat measurement
 */
public int scaleMouseX(int mouseX) {
	return (int) (mouseX * width / (double) mc.displayWidth / sizeMultiplier);
}

/**
 * Scales LWJGL mouse position to Mincerfat mouse position on the y axis.
 * @param mouseY a position on the y axis
 * @return the position scaled to Mincerfat measurement
 */
public int scaleMouseY(int mouseY) {
	return (int) ((height - mouseY * height / (double) mc.displayHeight - 1) / sizeMultiplier);
}

/**
 * Notifies components when the mouse is clicked.
 * @param mouseX x position of mouse
 * @param mouseY y position of mouse
 * @param mouseButton ID of the button clicked
 */
@Override
protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
	if(subGUI != null) {
		subGUI.mouseClicked(mouseX, mouseY, mouseButton);
		return;
	}
	mouseX /= sizeMultiplier;
	mouseY /= sizeMultiplier;
	boolean alreadyProcessed = subMenus.mouseClicked(mouseX, mouseY, mouseButton, false);
	ClickGUIButtonBase[] array = buttons.toArray(new ClickGUIButtonBase[buttons.size()]);
	for(int i = 0; i < array.length; i++)
		alreadyProcessed = array[i].mouseClicked(mouseX, mouseY, mouseButton, alreadyProcessed) || alreadyProcessed;
}

/**
 * Notifies components when the mouse is scrolled.
 * @param mouseX x position of mouse
 * @param mouseY y position of mouse
 * @param up if the scroll direction is up
 */
protected void mouseScrolled(int mouseX, int mouseY, boolean up) {
	if(subGUI != null) {
		subGUI.mouseScrolled(mouseX, mouseY, up);
		return;
	}
	boolean alreadyProcessed = subMenus.mouseScrolled(mouseX, mouseY, up, false);
	ClickGUIButtonBase[] array = buttons.toArray(new ClickGUIButtonBase[buttons.size()]);
	for(int i = 0; i < array.length; i++)
		alreadyProcessed = array[i].mouseScrolled(mouseX, mouseY, up, alreadyProcessed) || alreadyProcessed;
}

/**
 * Gets the list of buttons that make up this {@code ClickGUI}.  This list may or may not be modifiable.
 * @return the list of buttons
 */
public List<ClickGUIButtonBase> getButtons() {
	return buttons;
}

/**
 * Gets the start of this {@code ClickGUI}'s {@link MenuChain}.
 * @return a {@code MenuChain}
 */
public MenuChain getSubMenus() {
	return subMenus;
}

/**
 * Gets this {@code ClickGUI}'s {@link UpdatableDisplayButton}.
 * @return an {@code UpdatableDisplayButton}
 */
public UpdatableDisplayButton getDisplayer() {
	return displayer;
}

/**
 * Gets this {@code ClickGUI}'s mouse permit.
 * If this permit is acquired by an object,
 * the object is notified for every LWJGL mouse event instead of other components of the GUI.
 * @return a permit for handling mouse activity
 */
public Permit<BiIntConsumer> getMousePermit() {
	return mousePermit;
}

/**
 * Gets this {@code ClickGUI}'s keyboard permit.
 * If this permit is acquired by an object, the object is notified for every LWJGL keyboard event instead of other components of the GUI.
 * @return a permit for handling keyboard activity
 */
public Permit<Runnable> getKeyboardPermit() {
	return keyboardPermit;
}

/**
 * Gets the size scale of this {@code ClickGUI}.
 * @return the size scale
 */
public double getSizeMultiplier() {
	return sizeMultiplier;
}

@Override
public boolean doesGuiPauseGame() {
	return false;
}

/**
 * Gets the {@code subGUI} if it exists.  When a {@code subGUI} exists, rendering and user input are forwarded to it.
 * @return the {@code subGUI}, or {@code null} if it does not exist
 */
public ClickGUI getSubGUI() {
	return subGUI;
}

/**
 * Sets the {@code subGUI}.  When a {@code subGUI} exists, rendering and user input are forwarded to it.
 * @param subGUI the {@code subGUI}, or {@code null} for no {@code subGUI}
 */
public void setSubGUI(ClickGUI subGUI) {
	if(!Objects.equals(this.subGUI, subGUI)) subMenus.closeNext();
	this.subGUI = subGUI;
}

/**
 * Removes the {@code subGUI}.
 * This method is equivalent to the following:
 * <blockquote>
 * {@code setSubGUI(null)}
 * </blockquote>
 * @see #setSubGUI(ClickGUI)
 */
public final void closeSubGUI() {
	setSubGUI(null);
}

/**
 * Gets the contained instance of {@link Minecraft}.
 * This is null when the {@code ClickGUI} is constructed but given a value when
 * {@link #setWorldAndResolution(Minecraft, int, int)} is called.
 * @return
 */
public Minecraft getMinecraft() {
	return mc;
}

/**
 * Gets the width of the GUI.
 * @return the width of the GUI
 */
public int getWidth() {
	return width;
}

/**
 * Gets the height of the GUI.
 * @return the height of the GUI
 */
public int getHeight() {
	return height;
}

}