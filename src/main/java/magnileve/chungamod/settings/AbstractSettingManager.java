package magnileve.chungamod.settings;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.AnnotationFormatError;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONTokener;
import org.json.JSONWriter;

import magnileve.chungamod.Chung;
import magnileve.chungamod.modules.ModuleID;
import magnileve.chungamod.modules.ContainsInit;
import magnileve.chungamod.modules.Init;
import magnileve.chungamod.util.ArrayBuildList;
import magnileve.chungamod.util.Bucket;
import magnileve.chungamod.util.Util;
import magnileve.chungamod.util.function.TriConsumer;
import magnileve.chungamod.util.json.GoodLookingJSONWriter;
import magnileve.chungamod.util.json.JSONManager;
import magnileve.chungamod.util.json.JSONUtil;
import magnileve.chungamod.util.json.ValueLimiterSyntaxException;

/**
 * A {@link SettingManager} that loads and saves settings by reading and writing JSON for each config and plugin.
 * @author Magnileve
 */
@ContainsInit
public abstract class AbstractSettingManager implements SettingManager {

private static final String INDENT_PROPERTY = "chungamod.indentJSON";
private static final int DEFAULT_INDENT = 2;
private static volatile int indent;

private final Logger log;
private final JSONManager json;
private final Set<String> configs;
private final SettingMapFactory mapFactory;

@Init
private static void init() {
	Chung.SYSTEM.addProperty(INDENT_PROPERTY, String.valueOf(DEFAULT_INDENT), value -> {
		try {
			indent = Short.parseShort(Chung.SYSTEM.getProperty(INDENT_PROPERTY));
		} catch(NumberFormatException e) {
			Chung.SYSTEM.setProperty(INDENT_PROPERTY, null);
		}
	});
}

/**
 * Gets the amount of spaces used for indenting JSON.
 * @return the amount of spaces used for indenting JSON
 */
public static int getJSONIndent() {
	return indent;
}

/**
 * Creates a new {@code AbstractSettingManager}.
 * @param log logger
 * @param json converts settings between JSON and Java objects
 * @param configs initial enabled configs
 * @param mapFactory creates setting maps
 */
public AbstractSettingManager(Logger log, JSONManager json, Collection<String> configs, SettingMapFactory mapFactory) {
	this.log = log;
	this.json = json;
	this.configs = new LinkedHashSet<>(configs);
	this.mapFactory = mapFactory;
}

/**
 * Opens a {@link Reader} for settings of a config and plugin.
 * @param config a config
 * @param pluginID a plugin
 * @return a new {@code Reader} ready to read setting JSON
 * @throws IOException if an I/O error occurs
 */
protected abstract Reader getReader(String config, String pluginID) throws IOException;

/**
 * Opens a {@link Writer} for settings of a config and plugin.
 * @param config a config
 * @param pluginID a plugin
 * @return a new {@code Writer} ready to write setting JSON
 * @throws IOException if an I/O error occurs
 */
protected abstract Writer getWriter(String config, String pluginID) throws IOException;

/**
 * Gets a set of all plugins with settings in a config.
 * @param config a config
 * @return all plugins with settings in the given config
 */
protected abstract Set<String> getPluginsInConfig(String config);

/**
 * Removes a plugin from a config, and removes the config if it is empty.
 * @param config a config
 * @param pluginID a plugin
 * @throws IOException if an I/O error occurs
 */
protected abstract void saveEmpty(String config, String pluginID) throws IOException;

/**
 * Gets a module ID from a name and plugin.
 * @param name module name
 * @param pluginID plugin of module
 * @return the module with the given name and plugin
 */
protected abstract ModuleID<?> getModule(String name, String pluginID);

@Override
public void save(Map<ModuleID<?>, Map<String, Object>> settingsMap, String config, String pluginID) {
	log.trace("Saving settings for plugin {} config {}", pluginID, config);
	if(settingsMap.isEmpty()) try {
		saveEmpty(config, pluginID);
	} catch(IOException e) {
		log.error("Failed to save empty settings", e);
	} else try(Writer write = getWriter(config, pluginID)) {
		JSONWriter w = indent < 0 ? new JSONWriter(write) : new GoodLookingJSONWriter(write, indent);
		w.object();
		for(Entry<ModuleID<?>, Map<String, Object>> entry:settingsMap.entrySet()) {
			ModuleID<?> m = entry.getKey();
			if(pluginID.equals(m.getPluginID())) {
				w.key(m.getName());
				writeSettings(w, SettingInfo.getTree(m), entry.getValue());
			}
		}
		w.endObject();
	} catch(IOException e) {
		log.error("Failed to save settings to file", e);
	}
}

/**
 * Writes settings to JSON recursively.
 * @param w writes JSON
 * @param nodes setting identifiers
 * @param map map of values
 */
@SuppressWarnings("unchecked")
private void writeSettings(JSONWriter w, SettingInfo nodes, Map<String, Object> map) {
	w.object();
	for(SettingInfo node:nodes) {
		String name = node.getName();
		if(!map.containsKey(name)) continue;
		w.key(name);
		Object value = map.get(name);
		try {
			if(node.isTree()) writeSettings(w, node, (Map<String, Object>) value);
			else if(value == null) w.value(null);
			else json.serializeCast(w, node.getSetting().type(), value);
		} catch(ClassCastException e) {
			log.error(() -> log.getMessageFactory().newMessage("Incompatible type of setting " + node.getName() +
					"\nExpected: " + node.getSetting().type() +
					"\nReceived: " + value.getClass()));
			w.value(null);
		}
	}
	w.endObject();
}

@Override
public void loadDefaultSettings(Map<String, Map<String, Bucket<SettingInfoMap, Map<String, Object>>>> modules,
		TriConsumer<ModuleID<?>, String[], Object> onNewSetting) {
	Collection<Bucket<ModuleID<?>, Collection<Bucket<String[], Object>>>> changes = onNewSetting == null ?
			null : new ArrayBuildList<>(Bucket.class);
	for(Map.Entry<String, Map<String, Bucket<SettingInfoMap, Map<String, Object>>>> plugin:modules.entrySet()) {
		String pluginID = plugin.getKey();
		for(Map.Entry<String, Bucket<SettingInfoMap, Map<String, Object>>> m:plugin.getValue().entrySet()) {
			Collection<Bucket<String[], Object>> moduleChanges = changes == null ? null : new ArrayBuildList<>(Bucket.class);
			Bucket<SettingInfoMap, Map<String, Object>> bucket = m.getValue();
			loadDefaultSettings(bucket.getE1(), bucket.getE2(), new ArrayBuildList<>(new String[1]), moduleChanges);
			if(changes != null) changes.add(Bucket.of(getModule(m.getKey(), pluginID), moduleChanges));
		}
	}
	if(onNewSetting != null) for(Bucket<ModuleID<?>, Collection<Bucket<String[], Object>>> moduleChanges:changes) {
		ModuleID<?> m = moduleChanges.getE1();
		for(Bucket<String[], Object> change:moduleChanges.getE2()) onNewSetting.accept(m, change.getE1(), change.getE2());
	}
}

/**
 * Loads default setting values recursively.
 * @param settings setting identifiers
 * @param map map to load values into
 * @param settingPathBuilder contains the current setting path
 * @param changes collects changes in the map if not null
 */
private void loadDefaultSettings(SettingInfoMap settings, Map<String, Object> map, ArrayBuildList<String> settingPathBuilder,
		Collection<Bucket<String[], Object>> changes) {
	int pathSize = settingPathBuilder.size();
	settingPathBuilder.add(null);
	String[] settingPath = settingPathBuilder.getArray();
	for(SettingInfoMap settingEntry:settings.getNodeMap().values()) {
		SettingInfo node = settingEntry.getNode();
		String name = node.getName();
		settingPath[pathSize] = name;
		if(node.isTree()) loadDefaultSettings(settingEntry, SettingUtil.getSubMap(map, name, this), settingPathBuilder, changes);
		else {
			Setting setting = node.getSetting();
			Class<?> type = setting.type();
			String valueString = SettingUtil.prepare(setting.value(), type);
			Object value;
			try {
				value = json.deserialize(new JSONTokener(valueString), type, setting.limits());
			} catch(JSONException | IllegalArgumentException | IllegalStateException e) {
				log.fatal("Error parsing default setting value", e);
				throw new AnnotationFormatError("Unable to parse default JSON of setting " + name +
						" to " + type.getName() + ": \"" + valueString +
						(setting.limits().isEmpty() ? "\"" : "\" with limits: \"" + setting.limits() + "\""));
			}
			Object oldValue = map.put(node.getName(), value);
			if(!(changes == null || Util.equals(value, oldValue))) changes.add(Bucket.of(settingPath.clone(), value));
		}
	}
	settingPathBuilder.remove(pathSize);
}

@Override
public void loadConfig(String config, Map<ModuleID<?>, Map<String, Object>> configSettings,
		TriConsumer<ModuleID<?>, String[], Object> onNewSetting) {
	for(String plugin:getPluginsInConfig(config)) {
		Collection<Bucket<ModuleID<?>, Collection<Bucket<String[], Object>>>> changes = onNewSetting == null ?
				null : new ArrayBuildList<>(Bucket.class);
		try(Reader read = getReader(config, plugin)) {
			JSONTokener p = new JSONTokener(read);
			for(String moduleName:JSONUtil.iterateJSONObject(p)) {
				ModuleID<?> m = getModule(moduleName, plugin);
				if(m == null) {
					log.info("Settings in config {} for missing module {} in plugin {} will not be saved",
							config, moduleName, plugin);
					p.nextValue();
					continue;
				}
				Map<String, Object> map = newMap();
				configSettings.put(m, map);
				Collection<Bucket<String[], Object>> moduleChanges = changes == null ? null : new ArrayBuildList<>(Bucket.class);
				parseSettingJSON(p, new SettingInfoMap(m.getModuleType()), map, new ArrayBuildList<>(new String[1]), moduleChanges);
				if(changes != null) changes.add(Bucket.of(m, moduleChanges));
			}
		} catch(IOException | JSONException e) {
			log.error("Error reading file", e);
		}
		if(onNewSetting != null) for(Bucket<ModuleID<?>, Collection<Bucket<String[], Object>>> moduleChanges:changes) {
			ModuleID<?> m = moduleChanges.getE1();
			for(Bucket<String[], Object> change:moduleChanges.getE2()) onNewSetting.accept(m, change.getE1(), change.getE2());
		}
			
	}
}

@Override
public void loadSettings(Map<String, Map<String, Bucket<SettingInfoMap, Map<String, Object>>>> modules,
		String config, TriConsumer<ModuleID<?>, String[], Object> onNewSetting) {
	for(Entry<String, Map<String, Bucket<SettingInfoMap, Map<String, Object>>>> plugin:modules.entrySet()) {
		String pluginID = plugin.getKey();
		Collection<Bucket<ModuleID<?>, Collection<Bucket<String[], Object>>>> changes = onNewSetting == null ?
				null : new ArrayBuildList<>(Bucket.class);
		try(Reader read = getReader(config, pluginID)) {
			JSONTokener p = new JSONTokener(read);
			Map<String, Bucket<SettingInfoMap, Map<String, Object>>> map = plugin.getValue();
			for(String moduleName:JSONUtil.iterateJSONObject(p)) {
				Bucket<SettingInfoMap, Map<String, Object>> bucket = map.get(moduleName);
				if(bucket == null) p.nextValue();
				else {
					Collection<Bucket<String[], Object>> moduleChanges = changes == null ? null : new ArrayBuildList<>(Bucket.class);
					parseSettingJSON(p, bucket.getE1(), bucket.getE2(), new ArrayBuildList<>(new String[1]), moduleChanges);
					if(changes != null) changes.add(Bucket.of(getModule(moduleName, pluginID), moduleChanges));
				}
			}
		} catch(IOException | JSONException e) {
			log.error("Error reading file", e);
		}
		if(onNewSetting != null) for(Bucket<ModuleID<?>, Collection<Bucket<String[], Object>>> moduleChanges:changes) {
			ModuleID<?> m = moduleChanges.getE1();
			for(Bucket<String[], Object> change:moduleChanges.getE2()) onNewSetting.accept(m, change.getE1(), change.getE2());
		}
	}
}

@Override
public Object loadSetting(ModuleID<?> m, String[] settingPath) {
	String name = m.getName();
	String pluginID = m.getPluginID();
	SettingInfoMap settings = new SettingInfoMap(SettingInfo.getTree(m), settingPath);
	Map<String, Object> map = new HashMap<>(1);
	loadSetting(TEMPORARY_CONFIG, pluginID, name, settings, map);
	if(map.isEmpty()) {
		for(String config:getActiveConfigs()) {
			loadSetting(config, pluginID, name, settings, map);
			if(!map.isEmpty()) break;
		}
		if(map.isEmpty()) loadDefaultSettings(settings, map, new ArrayBuildList<>(new String[1]), null);
	}
	return SettingUtil.getValue(map, settingPath, settings.getNode());
}

/**
 * Loads a single setting of a module.
 * @param config name of config
 * @param pluginID plugin
 * @param moduleName module name
 * @param settings setting identifiers
 * @param map map to load setting into
 */
private void loadSetting(String config, String pluginID, String moduleName, SettingInfoMap settings, Map<String, Object> map) {
	if(getPluginsInConfig(config).contains(pluginID)) {
		try(Reader read = getReader(config, pluginID)) {
			JSONTokener p = new JSONTokener(read);
			for(String checkName:JSONUtil.iterateJSONObject(p)) if(moduleName.equals(checkName))
				parseSettingJSON(p, settings, map, new ArrayBuildList<>(new String[1]), null);
			else p.nextValue();
		} catch(IOException | JSONException e) {
			log.error("Error reading file", e);
		}
	}
}

/**
 * Reads settings from JSON recursively.
 * @param p reads JSON
 * @param settings setting identifiers
 * @param map map to read settings into
 * @param settingPathBuilder contains the current setting path
 * @param changes if not null, collects changes in the setting map
 * @throws JSONException if a JSON parsing error occurs
 */
private void parseSettingJSON(JSONTokener p, SettingInfoMap settings, Map<String, Object> map,
		ArrayBuildList<String> settingPathBuilder, Collection<Bucket<String[], Object>> changes) throws JSONException {
	int pathSize = settingPathBuilder.size();
	settingPathBuilder.add(null);
	String[] settingPath = settingPathBuilder.getArray();
	for(String name:JSONUtil.iterateJSONObject(p)) {
		SettingInfoMap settingEntry = settings.get(name);
		if(settingEntry == null) {
			p.nextValue();
			continue;
		}
		SettingInfo node = settingEntry.getNode();
		settingPath[pathSize] = name;
		if(node.isTree()) {
			parseSettingJSON(p, settingEntry, SettingUtil.getSubMap(map, name, this), settingPathBuilder, changes);
			if(settingEntry.getNodeMap().isEmpty()) settings.remove(name);
		} else {
			Object value;
			boolean notifyChange;
			try {
				Setting setting = node.getSetting();
				value = json.deserialize(p, setting.type(), setting.limits());
				Object oldValue = map.put(name, value);
				notifyChange = !(changes == null || Util.equals(value, oldValue));
				settings.remove(name);
			} catch(ValueLimiterSyntaxException e) {
				throw new AnnotationFormatError("Invalid syntax for limits on setting " + node.getName(), e);
			} catch(JSONException | IllegalArgumentException e) {
				continue;
			} catch(IllegalStateException e) {
				throw new AnnotationFormatError("Unable to parse JSON for " + node.getName(), e);
			}
			if(notifyChange) changes.add(Bucket.of(settingPath.clone(), value));
		}
	}
	settingPathBuilder.remove(pathSize);
}

@Override
public void clearTemporaryConfig() {
	for(String pluginID:getPluginsInConfig(TEMPORARY_CONFIG)) try {
		saveEmpty(TEMPORARY_CONFIG, pluginID);
	} catch (IOException e) {
		log.error("Failed to save empty settings", e);
	}
}

@Override
public Set<String> getActiveConfigs() {
	return configs;
}

@Override
public Map<String, Object> newMap() {
	return mapFactory.newMap();
}

}