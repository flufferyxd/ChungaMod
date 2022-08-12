package magnileve.chungamod.modules;

/**
 * A {@code ModuleID} identifies a module that creates instances of a type implementing {@link Module}.
 * @param <T> module type
 * @author Magnileve
 */
public interface ModuleID<T extends Module> extends Comparable<ModuleID<?>> {

/**
 * Gets the name of this module.
 * @return the name of this module
 */
public String getName();

/**
 * Gets the plugin ID of this module.
 * @return the plugin ID of this module
 */
public String getPluginID();

/**
 * Gets the instance type of this module.
 * @return the instance type of this module
 */
public Class<T> getModuleType();

/**
 * Gets the category name of this module.
 * @return the category name of this module.
 */
public String getCategory();

/**
 * Gets a description of this module.
 * @return a description of this module
 */
public String getDescription();

/**
 * Indicates if this module is currently enabled.
 * @return {@code true} if this module is currently enabled; {@code false} otherwise
 */
public boolean isEnabled();

/**
 * Gets the current instance of this module if one exists
 * @return the current instance of this module, or {@code null} if one does not exist
 */
public T getInstance();

/**
 * Compares the name followed by the plugin ID of this module to another lexicographically.
 * @param o the module to be compared
 * @return a negative integer, zero, or a positive integer as this object's name followed by plugin ID
 * are lexicographically less than, equal to, or greater than those of the specified module.
 */
@Override
public default int compareTo(ModuleID<?> o) {
	int value = getName().compareTo(o.getName());
	return value == 0 ? getPluginID().compareTo(o.getPluginID()) : value;
}

}