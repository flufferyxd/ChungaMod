package magnileve.chungamod.gui;

/**
 * Basic implementation of {@link MenuChain}.
 * @author Magnileve
 */
public class MenuChainImpl implements MenuChain {

private MenuChain next;
private Menu menu;

/**
 * Creates a new menu chain link containing the given menu.
 * @param menu this link's menu
 */
public MenuChainImpl(Menu menu) {
	this.menu = menu;
}

/**
 * Creates a new menu chain link not yet containing a menu.
 * This constructor is equivalent to the following:
 * <blockquote>
 * {@code MenuChainImpl(null)}
 * </blockquote>
 * @see #MenuChainImpl(Menu)
 */
public MenuChainImpl() {
	this(null);
}

@Override
public void draw() {
	menu.draw();
	if(next != null) next.draw();
}

@Override
public boolean updateHovered(int mouseX, int mouseY, boolean alreadyProcessed) {
	if(next != null) alreadyProcessed = next.updateHovered(mouseX, mouseY, alreadyProcessed) || alreadyProcessed;
	alreadyProcessed = menu.updateHovered(mouseX, mouseY, alreadyProcessed) || alreadyProcessed;
	return alreadyProcessed;
}

@Override
public boolean mouseClicked(int mouseX, int mouseY, int mouseButton, boolean alreadyProcessed) {
	boolean nextClicked = false;
	if(next != null) {
		nextClicked = next.mouseClicked(mouseX, mouseY, mouseButton, alreadyProcessed);
		if(nextClicked) alreadyProcessed = true;
		else if(mouseButton == 0 || mouseButton == 1) closeNext();
	}
	return menu.mouseClicked(mouseX, mouseY, mouseButton, alreadyProcessed) || nextClicked;
}

@Override
public boolean mouseScrolled(int mouseX, int mouseY, boolean up, boolean alreadyProcessed) {
	boolean nextScrolled = next == null ? false : next.mouseScrolled(mouseX, mouseY, up, alreadyProcessed) || alreadyProcessed;
	return menu.mouseScrolled(mouseX, mouseY, up, alreadyProcessed || nextScrolled) || nextScrolled;
}

@Override
public void setNext(MenuChain next) {
	this.next = next;
}

@Override
public void setMenu(Menu menu) {
	this.menu = menu;
}

@Override
public MenuChain getNext() {
	return next;
}

@Override
public Menu getMenu() {
	return menu;
}

}