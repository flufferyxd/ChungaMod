package magnileve.chungamod.settings;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import magnileve.chungamod.modules.ModuleID;
import magnileve.chungamod.util.Bucket;
import magnileve.chungamod.util.Util;

/**
 * This class contains static utility methods for interacting with setting maps.
 * @author Magnileve
 */
public class SettingUtil {

private SettingUtil() {}

/**
 * Called before parsing JSON from an input {@code String}.
 * If the value type is {@code String}, and the input does not start with a quote or equal "null",
 * it is formatted through {@link JSONObject#quote(String)}.
 * @param value input string
 * @param type value type
 * @return the input string, or a JSON String formatted version of it
 */
public static String prepare(String value, Class<?> type) {
	if(type.equals(String.class) && !value.isEmpty()) {
		char c = value.charAt(0);
		if(c != '\"' && c != '\'' && !value.equals("null")) return JSONObject.quote(value);
	}
	return value;
}

/**
 * Gets the subsetting map with the given name, creating one if it does not exist.
 * @param map current setting map
 * @param name name of subsetting branch
 * @param mapFactory creates the map if it does not exist
 * @return the subsetting map
 * @throws ClassCastException if the given setting map has a value with this name, but it is not a map
 */
@SuppressWarnings("unchecked")
public static Map<String, Object> getSubMap(Map<String, Object> map, String name, SettingMapFactory mapFactory) {
	return (Map<String, Object>) map.computeIfAbsent(name, k -> mapFactory.newMap());
}

/**
 * Sets the value of a setting.
 * @param settings maps modules to setting maps
 * @param m module of setting
 * @param settingPath path of setting
 * @param value new value
 * @param mapFactory creates setting maps if they do not exist
 * @return the previous value of the setting, or {@code null} if one did not exist
 * @throws InvalidSettingPathException if this setting path does not point to a setting
 * @throws UnsetSettingException if {@code mapFactory} is {@code null}, and the setting map does not exist
 */
public static Object setValue(Map<ModuleID<?>, Map<String, Object>> settings, ModuleID<?> m, String[] settingPath, Object value,
		SettingMapFactory mapFactory) {
	return setValue(settings.computeIfAbsent(m, k -> mapFactory.newMap()), settingPath, value, SettingInfo.getTree(m), mapFactory);
}

/**
 * Sets the value of a setting.
 * @param settings maps modules to setting maps
 * @param settingPath path of setting
 * @param value new value
 * @param node root setting identifier
 * @param mapFactory creates setting maps if they do not exist
 * @return the previous value of the setting, or {@code null} if one did not exist
 * @throws InvalidSettingPathException if this setting path does not point to a setting
 * @throws UnsetSettingException if {@code mapFactory} is {@code null}, and the setting map does not exist
 */
public static Object setValue(Map<String, Object> settings, String[] settingPath, Object value, SettingInfo node,
		SettingMapFactory mapFactory) {
	return getMapOfSetting(settings, settingPath, node, settingPath.length - 1, null, mapFactory)
			.put(node.getNode(settingPath).getName(), value);
}

/**
 * Gets the value of a setting.
 * @param settings root setting map
 * @param settingPath path of setting
 * @param node root setting identifier
 * @return the value of the given setting
 * @throws InvalidSettingPathException if this setting path does not point to a setting
 * @throws UnsetSettingException if the setting does not have a value
 */
public static Object getValue(Map<String, Object> settings, String[] settingPath, SettingInfo node) {
	return get(getMapOfSetting(settings, settingPath, node, settingPath.length - 1, null, null),
			node.getNode(settingPath).getName(), settingPath);
}

/**
 * Gets the value of a setting of a module.
 * @param settings map of modules to their setting maps
 * @param m module of setting
 * @param settingPath path of setting
 * @param node root setting identifier
 * @return the value of the given setting
 * @throws InvalidSettingPathException if this setting path does not point to a setting
 * @throws UnsetSettingException if the setting does not have a value
 */
public static Object getValue(Map<ModuleID<?>, Map<String, Object>> settings, ModuleID<?> m,
		String[] settingPath, SettingInfo node) {
	Map<String, Object> root = settings.get(m);
	if(root == null) throw new UnsetSettingException(settingPath);
	return getValue(root, settingPath, SettingInfo.getTree(m));
}

/**
 * Gets and removes the value of a setting.
 * @param settings root setting map
 * @param settingPath path of setting
 * @param node root setting identifier
 * @return the value of the given setting
 * @throws InvalidSettingPathException if this setting path does not point to a setting
 * @throws UnsetSettingException if the setting does not have a value
 */
public static Object removeValue(Map<String, Object> settings, String[] settingPath, SettingInfo node) {
	int l = settingPath.length - 1;
	Deque<Map<String, Object>> maps = new ArrayDeque<>();
	return remove(getMapOfSetting(settings, settingPath, node, l, maps, null),
			node.getNode(settingPath).getName(), settingPath, node, l, maps);
}

/**
 * Gets and removes the value of a setting of a module.
 * @param settings map of modules to their setting maps
 * @param m module of setting
 * @param settingPath path of setting
 * @param node root setting identifier
 * @return the value of the given setting
 * @throws InvalidSettingPathException if this setting path does not point to a setting
 * @throws UnsetSettingException if the setting does not have a value
 */
public static Object removeValue(Map<ModuleID<?>, Map<String, Object>> settings, ModuleID<?> m,
		String[] settingPath, SettingInfo node) {
	Map<String, Object> root = settings.get(m);
	if(root == null) throw new UnsetSettingException(settingPath);
	Object value = removeValue(root, settingPath, SettingInfo.getTree(m));
	if(root.isEmpty()) settings.remove(m);
	return value;
}

/**
 * Gets the setting map that a given setting should be stored in,
 * but if the map does not exist, an empty map (immutable) is returned.
 * @param root root setting map
 * @param settingPath path of setting
 * @param node root setting identifier
 * @param depth the index after the setting map in {@code settingPath}, usually {@code settingPath.length - 1}
 * @return the setting map representing {@code settingPath} at index {@code depth}, or an empty map if one does not exist.
 * @throws InvalidSettingPathException if this setting path does not point to a subsetting map
 */
@SuppressWarnings("unchecked")
public static Map<String, Object> getMapOfSettingSafe(Map<String, Object> root, String[] settingPath, SettingInfo node, int depth) {
	try {
		return getMapOfSetting(root, settingPath, node, depth, null, null);
	} catch(UnsetSettingException e) {
		return Collections.EMPTY_MAP;
	}
}

/**
 * Gets the setting map that a given setting should be stored in.
 * @param root root setting map
 * @param settingPath path of setting
 * @param node root setting identifier
 * @param depth the index after the setting map in {@code settingPath}, usually {@code settingPath.length - 1}
 * @param maps if not null, each setting map is added to this stack, not including the returned map
 * @param mapFactory if not null, creates new setting maps when they do not exist
 * @return the setting map representing {@code settingPath} at index {@code depth}
 * @throws InvalidSettingPathException if this setting path does not point to a subsetting map
 * @throws UnsetSettingException if {@code mapFactory} is {@code null}, and the setting map does not exist
 */
@SuppressWarnings("unchecked")
public static Map<String, Object> getMapOfSetting(Map<String, Object> root, String[] settingPath, SettingInfo node, int depth,
		Deque<Map<String, Object>> maps, SettingMapFactory mapFactory) {
	for(int i = 0; i < depth; i++) {
		node = node.getNode(settingPath[i]);
		if(node == null || !node.isTree())
			throw new InvalidSettingPathException(settingPath);
		if(maps != null) maps.push(root);
		root = (Map<String, Object>) root.computeIfAbsent(node.getName(), k -> {
			if(mapFactory == null) throw new UnsetSettingException(settingPath);
			return mapFactory.newMap();
		});
	}
	return root;
}

/**
 * Gets the value of a setting.
 * @param settings map containing the setting
 * @param name name of setting
 * @param settingPath path of setting, only used for exception information
 * @return the value of the given setting
 * @throws UnsetSettingException if this setting does not have a value
 */
public static Object get(Map<String, Object> settings, String name, String[] settingPath) {
	Object value = settings.get(name);
	if(value == null && !settings.containsKey(name)) throw new UnsetSettingException(settingPath);
	return value;
}

/**
 * Gets and removes the value of a setting.
 * @param settings map containing the setting
 * @param name name of setting
 * @param settingPath path of setting, only used for exception information
 * @param root root setting identifier
 * @param index the index after the setting map in {@code settingPath}, usually {@code settingPath.length - 1}
 * @param maps a stack of setting maps in the setting path
 * @return the value of the given setting
 * @throws UnsetSettingException if this setting does not have a value
 */
public static Object remove(Map<String, Object> settings, String name, String[] settingPath,
		SettingInfo root, int index, Deque<Map<String, Object>> maps) {
	int size = settings.size();
	Object value = settings.remove(name);
	if(value == null && settings.size() == size) throw new UnsetSettingException(settingPath);
	if(settings.isEmpty()) {
		SettingInfo[] nodes = new SettingInfo[index];
		for(int i = 0; i < index; i++) nodes[i] = (root = root.getNode(settingPath[i]));
		int i = index - 1;
		Map<String, Object> prev = settings;
		while(prev.isEmpty() && i >= 0) {
			prev = maps.pop();
			prev.remove(nodes[i--].getName());
		}
	}
	return value;
}

/**
 * Deep copies a setting map.  This includes copying contained setting maps and arrays.
 * @param fromMap map to be copied
 * @param toMap map to put copied entries into
 * @param mapFactory creates setting maps
 */
@SuppressWarnings("unchecked")
public static void copySettings(Map<String, Object> fromMap, Map<String, Object> toMap, SettingMapFactory mapFactory) {
	for(Map.Entry<String, Object> setting:fromMap.entrySet()) {
		Object value = setting.getValue();
		if(value instanceof Map) {
			Map<String, Object> newValue = mapFactory.newMap();
			copySettings((Map<String, Object>) value, newValue, mapFactory);
			value = newValue;
		} else value = Util.recursiveArrayClone(value);
		toMap.put(setting.getKey(), value);
	}
}

/**
 * Deep copies a setting map.  This includes copying contained setting maps and arrays.
 * @param map map to be copied
 * @param mapFactory creates setting maps
 */
public static Map<String, Object> copySettings(Map<String, Object> map, SettingMapFactory mapFactory) {
	Map<String, Object> copy = mapFactory.newMap();
	copySettings(map, copy, mapFactory);
	return copy;
}

/**
 * Deep copies setting maps of modules.  This includes copying contained setting maps and arrays.
 * @param fromMap contains maps to be copied
 * @param toMap map to put copied maps into
 * @param mapFactory creates setting maps
 */
public static void copySettingsGroup(Map<ModuleID<?>, Map<String, Object>> fromMap, Map<ModuleID<?>, Map<String, Object>> toMap,
		SettingMapFactory mapFactory) {
	for(Map.Entry<ModuleID<?>, Map<String, Object>> entry:fromMap.entrySet())
		copySettings(entry.getValue(), toMap.computeIfAbsent(entry.getKey(), k -> mapFactory.newMap()), mapFactory);
	
}

/**
 * Groups maps of modules to their setting maps by the plugin IDs of the modules.
 * @param fromMap maps modules to their setting maps
 * @param toMap maps plugin IDs to modules to their setting maps as provided by {@code fromMap}
 */
public static void groupByPlugin(Map<ModuleID<?>, Map<String, Object>> fromMap, Map<String, Map<ModuleID<?>, Map<String, Object>>> toMap) {
	for(Map.Entry<ModuleID<?>, Map<String, Object>> entry:fromMap.entrySet()) {
		ModuleID<?> m = entry.getKey();
		toMap.computeIfAbsent(m.getPluginID(), k -> new HashMap<>()).put(m, entry.getValue());
	}
}

/**
 * Groups containers of modules and their settings by the plugin IDs of the modules.
 * @param modules containers of modules and their settings
 * @param map maps plugin IDs to modules to their settings as provided by {@code modules}
 */
public static void sortByPlugin(Iterable<Bucket<ModuleID<?>, Bucket<SettingInfoMap, Map<String, Object>>>> modules,
		Map<String, Map<String, Bucket<SettingInfoMap, Map<String, Object>>>> map) {
	for(Bucket<ModuleID<?>, Bucket<SettingInfoMap, Map<String, Object>>> m:modules)
		map.computeIfAbsent(m.getE1().getPluginID(), k -> new HashMap<>())
				.put(m.getE1().getName(), m.getE2());
}

/**
 * Copies settings that have changed to a new map.
 * @param initial a setting map
 * @param current an updated setting map
 * @param toMap receives settings in {@code current} that are not the same in {@code initial}
 */
@SuppressWarnings("unchecked")
public static void copyChanged(Map<String, Object> initial, Map<String, Object> current, Map<String, Object> toMap) {
	for(Map.Entry<String, Object> setting:current.entrySet()) {
		String name = setting.getKey();
		Object value = setting.getValue();
		if(value instanceof Map) {
			Map<String, Object> subInitial = (Map<String, Object>) initial.get(name);
			Map<String, Object> subToMap = new HashMap<>();
			copyChanged(subInitial, (Map<String, Object>) value, subToMap);
			if(!subToMap.isEmpty()) toMap.put(name, subToMap);
		} else if(value == null ? !(initial.get(name) == null && initial.containsKey(name)) :
			!Util.equals(value, initial.get(name))) toMap.put(name, value);
	}
}

}