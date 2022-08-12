package magnileve.chungamod.events;

import net.minecraft.client.multiplayer.WorldClient;

/**
 * Posted upon joining or leaving a server (includes singleplayer).
 * If joining, the loading {@code WorldClient} is contained.
 * @author Magnileve
 */
public class ConnectionEvent {

private final WorldClient world;

/**
 * Creates a new {@code ConnectionEvent} with the loading world if one exists.
 * @param world the loading world, or {@code null} if not applicable
 */
public ConnectionEvent(WorldClient world) {
	this.world = world;
}

/**
 * Determines if this event is for joining a server.
 * @return {@code true} if this event is for joining; {@code false} if this event is for leaving
 */
public boolean isJoin() {
	return world != null;
}

/**
 * Gets the loading world if this event is a join; {@code null} otherwise.
 * @return the world
 */
public WorldClient getWorld() {
	return world;
}

}