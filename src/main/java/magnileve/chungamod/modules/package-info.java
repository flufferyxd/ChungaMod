/**
 * <p>
 * Contains types for loading, accessing, and declaring modules.
 * </p>
 * <p>
 * A module is something that can be turned on or off.
 * Modules can have an instance of a specified type implementing {@link magnileve.chungamod.modules.Module Module}
 * and are identified by {@link magnileve.chungamod.modules.ModuleID ModuleID}.
 * The {@link magnileve.chungamod.modules.ModuleLoader ModuleLoader} loads messages and keeps a
 * {@link magnileve.chungamod.modules.ModuleManager ModuleManager} for each module that allows for management of the module.
 * Each module contains settings that can be accessed by itself or its manager.
 * </p>
 */
package magnileve.chungamod.modules;