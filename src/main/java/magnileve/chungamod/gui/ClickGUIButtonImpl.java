package magnileve.chungamod.gui;

import java.util.Objects;

import org.lwjgl.opengl.GL11;

import com.google.common.base.MoreObjects;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;

/**
 * Basic implementation of {@link ClickGUIButton}.
 * {@link #init()} must be called before a new button can be rendered unless specified otherwise by a subclass.
 * @author Magnileve
 */
public abstract class ClickGUIButtonImpl extends GuiButton implements ClickGUIButton {

/**
 * Factory to build renderer for this button.
 */
protected final ButtonRendererFactory<ClickGUIButton> rendererFactory;

private String name;
private ButtonRenderer renderer;

/**
 * Creates a new button.
 * @param id button ID
 * @param x x position
 * @param y y position
 * @param widthIn width
 * @param heightIn height
 * @param name name of button; also the display string unless used differently by a subclass
 * @param rendererFactory factory to build renderer for this button
 * @see #init()
 */
public ClickGUIButtonImpl(int id, int x, int y, int widthIn, int heightIn, String name, ButtonRendererFactory<ClickGUIButton> rendererFactory) {
	super(id, x, y, widthIn, heightIn, name);
	this.name = name;
	this.rendererFactory = rendererFactory;
}

/**
 * Initializes this button.
 * @return this button
 */
public ClickGUIButtonImpl init() {
	updateRenderer();
	return this;
}

/**
 * Called when this button has been clicked.
 * @param mouseButton the mouse button clicked
 */
protected abstract void onClick(int mouseButton);

@Override
public void draw() {
	preButtonRender();
	Tessellator tessellator = Tessellator.getInstance();
	drawButton(tessellator, tessellator.getBuffer());
	postButtonRender();
	drawText();
}

@Override
public void drawButton(Tessellator tessellator, BufferBuilder buffer) {
	renderer.drawButton(tessellator, buffer);
}

@Override
public void drawText() {
	renderer.drawText();
}

/**
 * Rebuilds this button's renderer to account for any changes since the last build.
 */
protected void updateRenderer() {
	renderer = rendererFactory.buildRenderer(this);
}

@Override
public boolean updateHovered(int mouseX, int mouseY, boolean alreadyProcessed) {
	boolean nowHovered = !alreadyProcessed && mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
	if(hovered != nowHovered) {
		hovered = nowHovered;
		updateRenderer();
	}
	return nowHovered;
}

@Override
public boolean mouseClicked(int mouseX, int mouseY, int mouseButton, boolean alreadyProcessed) {
	boolean hovered = updateHovered(mouseX, mouseY, alreadyProcessed);
	if(hovered) onClick(mouseButton);
	return hovered;
}

@Override
public boolean mouseScrolled(int mouseX, int mouseY, boolean up, boolean alreadyProcessed) {
	return false;
}

@Override
public void setDisplayString(String displayString) {
	name = displayString;
	this.displayString = rendererFactory.trim(this, displayString);
	updateRenderer();
}

@Override
public String getDisplayString() {
	return displayString;
}

@Override
public String getName() {
	return name;
}

@Override
public int getX() {
	return x;
}

@Override
public int getY() {
	return y;
}

@Override
public void setX(int x) {
	this.x = x;
	updateRenderer();
}

@Override
public void setY(int y) {
	this.y = y;
	updateRenderer();
}

@Override
public void setPos(int x, int y) {
	this.x = x;
	this.y = y;
	updateRenderer();
}

@Override
public int getWidth() {
	return width;
}

@Override
public int getHeight() {
	return height;
}

@Override
public boolean isHovered() {
	return hovered;
}

@Override
public boolean equals(Object obj) {
	return obj instanceof ClickGUIButton ? Objects.equals(getName(), ((ClickGUIButton) obj).getName()) : false;
}

@Override
public String toString() {
	return MoreObjects.toStringHelper(this).add("name", getName()).add("x", getX()).add("y", getY()).toString();
}

/**
 * Indicates if the name of this button is not fully visible in the display string.
 * @return {@code true} if not all characters of this button's name are in the display string; {@code false} otherwise 
 */
protected boolean isNameTrimmed() {
	String name = getName();
	return name == null ? false : !name.equals(displayString);
}

/**
 * Prepares OpenGL states for rendering buttons.
 */
public static void preButtonRender() {
	GlStateManager.disableTexture2D();
	GlStateManager.enableBlend();
	GlStateManager.disableAlpha();
	GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
			GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
			GlStateManager.SourceFactor.ONE,
			GlStateManager.DestFactor.ZERO);
	GlStateManager.shadeModel(GL11.GL_SMOOTH);
}

/**
 * Returns OpenGL states for rendering buttons back to their normal values.
 */
public static void postButtonRender() {
	GlStateManager.shadeModel(GL11.GL_FLAT);
	GlStateManager.disableBlend();
	GlStateManager.enableAlpha();
	GlStateManager.enableTexture2D();
}

}