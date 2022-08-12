package magnileve.chungamod.settings;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import magnileve.chungamod.modules.ModuleID;
import magnileve.chungamod.util.function.TriConsumer;

/**
 * Provides access to get and modify settings of a setting system.
 * @author Magnileve
 */
public interface SettingMapper {

/**
 * Sets a value of a setting for a module.
 * @param config name of config
 * @param m the module
 * @param value the new value
 * @param settingPath path of setting
 * @throws InvalidSettingPathException if the provided setting path is invalid
 */
public Object set(String config, ModuleID<?> m, Object value, String... settingPath);

/**
 * Gets a setting for a module.
 * @param config name of config
 * @param m the module
 * @param settingPath path of setting
 * @return value of setting
 * @throws UnsetSettingException if the setting does not have a value
 * @throws InvalidSettingPathException if the provided setting path is invalid
 */
public Object get(String config, ModuleID<?> m, String... settingPath);

/**
 * Gets a setting for a module, or gets a default value if the setting does not have a value.
 * @param config name of config
 * @param m the module
 * @param getDefault supplier of default value
 * @param settingPath path of setting
 * @return value of setting, or the returned value of {@code getDefault} if the setting does not have a value
 * @throws InvalidSettingPathException if the provided setting path is invalid
 */
public default Object getOrDefault(String config, ModuleID<?> m, Supplier<?> getDefault, String... settingPath) {
	try {
		return get(config, m, settingPath);
	} catch(UnsetSettingException e) {
		return getDefault.get();
	}
}

/**
 * Gets and removes a setting for a module.
 * @param config name of config
 * @param m the module
 * @param settingPath path of setting
 * @return previous value of setting
 * @throws UnsetSettingException if the setting does not have a value to be removed
 * @throws InvalidSettingPathException if the provided setting path is invalid
 */
public Object remove(String config, ModuleID<?> m, String... settingPath);

/**
 * Creates a new {@link SettingTraverser} for a module with the given settingPath as its root.
 * @param config name of config
 * @param m the module
 * @param settingPath the root of the traverser
 * @return the new {@link SettingTraverser}
 */
public default SettingTraverser traverser(String config, ModuleID<?> m, String... settingPath) {
	Map<ModuleID<?>, Map<String, Object>> settings = new HashMap<>();
	loadConfig(config, settings);
	return new SettingTraverser(m, settings, settingPath);
}

/**
 * Loads a saved config into a map.
 * @param config name of config
 * @param configSettings map for settings to be loaded into
 * @param onNewSetting optional listener for new or changed settings in the map
 */
public void loadConfig(String config, Map<ModuleID<?>, Map<String, Object>> configSettings,
		TriConsumer<ModuleID<?>, String[], Object> onNewSetting);

/**
 * Loads a saved config into a map.
 * @param config name of config
 * @param configSettings map for settings to be loaded into
 */
public default void loadConfig(String config, Map<ModuleID<?>, Map<String, Object>> configSettings) {
	loadConfig(config, configSettings, null);
}

/**
 * Loads a config into a new <tt>ConfigBuffer</tt>.
 * @param config name of config
 * @return a new <tt>ConfigBuffer</tt> containing the loaded config
 */
public ConfigBuffer loadConfig(String config);

}