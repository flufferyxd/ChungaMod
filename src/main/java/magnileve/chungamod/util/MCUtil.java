package magnileve.chungamod.util;

import magnileve.chungamod.modules.ContainsInit;
import magnileve.chungamod.modules.Init;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;

/**
 * Contains static utility methods involving the Mincerfat code.
 * @author Magnileve
 */
@ContainsInit
public class MCUtil {

private static Minecraft mc;

@Init
public static void init(Minecraft mcIn) {
	mc = mcIn;
}

private MCUtil() {}

/**
 * Gets the {@code BlockPos} an entity is currently within.  This is done by rounding an entity position to floor.
 * @param e an entity
 * @return the {@code BlockPos} the given entity is currently within
 */
public static BlockPos getPos(Entity e) {
	return new BlockPos(e.posX, e.posY, e.posZ);
}

/**
 * Gets the {@code BlockPos} the player is currently within.  This is done by rounding the player's position to floor.
 * @return the {@code BlockPos} the player is currently within
 */
public static BlockPos playerPos() {
	return new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
}

/**
 * Sends a client-side message in chat.
 * @param message message to be displayed
 * @throws NullPointerException if there is no current chat GUI
 */
public static void sendMessage(String message) {
	mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString("[Chungamod] " + message));
}

/**
 * Sends a client-side message in chat if it is possible to do so.
 * @param message message to be displayed
 */
public static void trySendMessage(String message) {
	if(mc.ingameGUI != null) sendMessage(message);
}

/**
 * Parses a {@code BlockPos} from a {@code String}.
 * @param coords input coordinates
 * @return a {@code BlockPos} containing these coordinates
 * @throws IllegalArgumentException if {@code coords} is not of length {@code 3},
 * or relative coordinates are used when a player does not exist
 * @throws NumberFormatException if an input string does not contain a parsable integer
 */
public static BlockPos parseCoords(String[] coords) {
	if(coords.length != 3) throw new IllegalArgumentException("Format: <x> <y> <z>");
	Entity camera = null;
	int[] ints = new int[3];
	for(int i = 0; i < 3; i++) {
		if(coords[i].startsWith("~")) {
			if(camera == null) {
				camera = mc.getRenderViewEntity();
				if(camera == null) throw new IllegalArgumentException("Cannot use relative coordinates when player is null");
			}
			ints[i] = (int) (i == 0 ? camera.posX : i == 1 ? camera.posY : camera.posZ) + Integer.parseInt(coords[i].substring(1));
		}
		ints[i] = Integer.parseInt(coords[i]);
	}
	return new BlockPos(ints[0], ints[1], ints[2]);
}

/**
 * Gets a vector one unit away from the origin in the given direction.
 * @param pitch pitch of rotation
 * @param yaw yaw of rotation
 * @return vector with rotation of the input pitch and yaw
 */
public static Vec3d getVectorForRotation(float pitch, float yaw) {
	float f = - MathHelper.cos(pitch * -0.017453292F);
	return new Vec3d((double) (MathHelper.sin(yaw * -0.017453292F - (float) Math.PI) * f),
			(double) MathHelper.sin(pitch * -0.017453292F),
			(double) (MathHelper.cos(yaw * -0.017453292F - (float) Math.PI) * f));
}

}