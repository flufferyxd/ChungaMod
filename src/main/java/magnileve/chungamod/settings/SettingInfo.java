package magnileve.chungamod.settings;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Iterator;

import magnileve.chungamod.modules.ModuleID;

/**
 * Identifies a setting.
 * @author Magnileve
 */
public abstract class SettingInfo implements Iterable<SettingInfo> {

/**
 * Identifies the setting {@value Setting#ON} implicitly declared by any module that does not explicitly declare it.
 */
private static final SettingInfo SETTING_ON = new Value(new Setting() {
	@Override public Class<? extends Annotation> annotationType() {return Setting.class;}
	@Override public String name() {return Setting.ON;}
	@Override public Class<?> type() {return Boolean.class;}
	@Override public String limits() {return "";}
	@Override public String value() {return "false";}
	@Override public String description() {return "";}
});

private SettingInfo() {}

/**
 * Gets the declared {@link Setting} annotation of this node.
 * @return the declared {@link Setting} annotation of this node
 * @throws IllegalStateException if this node contains subsettings rather than a single setting
 */
public abstract Setting getSetting();

/**
 * Gets a subsetting node from this branch.
 * @param name name of subsetting
 * @return a node representing the current setting path with {@code name} appended, or {@code null} if no node is found
 * @throws IllegalStateException if this node contains a single setting rather than subsettings
 */
public abstract SettingInfo getNode(String name);

/**
 * Gets the number of subsetting nodes this branch points to.
 * @return the number of subsetting nodes this branch points to
 * @throws IllegalStateException if this node contains a single setting rather than subsettings
 */
public abstract int getNodeCount();

/**
 * Gets the name of this node.
 * @return
 */
public abstract String getName();

/**
 * Indicates if this node branches to more nodes.
 * @return {@code true} if this node represents a subsetting branch; {@code false} if this node contains a single setting
 */
public abstract boolean isTree();

/**
 * Gets the node representing the setting path relative to the current node's setting path.
 * @param settingPath a setting path with the current node acting as the root
 * @return a node for the given setting path
 * @throws InvalidSettingPathException if the given setting path does not point to a node
 */
public SettingInfo getNode(String[] settingPath) {
	return getNode(settingPath, 0, settingPath.length);
}

/**
 * Gets the node representing a part of the setting path relative to the current node's setting path.
 * @param settingPath a setting path with the current node acting as the path before {@code start}
 * @param start starting index of {@code settingPath}, included
 * @param end ending index of {@code settingPath}, excluded
 * @return a node for part the given setting path
 * @throws InvalidSettingPathException if the given setting path does not point to a node
 */
public SettingInfo getNode(String[] settingPath, int start, int end) {
	SettingInfo node = this;
	for(;start < end; start++) {
		try {
			node = node.getNode(settingPath[start]);
		} catch(IllegalStateException e) {
			node = null;
		}
		if(node == null) throw new InvalidSettingPathException(settingPath);
	}
	return node;
}

/**
 * Gets a declared setting by a class.
 * @param moduleClass module type
 * @param settingPath path of setting
 * @return the declared {@link Setting} annotation
 * @throws InvalidSettingPathException if the given setting path does not have a declared setting
 */
public static Setting getSetting(Class<?> moduleClass, String... settingPath) {
	try {
		return getTree(moduleClass).getNode(settingPath).getSetting();
	} catch(IllegalStateException e) {
		throw new InvalidSettingPathException(settingPath);
	}
}

/**
 * Creates the root of a {@code SettingInfo} tree representing the settings declared by the given module.
 * Subsetting branches are lazily loaded.
 * @param m a module
 * @return a {@code SettingInfo} representing the root setting path of the module
 */
public static SettingInfo getTree(ModuleID<?> m) {
	return getTree(m.getModuleType(), false);
}

/**
 * Creates the root of a {@code SettingInfo} tree representing the settings declared by the given type.
 * Subsetting branches are lazily loaded.
 * @param moduleClass declaring type
 * @return a {@code SettingInfo} representing the root setting path of the declaring type
 */
public static SettingInfo getTree(Class<?> moduleClass) {
	return getTree(moduleClass, false);
}

/**
 * Creates the root of a {@code SettingInfo} tree representing the settings declared by the given type.
 * Subsetting branches are lazily loaded.
 * @param moduleClass declaring type
 * @param threadSafe if the returned tree should be thread safe
 * @return a {@code SettingInfo} representing the root setting path of the declaring type
 */
public static SettingInfo getTree(Class<?> moduleClass, boolean threadSafe) {
	Setting[] settings = moduleClass.getAnnotationsByType(Setting.class);
	Setting.Sub[] subSettings = moduleClass.getAnnotationsByType(Setting.Sub.class);
	SettingInfo[] nodes = new SettingInfo[settings.length + subSettings.length + 1];
	boolean customOn = false;
	for(int i = 1, iSet = 0, iSub = 0, nextSub = subSettings.length == 0 ? -1 : subSettings[0].index() + 1; i < nodes.length; i++) {
		if(i == nextSub) {
			Setting.Sub sub = subSettings[iSub++];
			nodes[i] = threadSafe ? new ThreadSafe(sub.name(), sub.value(), sub.subSettings()) :
				new ThreadUnsafe(sub.name(), sub.value(), sub.subSettings());
			nextSub = iSub == subSettings.length ? -1 : subSettings[iSub].index() + 1;
		} else {
			Setting setting = settings[iSet++];
			if(setting.name().equals(Setting.ON)) customOn = true;
			nodes[i] = new Value(setting);
		}
	}
	if(customOn) {
		SettingInfo[] shiftNodes = new SettingInfo[nodes.length - 1];
		System.arraycopy(nodes, 1, shiftNodes, 0, shiftNodes.length);
		return new Root(shiftNodes, moduleClass.getName());
	}
	nodes[0] = SETTING_ON;
	return new Root(nodes, moduleClass.getName());
}

private static class Value extends SettingInfo {
	private final Setting setting;
	
