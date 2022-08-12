package magnileve.chungamod.gui;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;

/**
 * Contains methods to render a button and its text separately.
 * @author Magnileve
 */
public interface ButtonRenderer {

/**
 * Draws this button.
 * @param tessellator instance of tessellator
 * @param buffer rendering buffer
 */
public void drawButton(Tessellator tessellator, BufferBuilder buffer);

/**
 * Draws text over this button.
 */
public void drawText();

/**
 * Renders nothing.
 */
public static final ButtonRenderer BLANK = new ButtonRenderer() {
	@Override public void drawButton(Tessellator tessellator, BufferBuilder buffer) {}
	@Override public void drawText() {}
};

}