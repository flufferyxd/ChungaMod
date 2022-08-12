package magnileve.chungamod.settings;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import java.util.function.Supplier;

import magnileve.chungamod.modules.ModuleID;
import magnileve.chungamod.util.Util;

/**
 * Traverses through a setting map to get values of settings.
 * @author Magnileve
 */
public class SettingTraverser {

private final Deque<Map<String, Object>> settings;
private final Deque<String> path;

/**
 * Creates a new {@code SettingTraverser} with the given setting map at the root path.
 * @param settings root setting map
 */
public SettingTraverser(Map<String, Object> settings) {
	this(settings, new ArrayDeque<>(0));
}

/**
 * Creates a new {@code SettingTraverser} with the given setting map at the given path.
 * @param settings setting map of the given setting path
 * @param path starting setting path
 */
public SettingTraverser(Map<String, Object> settings, String[] path) {
	this(settings, new ArrayDeque<>(Arrays.asList(path)));
}

/**
 * Creates a new {@code SettingTraverser} with the given setting map at the given path.
 * @param settings setting map of the given setting path
 * @param path starting setting path
 */
public SettingTraverser(Map<String, Object> settings, Deque<String> path) {
	this.settings = new ArrayDeque<>(1);
	this.settings.push(settings);
	this.path = path;
}

/**
 * Creates a new {@code SettingTraverser} for a module at a given setting path.
 * @param m a module
 * @param configSettings map of modules to setting maps
 * @param settingPath starting setting path
 */
public SettingTraverser(ModuleID<?> m, Map<ModuleID<?>, Map<String, Object>> configSettings, String... settingPath) {
	this(getMapOfSettingSafe(m, configSettings, settingPath), settingPath);
}

/**
 * Creates a shallow copy of a {@code SettingTraverser}.
 * @param copyOf {@code SettingTraverser} to be copied
 */
public SettingTraverser(SettingTraverser copyOf) {
	this.settings = new ArrayDeque<>(copyOf.settings);
	this.path = new ArrayDeque<>(copyOf.path);
}

/**
 * Gets the value of a setting.
 * @param <T> value type
 * @param name name of setting
 * @param type value type
 * @return the value of this setting
 * @throws UnsetSettingException if the given setting does not have a value
 * @throws IllegalArgumentException if the value of this setting is not an instance of the given value type
 * @throws InvalidSettingPathException if the value of this setting is a map, indicating subsettings
 */
public <T> T get(String name, Class<T> type) {
	return get(name, type, false, null);
}

/**
 * Gets the value of a setting.
 * @param <T> value type
 * @param name name of setting
 * @param type value type
 * @param allowNull if {@code false}, {@code null} values, including empty arrays, result in {@code UnsetSettingException}
 * @return the value of this setting
 * @throws UnsetSettingException if the given setting does not have a value
 * @throws IllegalArgumentException if the value of this setting is not an instance of the given value type
 * @throws InvalidSettingPathException if the value of this setting is a map, indicating subsettings
 */
public <T> T get(String name, Class<T> type, boolean allowNull) {
	return get(name, type, allowNull, null);
}

/**
 * Gets the value of a setting.
 * @param <T> value type
 * @param name name of setting
 * @param type value type
 * @param allowNull if {@code false}, {@code null} values, including empty arrays, result in {@code UnsetSettingException}
 * @param defaultValue if not {@code null}, the result of this supplier is returned when a setting does not have a value
 * @return the value of this setting
 * @throws UnsetSettingException if the given setting does not have a value, and {@code defaultValue} is {@code null}
 * @throws IllegalArgumentException if the value of this setting is not an instance of the given value type
 * @throws InvalidSettingPathException if the value of this setting is a map, indicating subsettings
 */
public <T> T get(String name, Class<T> type, boolean allowNull, Supplier<T> defaultValue) {
	@SuppressWarnings("unchecked")
	T value = (T) settings.peekFirst().get(name);
	if(value == null) {
		if(settings.peekFirst().containsKey(name)) {
			if(allowNull) return null;
		} else if(defaultValue != null) return defaultValue.get();
		throw new UnsetSettingException(getPath(name));
	} else {
		if(value instanceof Object[] && ((Object[]) value).length == 0) {
			if(!allowNull) throw new UnsetSettingException(getPath(name));
		}
		if(value instanceof Map) throw new InvalidSettingPathException(getPath(name));
		if(!type.isInstance(value)) throw new IllegalArgumentException("For setting " +
				Util.inverseSplit(getPath(name), "/") + ", expected type: " + type + ", received type: " + value.getClass());
	}
	return value;
}

/**
 * Determines if, at the current path, a setting or subsetting map exists with the given name.
 * @param name a name
 * @return {@code true} if the current setting map contains a mapping for the given name; {@code false} otherwise
 */
public boolean has(String name) {
	return settings.peekFirst().containsKey(name);
}

/**
 * Moves to a subsetting map.
 * @param name name of subsetting map
 * @throws InvalidSettingPathException if a subsetting map does not exist with the given name
 */
@SuppressWarnings("unchecked")
public void subSettings(String name) {
	try {
		Map<String, Object> map = (Map<String, Object>) settings.peekFirst().get(name);
		settings.push(map == null ? Collections.EMPTY_MAP : map);
		path.add(name);
	} catch(ClassCastException | NullPointerException e) {
		throw new InvalidSettingPathException(getPath(name));
	}
}

/**
 * Moves back one map in the setting path.
 * @throws IllegalStateException if this {@code SettingTraverser} is at the path it started at
 */
public void back() {
	Map<String, Object>  map = settings.pop();
	if(settings.isEmpty()) {
		settings.push(map);
		throw new IllegalStateException("Already at initial path");
	}
	path.pop();
}

/**
 * Creates a setting map of all settings this traverser can view.
 * @param currentRoot setting identifier of the current position of this traverser
 * @param mapFactory creates setting maps
 * @return a recursively copied map of settings that this traverser can view
 */
public Map<String, Object> toMap(SettingInfo currentRoot, SettingMapFactory mapFactory) {
	return toMap(currentRoot, mapFactory, settings.peekFirst());
}

/**
 * Recursively copies a setting map.
 * @param currentRoot setting identifier of the current position of this traverser
 * @param mapFactory creates setting maps
 * @param settings setting map to be copied
 * @return a recursively copied map from the given setting map
 */
@SuppressWarnings("unchecked")
public static Map<String, Object> toMap(SettingInfo currentRoot, SettingMapFactory mapFactory, Map<String, Object> settings) {
	Map<String, Object> map = mapFactory.newMap();
	for(SettingInfo node:currentRoot) {
		String name = node.getName();
		Object value = settings.get(name);
		if(value != null || settings.containsKey(name))
			map.put(name, node.isTree() ? toMap(node, mapFactory, (Map<String, Object>) value) : Util.recursiveArrayClone(value));
	}
	return map;
}

/**
 * Returns the amount of settings and subsettings in this traverser's current setting map.
 * @return the amount of settings and subsettings in this traverser's current setting map
 */
public int size() {
	return settings.peekFirst().size();
}

/**
 * Gets this traverser's current setting path.
 * @return this traverser's current setting path
 */
public String[] getPath() {
	return path.toArray(new String[path.size()]);
}

/**
 * Gets this traverser's current setting path with a name appended.
 * @param settingName name to be appended to the path
 * @return this traverser's current setting path with {@code settingName} appended
 */
public String[] getPath(String settingName) {
	String[] array = path.toArray(new String[path.size() + 1]);
	array[path.size()] = settingName;
	return array;
}

@Override
public String toString() {
	return "SettingTraverser of: " + path;
}

@SuppressWarnings("unchecked")
private static Map<String, Object> getMapOfSettingSafe(ModuleID<?> m,
		Map<ModuleID<?>, Map<String, Object>> configSettings, String[] settingPath) {
	Map<String, Object> settings = configSettings.get(m);
	if(settings != null) return SettingUtil.getMapOfSettingSafe(settings, settingPath, SettingInfo.getTree(m), settingPath.length);
	return Collections.EMPTY_MAP;
}

}