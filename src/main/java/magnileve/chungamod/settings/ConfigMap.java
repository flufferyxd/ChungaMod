package magnileve.chungamod.settings;

import java.util.Map;
import java.util.function.Supplier;

import magnileve.chungamod.modules.ModuleID;

/**
 * Provides access to get and modify settings of a config.
 * Whether or not modified settings are automatically saved is determined by implementation.
 * @author Magnileve
 */
public interface ConfigMap {

/**
 * Sets a value of a setting for a module.
 * @param m the module
 * @param value the new value
 * @param settingPath path of setting
 * @throws InvalidSettingPathException if the provided setting path is invalid
 */
public void set(ModuleID<?> m, Object value, String... settingPath);

/**
 * Gets a setting for a module.
 * @param m the module
 * @param settingPath path of setting
 * @return value of setting
 * @throws UnsetSettingException if the setting does not have a value
 * @throws InvalidSettingPathException if the provided setting path is invalid
 */
public Object get(ModuleID<?> m, String... settingPath);

/**
 * Gets a setting for a module, or gets a default value if the setting does not have a value.
 * @param m the module
 * @param getDefault supplier of default value
 * @param settingPath path of setting
 * @return value of setting, or the returned value of {@code getDefault} if the setting does not have a value
 * @throws InvalidSettingPathException if the provided setting path is invalid
 */
public default Object getOrDefault(ModuleID<?> m, Supplier<?> getDefault, String... settingPath) {
	try {
		return get(m, settingPath);
	} catch(UnsetSettingException e) {
		return getDefault.get();
	}
}

/**
 * Gets and removes a setting for a module.
 * @param m the module
 * @param settingPath path of setting
 * @return previous value of setting
 * @throws UnsetSettingException if the setting does not have a value to be removed
 * @throws InvalidSettingPathException if the provided setting path is invalid
 */
public Object remove(ModuleID<?> m, String... settingPath);

/**
 * Creates a new {@link SettingTraverser} for a module with the given setting path as its root.
 * @param m the module
 * @param settingPath the root setting path of the traverser
 * @return the new {@link SettingTraverser}
 * @throws InvalidSettingPathException if the given setting path is invalid
 */
public SettingTraverser traverser(ModuleID<?> m, String... settingPath);

/**
 * Merges a map of settings into this config.
 * @param settings a map of modules to setting maps to be imported
 */
public void importSettings(Map<ModuleID<?>, Map<String, Object>> settings);

/**
 * Checks if a setting exists and has a value.
 * @param m module of setting
 * @param settingPath path of setting
 * @return {@code true} if the setting exists and has a value; {@code false} otherwise
 */
public default boolean has(ModuleID<?> m, String... settingPath) {
	try {
		get(m, settingPath);
		return true;
	} catch(InvalidSettingPathException | UnsetSettingException e) {
		return false;
	}
}

}