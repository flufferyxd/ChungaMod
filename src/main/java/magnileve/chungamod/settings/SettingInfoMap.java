package magnileve.chungamod.settings;

import java.util.HashMap;
import java.util.Map;

import magnileve.chungamod.modules.ModuleID;
import magnileve.chungamod.util.ArrayBuildList;

/**
 * Wraps {@link SettingInfo} and allows for removal of sub-nodes in a branch.
 * @author Magnileve
 */
public class SettingInfoMap {

private final Map<String, SettingInfoMap> nodes;
private final SettingInfo node;

/**
 * Creates a new {@code SettingInfoMap} with a new {@code SettingInfo} root representing the settings declared by the given module.
 * @param m a module
 */
public SettingInfoMap(ModuleID<?> m) {
	this(SettingInfo.getTree(m));
}

/**
 * Creates a new {@code SettingInfoMap} with a new {@code SettingInfo} root representing the settings declared by the given type.
 * @param moduleClass declaring type
 */
public SettingInfoMap(Class<?> moduleClass) {
	this(SettingInfo.getTree(moduleClass));
}

/**
 * Creates a new {@code SettingInfoMap} containing all settings branching from a {@code SettingInfo} node.
 * @param node a node
 */
public SettingInfoMap(SettingInfo node) {
	this.node = node;
	if(node.isTree()) {
		nodes = new HashMap<>(node.getNodeCount());
		for(SettingInfo sub:node) nodes.put(sub.getName(), new SettingInfoMap(sub));
	} else nodes = null;
}

/**
 * Creates a new {@code SettingInfoMap} containing all settings within the given setting paths branching from a {@code SettingInfo} node.
 * @param root a node
 * @param settingPaths setting paths relative to the given node
 */
public SettingInfoMap(SettingInfo root, String[]... settingPaths) {
	this(root, settingPaths, new ArrayBuildList<>(new String[1]), 0);
}

private SettingInfoMap(SettingInfo node, String[][] settingPaths, ArrayBuildList<String> pathBuilder, int index) {
	this.node = node;
	if(node.isTree()) {
		nodes = new HashMap<>(node.getNodeCount());
		String[] atPath = pathBuilder.getArray();
		int nextIndex = index + 1;
		for(SettingInfo sub:node) {
			String name = sub.getName();
			loop:
			for(String[] path:settingPaths) if(index < path.length && path[index].equalsIgnoreCase(name)) {
				for(int i = 0; i < index; i++) if(!atPath[i].equalsIgnoreCase(path[i])) continue loop;
				pathBuilder.add(name);
				nodes.put(name, new SettingInfoMap(sub, settingPaths, pathBuilder, nextIndex));
				pathBuilder.remove(index);
				break;
			}
		}
	} else nodes = null;
}

/**
 * Gets this node's map of names to subsetting nodes.
 * @return this node's map of names to subsetting nodes
 * @throws IllegalStateException if this node does not point to subsettings
 */
public Map<String, SettingInfoMap> getNodeMap() {
	if(nodes == null) throw new IllegalStateException(node.getName() + " is not a setting list");
	return nodes;
}

/**
 * Gets this node's {@code SettingInfo}.
 * @return this node's {@code SettingInfo}
 */
public SettingInfo getNode() {
	return node;
}

/**
 * Gets a subsetting node from a name.
 * @param key the subsetting name
 * @return a subsetting node, or {@code null} if one does not exist with the given name
 * @throws IllegalStateException if this node does not point to subsettings
 */
public SettingInfoMap get(String key) {
	return getNodeMap().get(key);
}

/**
 * Gets and removes a subsetting node from a name.
 * @param key the subsetting name
 * @return a subsetting node, or {@code null} if one does not exist with the given name
 * @throws IllegalStateException if this node does not point to subsettings
 */
public SettingInfoMap remove(String key) {
	return getNodeMap().remove(key);
}

@Override
public String toString() {
	return node.getName() + ": " + (node.isTree() ? nodes.values().toString() : node.getSetting().type().getSimpleName());
}

}