/**
 * <p>
 * Contains interfaces and implementations used by Chungamod to manage a setting system.
 * Settings are organized in the order of plugin, module, and each name in the setting path.
 * </p>
 * <h2>Setting Path</h2>
 * <p>
 * Settings are identified by a {@link java.lang.String String} array called the setting path.
 * A {@link java.util.Map Map} is used as the root of setting storage.
 * Each {@code String} in the setting path before the last is a key that points to another contained map,
 * and the last {@code String} is a key that points to the value of the setting.
 * </p>
 * <h2>Module</h2>
 * <p>
 * Each root map of settings is linked with a {@link magnileve.chungamod.modules.ModuleID ModuleID}.
 * The {@code ModuleID} is used to find the setting maps, and settings are grouped by their modules when imported and exported.
 * </p>
 * <h2>Plugin</h2>
 * <p>
 * At the outer level, modules are grouped by their plugin IDs.
 * This grouping is supported to farther separate unrelated settings when imported and exported.
 * </p>
 * @author Magnileve
 */
package magnileve.chungamod.settings;