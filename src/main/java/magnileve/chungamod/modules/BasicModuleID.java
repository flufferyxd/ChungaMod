package magnileve.chungamod.modules;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Basic implementation of {@link ModuleID}.
 * @param <T> module type
 * @author Magnileve
 */
public class BasicModuleID<T extends Module> implements ModuleID<T> {

private final String name;
private final String pluginID;
private final Class<T> moduleType;
private final String category;
private final String description;
private final BooleanSupplier isEnabled;
private final Supplier<T> getInstance;

private int hash;

/**
 * Creates a new {@code BasicModuleID}.
 * @param name name of module
 * @param pluginID plugin ID of module
 * @param moduleType module type
 * @param category category name of module
 * @param description description of module
 * @param isEnabled indicates if this module is enabled
 * @param getInstance gets this module's instance, or {@code null} if one does not exist
 */
public BasicModuleID(String name, String pluginID, Class<T> moduleType, String category, String description,
		BooleanSupplier isEnabled, Supplier<T> getInstance) {
	this.name = name;
	this.pluginID = pluginID;
	this.moduleType = moduleType;
	this.category = category;
	this.description = description;
	this.isEnabled = isEnabled;
	this.getInstance = getInstance;
}

@Override
public String getName() {
	return name;
}

@Override
public String getPluginID() {
	return pluginID;
}

@Override
public Class<T> getModuleType() {
	return moduleType;
}

@Override
public String getCategory() {
	return category;
}

@Override
public String getDescription() {
	return description;
}

@Override
public boolean isEnabled() {
	return isEnabled.getAsBoolean();
}

@Override
public T getInstance() {
	return getInstance.get();
}

@Override
public String toString() {
	return getName();
}

@Override
public boolean equals(Object obj) {
	if(obj instanceof ModuleID) {
		ModuleID<?> m = (ModuleID<?>) obj;
		return getName().equals(m.getName()) && getPluginID().equals(m.getPluginID());
	}
	return false;
}

@Override
public int hashCode() {
	int h = hash;
	if(h == 0) hash = h = getName().hashCode() ^ getPluginID().hashCode();
	return h;
}

}