package magnileve.chungamod.gui.values;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntSupplier;

import magnileve.chungamod.Chung;
import magnileve.chungamod.gui.MenuChain;
import magnileve.chungamod.gui.ButtonRendererFactory;
import magnileve.chungamod.gui.ClickGUI;
import magnileve.chungamod.gui.ClickGUIButton;
import magnileve.chungamod.gui.Menu;
import magnileve.chungamod.gui.MenuButtonBuilder;
import magnileve.chungamod.gui.UpdatableDisplay;
import magnileve.chungamod.modules.ModuleID;
import magnileve.chungamod.settings.ConfigMap;
import magnileve.chungamod.settings.Setting;
import magnileve.chungamod.settings.SettingInfo;
import magnileve.chungamod.settings.SettingTraverser;
import magnileve.chungamod.settings.UnsetSettingException;
import magnileve.chungamod.util.ArrayBuildList;
import magnileve.chungamod.util.json.JSONManager;

/**
 * A button that creates a menu to access settings of a module when right clicked.
 * @author Magnileve
 */
public class ModuleButton extends BooleanButton {

private static final String[] SETTING_PATH_ON = Chung.US.settingPathOn();

/**
 * This button's module.
 */
protected final ModuleID<?> m;
/**
 * Provides access to settings.
 */
protected final ConfigMap config;
/**
 * This button's GUI.
 */
protected final ClickGUI clickGUI;
/**
 * Manages JSON for settings.
 */
protected final JSONManager json;
/**
 * Builds setting buttons.
 */
protected final ValueButtonFactory factory;
/**
 * Listener for new settings.
 */
protected final Consumer<String[]> onNewSetting;
/**
 * Gets default values for settings without a value.
 */
protected final BiFunction<Setting, Class<?>, Object> defaultGetter;

/**
 * Creates a new module button.
 * @param id button ID
 * @param x x position
 * @param y y position
 * @param widthIn width
 * @param heightIn height
 * @param rendererFactory factory to build renderer for this button
 * @param messageDisplayer used to display messages
 * @param menuButtonBuilder if not null, is applied to this button and adds buttons for a menu being built
 * @param menuChain menu chain link of this button's menu
 * @param buttonIDSupplier generates button IDs
 * @param m this button's module
 * @param config setting access
 * @param clickGUI this button's GUI
 * @param json manages JSON for settings
 * @param factory builds setting buttons
 * @param onNewSetting if not null, is notified for every new value of any setting
 * @param defaultGetter if not null, gets default values for settings without a value
 */
public ModuleButton(int id, int x, int y, int widthIn, int heightIn, ButtonRendererFactory<ClickGUIButton> rendererFactory,
		UpdatableDisplay messageDisplayer, Function<ValueButton<Boolean>, MenuButtonBuilder> menuButtonBuilder, MenuChain menuChain,
		IntSupplier buttonIDSupplier, ModuleID<?> m, ConfigMap config, ClickGUI clickGUI, JSONManager json, ValueButtonFactory factory,
		Function<ModuleButton, Consumer<String[]>> onNewSetting, BiFunction<Setting, Class<?>, Object> defaultGetter) {
	super(id, x, y, widthIn, heightIn, m.getName(), rendererFactory, messageDisplayer, (boolean) config.getOrDefault(m, defaultGetter == null ? () -> Boolean.FALSE :
			() -> (Boolean) defaultGetter.apply(SettingInfo.getSetting(m.getModuleType(), SETTING_PATH_ON), Boolean.class),
			SETTING_PATH_ON), null, menuButtonBuilder, menuChain, buttonIDSupplier, null);
	this.m = m;
	this.config = config;
	this.clickGUI = clickGUI;
	this.json = json;
	this.factory = factory;
	this.onNewSetting = onNewSetting == null ? null : onNewSetting.apply(this);
	this.defaultGetter = defaultGetter;
}

@Override
public ModuleButton init() {
	super.init();
	return this;
}

/**
 * Gets this button's module.
 * @return this button's module
 */
public ModuleID<?> getModule() {
	return m;
}

/**
 * Sets a setting to a value.
 * @param value a new value
 * @param settingPath path of setting
 * @return the value of the setting after calling to set it
 */
protected Object set(Object value, String... settingPath) {
	config.set(m, value, settingPath);
	if(onNewSetting != null) onNewSetting.accept(settingPath);
	return config.get(m, settingPath);
}

/**
 * Sets a setting to its default value.
 * @param settingPath path of setting
 * @return the value of the setting after calling to reset it
 */
protected Object setToDefault(String... settingPath) {
	config.remove(m, settingPath);
	if(onNewSetting != null) onNewSetting.accept(settingPath);
	return config.get(m, settingPath);
}

@Override
public Boolean processNewValue(Boolean newValue) {
	return (Boolean) set(newValue, SETTING_PATH_ON);
}

@Override
public Boolean processDefaultValue() throws IllegalArgumentException {
	try {
		return (Boolean) setToDefault(SETTING_PATH_ON);
	} catch(UnsetSettingException e) {
		throw new IllegalArgumentException(e);
	}
}

@Override
public String getHoverMessage() {
	return isNameTrimmed() ? getName() + ": " + m.getDescription() : m.getDescription();
}

@Override
protected void updateDisplay() {}

@Override
public void addMenuEntries(ArrayBuildList<ClickGUIButton> builder, Menu menu, MenuChain menuChain) {
	buildSettingButtons(builder, menu, SettingInfo.getTree(m),
			new ArrayBuildList<>(new String[1]), config.traverser(m), menuChain);
	super.addMenuEntries(builder, menu, menuChain);
}

/**
 * Builds setting buttons recursively.
 * @param builder adds setting buttons to this list
 * @param menu menu of the buttons being built
 * @param nodes info of each setting
 * @param pathBuilder the parent path of each setting
 * @param settings accesses settings
 * @param menuChain menu chain of the buttons being built
 */
private void buildSettingButtons(ArrayBuildList<ClickGUIButton> builder, Menu menu, Iterable<SettingInfo> nodes,
		ArrayBuildList<String> pathBuilder, SettingTraverser settings, MenuChain menuChain) {
	int pathSize = pathBuilder.size();
	pathBuilder.add(null);
	for(SettingInfo node:nodes) {
		String name = node.getName();
		pathBuilder.set(pathSize, name);
		if(node.isTree()) {
			int size = builder.size();
			builder.add(menu.newHeader(name));
			settings.subSettings(name);
			buildSettingButtons(builder, menu, node, pathBuilder, settings, menuChain);
			settings.back();
			if(builder.size() == size + 1) builder.remove(size);
		} else {
			Setting setting = node.getSetting();
			Class<?> type = setting.type();
			Object value;
			try {
				value = get(settings, type, name, setting);
			} catch(UnsetSettingException e) {
				continue;
			}
			builder.add(settingToButtonCast(type, setting, pathBuilder.toArray(), value, menuChain));
		}
	}
	pathBuilder.remove(pathSize);
}

/**
 * Gets a value of a setting.
 * @param <T> setting type
 * @param traverser accesses the setting
 * @param type setting type
 * @param name name of setting
 * @param setting setting info
 * @return the value of this setting
 * @throws UnsetSettingException if this setting does not have a value, and {@code defaultGetter} is {@code null}
 */
private <T> T get(SettingTraverser traverser, Class<T> type, String name, Setting setting) {
	return traverser.get(name, type, true, defaultGetter == null ? null : () -> type.cast(defaultGetter.apply(setting, type)));
}

/**
 * Builds a button to access a setting.
 * @param <T> setting type
 * @param type setting type
 * @param setting setting info
 * @param settingPath path of setting
 * @param currentValue current value of setting
 * @param menuChain menu chain of this button
 * @return a new value button for this setting
 * @see #settingToButton(Class, Setting, String[], Object, MenuChain)
 */
@SuppressWarnings("unchecked")
private <T> ValueButton<T> settingToButtonCast(Class<T> type,
		Setting setting, String[] settingPath, Object currentValue, MenuChain menuChain) {
	return settingToButton(type, setting, settingPath, (T) currentValue, menuChain);
}

/**
 * Builds a button to access a setting.
 * @param <T> setting type
 * @param type setting type
 * @param setting setting info
 * @param settingPath path of setting
 * @param currentValue current value of setting
 * @param menuChain menu chain of this button
 * @return a new value button for this setting
 */
@SuppressWarnings("unchecked")
protected <T> ValueButton<T> settingToButton(Class<T> type, Setting setting, String[] settingPath, T currentValue, MenuChain menuChain) {
	String limits = setting.limits();
	String name = setting.name();
	String description = setting.description();
	SettingButtonManager<T> manager = new SettingButtonManager<>(settingPath, type, limits);
	int id = buttonIDSupplier.getAsInt();
	
	return factory.build(id, 0, 0, getWidth(), getHeight(), name, currentValue,
			type.equals(Boolean.class) && name.equals(Setting.ON) && settingPath.length == 1 ? (ValueProcessor<T>) this : manager,
			manager, menuChain, description, type, true, limits).init();
}

/**
 * Processes values and supplies reset and JSON buttons for a setting button.
 * @param <T> setting type
 * @author Magnileve
 */
private class SettingButtonManager<T> implements ValueProcessor<T>, Function<ValueButton<T>, MenuButtonBuilder> {
	private final String[] settingPath;
	private final Class<T> type;
	private final String limits;
	
	/**
	 * Creates a new {@code SettingButtonManager}.
	 * @param settingPath path of setting
	 * @param type setting type
	 * @param limits value limiter string
	 */
	private SettingButtonManager(String[] settingPath, Class<T> type, String limits) {
		this.settingPath = settingPath;
		this.type = type;
		this.limits = limits;
	}

	@Override
	public T processNewValue(T newValue) {
		json.testLimits(newValue, type, limits);
		set(newValue, settingPath);
		return newValue;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T processDefaultValue() throws IllegalArgumentException {
		try {
			return (T) setToDefault(settingPath);
		} catch(UnsetSettingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public MenuButtonBuilder apply(ValueButton<T> t) {
		return (builder, menu, menuChain) -> {
			builder.add(new SetToDefaultButton<>(buttonIDSupplier.getAsInt(), rendererFactory, messageDisplayer, t));
			if(!(t instanceof JSONButton)) builder.add(new JSONButton<>(buttonIDSupplier.getAsInt(), 0, 0, getWidth(), getHeight(),
					rendererFactory, messageDisplayer, t.getValue(), t, null, menuChain, buttonIDSupplier,
					null, clickGUI.getKeyboardPermit(), type, json, true).init());
		};
	}
}

}