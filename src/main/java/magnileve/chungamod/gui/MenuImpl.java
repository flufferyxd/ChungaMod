package magnileve.chungamod.gui;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.Vec3i;

/**
 * Basic implementation of {@link Menu}.
 * @author Magnileve
 */
public class MenuImpl extends DisplayButton implements Menu {

/**
 * The internal list of buttons.  The list returned by {@link #buttons()} is backed by this list.
 */
protected final List<ClickGUIButton> buttons;
/**
 * The standard button height used to make headers.
 */
protected final int buttonHeight;
/**
 * The menu chain link of this menu.
 */
protected final MenuChain menuChain;

private final int dividerSize;
private final Vec3i scrollProperties;

/**
 * When equal to or greater than zero,
 * any scrolling over a button in this menu is overridden as scrolling over this menu.
 * This value counts down every time a call is made to {@link #updateHovered(int, int, boolean)}.
 */
protected int scrollTimer;

private int scrollHeight;
private int scrollableHeight;
private int topButton;
private int bottomButton;

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
 */
public MenuImpl(int id, int x, int y, int buttonWidth, int buttonHeight, ButtonRendererFactory<ClickGUIButton> rendererFactory,
		MenuChain menuChain, List<ClickGUIButton> buttons, MenuProperties properties) {
	this(id, x, y, buttonWidth, buttonHeight, rendererFactory, menuChain, buttons,
			properties.getDividerSize(), properties.getScrollProperties());
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
 */
public MenuImpl(int id, int x, int y, int buttonWidth, int buttonHeight, ButtonRendererFactory<ClickGUIButton> rendererFactory,
		MenuChain menuChain, List<ClickGUIButton> buttons, int dividerSize, Vec3i scrollProperties) {
	super(id, x, y, buttonWidth + dividerSize * 2, 0, null, rendererFactory);
	this.buttonHeight = buttonHeight;
	this.menuChain = menuChain;
	this.buttons = buttons;
	this.dividerSize = dividerSize;
	this.scrollProperties = scrollProperties;
	scrollTimer = -1;
}

@Override
public MenuImpl init() {
	super.init();
	if(!buttons.isEmpty()) updateButtonPositions(0);
	return this;
}

@Override
public boolean updateHovered(int mouseX, int mouseY, boolean alreadyProcessed) {
	boolean hovered = super.updateHovered(mouseX, mouseY, alreadyProcessed);
	if(!hovered) alreadyProcessed = true;
	for(ClickGUIButton button:buttons.subList(topButton, bottomButton))
		alreadyProcessed = button.updateHovered(mouseX, mouseY, alreadyProcessed) || alreadyProcessed;
	if(scrollTimer >= 0 && --scrollTimer < 0) updateRenderer();
	return hovered;
}

/**
 * Draws this menu, then draws each button in this menu.
 */
@Override
public void drawButton(Tessellator tessellator, BufferBuilder buffer) {
	super.drawButton(tessellator, buffer);
	for(ClickGUIButton button:buttons.subList(topButton, bottomButton)) button.drawButton(tessellator, buffer);
}

/**
 * Draws text over each button in this menu when appropriate.
 */
@Override
public void drawText() {
	for(ClickGUIButton button:buttons.subList(topButton, bottomButton)) button.drawText();
}

@Override
public boolean mouseClicked(int mouseX, int mouseY, int mouseButton, boolean alreadyProcessed) {
	alreadyProcessed = !super.updateHovered(mouseX, mouseY, alreadyProcessed);
	List<ClickGUIButton> subList = buttons.subList(topButton, bottomButton);
	ClickGUIButton[] buttonArray = subList.toArray(new ClickGUIButton[subList.size()]);
	for(int i = buttonArray.length - 1; i >= 0; i--) alreadyProcessed = buttonArray[i].mouseClicked(
			mouseX, mouseY, mouseButton, alreadyProcessed) || alreadyProcessed;
	return hovered;
}

/**
 * Unless {@link #scrollTimer} is equal to or greater than zero, calls {@code mouseScrolled} on each button in this menu.
 * Then, if this menu is hovered over, the scroll has not been processed, and this menu's height is less than the scrollable height,
 * resets the scroll timer and scrolls through the buttons.
 */
@Override
public boolean mouseScrolled(int mouseX, int mouseY, boolean up, boolean alreadyProcessed) {
	alreadyProcessed = !super.updateHovered(mouseX, mouseY, alreadyProcessed);
	if(scrollTimer < 0) {
		List<ClickGUIButton> subList = buttons.subList(topButton, bottomButton);
		ClickGUIButton[] buttonArray = subList.toArray(new ClickGUIButton[subList.size()]);
		for(int i = buttonArray.length - 1; i >= 0; i--) alreadyProcessed = buttonArray[i].mouseScrolled(
				mouseX, mouseY, up, alreadyProcessed) || alreadyProcessed;
	}
	if(!alreadyProcessed && getScrollableHeight() != getHeight()) {
		scrollTimer = scrollProperties.getZ();
		int offset = scrollProperties.getY();
		offsetButtons(scroll(up ? offset * -1 : offset));
		updateRenderer();
	}
	return hovered;
}

@Override
public String getName() {
	if(!buttons.isEmpty()) {
		ClickGUIButton header = buttons.get(0);
		if(header instanceof Header) return header.getName();
	}
	return null;
}

@Override
public List<ClickGUIButton> buttons() {
	return new UpdatingButtonList();
}

/**
 * @throws IndexOutOfBoundsException {@inheritDoc}
 */
@Override
public void updateButtonPositions(int index) {
	if(index < 0 || index > buttons.size()) throw new IndexOutOfBoundsException(Integer.toString(index));
	if(menuChain != null) menuChain.closeNext();
	ClickGUIButton button;
	Iterator<ClickGUIButton> iter;
	int minX = getX() + dividerSize, maxX = getX() + getWidth() - dividerSize, scrollHeight = getScrollHeight(),
			buttonX, buttonY, nextYIncrease;
	if(index == 0) {
		iter = buttons.iterator();
		buttonX = minX;
		buttonY = getY() + dividerSize - scrollHeight;
		nextYIncrease = 0;
	} else {
		iter = buttons.listIterator(index - 1);
		button = iter.next();
		buttonX = button.getX() + button.getWidth() + dividerSize;
		buttonY = button.getY();
		nextYIncrease = button.getHeight();
	}
	while(iter.hasNext()) {
		button = iter.next();
		int buttonWidth = button.getWidth();
		if(buttonX + buttonWidth > maxX) {
			buttonX = minX;
			buttonY += nextYIncrease + dividerSize;
			nextYIncrease = 0;
		}
		button.setPos(buttonX, buttonY);
		int buttonHeight = button.getHeight();
		if(buttonHeight > nextYIncrease) nextYIncrease = buttonHeight;
		buttonX += buttonWidth + dividerSize;
	}
	scrollableHeight = buttonY + scrollHeight + nextYIncrease + dividerSize - getY();
	height = Math.min(scrollProperties.getX(), scrollableHeight);
	if(scrollHeight + height > scrollableHeight) offsetButtons(scroll(0));
	updateRenderSection();
	updateRenderer();
}

@Override
public ClickGUIButton newHeader(String displayString) {
	Header h = new Header();
	h.setDisplayString(displayString);
	return h;
}

@Override
public int getRemainingWidth(ClickGUIButton... buttons) {
	int remaining = getWidth() - dividerSize * 2;
	for(int i = 0; i < buttons.length; i++) remaining -= buttons[i].getWidth() + dividerSize;
	return remaining;
}

/**
 * Scrolls through this menu's buttons.
 * @param yOffset the height to scroll
 * @return the distance buttons should be offset by
 */
protected int scroll(int yOffset) {
	scrollHeight += yOffset;
	if(scrollHeight < 0) {
		yOffset -= scrollHeight;
		scrollHeight = 0;
	} else {
		int height = getHeight(), scrollableHeight = getScrollableHeight();
		if(scrollHeight + height > scrollableHeight) {
			int newTopY = scrollableHeight - height;
			yOffset += newTopY - scrollHeight;
			scrollHeight = newTopY;
		}
	}
	return yOffset * -1;
}

/**
 * Offsets buttons.
 * @param yOffset the y offset for each button
 */
protected void offsetButtons(int yOffset) {
	for(ClickGUIButton button:buttons) button.setY(button.getY() + yOffset);
	updateRenderSection();
}

/**
 * Iterates through the list of buttons and updates the cached range that should be visible.
 */
protected void updateRenderSection() {
	Iterator<ClickGUIButton> iter = buttons.iterator();
	int i = -1, y = getY(), height = getHeight();
	do {
		if(!iter.hasNext()) {
			topButton = 0;
			bottomButton = 0;
			return;
		}
		i++;
	} while(iter.next().getY() < y);
	topButton = i++;
	if(iter.hasNext()) {
		ClickGUIButton next = iter.next();
		while(next.getY() + next.getHeight() <= y + height) {
			i++;
			if(!iter.hasNext()) break;
			next = iter.next();
		}
	}
	bottomButton = i;
}

@Override
public int getDividerSize() {
	return dividerSize;
}

@Override
public Vec3i getScrollProperties() {
	return scrollProperties;
}

@Override
public int getScrollHeight() {
	return scrollHeight;
}

@Override
public int getScrollableHeight() {
	return scrollableHeight;
}

@Override
public boolean isBeingScrolled() {
	return scrollTimer >= 0;
}

/**
 * A basic menu header.  This header contains a display string and is the length of one line of buttons.
 * @author Magnileve
 */
public class Header implements ClickGUIButton {
	protected String name;
	protected String displayString;
	
	private ButtonRenderer renderer;
	private int headerY;
	
	@Override public void draw() {}
	@Override public boolean updateHovered(int mouseX, int mouseY, boolean alreadyProcessed) {return false;}
	@Override public boolean mouseClicked(int mouseX, int mouseY, int mouseButton, boolean alreadyProcessed) {return false;}
	@Override public boolean mouseScrolled(int mouseX, int mouseY, boolean up, boolean alreadyProcessed) {return false;}
	
	/**
	 * Rebuilds this button's renderer to account for any changes since the last build.
	 */
	protected void updateRenderer() {
		renderer = rendererFactory.buildRenderer(this);
	}
	
	@Override
	public void drawButton(Tessellator tessellator, BufferBuilder buffer) {
		renderer.drawButton(tessellator, buffer);
	}
	
	@Override
	public void drawText() {
		renderer.drawText();
	}
	
	@Override
	public void setDisplayString(String displayString) {
		name = displayString;
		this.displayString = rendererFactory.trim(this, displayString);
		updateRenderer();
	}
	
	@Override public String getDisplayString() {return displayString;}
	@Override public String getName() {return name;}
	@Override public int getX() {return MenuImpl.this.getX() + getDividerSize();}
	@Override public int getY() {return headerY;}
	@Override public void setX(int x) {updateRenderer();}
	@Override public void setY(int y) {this.headerY = y; updateRenderer();}
	@Override public void setPos(int x, int y) {setY(y);}
	@Override public int getWidth() {return MenuImpl.this.getWidth() - getDividerSize() * 2;}
	@Override public int getHeight() {return buttonHeight;}
	@Override public boolean isHovered() {return false;}
}

/**
 * Wraps the internal button list to include calls to
 * {@link MenuImpl#updateButtonPositions(int) updateButtonPositions(int)} when the list is modified.
 * @author Magnileve
 */
protected class UpdatingButtonList extends AbstractList<ClickGUIButton> {
	@Override
	public ClickGUIButton get(int index) {
		return buttons.get(index);
	}
	
	@Override
	public ClickGUIButton set(int index, ClickGUIButton element) {
		ClickGUIButton returnValue =  buttons.set(index, element);
		updateButtonPositions(index);
		return returnValue;
    }
	
	@Override
	public void add(int index, ClickGUIButton element) {
        buttons.add(index, element);
        updateButtonPositions(index);
    }
	
	@Override
	public ClickGUIButton remove(int index) {
		ClickGUIButton returnValue =  buttons.remove(index);
		updateButtonPositions(index);
		return returnValue;
    }
	
	@Override
	public int size() {
		return buttons.size();
	}
	
	@Override
	public boolean addAll(Collection<? extends ClickGUIButton> c) {
		int index = buttons.size();
        if(!buttons.addAll(c)) return false;
        updateButtonPositions(index);
        return true;
    }
	
	@Override
	public boolean addAll(int index, Collection<? extends ClickGUIButton> c) {
        if(!buttons.addAll(index, c)) return false;
        updateButtonPositions(index);
        return true;
    }
	
	@Override
	public void forEach(Consumer<? super ClickGUIButton> action) {
		buttons.forEach(action);
	}
}

}