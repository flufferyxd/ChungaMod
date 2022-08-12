package magnileve.chungamod.modules;

import java.util.Map;
import java.util.concurrent.Callable;

import magnileve.chungamod.Chung;
import magnileve.chungamod.settings.SettingMapFactory;
import magnileve.chungamod.settings.SettingUtil;

/**
 * Manages a {@link ModuleID}.
 * @param <T> module type
 * @author Magnileve
 */
public class ModuleManager<T extends Module> {

/**
 * For singleton modules, add this value to {@code flags} in the constructor using a binary {@code or}.
 */
public static final int SINGLETON_FLAG = 1;
/**
 * For modules that should be given an instance regardless of enabled status,
 * add this value to {@code flags} in the constructor using a binary {@code or}.
 */
public static final int ALWAYS_INSTANTIATE_FLAG = 2;

static final String[] SETTING_PATH_ON = Chung.US.settingPathOn();

private final ModuleID<T> moduleID;
private final Map<String, Object> settings;
private final Class<?>[] subscribedEvents;
private final int flags;
private final Callable<T> factory;

private boolean enabled;
private T instance;

/**
 * Creates a new {@code ModuleManager} with a new {@code ModuleID}.
 * @param moduleType module type
 * @param name name of module
 * @param settings module settings map
 * @param category category name of module
 * @param description description of module
 * @param pluginID plugin ID
 * @param subscribedEvents event types this module listens for
 * @param flags module property flags
 */
public ModuleManager(Class<T> moduleType, String name, Map<String, Object> settings,
		String category, String description, String pluginID, Class<?>[] subscribedEvents, int flags) {
	moduleID = new BasicModuleID<>(name, pluginID, moduleType, category, description, this::isEnabled, this::getInstance);
	this.settings = settings;
	this.subscribedEvents = subscribedEvents;
	this.flags = flags;
	factory = (Callable<T>) Chung.US.getCallableFactory(moduleType, this);
}

/**
 * Creates a deep clone of a {@code ModuleManager}.
 * @param m module manager to clone
 * @param mapFactory creates new maps for setting map clone
 */
public ModuleManager(ModuleManager<T> m, SettingMapFactory mapFactory) {
	ModuleID<T> m1 = m.getModuleID();
	moduleID = new BasicModuleID<>(m1.getName(), m1.getPluginID(), m1.getModuleType(), m1.getCategory(), m1.getDescription(),
			this::isEnabled, this::getInstance);
	this.settings = SettingUtil.copySettings(m.getSettings(), mapFactory);
	this.subscribedEvents = m.getSubscribedEvents();
	this.flags = m.getFlags();
	factory = m.getFactory();
}

/**
 * Gets this module manager's module ID.
 * @return this module manager's module ID
 */
public ModuleID<T> getModuleID() {
	return moduleID;
}

/**
 * Gets this module's setting map.
 * @return this module's setting map
 */
public Map<String, Object> getSettings() {
	return settings;
}

/**
 * Indicates if this module has a singleton instance.
 * @return {@code true} if this module has a singleton instance; {@code false} otherwise
 */
public boolean isSingleton() {
	return (flags & SINGLETON_FLAG) == SINGLETON_FLAG;
}

/**
 * Indicates if this module should be given an instance regardless of enabled status
 * @return {@code true} if this module should be given an instance regardless of enabled status; {@code false} otherwise
 */
public boolean alwaysInstantiate() {
	return (flags & ALWAYS_INSTANTIATE_FLAG) == ALWAYS_INSTANTIATE_FLAG;
}

/**
 * Gets this module's property flags.
 * @return this module's property flags
 * @see #SINGLETON_FLAG
 * @see #ALWAYS_INSTANTIATE_FLAG
 */
public int getFlags() {
	return flags;
}

/**
 * Indicates if this module is enabled.
 * @return {@code true} if this module is enabled; {@code false} otherwise
 */
public boolean isEnabled() {
	return enabled;
}

/**
 * Sets whether or not this module is enabled.
 * @param enabled {@code true} if this module is enabled
 */
public void setEnabled(boolean enabled) {
	this.enabled = enabled;
}

/**
 * Gets the current instance of this module if one exists
 * @return the current instance of this module, or {@code null} if one does not exist
 */
public T getInstance() {
	return instance;
}

/**
 * Sets the current instance of this module.
 * @param instance the current instance, or {@code null} to remove the current instance
 */
public void setInstance(T instance) {
	this.instance = instance;
}

/**
 * Gets the factory for instances of this module.
 * @return the factory for instances of this module
 */
public Callable<T> getFactory() {
	return factory;
}

/**
 * Gets the event types this module listens for.
 * @return the event types this module listens for
 */
public Class<?>[] getSubscribedEvents() {
	return subscribedEvents;
}

@Override
public String toString() {
	return moduleID.toString();
}

@Override
public boolean equals(Object obj) {
	return obj instanceof ModuleManager ? getModuleID().equals(((ModuleManager<?>) obj).getModuleID()) : false;
}

@Override
public int hashCode() {
	return getModuleID().hashCode();
}

}