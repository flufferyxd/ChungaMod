package magnileve.chungamod.settings;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import magnileve.chungamod.modules.ModuleID;

/**
 * Provides access to get, modify, and save settings of a config.
 * @author Magnileve
 */
public abstract class ConfigBuffer implements ConfigMap {

/**
 * The map of settings in this buffer.
 */
protected final Map<ModuleID<?>, Map<String, Object>> settings;
/**
 * Immutable list of initial plugin IDs in this buffer.
 */
protected final List<String> pluginIDs;
/**
 * Creates setting maps.
 */
protected final SettingMapFactory mapFactory;

/**
 * Creates a new config buffer for the given config map.
 * @param settings map of modules to setting maps
 * @param mapFactory creates setting maps
 */
public ConfigBuffer(Map<ModuleID<?>, Map<String, Object>> settings, SettingMapFactory mapFactory) {
	this.settings = settings;
	this.mapFactory = mapFactory;
	pluginIDs = Collections.unmodifiableList(settings.keySet().stream().map(m -> m.getPluginID()).distinct().collect(Collectors.toList()));
}

/**
 * Saves the settings in this config buffer.
 */
public abstract void save();

@Override
public void set(ModuleID<?> m, Object value, String... settingPath) {
	SettingUtil.setValue(settings, m, settingPath, value, mapFactory);
}

@Override
public Object get(ModuleID<?> m, String... settingPath) {
	return SettingUtil.getValue(settings, m, settingPath, SettingInfo.getTree(m));
}

public Object remove(ModuleID<?> m, String... settingPath) {
	return SettingUtil.removeValue(settings, m, settingPath, SettingInfo.getTree(m));
}

@Override
public SettingTraverser traverser(ModuleID<?> m, String... settingPath) {
	return new SettingTraverser(m, settings, settingPath);
}

@Override
public void importSettings(Map<ModuleID<?>, Map<String, Object>> settings) {
	SettingUtil.copySettingsGroup(settings, this.settings, mapFactory);
}

}