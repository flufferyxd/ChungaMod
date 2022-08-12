package magnileve.chungamod.modules;

import magnileve.chungamod.Chung;
import magnileve.chungamod.settings.SettingListener;

/**
 * {@code Module} represents a module that can be turned on and off while in-game.
 * @author Magnileve
 * @see ModuleID
 * @see ModuleInfo
 */
public interface Module extends SettingListener {

/**
 * Called when the module is turned off in-game.
 */
public void disable();

/**
 * Called when the client disconnects from the server while the module is on.
 */
public default void softDisable() {}

@Override
public default void onNewSetting(String[] settingPath, Object value) {
	restart();
}

/**
 * Restarts this module.
 */
public default void restart() {
	Chung.US.restartModule(getModuleID());
}

/**
 * Turns off this module.
 */
public default void selfDisable() {
	set(false, ModuleManager.SETTING_PATH_ON);
}

/**
 * Gets the declaring type of this module.
 * This method must be overridden when the module instance can be a subclass or implementing class of the module type.
 * @return the declaring type of this module
 */
public default Class<? extends Module> getModuleClass() {
	return getClass();
}

/**
 * Gets this module's module id.
 * @return this module's module id
 */
public default ModuleID<?> getModuleID() {
	return Chung.US.getModule(getModuleClass());
}

/**
 * Set a setting for this module.
 * @param value new value of setting
 * @param settingPath path of setting
 */
public default void set(Object value, String... settingPath) {
	Chung.US.set(getModuleID(), value, settingPath);
}

/**
 * Set a setting for this module.
 * @param settingPath path of setting separated by {@code '/'}
 * @param value new value of setting
 */
public default void setByString(Object value, String settingPath) {
	Chung.US.set(getModuleID(), value, settingPath.split("/"));
}

/**
 * Get a setting for this module.
 * @param settingPath path of setting
 * @return value of setting
 */
public default Object get(String... settingPath) {
	return Chung.US.get(getModuleID(), settingPath);
}

}