	private Value(Setting setting) {
		this.setting = setting;
	}
	
	@Override
	public Setting getSetting() {
		return setting;
	}
	
	@Override
	public String getName() {
		return setting.name();
	}
	
	@Override
	public String toString() {
		return setting.name() + ": " + setting.type().getSimpleName();
	}
	
	@Override
	public boolean isTree() {
		return false;
	}
	
	@Override
	public Iterator<SettingInfo> iterator() {
		throw wrongNodeType();
	}
	
	@Override
	public SettingInfo getNode(String name) {
		throw wrongNodeType();
	}
	
	@Override
	public int getNodeCount() {
		throw wrongNodeType();
	}
	
	private IllegalStateException wrongNodeType() {
		return new IllegalStateException(setting.name() + " is not a setting list");
	}
}

private static abstract class Tree extends SettingInfo {
	private final String name;
	private final Setting[] settingsA;
	private final Annotation[] subSettingsA;
	
	protected abstract SettingInfo newNode(String name, Setting[] settingsA, Annotation[] subSettingsA);
	
	protected abstract SettingInfo[] getSubSettings();
	
	private Tree(String name, Setting[] settingsA, Annotation[] subSettingsA) {
		this.settingsA = settingsA;
		this.subSettingsA = subSettingsA;
		this.name = name;
	}
	
	@Override
	public SettingInfo getNode(String name) {
		for(SettingInfo node:getSubSettings()) if(node.getName().equalsIgnoreCase(name)) return node;
		return null;
	}
	
	@Override
	public int getNodeCount() {
		return subSettingsA.length + settingsA.length;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name + ": " + Arrays.toString(getSubSettings());
	}
	
	@Override
	public boolean isTree() {
		return true;
	}
	
	@Override
	public Setting getSetting() {
		throw new IllegalStateException(name + " is a setting list");
	}
	
	@Override
	public Iterator<SettingInfo> iterator() {
		SettingInfo[] subSettings = getSubSettings();
		return new Iterator<SettingInfo>() {
			int i = 0;
			
			@Override
			public boolean hasNext() {
				return i != subSettings.length;
			}
			
			@Override
			public SettingInfo next() {
				return subSettings[i++];
			}
		};
	}
	
