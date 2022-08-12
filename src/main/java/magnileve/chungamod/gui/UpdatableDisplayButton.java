package magnileve.chungamod.gui;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;

/**
 * A button intended for displaying messages.
 * @author Magnileve
 */
public interface UpdatableDisplayButton extends ClickGUIButton, UpdatableDisplay {

/**
 * Determines if a message is being displayed.
 * @return {@code true} if a message is being displayed; {@code false} otherwise
 */
public boolean isVisible();

/**
 * Does not process mouse input or display messages, but appropriately runs {@code changeNotifier} Runnables.
 * @author Magnileve
 */
public class EmptyImpl extends UpdatableDisplay.EmptyImpl implements UpdatableDisplayButton {
	/**
	 * Creates a new {@code EmptyImpl}.
	 * @param changeNotifier if not null, runs the next time {@code display} or {@code hide} is called.
	 */
	public EmptyImpl(Runnable changeNotifier) {super(changeNotifier);}
	/**
	 * Creates a new {@code EmptyImpl} with no initial {@code changeNotifier}.
	 */
	public EmptyImpl() {}
	
	@Override public void draw() {}
	@Override public boolean updateHovered(int mouseX, int mouseY, boolean alreadyProcessed) {return false;}
	@Override public boolean mouseClicked(int mouseX, int mouseY, int mouseButton, boolean alreadyProcessed) {return false;}
	@Override public boolean mouseScrolled(int mouseX, int mouseY, boolean up, boolean alreadyProcessed) {return false;}
	@Override public boolean isVisible() {return false;}
	@Override public void setDisplayString(String displayString) {}
	@Override public void drawButton(Tessellator tessellator, BufferBuilder buffer) {}
	@Override public void drawText() {}
	@Override public String getDisplayString() {return null;}
	@Override public String getName() {return null;}
	@Override public int getX() {return 0;}
	@Override public int getY() {return 0;}
	@Override public void setX(int x) {}
	@Override public void setY(int y) {}
	@Override public void setPos(int x, int y) {}
	@Override public int getWidth() {return 0;}
	@Override public int getHeight() {return 0;}
	@Override public boolean isHovered() {return false;}
}

}