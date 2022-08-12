package magnileve.chungamod.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;

import org.json.JSONTokener;

import magnileve.chungamod.Chung;
import magnileve.chungamod.gui.values.ModuleButton;
import magnileve.chungamod.gui.values.ValueButtonFactory;
import magnileve.chungamod.modules.ModuleID;
import magnileve.chungamod.settings.ConfigBuffer;
import magnileve.chungamod.settings.SettingInfo;
import magnileve.chungamod.settings.SettingManager;
import magnileve.chungamod.settings.SettingMapper;
import magnileve.chungamod.settings.SettingTraverser;
import magnileve.chungamod.settings.SettingUtil;
import magnileve.chungamod.util.ArrayBuildList;
import magnileve.chungamod.util.json.JSONManager;
import magnileve.chungamod.util.math.Vec2i;

/**
 * A button that can create a new {@link ConfigBuffer} and GUI to access it.
 * @author Magnileve
 */
public class ConfigButton extends HoverInfoButton {

private final SettingMapper settings;
private final ClickGUI clickGUI;
private final Collection<ModuleID<?>> modules;
private final JSONManager json;
private final ValueButtonFactory factory;
private final IntSupplier buttonIDSupplier;
private final String[] categoryNames;
private final Vec2i[] categoryPositions;
private final MenuProperties menuProperties;
private final boolean enabled;

/**
 * Creates a new button for a config.
 * @param id button ID
 * @param x x position
 * @param y y position
 * @param widthIn width
 * @param heightIn height
 * @param config name of config
 * @param rendererFactory factory to build renderer for this button
 * @param settings used to access config
 * @param clickGUI GUI of this button
 * @param modules all modules the config can have settings for
 * @param json instance of {@code JSONManager}
 * @param factory factory to build setting buttons
 * @param categoryNames contains each category name to make a menu for
 * @param categoryPositions contains the position for each menu in the same index in {@code categoryNames}
 * @param buttonIDSupplier generates button IDs
 * @param menuProperties properties of menus
 * @param enabled if this config is currently enabled
 */
public ConfigButton(int id, int x, int y, int widthIn, int heightIn, String config, ButtonRendererFactory<ClickGUIButton> rendererFactory,
		SettingMapper settings, ClickGUI clickGUI, Collection<ModuleID<?>> modules, JSONManager json, ValueButtonFactory factory,
		String[] categoryNames, Vec2i[] categoryPositions, IntSupplier buttonIDSupplier, MenuProperties menuProperties, boolean enabled) {
	super(id, x, y, widthIn, heightIn, config, rendererFactory, clickGUI.getDisplayer());
	this.settings = settings;
	this.clickGUI = clickGUI;
	this.modules = modules;
	this.json = json;
	this.factory = factory;
	this.buttonIDSupplier = buttonIDSupplier;
	this.categoryNames = categoryNames;
	this.categoryPositions = categoryPositions;
	this.menuProperties = menuProperties;
	this.enabled = enabled;
}

/**
 * When left of right clicked, loads a {@link ConfigBuffer} and creates a GUI to access it.
 * If left clicked, the GUI contains all modules and settings.
 * If right clicked, the GUI contains only settings with a value in the config buffer.
 */
@Override
protected void onClick(int mouseButton) {
	if(mouseButton == 0 || mouseButton == 1) newSubGUI(settings.loadConfig(getName()), mouseButton == 0);
}

/**
 * Gets if this config is currently enabled.
 * @return {@code true} if the config represented by this button is currently enabled; {@code false} otherwise
 */
public boolean isEnabled() {
	return enabled;
}

/**
 * Creates a new GUI for accessing a {@link ConfigBuffer} and sets it as the {@code subGUI} of this button's GUI.
 * @param config the config buffer to access
 * @param viewAll {@code true} if all modules and settings should be visible;
 * {@code false} if only settings with values should be visible
 */
private void newSubGUI(ConfigBuffer config, boolean viewAll) {
	List<ClickGUIButtonBase> menus = new ArrayList<>(8);
	ClickGUI subGUI = newSubGUI(menus, config);
	if(viewAll) viewAll(subGUI, menus, config);
	else viewSet(subGUI, menus, config);
	menus.add(getConfigMenu(subGUI, config, viewAll));
}

/**
 * Creates a new {@link ClickGUI} that saves a {@link ConfigBuffer} when closed.
 * @param menus the buttons of the GUI
 * @param config the config buffer to save when closed
 * @return a new {@code ClickGUI}
 */
private ClickGUI newSubGUI(List<ClickGUIButtonBase> menus, ConfigBuffer config) {
	ClickGUI subGUI = new ClickGUI(menus, clickGUI.getSubMenus(), clickGUI.getDisplayer(),
			clickGUI.getMousePermit(), clickGUI.getKeyboardPermit(), () -> {
				config.save();
				clickGUI.closeSubGUI();
			}, clickGUI.getSizeMultiplier());
	subGUI.setWorldAndResolution(clickGUI.getMinecraft(), clickGUI.getWidth(), clickGUI.getHeight());
	clickGUI.setSubGUI(subGUI);
	return subGUI;
}

/**
 * Makes a new menu containing buttons to save, cancel, and change views of the config.
 * @param clickGUI the config GUI
 * @param config the config buffer
 * @param viewAll if all modules and settings are currently visible
 * @return a menu with buttons
 */
private Menu getConfigMenu(ClickGUI clickGUI, ConfigBuffer config, boolean viewAll) {
	Vec2i configMenuPosition = (Vec2i) Chung.US.get(Chung.US.getModule(ClickGUIModule.class), "Automatic", "ConfigMenuPosition");
	Menu configMenu = new MovableMenu(buttonIDSupplier.getAsInt(), 
			configMenuPosition.getX(), configMenuPosition.getY(), getWidth(), getHeight(), rendererFactory,
			clickGUI.getSubMenus(), new ArrayList<>(), menuProperties,
			(x, y) -> Chung.US.set(Chung.US.getModule(ClickGUIModule.class), new Vec2i(x, y),
			"Automatic", "ConfigMenuPosition"), clickGUI.getMousePermit()).init();
	configMenu.buttons().addAll(Arrays.asList(configMenu.newHeader("Config: " + getName()),
			new ClickGUIButtonImpl(buttonIDSupplier.getAsInt(), 0, 0, getWidth(), getHeight(), "Save", rendererFactory) {
		@Override
		protected void onClick(int mouseButton) {
			if(mouseButton == 0) {
				config.save();
				ConfigButton.this.clickGUI.closeSubGUI();
			}
		}
	}, new ClickGUIButtonImpl(buttonIDSupplier.getAsInt(), 0, 0, getWidth(), getHeight(), "Cancel", rendererFactory) {
		@Override
		protected void onClick(int mouseButton) {
			if(mouseButton == 0) ConfigButton.this.clickGUI.closeSubGUI();
		}
	}, new ClickGUIButtonImpl(buttonIDSupplier.getAsInt(), 0, 0, getWidth(), getHeight(),
			"View: " + (viewAll ? "All" : "Set"), rendererFactory) {
		@Override
		protected void onClick(int mouseButton) {
			if(mouseButton == 0) newSubGUI(config, !viewAll);
		}
	}));
	return configMenu;
}

/**
 * Creates menus of all modules and all of their settings for the given config.
 * @param clickGUI GUI of menu
 * @param menus adds created menus to this list
 * @param config the config buffer
 */
private void viewAll(ClickGUI clickGUI, List<ClickGUIButtonBase> menus, ConfigBuffer config) {
	ClickGUIModule.getCategoryMenus(menus, clickGUI, ClickGUIModule.putModulesIntoCategories(modules),
			categoryNames, categoryPositions, config, json, factory, null,
			(setting, type) -> json.deserialize(new JSONTokener(SettingUtil.prepare(setting.value(), type)), type, setting.limits()),
			button -> menuButtonBuilder((ModuleButton) button, config), buttonIDSupplier,
			getWidth(), getHeight(), rendererFactory, menuProperties);
}

/**
 * Creates a menu button builder to add a button that imports settings to a config buffer from another config.
 * @param button the module button
 * @param config the config buffer
 * @return a menu button builder
 */
private MenuButtonBuilder menuButtonBuilder(ModuleButton button, ConfigBuffer config) {
	return (builder, menu, menuChain) -> builder.add(new ClickGUIButtonImpl(buttonIDSupplier.getAsInt(),
			0, 0, getWidth(), getHeight(), "Import", rendererFactory) {
		@Override
		public boolean mouseClicked(int mouseX, int mouseY, int mouseButton, boolean alreadyProcessed) {
			boolean hovered = super.mouseClicked(mouseX, mouseY, mouseButton, alreadyProcessed);
			if(hovered && mouseButton == 1) {
				MenuChain newLink = new MenuChainImpl();
				Menu menu = new MenuImpl(buttonIDSupplier.getAsInt(), mouseX, mouseY, getWidth(), getHeight(), rendererFactory,
						newLink, new ArrayList<>(0), menuProperties).init();
				Collection<String> configs = Chung.US.getAvailableConfigs();
				List<ClickGUIButton> builder = new ArrayBuildList<>(new ClickGUIButton[configs.size()]);
				for(String importName:configs) builder.add(new ClickGUIButtonImpl(buttonIDSupplier.getAsInt(),
						0, 0, getWidth(), getHeight(), importName, rendererFactory) {
					@Override
					protected void onClick(int mouseButton) {
						if(mouseButton == 0) importConfig(importName, config, button, menuChain);
					}
				});
				menu.buttons().addAll(builder);
				newLink.setMenu(menu);
				menuChain.setNext(newLink);
			} else if(alreadyProcessed) hideDisplayedMessage();
			return hovered;
		}
		
		@Override
		protected void onClick(int mouseButton) {
			if(mouseButton == 0) importConfig(SettingManager.TEMPORARY_CONFIG, config, button, menuChain);
		}
	});
}

/**
 * Imports module settings from another config into a {@link ConfigBuffer}.
 * @param configName name of config to be imported
 * @param configBuffer buffer to import config to
 * @param button the module button
 * @param menuChain the menu chain link of the module settings menu
 */
private void importConfig(String configName, ConfigBuffer configBuffer, ModuleButton button, MenuChain menuChain) {
	Map<ModuleID<?>, Map<String, Object>> settingMap = new HashMap<>(8);
	settings.loadConfig(configName, settingMap);
	ModuleID<?> m = button.getModule();
	Map<String, Object> moduleSettings = SettingManager.TEMPORARY_CONFIG.equals(configName) ?
			Chung.US.traverser(m).toMap(SettingInfo.getTree(m), HashMap::new) : settingMap.get(m);
	if(moduleSettings != null) {
		Map<ModuleID<?>, Map<String, Object>> moduleSettingMap = new HashMap<>(1);
		moduleSettingMap.put(m, moduleSettings);
		configBuffer.importSettings(moduleSettingMap);
		Menu menu = menuChain.getMenu();
		List<ClickGUIButton> buttons = menu.buttons();
		buttons.clear();
		ArrayBuildList<ClickGUIButton> builder = new ArrayBuildList<>(new ClickGUIButton[1]);
		button.addMenuEntries(builder, menu, menuChain);
		buttons.addAll(builder);
	}
}

/**
 * Creates menus of modules with any settings containing values for the given config.
 * @param clickGUI GUI of menu
 * @param menus adds created menus to this list
 * @param config the config buffer
 */
private void viewSet(ClickGUI clickGUI, List<ClickGUIButtonBase> menus, ConfigBuffer config) {
	ClickGUIModule.getCategoryMenus(menus, clickGUI, ClickGUIModule.putModulesIntoCategories(modules),
			categoryNames, categoryPositions, config, json, factory, (button, menu) -> settingPath -> {
				SettingTraverser settings = config.traverser(button.getModule());
				if(settings.size() == 0) menu.buttons().remove(button);
				else {
					List<ClickGUIButton> buttons = clickGUI.getSubMenus().getNext().getMenu().buttons();
					int l = settingPath.length - 1;
					for(int i = 0; i < l; i++) {
						String setting = settingPath[i];
						settings.subSettings(setting);
						if(settings.size() == 0) removeButton(buttons, setting);
					}
					if(!settings.has(settingPath[l])) removeButton(buttons, settingPath[l]);
				}
			}, null, null, buttonIDSupplier, getWidth(), getHeight(), rendererFactory, menuProperties);
}

/**
 * Removes a button with the given name from a list, if one exists.
 * @param buttons a list of buttons
 * @param name a button name
 */
private static void removeButton(List<ClickGUIButton> buttons, String name) {
	Iterator<ClickGUIButton> iter = buttons.iterator();
	while(iter.hasNext()) if(name.equals(iter.next().getName())) {
		iter.remove();
		break;
	}
}

}