package magnileve.chungamod.gui;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntSupplier;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import magnileve.chungamod.Chung;
import magnileve.chungamod.Hotkeys;
import magnileve.chungamod.Tick;
import magnileve.chungamod.gui.values.ValueProcessor;
import magnileve.chungamod.modules.ChungamodPlugin;
import magnileve.chungamod.modules.Factory;
import magnileve.chungamod.modules.ModuleInfo;
import magnileve.chungamod.modules.Module;
import magnileve.chungamod.modules.ModuleID;
import magnileve.chungamod.gui.values.ArrayButton;
import magnileve.chungamod.gui.values.ModuleButton;
import magnileve.chungamod.gui.values.ValueButton;
import magnileve.chungamod.gui.values.ValueButtonFactory;
import magnileve.chungamod.settings.ConfigMap;
import magnileve.chungamod.settings.GetSetting;
import magnileve.chungamod.settings.Setting;
import magnileve.chungamod.settings.SettingTraverser;
import magnileve.chungamod.util.ArrayBuildList;
import magnileve.chungamod.util.Corner;
import magnileve.chungamod.util.KeyConflictModifier;
import magnileve.chungamod.util.Permit;
import magnileve.chungamod.util.Util;
import magnileve.chungamod.util.function.BiIntConsumer;
import magnileve.chungamod.util.json.JSONManager;
import magnileve.chungamod.util.math.Vec2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.Vec3i;

/**
 * ClickGUI module for Chungamod.
 * This GUI allows interaction with configs and settings for all modules in the {@link Chung#US} module system.
 * @author Magnileve
 */
@ChungamodPlugin(id = "CoreModules", level = ChungamodPlugin.Level.TYPE)
@ModuleInfo(name = "ClickGUI", category = "Client", description = "Mouse-based GUI for module management")
@Setting(name = "Size", 			type = BigDecimal.class,	value = "0.7", 	limits = "range:0.1,10", description = "Multiplier for GUI size")
@Setting(name = "MenuHeight", 		type = Short.class, 		value = "300", 	limits = "range:1,1000", description = "Maximum height of menus")
@Setting(name = "ScrollSpeed", 		type = Short.class, 		value = "15", 	limits = "range:-200,200", description = "Speed of scrolling")
@Setting(name = "MenuScrollTimer", 	type = Short.class, 		value = "80", 	limits = "range:0,250",
		description = "How many render updates menu scrolling overrides button scrolling")