	protected SettingInfo[] getSubSettingNodes() {
		SubSetting[] subSettings = SubSetting.array(subSettingsA);
		SettingInfo[] nodes = new SettingInfo[settingsA.length + subSettings.length];
		for(int i = 0, iSet = 0, iSub = 0, nextSub = subSettings.length == 0 ? -1 : subSettings[0].index; i < nodes.length; i++) {
			if(i == nextSub) {
				SubSetting sub = subSettings[iSub++];
				nodes[i] = newNode(sub.name, sub.value, sub.subSettings);
				nextSub = iSub == subSettings.length ? -1 : subSettings[iSub].index;
			} else nodes[i] = new Value(settingsA[iSet++]);
		}
		return nodes;
	}
}

private static class Root extends Tree {
	private final SettingInfo[] subSettings;
	
	private Root(SettingInfo[] subSettings, String name) {
		super(name, null, null);
		this.subSettings = subSettings;
	}
	
	@Override
	protected SettingInfo[] getSubSettings() {
		return subSettings;
	}
	
	@Override
	public int getNodeCount() {
		return subSettings.length;
	}
	
	@Override
	protected SettingInfo newNode(String name, Setting[] settingsA, Annotation[] subSettingsA) {
		return null;
	}
}

private static class ThreadUnsafe extends Tree {
	private SettingInfo[] subSettings;
	
	private ThreadUnsafe(String name, Setting[] settingsA, Annotation[] subSettingsA) {
		super(name, settingsA, subSettingsA);
	}
	
	@Override
	protected SettingInfo[] getSubSettings() {
		if(subSettings == null) subSettings = getSubSettingNodes();
		return subSettings;
	}
	
	@Override
	protected SettingInfo newNode(String name, Setting[] settingsA, Annotation[] subSettingsA) {
		return new ThreadUnsafe(name, settingsA, subSettingsA);
	}
}

private static class ThreadSafe extends Tree {
	private volatile SettingInfo[] subSettings;

	private ThreadSafe(String name, Setting[] settingsA, Annotation[] subSettingsA) {
		super(name, settingsA, subSettingsA);
	}
	
	@Override
	protected SettingInfo[] getSubSettings() {
		if(subSettings == null) synchronized(this) {
			if(subSettings == null) subSettings = getSubSettingNodes();
		}
		return subSettings;
	}
	
	@Override
	protected SettingInfo newNode(String name, Setting[] settingsA, Annotation[] subSettingsA) {
		return new ThreadSafe(name, settingsA, subSettingsA);
	}
}

private static class SubSetting {
	private final int index;
	private final String name;
	private final Setting[] value;
	private final Annotation[] subSettings;
	
	private SubSetting(int index, String name, Setting[] value, Annotation[] subSettings) {
		this.index = index;
		this.name = name;
		this.value = value;
		this.subSettings = subSettings;
	}
	
	private static SubSetting[] array(Annotation[] a) {
		SubSetting[] array = new SubSetting[a.length];
		if(a instanceof Setting.Sub[]) {
			Setting.Sub[] a1 = (Setting.Sub[]) a;
			for(int i = 0; i < array.length; i++) {
				Setting.Sub a2 = a1[i];
				array[i] = new SubSetting(a2.index(), a2.name(), a2.value(), a2.subSettings());
			}
		} else if(a instanceof Setting.SubSub[]) {
			Setting.SubSub[] a1 = (Setting.SubSub[]) a;
			for(int i = 0; i < array.length; i++) {
				Setting.SubSub a2 = a1[i];
				array[i] = new SubSetting(a2.index(), a2.name(), a2.value(), a2.subSettings());
			}
		} else if(a instanceof Setting.SubSubSub[]) {
			Setting.SubSubSub[] a1 = (Setting.SubSubSub[]) a;
			for(int i = 0; i < array.length; i++) {
				Setting.SubSubSub a2 = a1[i];
				array[i] = new SubSetting(a2.index(), a2.name(), a2.value(), a2.subSettings());
			}
		} else if(a instanceof Setting.SubSubSubSub[]) {
			Setting.SubSubSubSub[] a1 = (Setting.SubSubSubSub[]) a;
			for(int i = 0; i < array.length; i++) {
				Setting.SubSubSubSub a2 = a1[i];
				array[i] = new SubSetting(a2.index(), a2.name(), a2.value(), new Annotation[0]);
			}
		}
		return array;
	}
}

}