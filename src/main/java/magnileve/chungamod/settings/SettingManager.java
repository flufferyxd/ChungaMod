package magnileve.chungamod.settings;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import magnileve.chungamod.modules.ModuleID;
import magnileve.chungamod.util.Bucket;
import magnileve.chungamod.util.Util;
import magnileve.chungamod.util.function.TriConsumer;

/**
 * The {@code SettingManager} is used to load, save, and access settings of a setting system.
 * @author Magnileve
 */
public interface SettingManager extends SettingMapper, SettingMapFactory {

/**
 * The temporary config is used to quickly modify settings.
 * It is always the first loaded config, so its settings will always be loaded.
 * Periodically, the temporary config can be cleared of its settings, usually when reloading configs.
 */
public static final String TEMPORARY_CONFIG = "tmp";

/**
 * Saves settings for a plugin to a config.
 * @param settingsMap map of settings
 * @param config config name
 * @param pluginID plugin ID
 */
public void save(Map<ModuleID<?>, Map<String, Object>> settingsMap, String config, String pluginID);

/**
 * Loads settings from a config for the given modules.
 * @param modules maps plugins to modules of a plugin to their setting information and maps to be loaded into
 * @param config name of config
 * @param onNewSetting optional listener for changed values of settings
 */
public void loadSettings(Map<String, Map<String, Bucket<SettingInfoMap, Map<String, Object>>>> modules,
		String config, TriConsumer<ModuleID<?>, String[], Object> onNewSetting);

/**
 * Loads the default values of settings for the given modules.
 * @param modules maps plugins to modules of a plugin to their setting information and maps to be loaded into
 * @param onNewSetting optional listener for changed values of settings
 */
public void loadDefaultSettings(Map<String, Map<String, Bucket<SettingInfoMap, Map<String, Object>>>> modules,
		TriConsumer<ModuleID<?>, String[], Object> onNewSetting);

/**
 * Loads a single setting of a module, resolving the value in the same way as {@link #loadSettings(Iterable, TriConsumer)}.
 * @param m module of setting
 * @param settingPath path of setting
 * @return the loaded value of the given setting
 */
public Object loadSetting(ModuleID<?> m, String[] settingPath);

/**
 * Gets a {@link Set} of configs that should be loaded from.
 * @return a {@link Set} of configs that should be loaded from
 * @implSpec Sets usually have uncontrollable order of iteration,
 * but the set returned from this method should have a controlled order of iteration,
 * or else the order in which configs are loaded will not be controllable.
 */
public Set<String> getActiveConfigs();

/**
 * Deletes all settings stored in {@value #TEMPORARY_CONFIG}.
 */
public void clearTemporaryConfig();

/**
 * <p>
 * Loads all settings for the given modules.
 * The order of resolving values for settings, with the first found value taking priority, is as follows:
 * </p>
 * <p style="margin-left:40px">
 * - First, config {@value #TEMPORARY_CONFIG} is loaded.<br>
 * - Next, configs are loaded in the order of iteration of {@link #getActiveConfigs()}.<br>
 * - Finally, default values are loaded.
 * </p>
 * @param modules contains modules, their setting information, and their setting maps to be loaded into
 * @param onNewSetting optional listener for changed values of settings
 */
public default void loadSettings(Iterable<Bucket<ModuleID<?>, Bucket<SettingInfoMap, Map<String, Object>>>> modules,
		TriConsumer<ModuleID<?>, String[], Object> onNewSetting) {
	Map<String, Map<String, Bucket<SettingInfoMap, Map<String, Object>>>> pluginMap = new HashMap<>(4);
	SettingUtil.sortByPlugin(modules, pluginMap);
	loadSettings(pluginMap, TEMPORARY_CONFIG, onNewSetting);
	for(String config:getActiveConfigs()) loadSettings(pluginMap, config, onNewSetting);
	loadDefaultSettings(pluginMap, onNewSetting);
}

/**
 * Loads all settings for the given modules.
 * This method is equivalent to the following:
 * <blockquote>
 * {@code loadSettings(modules, null)}
 * </blockquote>
 * @param modules contains modules, their setting information, and their setting maps to be loaded into
 * @see #loadSettings(Iterable, TriConsumer)
 */
public default void loadSettings(Iterable<Bucket<ModuleID<?>, Bucket<SettingInfoMap, Map<String, Object>>>> modules) {
	loadSettings(modules, null);
}

/**
 * @throws InvalidSettingPathException {@inheritDoc}
 */
@Override
public default Object set(String config, ModuleID<?> m, Object value, String... settingPath) {
	Map<ModuleID<?>, Map<String, Object>> settings = new HashMap<>();
	loadConfig(config, settings);
	Object oldValue = SettingUtil.setValue(settings, m, settingPath, value, this);
	if(!Util.equals(value, oldValue)) save(settings, config, m.getPluginID());
	return oldValue;
}

/**
 * @throws UnsetSettingException {@inheritDoc}
 * @throws InvalidSettingPathException {@inheritDoc}
 */
@Override
public default Object get(String config, ModuleID<?> m, String... settingPath) {
	Map<ModuleID<?>, Map<String, Object>> settings = new HashMap<>();
	loadConfig(config, settings);
	Map<String, Object> root = settings.get(m);
	if(root == null) throw new InvalidSettingPathException(settingPath);
	return SettingUtil.getValue(settings, m, settingPath, SettingInfo.getTree(m));
}

/**
 * @throws UnsetSettingException {@inheritDoc}
 * @throws InvalidSettingPathException {@inheritDoc}
 */
@Override
public default Object remove(String config, ModuleID<?> m, String... settingPath) {
	Map<ModuleID<?>, Map<String, Object>> settings = new HashMap<>();
	loadConfig(config, settings);
	Map<String, Object> root = settings.get(m);
	if(root == null) throw new InvalidSettingPathException(settingPath);
	Object oldValue = SettingUtil.removeValue(settings, m, settingPath, SettingInfo.getTree(m));
	save(settings, config, m.getPluginID());
	return oldValue;
}

@Override
public default ConfigBuffer loadConfig(String config) {
	Map<ModuleID<?>, Map<String, Object>> getSettings = new HashMap<>();
	loadConfig(config, getSettings);
	return new ConfigBuffer(getSettings, this::newMap) {
		@Override
		public void save() {
			Map<String, Map<ModuleID<?>, Map<String, Object>>> moduleMap = new HashMap<>(4);
			for(String pluginID:pluginIDs) moduleMap.put(pluginID, new HashMap<>(8));
			SettingUtil.groupByPlugin(settings, moduleMap);
			for(Map.Entry<String, Map<ModuleID<?>, Map<String, Object>>> entry:moduleMap.entrySet())
				SettingManager.this.save(entry.getValue(), config, entry.getKey());
		}
	};
}

}