@Setting.SubSettingsArray({
	@Setting.Sub(index = 4, name = "Buttons", value = {
		@Setting(name = "Color", 		type = Color.class, 	value = "\"#FF203040\"", 	description = "Color of buttons"),
		@Setting(name = "BorderColor", 	type = Color.class, 	value = "\"#FF406080\"", 	description = "Color of button borders"),
		@Setting(name = "TextColor", 	type = Color.class, 	value = "\"#FFFFFFFF\"", 	description = "Color of text on buttons"),
		@Setting(name = "BorderWidth", type = BigDecimal.class, value = "1", limits = "range:0,10", description = "Width of button borders"),
		@Setting(name = "BrightenFactor", type = BigDecimal.class, value = "0.4", 			description = "Factor for brightening buttons"),
		@Setting(name = "Width", 		type = Short.class, value= "108", 	limits = "range:1,720", description = "Standard button width"),
		@Setting(name = "Height", 		type = Short.class, value= "12", 	limits = "range:1,80", description = "Standard button height"),
		@Setting(name = "DividerSize", 	type = Short.class, value= "2", 	limits = "range:0,20", description = "Space between buttons")
	}),
	@Setting.Sub(index = 5, name = "Messages", value = {
		@Setting(name = "UseNext-ToMessages", 	type = Boolean.class, 	value = "true",
				description = "Displays a message next to the button being hovered over"),
		@Setting(name = "UseCornerMessages", 	type = Boolean.class,
				description = "Displays a message in the corner"),
		@Setting(name = "MessageWidth", 		type = Short.class, 	value="216", 	limits = "range:10,3840",
				description = "(With UseNext-ToMessages) Maximum width of message text"),
		@Setting(name = "DisplayCorner", 		type = Corner.class, 	value = "BOTTOM_LEFT",
				description = "(With UseCornerMessages) Corner to display messages"),
		@Setting(name = "CornerDisplayTime", 	type = Short.class, 	value = "15", 	limits = "range:0,500",
				description = "(With UseCornerMessages) Ticks to display message when not hovered"),
		@Setting(name = "CornerFadeOutTime", 	type = Short.class, 	value = "25", 	limits = "range:0,200",
				description = "(With UseCornerMessages) Ticks for message to fade out"),
	}),
	@Setting.Sub(index = 6, name = "Automatic", value = {
		@Setting(name = "CategoryNames", type = String[].class, description = "(Automatic) Stores category names for modules"),
		@Setting(name = "CategoryPositions", type = Vec2i[].class, description = "(Automatic) Stores positions for categories of modules"),
		@Setting(name = "ConfigMenuPosition", type = Vec2i.class, description = "(Automatic) Stores position of config menu")
	})
})
public class ClickGUIModule extends ClickGUI implements Module, Runnable {

private final int[] closeGUIHotkeys;

@Factory
private static ClickGUIModule newInstance(Minecraft mc,
		@GetSetting("Size") BigDecimal sizeMultiplier,
		@GetSetting("MenuHeight") Short menuHeight,
		@GetSetting("ScrollSpeed") Short scrollSpeed,
		@GetSetting("MenuScrollTimer") Short menuScrollTimer,
		@GetSetting({"Buttons", "BrightenFactor"}) BigDecimal brightenFactor,
		@GetSetting({"Buttons", "BorderWidth"}) BigDecimal buttonBorderWidth,
		@GetSetting({"Buttons", "Color"}) Color buttonColor,
		@GetSetting({"Buttons", "BorderColor"}) Color buttonBorderColor,
		@GetSetting({"Buttons", "TextColor"}) Color buttonTextColor,
		@GetSetting({"Buttons", "Width"}) Short getButtonWidth,
		@GetSetting({"Buttons", "Height"}) Short getButtonHeight,
		@GetSetting({"Buttons", "DividerSize"}) Short getDividerSize,
		@GetSetting({"Messages", "UseNext-ToMessages"}) Boolean useNextToMessages,
		@GetSetting({"Messages", "UseCornerMessages"}) Boolean useCornerMessages,
		@GetSetting("Messages") SettingTraverser messageSettings,
		@GetSetting(value = {"Automatic", "CategoryNames"}, allowNull = true) String[] categoryNames,
		@GetSetting(value = {"Automatic", "CategoryPositions"}, allowNull = true) Vec2i[] getCategoryPositions,
		@GetSetting({"Automatic", "ConfigMenuPosition"}) Optional<Vec2i> getConfigMenuPosition) {
	double sizeMultiplierValue = sizeMultiplier.doubleValue();
	Buttons.ButtonProperties buttonProperties = new Buttons.ButtonProperties(brightenFactor.doubleValue(),
			buttonColor.getRed(), buttonColor.getGreen(), buttonColor.getBlue(), buttonColor.getAlpha(),
			buttonBorderColor.getRed(), buttonBorderColor.getGreen(), buttonBorderColor.getBlue(), buttonBorderColor.getAlpha(),
			buttonTextColor.getRGB(), (float) (buttonBorderWidth.doubleValue() * sizeMultiplierValue));
	int dividerSize = getDividerSize,
			buttonWidth = getButtonWidth,
			buttonHeight = getButtonHeight;
	Vec2i configMenuPosition = getConfigMenuPosition.orElse(null);
	
	ArrayBuildList<KeyBinding> keyBindingList = Hotkeys.getKeyBindingsForCommand("toggle ClickGUI");
	KeyBinding[] keyBindings = keyBindingList.getArray();
	int keyBindingCount = keyBindingList.size();
	int[] closeGUIHotkeys = new int[keyBindingCount];
	for(int i = 0; i < keyBindingCount; i++) closeGUIHotkeys[i] = keyBindings[i].getKeyCode();
	
	Collection<ModuleID<?>> modules = Chung.US.getAllModules();
	Map<String, List<ModuleID<?>>> getCategories = putModulesIntoCategories(modules);
	if(categoryNames.length != getCategoryPositions.length) {
		categoryNames = new String[0];
		getCategoryPositions = new Vec2i[0];
	}
	if(!getConfigMenuPosition.isPresent() || getCategories.size() != categoryNames.length) {
		int categoryX = 20;
		for(String name:getCategories.keySet()) {
			int getNameIndex = 0;
			for(String checkName:categoryNames) {
				if(name.equals(checkName)) break;
				getNameIndex++;
			}
			if(getNameIndex == categoryNames.length) {
				categoryNames = Arrays.copyOf(categoryNames, categoryNames.length + 1);
				categoryNames[categoryNames.length - 1] = name;
				getCategoryPositions = Arrays.copyOf(getCategoryPositions, getCategoryPositions.length + 1);
				getCategoryPositions[getCategoryPositions.length - 1] = new Vec2i(categoryX, 20);
				categoryX += 120;
			}
		}
		ModuleID<ClickGUIModule> m = Chung.US.getModule(ClickGUIModule.class);
		if(!getConfigMenuPosition.isPresent()) {
			configMenuPosition = new Vec2i(categoryX, 20);
			Chung.US.set(m, configMenuPosition, "Automatic", "ConfigMenuPosition");
		}
		Chung.US.set(m, categoryNames, "Automatic", "CategoryNames");
		Chung.US.set(m, getCategoryPositions, "Automatic", "CategoryPositions");
	}
	Vec2i[] categoryPositions = getCategoryPositions;
	
	FontRenderer fontRenderer = mc.fontRenderer;
	IntSupplier buttonIDSupplier = Util.newIDSupplier();
	ButtonRendererFactory<ClickGUIButton> rendererFactory = Buttons.rendererFactory(fontRenderer, buttonProperties);
	UpdatableDisplayButton displayer;
	if(useCornerMessages) {
		ScaledResolution scaledResolution = new ScaledResolution(mc);
		int getWidth = (int) (scaledResolution.getScaledWidth() / sizeMultiplierValue);
		int getHeight = (int) (scaledResolution.getScaledHeight() / sizeMultiplierValue);
		if(useNextToMessages) displayer = new UpdatableDisplayButton.EmptyImpl() {
				private final UpdatableDisplayButton nextToDisplay = new ResizableDisplayButton(buttonIDSupplier.getAsInt(), 0, 0,
						messageSettings.get("MessageWidth", Short.class), buttonHeight, null, rendererFactory, dividerSize,
						false, null, fontRenderer::listFormattedStringToWidth, fontRenderer::getStringWidth).init();
				private final UpdatableDisplayButton cornerDisplay = new CornerDisplayButton(buttonIDSupplier.getAsInt(),
						getWidth, getHeight, getWidth, buttonHeight, null, rendererFactory, dividerSize,
						false, null, fontRenderer::listFormattedStringToWidth, fontRenderer::getStringWidth,
						messageSettings.get("DisplayCorner", Corner.class), messageSettings.get("CornerDisplayTime", Short.class),
						messageSettings.get("CornerFadeOutTime", Short.class)).init();
				
				@Override
				public void display(String message, int x, int y, Runnable changeNotifier) {
					update(changeNotifier);
					nextToDisplay.display(message, x, y, null);
					cornerDisplay.display(message, x, y, null);
				}
				
				@Override
				public void hide(Runnable changeNotifier) {
					update(changeNotifier);
					nextToDisplay.hide();
					cornerDisplay.hide();
				}
				
				@Override
				public void draw() {
					nextToDisplay.draw();
					cornerDisplay.draw();
				}
			};
		else displayer = new CornerDisplayButton(buttonIDSupplier.getAsInt(), getWidth, getHeight, getWidth, buttonHeight, null,
				rendererFactory, dividerSize, false, null, fontRenderer::listFormattedStringToWidth,
				fontRenderer::getStringWidth, messageSettings.get("DisplayCorner", Corner.class),
				messageSettings.get("CornerDisplayTime", Short.class), messageSettings.get("CornerFadeOutTime", Short.class)).init();
	} else if(useNextToMessages) displayer = new ResizableDisplayButton(buttonIDSupplier.getAsInt(),
			0, 0, messageSettings.get("MessageWidth", Short.class), buttonHeight, null, rendererFactory,
			dividerSize, false, null, fontRenderer::listFormattedStringToWidth, fontRenderer::getStringWidth).init();
	else displayer = new UpdatableDisplayButton.EmptyImpl();
	
	List<ClickGUIButtonBase> buttons = new ArrayList<>(getCategories.size());
	MenuChain subMenus = MenuChain.start(dividerSize, new Vec3i(menuHeight, scrollSpeed, menuScrollTimer));
	MenuProperties menuProperties = subMenus.getMenuProperties();
	JSONManager json = Chung.US.getJSONManager();
	
	ClickGUIModule instance = new ClickGUIModule(mc, buttons, subMenus, displayer, new Permit<>(), new Permit<>(),
			sizeMultiplierValue, closeGUIHotkeys);
	ValueButtonFactory factory = Buttons.defaultValueButtonFactory(instance, buttonIDSupplier, json, rendererFactory);
	getCategoryMenus(buttons, instance, getCategories, categoryNames, categoryPositions, Chung.US,
			json, factory, null, null, null, buttonIDSupplier, buttonWidth, buttonHeight, rendererFactory, menuProperties);
	
	Menu configMenu = new MovableMenu(buttonIDSupplier.getAsInt(), configMenuPosition.getX(), configMenuPosition.getY(),
			buttonWidth, buttonHeight, rendererFactory, subMenus, new ArrayList<>(), menuProperties,
			(x, y) -> Chung.US.set(Chung.US.getModule(ClickGUIModule.class), new Vec2i(x, y),
			"Automatic", "ConfigMenuPosition"), instance.getMousePermit()).init();
	Set<String> availableConfigs = Chung.US.getAvailableConfigs();
	String[] configs = Chung.SYSTEM.getProperty(Chung.CONFIGURATIONS_PROPERTY).split(",");
	if(configs.length == 1 && configs[0].isEmpty()) configs = Util.STRING_ARRAY_0;
	for(String config:configs) availableConfigs.add(config);
	ClickGUIButton[] array = new ClickGUIButton[availableConfigs.size() + 3];
	array[0] = configMenu.newHeader("Configs");
	array[1] = new ClickGUIButtonImpl(buttonIDSupplier.getAsInt(), 0, 0, buttonWidth, buttonHeight, "Reload", rendererFactory) {
		@Override
		protected void onClick(int mouseButton) {
			if(mouseButton == 0) {
				ModuleID<ClickGUIModule> m = Chung.US.getModule(ClickGUIModule.class);
				String[] on = Chung.US.settingPathOn();
				int mouseX = Mouse.getX();
				int mouseY = Mouse.getY();
				Chung.US.set(m, false, on);
				Chung.US.reloadSettings();
				Chung.US.set(m, true, on);
				Mouse.setCursorPosition(mouseX, mouseY);
			}
		}
	}.init();
	array[2] = new ArrayButton<String>(buttonIDSupplier.getAsInt(), 0, 0, buttonWidth, buttonHeight, "Enabled", rendererFactory, displayer,
			configs, new ValueProcessor<String[]>() {
		@Override
		public String[] processNewValue(String[] newValue) throws IllegalArgumentException {
			json.testLimits(newValue, String[].class, "matches:[^,]++");
			Chung.SYSTEM.setProperty(Chung.CONFIGURATIONS_PROPERTY, Util.inverseSplit(newValue, ","));
			return newValue;
		}
		
		@Override
		public String[] processDefaultValue() {
			Chung.SYSTEM.setProperty(Chung.CONFIGURATIONS_PROPERTY, "");
			return Util.STRING_ARRAY_0;
		}
	}, null, subMenus, buttonIDSupplier, null, String.class, instance, json, "matches:[^,]++", factory).init();
	int i = 3;
	for(String config:Util.sort(availableConfigs)) array[i++] = new ConfigButton(buttonIDSupplier.getAsInt(), 0, 0,
			buttonWidth, buttonHeight, config, rendererFactory, Chung.US, instance, modules, json, factory, categoryNames,
			categoryPositions, buttonIDSupplier, menuProperties, Util.indexOf(configs, config) >= 0).init();
	configMenu.buttons().addAll(Arrays.asList(array));
	buttons.add(configMenu);
	
	return instance;
}

private ClickGUIModule(Minecraft mc, List<ClickGUIButtonBase> buttons, MenuChain subMenus, UpdatableDisplayButton displayer,
		Permit<BiIntConsumer> mousePermit, Permit<Runnable> keyboardPermit, double sizeMultiplier, int[] closeGUIHotkeys) {
	super(buttons, subMenus, displayer, mousePermit, keyboardPermit, null, sizeMultiplier);
	this.closeGUIHotkeys = closeGUIHotkeys;
	this.mc = mc;
}

/**
 * Creates menus for each category of module, which allow interaction with settings for each module.
 * @param buttons adds menus to this list
 * @param instance instance of {@link ClickGUI} that the menus are being added to
 * @param categories maps category names to lists of modules in each category
 * @param categoryNames contains each category name to make a menu for
 * @param categoryPositions contains the position for each menu in the same index in {@code categoryNames}
 * @param config the config to be accessed
 * @param json instance of {@link JSONManager}
 * @param factory factory to create instances of {@link ValueButton}
 * @param onNewSetting produces listeners for new settings of modules
 * @param defaultGetter gets the default value of a setting
 * @param menuButtonBuilder adds buttons to menus for modules
 * @param buttonIDSupplier generates button IDs
 * @param buttonWidth standard button width
 * @param buttonHeight standard button height
 * @param rendererFactory builds renderers for buttons
 * @param menuProperties properties of menus
 */
public static void getCategoryMenus(List<ClickGUIButtonBase> buttons, ClickGUI instance, Map<String, List<ModuleID<?>>> categories,
		String[] categoryNames, Vec2i[] categoryPositions, ConfigMap config, JSONManager json, ValueButtonFactory factory,
		BiFunction<ModuleButton, Menu, Consumer<String[]>> onNewSetting, BiFunction<Setting, Class<?>, Object> defaultGetter,
		Function<ValueButton<Boolean>, MenuButtonBuilder> menuButtonBuilder, IntSupplier buttonIDSupplier,
		int buttonWidth, int buttonHeight, ButtonRendererFactory<ClickGUIButton> rendererFactory, MenuProperties menuProperties) {
	for(Map.Entry<String, List<ModuleID<?>>> e:categories.entrySet()) {
		String name = e.getKey();
		int getNameIndex = 0;
		for(String checkName:categoryNames) {
			if(name.equals(checkName)) break;
			getNameIndex++;
		}
		int nameIndex = getNameIndex;
		
		List<ModuleID<?>> list = e.getValue();
		list.sort(null);
		ClickGUIButton[] menuButtons = new ClickGUIButton[list.size() + 1];
		Menu menu = new MovableMenu(buttonIDSupplier.getAsInt(), categoryPositions[nameIndex].getX(), categoryPositions[nameIndex].getY(),
				buttonWidth, buttonHeight, rendererFactory, instance.getSubMenus(), new ArrayList<>(menuButtons.length),
				menuProperties, (x, y) -> {
					categoryPositions[nameIndex] = new Vec2i(x, y);
					Chung.US.set(Chung.US.getModule(ClickGUIModule.class), categoryPositions, "Automatic", "CategoryPositions");
				}, instance.getMousePermit()).init();
		menuButtons[0] = menu.newHeader(name);
		int h = 1;
		for(ModuleID<?> m:list) if(defaultGetter != null || config.traverser(m).size() != 0)
			menuButtons[h++] = new ModuleButton(buttonIDSupplier.getAsInt(), 0, 0, buttonWidth, buttonHeight, rendererFactory,
					instance.getDisplayer(), menuButtonBuilder, instance.getSubMenus(), buttonIDSupplier, m, config, instance, json, factory,
					onNewSetting == null ? null : button -> onNewSetting.apply(button, menu), defaultGetter).init();
		if(h != 1) {
			menu.buttons().addAll(Arrays.asList(h == menuButtons.length ? menuButtons : Arrays.copyOf(menuButtons, h)));
			buttons.add(menu);
		}
	}
}

/**
 * Sorts modules by their categories.
 * @param modules the modules to sort
 * @return a map of categories to lists of modules in each category
 */
public static Map<String, List<ModuleID<?>>> putModulesIntoCategories(Collection<ModuleID<?>> modules) {
	Map<String, List<ModuleID<?>>> categories = new HashMap<>();
	for(ModuleID<?> m:modules) categories.computeIfAbsent(m.getCategory(), k -> new ArrayList<>()).add(m);
	return categories;
}

@Override
public void run() {
	mc.displayGuiScreen(this);
}

@Override
public void onGuiClosed() {
	if((boolean) get(Setting.ON)) selfDisable();
}

@Override
protected void keyTyped(char typedChar, int keyCode) throws IOException {
	super.keyTyped(typedChar, keyCode);
	for(int checkKeyCode:closeGUIHotkeys) if(keyCode == checkKeyCode) {
		for(KeyBinding keyBinding:Hotkeys.getKeyBindingsForCommand("toggle ClickGUI")) if(keyBinding.getKeyCode() == keyCode) {
			KeyConflictModifier keyConflict = new KeyConflictModifier(keyBinding, KeyConflictModifier.INACTIVE_KEYBIND);
			Tick.MAIN.add(() -> {
				if(Keyboard.isKeyDown(keyCode)) return 1;
				keyConflict.revert();
				KeyBinding.setKeyBindState(keyCode, false);
				return -1;
			}, 1);
			break;
		};
		selfDisable();
		break;
	}
}

@Override
public void onNewSetting(String[] settingPath, Object value) {
	if(!settingPath[0].equals("Automatic")) restart();
}

@Override
public void disable() {
	mc.displayGuiScreen(null);
}

}