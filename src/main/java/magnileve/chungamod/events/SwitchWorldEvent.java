package magnileve.chungamod.events;

import net.minecraft.client.multiplayer.WorldClient;

/**
 * Posted upon switching worlds without leaving or joining a server (or singleplayer).
 * In vanilla, this is caused by travel through nether or end portals.
 * @author Magnileve
 */
public class SwitchWorldEvent {

private final WorldClient world;

/**
 * Creates a new {@code SwitchWorldEvent} with the loading world.
 * @param world the loading world
 */
public SwitchWorldEvent(WorldClient world) {
	this.world = world;
}

/**
 * Gets the loading world.
 * @return the world
 */
public WorldClient getWorld() {
	return world;
}

}