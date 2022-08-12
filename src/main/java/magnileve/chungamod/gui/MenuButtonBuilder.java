package magnileve.chungamod.gui;

import magnileve.chungamod.util.ArrayBuildList;

/**
 * Adds buttons for a menu being built.
 * @author Magnileve
 */
@FunctionalInterface
public interface MenuButtonBuilder {

/**
 * Adds buttons for a menu being built.<br>
 * Note: Do not add buttons through {@link Menu#buttons()}; only add buttons to {@code builder}.
 * @param builder the list of buttons to be added to the menu
 * @param menu the menu being built
 * @param menuChain the menu chain link of the menu being built
 */
public void addMenuEntries(ArrayBuildList<ClickGUIButton> builder, Menu menu, MenuChain menuChain);

}