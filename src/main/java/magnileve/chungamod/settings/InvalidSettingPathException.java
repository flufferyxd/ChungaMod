package magnileve.chungamod.settings;

import magnileve.chungamod.util.Util;

/**
 * Thrown when an invalid setting path has been passed to a method.
 * A setting path is invalid when it points to nothing,
 * it points to subsettings when a setting was expected,
 * or it points to a setting when subsettings were expected.
 * @author Magnileve
 */
public class InvalidSettingPathException extends IllegalArgumentException {

private static final long serialVersionUID = 3600089393741051756L;

private final String[] settingPath;

/**
 * Constructs an {@code InvalidSettingPathException} with the given setting path.
 * @param settingPath the invalid setting path
 */
public InvalidSettingPathException(String[] settingPath) {
	super("Setting path " + Util.inverseSplit(settingPath, "/") + " is not valid");
	this.settingPath = settingPath.clone();
}

/**
 * Gets the invalid setting path that caused this exception.
 * @return the invalid setting path that caused this exception
 */
public String[] getSettingPath() {
	return settingPath.clone();
}

}