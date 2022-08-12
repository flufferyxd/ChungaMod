package magnileve.chungamod.gui;

import java.util.Iterator;
import java.util.List;

import org.lwjgl.input.Mouse;

import magnileve.chungamod.util.Permit;
import magnileve.chungamod.util.function.BiIntConsumer;
import net.minecraft.util.math.Vec3i;

/**
 * A menu that can be moved by clicking on its top and dragging.
 * @author Magnileve
 */
public class MovableMenu extends MenuImpl {

private final Permit<BiIntConsumer> mousePermit;
private final BiIntConsumer onNewPosition;

private BiIntConsumer mouseHandler;
private int[] xOffsets;
private int[] yOffsets;

/**
 * Creates a new menu.
 * @param id button ID
 * @param x x position
 * @param y y position
 * @param buttonWidth width of one row of buttons
 * @param buttonHeight standard button height
 * @param rendererFactory factory to build renderer for this button
 * @param menuChain menu chain link of this menu
 * @param buttons initial list of buttons
 * @param properties provides values for {@code MenuProperties}
 * @param onNewPosition called when this menu has been dragged to a new position
 * @param mousePermit permit for handling mouse activity
 */
public MovableMenu(int id, int x, int y, int buttonWidth, int buttonHeight, ButtonRendererFactory<ClickGUIButton> rendererFactory,
		MenuChain menuChain, List<ClickGUIButton> buttons, MenuProperties properties,
		BiIntConsumer onNewPosition, Permit<BiIntConsumer> mousePermit) {
	this(id, x, y, buttonWidth, buttonHeight, rendererFactory, menuChain, buttons,
			properties.getDividerSize(), properties.getScrollProperties(), onNewPosition, mousePermit);
}

/**
 * Creates a new menu.
 * @param id button ID
 * @param x x position
 * @param y y position
 * @param buttonWidth width of one row of buttons
 * @param buttonHeight standard button height
 * @param rendererFactory factory to build renderer for this button
 * @param menuChain menu chain link of this menu
 * @param buttons initial list of buttons
 * @param dividerSize size of the space intended to be between borders of buttons
 * @param scrollProperties values of properties for scrolling
 * @param onNewPosition called when this menu has been dragged to a new position
 * @param mousePermit permit for handling mouse activity
 */
public MovableMenu(int id, int x, int y, int buttonWidth, int buttonHeight, ButtonRendererFactory<ClickGUIButton> rendererFactory,
		MenuChain menuChain, List<ClickGUIButton> buttons, int dividerSize, Vec3i scrollProperties,
		BiIntConsumer onNewPosition, Permit<BiIntConsumer> mousePermit) {
	super(id, x, y, buttonWidth, buttonHeight, rendererFactory, menuChain, buttons, dividerSize, scrollProperties);
	this.mousePermit = mousePermit;
	this.onNewPosition = onNewPosition;
}

@Override
public boolean mouseClicked(int mouseX, int mouseY, int mouseButton, boolean alreadyProcessed) {
	boolean hovered = super.mouseClicked(mouseX, mouseY, mouseButton, alreadyProcessed);
	if(hovered && mouseButton == 0 && mouseY < (buttons.size() > 1 ? buttons.get(1).getY() : getY() + getHeight())) {
		if((mouseHandler = mousePermit.getIfAcquired(() -> (mouseX1, mouseY1) -> {
			if(Mouse.getEventButton() == 0 && !Mouse.getEventButtonState()) {
				mousePermit.release(mouseHandler);
				mouseHandler = null;
				xOffsets = null;
				yOffsets = null;
				onNewPosition.accept(getX(), getY());
			} else {
				setPos(mouseX1 - xOffsets[0], mouseY1 - yOffsets[0]);
				Iterator<ClickGUIButton> iter = buttons.iterator();
				for(int i = 1; i < xOffsets.length && iter.hasNext(); i++)
					iter.next().setPos(mouseX1 - xOffsets[i], mouseY1 - yOffsets[i]);
			}
		})) != null) {
			int arraySize = buttons.size() + 1;
			xOffsets = new int[arraySize];
			yOffsets = new int[arraySize];
			xOffsets[0] = mouseX - getX();
			yOffsets[0] = mouseY - getY();
			int i = 1;
			for(Iterator<ClickGUIButton> iter = buttons.iterator(); i < arraySize; i++) {
				ClickGUIButton button = iter.next();
				xOffsets[i] = mouseX - button.getX();
				yOffsets[i] = mouseY - button.getY();
			}
		}
	}
	return hovered;
}

}