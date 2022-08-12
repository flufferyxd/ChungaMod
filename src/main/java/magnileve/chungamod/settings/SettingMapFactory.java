package magnileve.chungamod.settings;

import java.util.Map;

/**
 * Creates setting maps.
 * @author Magnileve
 */
@FunctionalInterface
public interface SettingMapFactory {

/**
 * Creates a new setting map.
 * @return
 */
public Map<String, Object> newMap();

}