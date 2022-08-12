package magnileve.chungamod.settings;

import magnileve.chungamod.modules.ModuleConstructionException;
import magnileve.chungamod.util.Util;

/**
 * Thrown when the value of a setting has been called for, but the setting does not have a value.
 * @author Magnileve
 */
public class UnsetSettingException extends ModuleConstructionException {

private static final long serialVersionUID = -4818593353693442196L;

private final String[] settingPath;

/**
 * Constructs an {@code UnsetSettingException} with the given setting path.
 * @param settingPath the setting path containing no value
 */
public UnsetSettingException(String[] settingPath) {
	super("Setting " + Util.inverseSplit(settingPath, "/") + " must have a value");
	this.settingPath = settingPath.clone();
}

/**
 * Gets the unset setting path that caused this exception.
 * @return the unset setting path that caused this exception
 */
public String[] getSettingPath() {
	return settingPath.clone();
}

}