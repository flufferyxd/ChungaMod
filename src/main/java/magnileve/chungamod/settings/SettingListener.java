package magnileve.chungamod.settings;

/**
 * Listens for any changes in the settings of a module.
 * @author Magnileve
 */
@FunctionalInterface
public interface SettingListener {

/**
 * Called when a setting of the assigned module is changed.
 * @param settingPath path of setting
 * @param value new value of setting
 */
public void onNewSetting(String[] settingPath, Object value);

}