package magnileve.chungamod.gui;

import net.minecraft.util.math.Vec3i;

/**
 * <p>
 * A link in a chain, containing a {@link Menu}.
 * </p>
 * <p>
 * When an action is performed in a menu calling for the creation of another menu, such as right-clicking,
 * the new menu can be added to GUI by creating a new link in the menu chain.
 * When rendering, a link in the chain renders its menu and then calls the next link, if it exists, to render.
 * When handling mouse activity, a link calls the next link, if it exists, to handle the mouse activity before calling its menu.
 * </p>
 * @author Magnileve
 */
public interface MenuChain extends ClickGUIButtonBase {

/**
 * Sets the next link in the chain.
 * @param next the next link, or {@code null} for no link
 */
public void setNext(MenuChain next);

/**
 * Removes the next link in the chain if it exists.
 * This method is equivalent to the following:
 * <blockquote>
 * {@code setNext(null)}
 * </blockquote>
 * @see #setNext(MenuChain)
 */
public default void closeNext() {
	setNext(null);
}

/**
 * Sets the menu of this link.
 * @param menu the menu of this link
 * @throws IllegalStateException if called on the start of the chain
 */
public void setMenu(Menu menu);

/**
 * Gets the next link in the menu chain.
 * @return the next link, or {@code null} if this is the last link
 */
public MenuChain getNext();

/**
 * Gets the menu of this link.
 * @return the menu of this link
 * @throws IllegalStateException if called on the start of the chain
 */
public Menu getMenu();

/**
 * Gets the menu properties of this link.
 * @return the menu properties of this link
 */
public default MenuProperties getMenuProperties() {
	return getMenu();
}

/**
 * Creates and sets the next link in the chain with the given menu.
 * @param next the menu of the next link to be created
 * @return the new next link
 */
public default MenuChain makeNext(Menu next) {
	MenuChain newLink = new MenuChainImpl(next);
	setNext(newLink);
	return newLink;
}

/**
 * Creates the start of a new menu chain.
 * This link cannot have its own menu; a menu should be added by adding another link to the chain.
 * @param dividerSize value to be returned by {@code getMenuProperties().getDividerSize()}
 * @param scrollProperties value to be returned by {@code getMenuProperties().getScrollProperties()}
 * @return the start of the new menu chain
 */
public static MenuChain start(int dividerSize, Vec3i scrollProperties) {
	MenuProperties properties = new MenuProperties() {
		@Override public int getDividerSize() {return dividerSize;}
		@Override public Vec3i getScrollProperties() {return scrollProperties;}
	};
	return new MenuChain() {
		private MenuChain next;
		
		@Override
		public void draw() {
			if(next != null) next.draw();
		}
		
		@Override
		public boolean updateHovered(int mouseX, int mouseY, boolean alreadyProcessed) {
			if(next != null) return next.updateHovered(mouseX, mouseY, alreadyProcessed);
			return false;
		}
		
		@Override
		public boolean mouseClicked(int mouseX, int mouseY, int mouseButton, boolean alreadyProcessed) {
			if(next != null) {
				boolean clicked = next.mouseClicked(mouseX, mouseY, mouseButton, alreadyProcessed);
				if(!clicked && (mouseButton == 0 || mouseButton == 1)) next = null;
				return clicked;
			}
			return false;
		}
		
		@Override
		public boolean mouseScrolled(int mouseX, int mouseY, boolean up, boolean alreadyProcessed) {
			return next == null ? false : next.mouseScrolled(mouseX, mouseY, up, alreadyProcessed);
		}
		
		@Override
		public void setNext(MenuChain next) {
			this.next = next;
		}

		@Override
		public void setMenu(Menu menu) {
			throw new IllegalStateException("Cannot set menu at start of menu chain");
		}

		@Override
		public MenuChain getNext() {
			return next;
		}

		@Override
		public Menu getMenu() {
			throw new IllegalStateException("Cannot get menu at start of menu chain");
		}

		@Override
		public MenuProperties getMenuProperties() {
			return properties;
		}
	};
}

}